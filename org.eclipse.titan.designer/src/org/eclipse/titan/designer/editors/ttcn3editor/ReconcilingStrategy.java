/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.GlobalIntervalHandler;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ParserFactory;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Kristof Szabados
 * */
public final class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	public static final String OUTLINEUPDATE = "Outline update";
	public static final String FOLDING_UPDATE = "Folding update";

	private final TTCN3Editor editor;
	private IDocument document;
	private StringBuilder actualCode;

	private String[] delimeters;
	private WorkspaceJob lastIncrementalSyntaxCheck = null;

	public ReconcilingStrategy(final TTCN3Editor editor) {
		this.editor = editor;
	}

	TTCN3Editor getEditor() {
		return editor;
	}

	private IDocument getDocument() {
		return document;
	}

	@Override
	public void setDocument(final IDocument document) {
		this.document = document;
		actualCode = new StringBuilder(document.get());

		delimeters = editor.getDocument().getLegalLineDelimiters();
	}

	// This function is never used in practice
	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		if (DirtyRegion.INSERT.equals(dirtyRegion.getType())) {
			actualCode.insert(dirtyRegion.getOffset(), dirtyRegion.getText());
		} else {
			actualCode.delete(dirtyRegion.getOffset(), dirtyRegion.getOffset() + dirtyRegion.getLength());
		}
		if (dirtyRegion.getOffset() == 0 && editor != null && document.getLength() == dirtyRegion.getLength()) {
			// The editor window was closed, we don't have to do a
			// thing
			if (!editor.isDirty()) {
				return;
			}
			IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
				analyze(false);
			}
			return;
		}

		// analyze2(dirtyRegion);
	}

	/**
	 * Activates incremental reconciling of the syntax of the specified
	 * dirty region.
	 * 
	 * @param dirtyRegion
	 *                the document region which has been changed
	 */
	public void reconcileSyntax(final DirtyRegion dirtyRegion) {
		double parserStart = System.nanoTime();
		if (document == null) {
			return;
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		int lineBreaks = 0;
		try {
			if (DirtyRegion.INSERT.equals(dirtyRegion.getType())) {
				lineBreaks = calculateLineBreaks(dirtyRegion.getText(), delimeters);
				actualCode.insert(dirtyRegion.getOffset(), dirtyRegion.getText());
			} else {
				lineBreaks = calculateLineBreaks(
						actualCode.substring(dirtyRegion.getOffset(), dirtyRegion.getOffset() + dirtyRegion.getLength()),
						delimeters);
				actualCode.delete(dirtyRegion.getOffset(), dirtyRegion.getOffset() + dirtyRegion.getLength());
			}
		} catch (StringIndexOutOfBoundsException e) {
			ErrorReporter.logExceptionStackTrace(e);
			ErrorReporter.logError("String length: " + actualCode.length() + " region type: " + dirtyRegion.getType()
					+ " region offset: " + dirtyRegion.getOffset() + " region length: " + dirtyRegion.getLength()
					+ " region text: '" + dirtyRegion.getText() + "'\n" + "Actual size of the document: "
					+ document.get().length());
			actualCode = new StringBuilder(document.get());
		}

		if (dirtyRegion.getOffset() == 0 && editor != null && document.getLength() == dirtyRegion.getLength()) {
			// The editor window was closed, we don't have to do a
			// thing
			if (!editor.isDirty()) {
				return;
			}
			IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
				analyze(false);
			}

			return;
		}

		int firstLine;

		try {
			firstLine = document.getLineOfOffset(dirtyRegion.getOffset());
		} catch (BadLocationException e) {
			ErrorReporter.logWarningExceptionStackTrace(e);
			ErrorReporter.logWarning("Offset became invalid, fallback method used. Document length: " + document.getLength()
					+ " region offset: " + dirtyRegion.getOffset());
			firstLine = 0;
		}

		final IFile editedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editedFile == null || ResourceExclusionHelper.isExcluded(editedFile)) {
			return;
		}

		final TTCN3ReparseUpdater reparser;
		int length = dirtyRegion.getLength();
		if (DirtyRegion.REMOVE.equals(dirtyRegion.getType())) {
			MarkerHandler.updateRemoveMarkers(editedFile, firstLine + 1, lineBreaks, dirtyRegion.getOffset(), -1 * length);
			reparser = ParserFactory.createTTCN3ReparseUpdater(editedFile, actualCode.toString(), firstLine + 1, -1 * lineBreaks,
					dirtyRegion.getOffset(), dirtyRegion.getOffset() + length, -1 * length);
		} else {
			MarkerHandler.updateInsertMarkers(editedFile, firstLine + 1, lineBreaks, dirtyRegion.getOffset(), length);
			reparser = ParserFactory.createTTCN3ReparseUpdater(editedFile, actualCode.toString(), firstLine + 1, lineBreaks, dirtyRegion.getOffset(),
					dirtyRegion.getOffset(), length);
		}

		final IProject project = editedFile.getProject();
		if (project == null) {
			return;
		}

		WorkspaceJob tempLastIncrementalSyntaxCheck = lastIncrementalSyntaxCheck;
		if (tempLastIncrementalSyntaxCheck != null) {
			try {
				tempLastIncrementalSyntaxCheck.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			lastIncrementalSyntaxCheck = null;
		}
		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		lastIncrementalSyntaxCheck = sourceParser.updateSyntax(editedFile, reparser);
		WorkspaceJob op = new WorkspaceJob("reparse information update") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				WorkspaceJob temp_lastIncrementalSyntaxCheck = lastIncrementalSyntaxCheck;
				if (temp_lastIncrementalSyntaxCheck != null) {
					try {
						temp_lastIncrementalSyntaxCheck.join();
					} catch (InterruptedException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
				sourceParser.addModulesToBeSemanticallyAnalyzed(reparser.moduleToBeReanalysed);
				if (reparser.fullAnalysysNeeded) {
					sourceParser.setFullSemanticAnalysisNeeded();
				}
				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.LONG);
		op.setSystem(true);
		op.setUser(false);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();

		if (store.getBoolean(PreferenceConstants.DISPLAYDEBUGINFORMATION)) {
			TITANDebugConsole.println("Refreshing the syntax took " + (System.nanoTime() - parserStart) * (1e-9) + " secs");
		}

		if (!reparser.fullAnalysysNeeded) {
			return;
		}

		op = new WorkspaceJob(FOLDING_UPDATE) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						// TODO optimize for incremental
						// usage
						List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(getDocument());
						getEditor().updateFoldingStructure(positions);
					}
				});
				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.LONG);
		op.setSystem(true);
		op.setUser(false);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.setRule(editedFile);
		op.schedule();
	}

	/**
	 * Activates reconciling of the semantic meanings.
	 */
	public void reconcileSemantics() {
		final IFile editedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editedFile == null || ResourceExclusionHelper.isExcluded(editedFile)) {
			return;
		}

		final WorkspaceJob temp_lastIncrementalSyntaxCheck = lastIncrementalSyntaxCheck;
		if (temp_lastIncrementalSyntaxCheck != null) {
			try {
				temp_lastIncrementalSyntaxCheck.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			lastIncrementalSyntaxCheck = null;
		}

		IProject project = editedFile.getProject();
		if (project == null) {
			return;
		}

		TITANDebugConsole.println("Reconciling semantics at " + System.nanoTime() + " time.");
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		sourceParser.analyzeAll();

		WorkspaceJob op = new WorkspaceJob(OUTLINEUPDATE) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, editedFile)) {
							getEditor().refreshOutlinePage();
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.LONG);
		op.setSystem(true);
		op.setUser(false);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.setRule(project);
		op.schedule();
	}

	@Override
	public void reconcile(final IRegion partition) {
		fullReconciliation(false);
	}

	@Override
	public void initialReconcile() {
		fullReconciliation(true);
	}

	private void fullReconciliation(final boolean is_initial) {
		actualCode = new StringBuilder(document.get());

		GlobalIntervalHandler.putInterval(document, null);
		IPreferencesService prefs = Platform.getPreferencesService();
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			analyze(is_initial);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(getDocument());
					getEditor().updateFoldingStructure(positions);
					final IFile editedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
					if (!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, editedFile)) {
						getEditor().refreshOutlinePage();
					}
				}
			});
		}
	}

	public void analyze(final boolean is_initial) {
		final IFile editedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editedFile == null || ResourceExclusionHelper.isExcluded(editedFile)) {
			return;
		}

		IProject project = editedFile.getProject();
		if (project == null) {
			return;
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(getDocument());
				getEditor().updateFoldingStructure(positions);
			}
		});

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		if (is_initial || !editor.isSemanticCheckingDelayed()) {
			final boolean minimizeMemoryUsage = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.MINIMISEMEMORYUSAGE, false, null);
			if (!is_initial || minimizeMemoryUsage) {
				projectSourceParser.reportOutdating(editedFile);
			}
			projectSourceParser.analyzeAll();
			ProjectConfigurationParser projectConfigurationParser = GlobalParser.getConfigSourceParser(project);
			projectConfigurationParser.analyzeAll();

			WorkspaceJob op = new WorkspaceJob(OUTLINEUPDATE) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, editedFile)) {
								getEditor().updateOutlinePage();
							}
						}
					});
					return Status.OK_STATUS;
				}
			};
			op.setPriority(Job.LONG);
			op.setSystem(true);
			op.setUser(false);
			op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			op.setRule(project);
			op.schedule();
		} else {
			projectSourceParser.reportSyntacticOutdatingOnly(editedFile);
			projectSourceParser.analyzeAllOnlySyntactically();
		}
	}

	@Override
	public void setProgressMonitor(final IProgressMonitor monitor) {
	}

	/**
	 * Calculates the lines breaks in the provided text, using the legal
	 * line delimeters of the document.
	 * 
	 * @param text
	 *                the text to be analyzed.
	 * @param legalLineDelimiters
	 *                the line delimiters legal in the actual document.
	 * 
	 * @return the number of linebreaks in the text.
	 * */
	public static int calculateLineBreaks(final String text, final String[] legalLineDelimiters) {
		if (text == null || text.length() == 0) {
			return 0;
		}

		int lineBreaks = 0;

		int actualIndex = 0;
		int nextIndex;
		int tempIndex;
		int delimeterLength;

		while (actualIndex >= 0) {
			nextIndex = -1;
			tempIndex = -1;
			delimeterLength = -1;

			for (int i = 0; i < legalLineDelimiters.length; i++) {
				tempIndex = text.indexOf(legalLineDelimiters[i], actualIndex);
				if (tempIndex >= 0) {
					// found line delimeter
					if (nextIndex == -1) {
						nextIndex = tempIndex;
						delimeterLength = legalLineDelimiters[i].length();
					} else if (tempIndex < nextIndex) {
						nextIndex = tempIndex;
						delimeterLength = legalLineDelimiters[i].length();
					} else if (tempIndex == nextIndex) {
						delimeterLength = Math.max(delimeterLength, legalLineDelimiters[i].length());
					}
				}
			}

			if (nextIndex >= 0) {
				actualIndex = nextIndex + delimeterLength;
				lineBreaks++;
			} else {
				actualIndex = -1;
			}
		}

		return lineBreaks;
	}
}
