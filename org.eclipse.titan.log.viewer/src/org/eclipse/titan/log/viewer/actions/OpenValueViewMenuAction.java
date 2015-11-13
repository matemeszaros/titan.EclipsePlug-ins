/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.io.IOException;
import java.text.ParseException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.readers.ValueReader;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.DetailsView;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.details.DetailData;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.EventSelection;
import org.eclipse.titan.log.viewer.views.text.table.TextTableView;

/**
 * Menu action for opening the value view
 *
 */
public class OpenValueViewMenuAction extends SelectionProviderAction {

	private static final String NAME = Messages.getString("OpenValueViewMenuAction.0"); //$NON-NLS-1$
	private EventSelection eventSelection;
	private ISelectionProvider view;
	private boolean forceEditorOpening = false;

	private ISelection delayedSelection = null;
	private InternalRunnable runnable = new InternalRunnable();

	class InternalRunnable implements Runnable {
		private boolean running = false;
		public boolean isRunning() {
			return running;
		}
		@Override
		public void run() {
			if (delayedSelection == null) {
				return;
			}

			running = true;
			selectionChanged(delayedSelection);
			delayedSelection = null;
			OpenValueViewMenuAction.this.run();
			running = false;

			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * Constructor
	 * @param view the MSC View
	 */
	public OpenValueViewMenuAction(final ISelectionProvider view, final boolean forceEditorOpening) {
		super(view, NAME);
		this.view = view;
		this.forceEditorOpening = forceEditorOpening;
	}

	/**
	 * Executes the open value view action, but in order to protect against overloading the real operation is run in a background thread.
	 *
	 * @param selection the selection to process
	 * */
	public void delayedRun(final ISelection selection) {
		delayedSelection = selection;

		if (runnable.isRunning()) {
			return;
		}

		Display.getDefault().asyncExec(runnable);
	}

	@Override
	public void run() {
		if (this.eventSelection == null) {
			return;
		}
		
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		LogFileMetaData logFileMetaData;
		if (this.view instanceof MSCView) {
			logFileMetaData = ((MSCView) this.view).getLogFileMetaData();
		} else if (this.view instanceof TextTableView) {
			logFileMetaData = ((TextTableView) this.view).getLogFileMetaData();
		} else {
			return;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(logFileMetaData.getProjectName());
		IFile logFile = project.getFile(logFileMetaData.getProjectRelativePath().substring(logFileMetaData.getProjectName().length() + 1));
		if (!logFile.exists()) {
			IViewReference[] viewReferences = activePage.getViewReferences();
			ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
			if (this.view instanceof TextTableView) {
				TitanLogExceptionHandler.handleException(new UserException("The log file could not be found.\n Please perform the Open Text Table action again.")); //$NON-NLS-1$
			} else {
				TitanLogExceptionHandler.handleException(new UserException(Messages.getString("OpenValueViewMenuAction.1"))); //$NON-NLS-1$
			}
			return;
		}
		
		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		}
		
		DetailsView detailsview = (DetailsView) activePage.findView(Constants.DETAILS_VIEW_ID);
		if (detailsview == null && !forceEditorOpening) {
			return;
		}
		
		if (forceEditorOpening) {
			try {
				detailsview = (DetailsView) activePage.showView(Constants.DETAILS_VIEW_ID);
			} catch (PartInitException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("MSCView.1") + e.getMessage())); //$NON-NLS-1$
				return;
			}
		}

		// pass log file meta data
		detailsview.setLogFileMetaData(logFileMetaData);

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

		} catch (final IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("MSCView.8"))); //$NON-NLS-1$
			return;
		} catch (final ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("MSCView.8"))); //$NON-NLS-1$
			return;
		}
		String message = logrecord.getMessage();
		DetailData detailData = new DetailData(eventObject.getName(), 
				eventObject.getPort(), 
				message, 
				testCase,
				eventObject.getEventType(),
				logrecord.getSourceInformation());

		detailsview.setData(detailData, false);
		detailsview.setFocus();
	}
	
	@Override
	public void selectionChanged(final ISelection selection) {
		if (selection instanceof EventSelection) {
			this.eventSelection = (EventSelection) selection;
			setEnabled(!this.eventSelection.isEmpty());
		}
		super.selectionChanged(selection);
	}
}
