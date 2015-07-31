/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.titan.log.viewer.views.msc.model.IEventObject;

/**
 * Opens the value view
 *
 */
public class OpenValueViewAction extends SelectionProviderAction implements DelayedSelectable {

	private static final String NAME = Messages.getString("OpenValueViewAction.0"); //$NON-NLS-1$
	private MSCView mscView;
	private Integer selectedLine;
	private boolean forceEditorOpening = false;

	private ISelection delayedSelection = null;

	private DelayedSelector runnable = new DelayedSelector(this);
	/**
	 * Constructor
	 * @param view the MSC View
	 */
	public OpenValueViewAction(final MSCView view, final boolean forceEditorOpening) {
		super(view.getMSCWidget(), NAME);
		this.mscView = view;
		this.forceEditorOpening = forceEditorOpening;
	}

	@Override
	public void dispose() {
		super.dispose();
		runnable.setShouldRun(false);
		synchronized (runnable.getLock()) {
			runnable.getLock().notifyAll();
		}
		runnable = null;
		mscView = null;
	}

	/**
	 * Executes the open value view action, but in order to protect against overloading the real operation is run in a background thread.
	 *
	 * @param selection the selection to process.
	 * */
	public void delayedRun(final ISelection selection) {
		delayedSelection = selection;

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
	public ISelection getDelayedSelection() {
		return delayedSelection;
	}

	@Override
	public void setDelayedSelection(ISelection delayedSelection) {
		this.delayedSelection = delayedSelection;
	}

	@Override
	public void run() {
		if (selectedLine == null || this.mscView == null || selectedLine < 2 || selectedLine >= this.mscView.getModel().getNumberOfEvents() + 2) {
			return;
		}

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (activePage == null) {
			return;
		}

		LogFileMetaData logFileMetaData = this.mscView.getLogFileMetaData();
		IProject project = getProjectByName(logFileMetaData);
		IFile logFile = getLogFileFromProject(logFileMetaData, project);

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
		
		DetailsView detailsview  = (DetailsView) activePage.findView(Constants.DETAILS_VIEW_ID);
		if (detailsview == null && !forceEditorOpening) {
			return;
		}
		
		if (forceEditorOpening) {
			try {
				detailsview = (DetailsView) activePage.showView(Constants.DETAILS_VIEW_ID);
			} catch (PartInitException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenValueViewAction.4") + e.getMessage())); //$NON-NLS-1$
				return;
			}
		}

			// pass log file meta data
			detailsview.setLogFileMetaData(this.mscView.getLogFileMetaData());

			ExecutionModel model = this.mscView.getModel();
			IEventObject ieventObject = model.getEvent(selectedLine - 2);
			if (!(ieventObject instanceof EventObject)) {
				return;
			}

			EventObject eventObject = (EventObject) ieventObject;
			String testCase = model.getTestCase().getTestCaseName();
			if ((testCase == null) || eventObject.getRecordNumber() == 0) {
				return;
			}
			
			// get value
			LogRecord logrecord;
			try {
				logrecord = ValueReader.getInstance().readLogRecordFromLogFileCached(
						this.mscView.getLogFileMetaData().getFilePath(),
						eventObject);
				
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenValueViewAction.3"))); //$NON-NLS-1$
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
	}

	private IFile getLogFileFromProject(LogFileMetaData logFileMetaData, IProject project) {
		return project.getFile(logFileMetaData.getProjectRelativePath().substring(logFileMetaData.getProjectName().length() + 1));
	}

	private IProject getProjectByName(LogFileMetaData logFileMetaData) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		return root.getProject(logFileMetaData.getProjectName());
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
}
