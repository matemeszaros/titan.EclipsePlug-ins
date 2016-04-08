/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Tuned version of the MonoReconciler.
 * 
 * @author Kristof Szabados
 * */
public class Reconciler implements IReconciler {

	/**
	 * Background thread for the reconciling activity.
	 */
	class BackgroundThread extends Thread {

		/** Has the reconciler been canceled. */
		private boolean isCanceled = false;
		/** Un-install invoked. */
		private volatile boolean uninstallInvoked = false;
		/** Some changes need to be processed. */
		private boolean isDirty = false;
		/** Is a reconciling strategy active. */
		private boolean isStrategyActive = false;

		/**
		 * Creates a new background thread. The thread runs with minimal
		 * priority.
		 *
		 * @param name
		 *                the thread's name
		 */
		public BackgroundThread(final String name) {
			super(name);
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

		/**
		 * Returns whether a reconciling strategy is active right now.
		 *
		 * @return <code>true</code> if a activity is active
		 */
		public boolean isActive() {
			return isStrategyActive;
		}

		/**
		 * Returns whether some changes need to be processed.
		 *
		 * @return <code>true</code> if changes wait to be processed
		 */
		public synchronized boolean isDirty() {
			return isDirty;
		}

		/**
		 * Cancels the background thread.
		 */
		public void cancel() {
			isCanceled = true;
			IProgressMonitor pm = progressMonitor;
			if (pm != null) {
				pm.setCanceled(true);
			}
		}

		/**
		 * Reset the background thread as the text viewer has been
		 * changed.
		 */
		public void reset() {
			synchronized (this) {
				isDirty = true;
			}

			reconcilerReset();
		}

		/**
		 * Indicate that uninstallation was invoked, and awake the
		 * background thread if sleeping
		 * */
		public void uninstall() {
			synchronized (this) {
				isDirty = true;
				uninstallInvoked = true;
			}
		}

		/**
		 * The background activity. Waits until there is something in
		 * the queue managing the changes that have been applied to the
		 * text viewer. Removes the first change from the queue and
		 * process it.
		 * <p>
		 * Calls {@link AbstractReconciler#initialProcess()} on
		 * entrance.
		 * </p>
		 */
		@Override
		public void run() {
			initialProcess();
			DirtyRegion region = null;

			while (!isCanceled) {
				if (!uninstallInvoked) {
					try {
						region = dirtyRegionQueue.poll(getReconcilerTimeout() + 5, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}

				if (isCanceled) {
					break;
				}

				if (region == null) {
					continue;
				}

				List<DirtyRegion> oldRegions = new ArrayList<DirtyRegion>();
				
				while(region != null) {
					oldRegions.add(region);
					region = dirtyRegionQueue.poll();
				}

				isStrategyActive = true;

				boolean full = false;
				for (DirtyRegion region2 : oldRegions) {
					if (region2.getOffset() == -1) {
						full = true;
						break;
					}
				}

				try {
					if (full || !isIncrementalReconciler() || oldRegions.size() > 100) {
						IDocument tmpDocument = getDocument();
						if (tmpDocument != null) {
							reconcilingStrategy.reconcile(new Region(0, tmpDocument.getLength()));
						}
					} else if (!oldRegions.isEmpty()) {
						oldRegions = mergeRegions(oldRegions);

						for (int i = 0; i < oldRegions.size(); i++) {
							reconcilingStrategy.reconcileSyntax(oldRegions.get(i));
						}

						if (!reconcilingStrategy.getEditor().isSemanticCheckingDelayed()) {
							reconcilingStrategy.reconcileSemantics();
						}
					} else {
						// This can only happen when the
						// reconciler was non
						// incremental when the change happened,
						// but is incremental when it is processed.
						IDocument tmpDocument = getDocument();
						if (tmpDocument != null) {
							reconcilingStrategy.reconcile(new Region(0, tmpDocument.getLength()));
						}
					}
				} catch (Exception e) {
					ErrorReporter.logExceptionStackTrace(e);
				}

				oldRegions.clear();

				synchronized (this) {
					isDirty = progressMonitor.isCanceled();
				}

				isStrategyActive = false;
			}
		}

		/**
		 * Merges the list of dirty regions it receives as parameters.
		 * <p>
		 * A remove change followed by an insert change (replace) can
		 * not be merged, as that would invalidate the starting offset
		 * of the change.
		 *
		 *
		 * @param originalRegions
		 *                the list of dirty regions to be merged.
		 *
		 * @return the merged list o dirty regions.
		 * */
		private List<DirtyRegion> mergeRegions(final List<DirtyRegion> originalRegions) {
			List<DirtyRegion> mergedRegions = new ArrayList<DirtyRegion>();
			DirtyRegion actual = originalRegions.get(0);
			DirtyRegion next;
			boolean merged = false;
			for (int i = 1; i < originalRegions.size(); i++) {
				next = originalRegions.get(i);
				merged = false;
				if (DirtyRegion.INSERT.equals(actual.getType())) {
					if (DirtyRegion.INSERT.equals(next.getType())) {
						// insert + insert
						if (actual.getOffset() + actual.getLength() == next.getOffset()) {
							actual = new DirtyRegion(actual.getOffset(), actual.getLength() + next.getLength(),
									DirtyRegion.INSERT, actual.getText() + next.getText());
							merged = true;
						}
					} else {
						// insert + remove
						if (actual.getOffset() + actual.getLength() == next.getOffset() + next.getLength()) {
							// deleted something
							int diff = actual.getOffset() - next.getOffset();
							if (diff < 0) {
								actual = new DirtyRegion(actual.getOffset(), -1 * diff, DirtyRegion.INSERT, actual
										.getText().substring(0, -1 * diff));
							} else if (diff > 0) {
								actual = new DirtyRegion(next.getOffset(), diff, DirtyRegion.REMOVE, null);
							} else {
								actual = new DirtyRegion(actual.getOffset(), 0, DirtyRegion.INSERT, "");
							}
							merged = true;
						}
					}
				} else if (DirtyRegion.REMOVE.equals(next.getType())) {
					// remove + remove
					if (next.getOffset() + next.getLength() == actual.getOffset()) {
						actual = new DirtyRegion(next.getOffset(), actual.getLength() + next.getLength(), DirtyRegion.REMOVE,
								null);
						merged = true;
					} else if (next.getOffset() == actual.getOffset()) {
						actual = new DirtyRegion(actual.getOffset(), actual.getLength() + next.getLength(),
								DirtyRegion.REMOVE, null);
						merged = true;
					}
				}

				if (!merged) {
					if (actual.getLength() != 0) {
						mergedRegions.add(actual);
					}
					actual = next;
				}
			}

			if (actual.getLength() != 0) {
				mergedRegions.add(actual);
			}

			return mergedRegions;
		}
	}

	/**
	 * Internal document listener and text input listener.
	 */
	class Listener implements IDocumentListener, ITextInputListener {

		@Override
		public void documentAboutToBeChanged(final DocumentEvent e) {
			// Do nothing
		}

		@Override
		public void documentChanged(final DocumentEvent e) {
			if (!backgroundThread.isDirty() && backgroundThread.isAlive()) {
				if (!isAllowedToModifyDocument && Thread.currentThread() == backgroundThread) {
					throw new UnsupportedOperationException("The reconciler thread is not allowed to modify the document");
				}
				aboutToBeReconciled();
			}

			/*
			 * The second OR condition handles the case when the
			 * document gets changed while still inside
			 * initialProcess().
			 */
			if (backgroundThread.isActive() || backgroundThread.isDirty() && backgroundThread.isAlive()) {
				progressMonitor.setCanceled(true);
			}

			if (isIncrementalReconciler()) {
				createDirtyRegion(e);
			} else {
				requestFullAnalyzes();
			}

			backgroundThread.reset();
		}

		@Override
		public void inputDocumentAboutToBeChanged(final IDocument oldInput, final IDocument newInput) {
			if (oldInput == document) {

				if (document != null) {
					document.removeDocumentListener(this);
				}

				if (isIncrementalReconciler()) {
					if (document != null && document.getLength() > 0 && backgroundThread.isDirty() && backgroundThread.isAlive()) {
						DocumentEvent e = new DocumentEvent(document, 0, document.getLength(), "");
						createDirtyRegion(e);
						backgroundThread.reset();
					}
				} else {
					requestFullAnalyzes();
				}

				document = null;
			}
		}

		@Override
		public void inputDocumentChanged(final IDocument oldInput, final IDocument newInput) {
			document = newInput;
			if (document == null) {
				return;
			}

			reconcilerDocumentChanged(document);

			document.addDocumentListener(this);

			if (!backgroundThread.isDirty()) {
				aboutToBeReconciled();
			}

			startReconciling();
		}

		/**
		 * Called when the reconciler is about to be un-installed..
		 *
		 * @param oldInput
		 *                the text viewer's previous input document
		 */
		public void uninstall(final IDocument oldInput) {
			if (oldInput == document) {
				if (document != null) {
					document.removeDocumentListener(this);
				}

				if (isIncrementalReconciler()) {
					if (document != null && document.getLength() > 0 && backgroundThread.isDirty() && backgroundThread.isAlive()) {
						DocumentEvent e = new DocumentEvent(document, 0, document.getLength(), "");
						createDirtyRegion(e);
						backgroundThread.uninstall();
					}
				} else {
					requestFullAnalyzes();
				}

				document = null;
			}
		}
	}

	/**
	 * Queue to manage the changes applied to the text viewer.
	 * A region starting at -1 offset means, that the whole document has to be analyzed.
	 * <p>
	 * This is a modification of DirtyRegionQueue.
	 */
	private LinkedBlockingQueue<DirtyRegion> dirtyRegionQueue;
	/** The background thread. */
	private BackgroundThread backgroundThread;
	/** Internal document and text input listener. */
	private Listener changeListener;
	
	/**
	 * true, if incremental reconciling is allowed.
	 * Incremental reconciling is used if this variable is true AND
	 * PreferenceConstants.USEINCREMENTALPARSING is also true in preferences.
	 */
	private boolean mIsIncrementalReconcilerAllowed = true;

	/** The progress monitor used by this reconciler. */
	private IProgressMonitor progressMonitor;
	/** Tells whether this reconciler is allowed to modify the document. */
	private boolean isAllowedToModifyDocument = true;

	/** The text viewer's document. */
	private IDocument document;
	/** The text viewer. */
	private ITextViewer textViewer;

	/** The reconciling strategy. */
	private ReconcilingStrategy reconcilingStrategy;

	/**
	 * Creates a new reconciler without configuring it.
	 *
	 * @param strategy
	 *                the reconciling strategy to be used
	 */
	protected Reconciler(final ReconcilingStrategy strategy) {
		Assert.isNotNull(strategy);

		progressMonitor = new NullProgressMonitor();
		reconcilingStrategy = strategy;
		reconcilingStrategy.setProgressMonitor(getProgressMonitor());
	}

	/**
	 * Hook called when the document whose contents should be reconciled has
	 * been changed, i.e., the input document of the text viewer this
	 * reconciler is installed on. Usually, subclasses use this hook to
	 * inform all their reconciling strategies about the change.
	 *
	 * @param document
	 *                the new reconciler document
	 */
	protected final void reconcilerDocumentChanged(final IDocument document) {
		reconcilingStrategy.setDocument(document);
	}

	/**
	 * Tells the reconciler whether any of the available reconciling
	 * strategies is interested in getting detailed dirty region information
	 * or just in the fact that the document has been changed. In the second
	 * case, the reconciling can not incrementally be pursued.
	 * NOTE: Incremental reconciling is used if aIncrementalReconcilerAllowed is true AND
	 *       PreferenceConstants.USEINCREMENTALPARSING is also true in preferences.
	 *
	 * @param aIsIncrementalReconcilerAllowed
	 *                indicates whether this reconciler will be configured
	 *                with incremental reconciling strategies
	 *                IF PreferenceConstants.USEINCREMENTALPARSING is also true in preferences
	 *
	 * @see DirtyRegion
	 * @see IReconcilingStrategy
	 * @see #isIncrementalReconciler()
	 */
	public final void allowIncrementalReconciler(final boolean aIsIncrementalReconcilerAllowed) {
		mIsIncrementalReconcilerAllowed = aIsIncrementalReconcilerAllowed;
	}

	/**
	 * Tells the reconciler whether it is allowed to change the document
	 * inside its reconciler thread.
	 * <p>
	 * If this is set to <code>false</code> an
	 * {@link UnsupportedOperationException} will be thrown when this
	 * restriction will be violated.
	 * </p>
	 *
	 * @param isAllowedToModify
	 *                indicates whether this reconciler is allowed to modify
	 *                the document
	 */
	public final void setIsAllowedToModifyDocument(final boolean isAllowedToModify) {
		isAllowedToModifyDocument = isAllowedToModify;
	}

	/**
	 * Sets the progress monitor of this reconciler.
	 *
	 * @param monitor
	 *                the monitor to be used
	 */
	public final void setProgressMonitor(final IProgressMonitor monitor) {
		Assert.isLegal(monitor != null);

		progressMonitor = monitor;
		reconcilingStrategy.setProgressMonitor(monitor);
	}

	/**
	 * Returns whether any of the reconciling strategies is interested in
	 * detailed dirty region information.
	 *
	 * @return whether this reconciler is incremental
	 *
	 * @see IReconcilingStrategy
	 */
	protected final boolean isIncrementalReconciler() {
		IPreferencesService prefs = Platform.getPreferencesService();
		// incremental reconcile is set in preferences
		return mIsIncrementalReconcilerAllowed && prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.USEINCREMENTALPARSING, false, null); 
	}

	/**
	 * Returns the input document of the text viewer this reconciler is
	 * installed on.
	 *
	 * @return the reconciler document
	 */
	protected final IDocument getDocument() {
		return document;
	}

	/**
	 * Returns the text viewer this reconciler is installed on.
	 *
	 * @return the text viewer this reconciler is installed on
	 */
	protected final ITextViewer getTextViewer() {
		return textViewer;
	}

	/**
	 * Returns the progress monitor of this reconciler.
	 *
	 * @return the progress monitor of this reconciler
	 */
	protected final IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	/**
	 * Returns the reconciling strategy of this reconciler.
	 *
	 * @param contentType
	 *                the content type for which to determine the
	 *                reconciling strategy
	 * @return the reconciling strategy of this reconciler.
	 * */
	@Override
	public final IReconcilingStrategy getReconcilingStrategy(final String contentType) {
		Assert.isNotNull(contentType);
		return reconcilingStrategy;
	}

	/*
	 * @see IReconciler#install(ITextViewer)
	 */
	@Override
	public final void install(final ITextViewer textViewer) {

		Assert.isNotNull(textViewer);
		this.textViewer = textViewer;

		synchronized (this) {
			if (backgroundThread != null) {
				return;
			}

			backgroundThread = new BackgroundThread(getClass().getName());
		}

		//dirtyRegionQueue = new ArrayList<DirtyRegion>();
		dirtyRegionQueue = new LinkedBlockingQueue<DirtyRegion>();

		changeListener = new Listener();
		textViewer.addTextInputListener(changeListener);

		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=67046
		// if the reconciler gets installed on a viewer that already has
		// a document
		// (e.g. when reusing editors), we force the listener to
		// register
		// itself as document listener, because there will be no input
		// change
		// on the viewer.
		// In order to do that, we simulate an input change.
		IDocument tmpDocument = textViewer.getDocument();
		if (tmpDocument != null) {
			changeListener.inputDocumentAboutToBeChanged(tmpDocument, tmpDocument);
			changeListener.inputDocumentChanged(tmpDocument, tmpDocument);
		}
	}

	/*
	 * @see IReconciler#uninstall()
	 */
	@Override
	public final void uninstall() {
		if (changeListener != null) {

			textViewer.removeTextInputListener(changeListener);
			if (document != null) {
				changeListener.uninstall(document);
				changeListener.inputDocumentChanged(document, null);
			}
			changeListener = null;

			synchronized (this) {
				// http://dev.eclipse.org/bugs/show_bug.cgi?id=19135
				BackgroundThread bt = backgroundThread;
				backgroundThread = null;
				bt.cancel();
			}
		}
	}

	/**
	 * Creates a dirty region for a document event and adds it to the queue.
	 *
	 * @param e
	 *                the document event for which to create a dirty region
	 */
	private void createDirtyRegion(final DocumentEvent e) {
		if (e.getLength() == 0 && e.getText() != null) {
			// Insert
			dirtyRegionQueue.add(new DirtyRegion(e.getOffset(), e.getText().length(), DirtyRegion.INSERT, e.getText()));
		} else if (e.getText() == null || e.getText().length() == 0) {
			// Remove
			dirtyRegionQueue.add(new DirtyRegion(e.getOffset(), e.getLength(), DirtyRegion.REMOVE, null));
		} else {
			// Replace (Remove + Insert)
			dirtyRegionQueue.add(new DirtyRegion(e.getOffset(), e.getLength(), DirtyRegion.REMOVE, null));
			dirtyRegionQueue.add(new DirtyRegion(e.getOffset(), e.getText().length(), DirtyRegion.INSERT, e.getText()));
		}
	}

	/**
	 * Request that the next analysis be a full analysis, even if
	 * incremental mode is active at that time.
	 * */
	private void requestFullAnalyzes() {
		dirtyRegionQueue.add(new DirtyRegion(-1, -1, DirtyRegion.INSERT, ""));
	}

	/**
	 * Hook for subclasses which want to perform some action as soon as
	 * reconciliation is needed.
	 * <p>
	 * Default implementation is to do nothing.
	 */
	protected void aboutToBeReconciled() {
		//Do nothing
	}

	/**
	 * This method is called on startup of the background activity. It is
	 * called only once during the life time of the reconciler. Clients may
	 * reimplement this method.
	 */
	protected final void initialProcess() {
		dirtyRegionQueue.clear();
		reconcilingStrategy.initialReconcile();
	}

	/**
	 * Forces the reconciler to reconcile the structure of the whole
	 * document.
	 */
	protected final void forceReconciling() {
		if (document != null) {
			if (!backgroundThread.isDirty() && backgroundThread.isAlive()) {
				aboutToBeReconciled();
			}

			if (backgroundThread.isActive()) {
				progressMonitor.setCanceled(true);
			}

			if (isIncrementalReconciler()) {
				DocumentEvent e = new DocumentEvent(document, 0, document.getLength(), document.get());
				createDirtyRegion(e);
			} else {
				requestFullAnalyzes();
			}

			startReconciling();
		}
	}

	/**
	 * Starts the reconciler to reconcile the queued dirty-regions.
	 */
	protected final synchronized void startReconciling() {
		if (backgroundThread == null) {
			return;
		}

		if (!backgroundThread.isAlive()) {
			try {
				backgroundThread.start();
			} catch (IllegalThreadStateException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		} else {
			backgroundThread.reset();
		}
	}

	/**
	 * Hook that is called after the reconciler thread has been reset.
	 */
	protected void reconcilerReset() {
		//Do nothing
	}

	/**
	 * Tells whether the code is running in this reconciler's background
	 * thread.
	 *
	 * @return <code>true</code> if running in this reconciler's background
	 *         thread
	 */
	protected final boolean isRunningInReconcilerThread() {
		return Thread.currentThread() == backgroundThread;
	}
	
	/**
	 * Gets the reconciler timeout, or in other words the background thread delay. 
	 * This is effective only if DELAYSEMANTICCHECKINGTILLSAVE is off,
	 * otherwise value is read, but ignored.
	 * @return the timeout value in milliseconds
	 */
	private final int getReconcilerTimeout() {
		IPreferencesService prefs = Platform.getPreferencesService();
		int timeout = prefs.getInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.RECONCILERTIMEOUT, 1, null);
		return 1000 * timeout;
	}
}
