/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
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
import org.eclipse.titan.log.viewer.views.text.table.TextTableView;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.EventSelection;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.OpenSourceAction;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

//TODO This class is basically the same as org.eclipse.titan.log.viewer.views.msc.ui.actions.OpenSourceAction
//	the common parts should be extracted
public class OpenSourceViewMenuAction extends SelectionProviderAction {

	private EventSelection eventSelection;
	private TextTableView view;

	private boolean silent;
	private boolean forceEditorOpening = false;

	private static String lastFilename = null;
	private static URI lastPath = null;

	private ISelection delayedselection = null;
	private InternalRunnable runnable = new InternalRunnable();

	class InternalRunnable implements Runnable {
		private boolean isRunning = false;
		public boolean isRunning() { return isRunning; }
		@Override
		public void run() {
			if (delayedselection == null) {
				return;
			}

			isRunning = true;
			selectionChanged(delayedselection);
			delayedselection = null;
			OpenSourceViewMenuAction.this.run();
			isRunning = false;

			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * Executes the open source action, but in order to protect against overloading the real operation is run in a background thread.
	 *
	 * @param selection the selection to process
	 * */
	public void delayedRun(final ISelection selection) {
		delayedselection = selection;

		if (runnable.isRunning()) {
			return;
		}

		Display.getDefault().asyncExec(runnable);
	}

	/**
	 * Constructor
	 * @param view the MSC View
	 */
	public OpenSourceViewMenuAction(final TextTableView view, final boolean silent, final boolean forceEditorOpening) {
		super(view, "Open Source");
		this.view = view;
		this.silent = silent;
		this.forceEditorOpening = forceEditorOpening;
	}

	@Override
	public void run() {
		if (this.eventSelection == null) {
			return;
		}

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		LogFileMetaData logFileMetaData;
		logFileMetaData = this.view.getLogFileMetaData();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(logFileMetaData.getProjectName());
		IFile logFile = project.getFile(logFileMetaData.getProjectRelativePath().substring(logFileMetaData.getProjectName().length() + 1));
		if (!logFile.exists()) {
			IViewReference[] viewReferences = activePage.getViewReferences();
			ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
			TitanLogExceptionHandler.handleException(new UserException("The log file could not be found.\n Please perform the Open Text Table action again.")); //$NON-NLS-1$
			return;
		}

		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		}

		EventObject eventObject = this.eventSelection.getEventObject();
		String testCase = this.eventSelection.getTestCaseName();
		if ((eventObject == null) || (testCase == null)) {
			return;
		}

		// get value
		LogRecord logrecord;
		try {
			logrecord = ValueReader.getInstance().readLogRecordFromLogFile(
					logFileMetaData.getFilePath(),
					eventObject);

		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("MSCView.8"))); //$NON-NLS-1$
			return;
		}

		SourceInformation sourceInformation = null;
		try {
			sourceInformation = SourceInformation.createInstance(logrecord.getSourceInformation());
		} catch (InvalidSourceInformationException e) {
			if (!silent) {
				String setting = logFileMetaData.getOptionsSettings("SourceInfoFormat");
				if (setting == null) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openError(
									new Shell(Display.getDefault()),
									"Error opening source", "This log file is not generated with source location information inserted. And it really does not seem to contain source location information"); //$NON-NLS-1$
						}
					});
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openError(new Shell(Display.getDefault()), "Error opening source", "This log record does not seem to contain source location information"); //$NON-NLS-1$
						}
					});
				}
			}

			return;
		}

		final String fileName = sourceInformation.getSourceFileName();
		if (fileName == null) {
			view.getViewSite().getActionBars().getStatusLineManager().setErrorMessage(
					"The name of the target file could not be extracted");
			return;
		}

		IFile targetFile;
		if (lastFilename != null && lastFilename.equals(fileName) && lastPath != null) {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(lastPath);
			if (files.length == 0) {
				view.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The file `" + lastFilename + "' could not be found");
				setLastFilename(null);
				return;
			}
			targetFile = files[0];
		} else {
			targetFile = OpenSourceAction.findSourceFile(project, fileName);
			if (targetFile == null) {
				view.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("The file `" + fileName + "' could not be found");
				return;
			}

			setLastFilename(fileName);
			setLastPath(targetFile.getLocationURI());
		}

		OpenSourceAction.openEditor(targetFile, sourceInformation.getLineNumber(), view, forceEditorOpening);
	}
	
	@Override
	public void selectionChanged(final ISelection selection) {
		if (selection instanceof EventSelection) {
			this.eventSelection = (EventSelection) selection;
			setEnabled(!this.eventSelection.isEmpty());
		}
		super.selectionChanged(selection);
	}

	private static synchronized void setLastFilename(String lastFilename) {
		OpenSourceViewMenuAction.lastFilename = lastFilename;
	}

	public static synchronized void setLastPath(URI lastPath) {
		OpenSourceViewMenuAction.lastPath = lastPath;
	}
}
