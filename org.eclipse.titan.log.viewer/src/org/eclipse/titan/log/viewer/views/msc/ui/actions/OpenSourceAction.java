/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.models.SourceInformation;
import org.eclipse.titan.log.viewer.models.SourceInformation.InvalidSourceInformationException;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.readers.ValueReader;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.titan.log.viewer.views.msc.model.IEventObject;

// TODO This class is basically the same as org.eclipse.titan.log.viewer.actions.OpenSourceViewMenuAction
//	the common parts should be extracted
public class OpenSourceAction extends SelectionProviderAction implements DelayedSelectable {

	private MSCView mscView;
	private Integer selectedLine;
	private boolean silent;
	private boolean forceEditorOpening = false;

	private static String lastFilename = null;
	private static URI lastPath = null;

	private ISelection delayedSelection = null;
	private DelayedSelector runnable = new DelayedSelector(this);

	/**
	 * Constructor 
	 * @param view the MSC View
	 */
	public OpenSourceAction(final MSCView view, final boolean silent, final boolean forceEditorOpening) {
		super(view.getMSCWidget(), "Open Source");
		this.mscView = view;
		this.silent = silent;
		this.forceEditorOpening = forceEditorOpening;
	}

	@Override
	public void dispose() {
		super.dispose();
		runnable.setShouldRun(false);
		synchronized (runnable.getLock()) {
			runnable.getLock().notify();
		}
		runnable = null;
		mscView = null;
	}

	/**
	 * Executes the open source action, but in order to protect against overloading the real operation is run in a background thread.
	 *
	 * @param selection the selection to process
	 * */
	public void delayedRun(final ISelection selection) {
		setDelayedSelection(selection);

		if (!runnable.isAlive()) {
			runnable.setPriority(Thread.MIN_PRIORITY);
			runnable.setDaemon(true);
			runnable.start();
		}

		synchronized (runnable.getLock()) {
			runnable.getLock().notify();
		}
	}

	@Override
	public void run() {
		if (selectedLineInvalid()) {
			return;
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}

		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return;
		}

		LogFileMetaData logFileMetaData = this.mscView.getLogFileMetaData();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(logFileMetaData.getProjectName());
		IFile logFile = project.getFile(logFileMetaData.getProjectRelativePath().substring(logFileMetaData.getProjectName().length() + 1));

		if (!logFile.exists()) {
			IViewReference[] viewReferences = activePage.getViewReferences();
			ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
			TitanLogExceptionHandler.handleException(new UserException(Messages.getString("OpenValueViewAction.1"))); //$NON-NLS-1$
			return;
		}
		
		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		}
		
		ExecutionModel model = this.mscView.getModel();
		String testCase = model.getTestCase().getTestCaseName();

		EventObject eventObject;
		int actualLine = selectedLine;
		SourceInformation sourceInformation = null;
		while (sourceInformation == null && actualLine > 2) {
			IEventObject ieventObject = model.getEvent(actualLine - 2);
			if (!(ieventObject instanceof EventObject)) {
				actualLine--;
				continue;
			}
			eventObject = (EventObject) ieventObject;
			if ((testCase == null) || eventObject.getRecordNumber() == 0) {
				return;
			}


			// get value
			LogRecord logrecord;
			try {
				logrecord = ValueReader.getInstance().readLogRecordFromLogFileCached(
						this.mscView.getLogFileMetaData().getFilePath(),
						eventObject);
	
			} catch (final IOException valueException) {
				ErrorReporter.logExceptionStackTrace(valueException);
				ErrorReporter.INTERNAL_ERROR(Messages.getString("OpenValueViewAction.3")); //$NON-NLS-1$
				return;
			} catch (final ParseException valueException) {
				ErrorReporter.logExceptionStackTrace(valueException);
				ErrorReporter.INTERNAL_ERROR(Messages.getString("OpenValueViewAction.3")); //$NON-NLS-1$
				return;
			}
	
			try {
				sourceInformation = SourceInformation.createInstance(logrecord.getSourceInformation());
			} catch (InvalidSourceInformationException e) {
				// Do nothing
				// try to find the closest source information
			}

			actualLine--;
		}

		if (sourceInformation == null) {
			if (!silent) {
				String setting = logFileMetaData.getOptionsSettings("SourceInfoFormat");
				if (setting == null) {
					ErrorReporter.parallelErrorDisplayInMessageDialog(
						"Error opening source", 
						"This log file is not generated with source location information inserted. And it really does not seem to contain source location information");
				} else {
					ErrorReporter.parallelErrorDisplayInMessageDialog(
						"Error opening source", 
						"This log record does not seem to contain source location information");
				}
			}

			return;
		}

		if (sourceInformation.getSourceFileName() == null) {
			mscView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage(
					"The name of the target file could not be extracted");
			return;
		}

		final String fileName = sourceInformation.getSourceFileName();

		IFile targetFile;
		if (lastFilename != null && lastFilename.equals(fileName) && lastPath != null) { 
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(lastPath);
			if (files.length == 0) {
				mscView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The file `" + lastFilename + "' could not be found");
				setLastFilename(null);
				return;
			}
			targetFile = files[0];
		} else {
			targetFile = findSourceFile(project, fileName);
			if (targetFile == null) {
				mscView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The file `" + fileName + "' could not be found");
				return;
			}

			setLastFilename(fileName);
			setLastPath(targetFile.getLocationURI());
		}

		openEditor(targetFile, sourceInformation.getLineNumber(), mscView, forceEditorOpening);
	}

	private boolean selectedLineInvalid() {
		return selectedLine == null || selectedLine < 3
				|| this.mscView == null
				|| this.mscView.getModel() == null
				|| selectedLine - 2 >= this.mscView.getModel().getNumberOfEvents();
	}

	@Override
	public void selectionChanged(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection) selection);
		}
	}

	@Override
	public void selectionChanged(final IStructuredSelection selection) {
		this.selectedLine = (Integer) selection.getFirstElement();
	}

	/**
	 * Opens the editor for the given source file
	 * 
	 * This method is intended to use only by {@link org.eclipse.titan.log.viewer.actions.OpenSourceViewMenuAction}
	 * 
	 * @param targetFile The source file
	 * @param lineNumber The line number to select
	 * @param logView The view which initiated this action (used only to report errors on the status line)
	 * @param forceEditorOpening 
	 */
	public static void openEditor(final IFile targetFile, final int lineNumber, final IViewPart logView, final boolean forceEditorOpening) {
		// search for the editor to open the file
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(targetFile.getName());
		if (desc == null) {
			desc = PlatformUI.getWorkbench().getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
			if (desc == null) {
				logView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The editor could not be found");
				return;
			}
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}

		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return;
		}

		try {
			FileEditorInput editorInput = new FileEditorInput(targetFile);
			IEditorPart editorPart = activePage.findEditor(editorInput);
			
			if (editorPart == null) {
				if (!forceEditorOpening) {
					return;
				}
				
				editorPart = activePage.openEditor(editorInput, desc.getId());
			}
			
			if (!(editorPart instanceof AbstractTextEditor)) {
				logView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("Could not jump to the target position");
				return;
			}
			
			AbstractTextEditor textEditor = (AbstractTextEditor) editorPart;

			IDocument targetDocument = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			if (targetDocument == null) {
				logView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The target document does not exist");
				return;
			}

			if (lineNumber >= targetDocument.getNumberOfLines()) {
				logView.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The position is after the last line in the source code.");
				return;
			}

			IRegion lineRegion = targetDocument.getLineInformation(lineNumber - 1); // Line numbers are indexed from zero in the editors
			activePage.bringToTop(textEditor);
			textEditor.selectAndReveal(lineRegion.getOffset(), lineRegion.getLength());
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Tries to find the source file in the given project.
	 * This functions searches the non-derived members of the project. If the file was not found in
	 * the project, the referenced projects are searched as well. 
	 * @param project The project to search the file in.
	 * @param fileName The file to find
	 * @return The file, or <code>null</code> if it can not be found
	 */
	public static IFile findSourceFile(final IProject project, final String fileName) {
		return findSourceFile(project, fileName, new ArrayList<IProject>());
	}

	/**
	 * Helper method for {@link OpenSourceAction#findSourceFile(IProject, String)}
	 * @param project the project
	 * @param fileName the name of the file to search for
	 * @param chain the list of the processed projects (helps to avoid infinite recursion)
	 * @return The file or <code>null</code> if it can not be found
	 */
	private static IFile findSourceFile(final IProject project, final String fileName, final List<IProject> chain) {
		try {
			FileFinder fileFinder = new FileFinder(fileName);
			final IResource[] members = project.members();
			for (IResource resource : members) {
				if (!resource.isDerived(IResource.CHECK_ANCESTORS)) {
					resource.accept(fileFinder);
				}
			}
			chain.add(project);
			if (fileFinder.getTargetFile() != null) {
				return fileFinder.getTargetFile();
			}

			final IProject[] referencedProjects = project.getReferencedProjects();
			for (IProject referencedProject : referencedProjects) {
				if (!chain.contains(referencedProject)) {
					IFile file = findSourceFile(referencedProject, fileName);
					if (file != null) {
						return file;
					}
				}
			}
			return null;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return null;
		}
	}

	@Override
	public ISelection getDelayedSelection() {
		return delayedSelection;
	}

	@Override
	public void setDelayedSelection(ISelection delayedSelection) {
		this.delayedSelection = delayedSelection;
	}

	private static synchronized void setLastFilename(String lastFilename) {
		OpenSourceAction.lastFilename = lastFilename;
	}

	public static synchronized void setLastPath(URI lastPath) {
		OpenSourceAction.lastPath = lastPath;
	}

}
