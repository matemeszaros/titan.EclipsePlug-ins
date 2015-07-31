/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.Parser;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;

public class RefreshMSCViewAction extends Action {

	private static final String NAME = Messages.getString("RefreshMSCViewAction.0"); //$NON-NLS-1$
	private MSCView mscView;

	public RefreshMSCViewAction(final MSCView mscView) {
		super(NAME);
		this.mscView = mscView;
	}

	@Override
	public void run() {
		// Set current log file meta data
		final LogFileMetaData logFileMetaData = this.mscView.getLogFileMetaData();
		ExecutionModel model = this.mscView.getModel();

		final PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(logFileMetaData.getProjectName());
		if (preferences.getVisualOrderComponents().isEmpty()) {
			String userE = Messages.getString("RefreshMSCViewAction.3"); //$NON-NLS-1$
			TitanLogExceptionHandler.handleException(new UserException(userE));
			return;
		}

		final IFile logFile = getSelectedLogFile(logFileMetaData);

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// Check if the log file exists
		if (!logFile.exists()) {
			IViewReference[] viewReferences = activePage.getViewReferences();
			ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
			TitanLogExceptionHandler.handleException(new UserException(Messages.getString("RefreshMSCViewAction.1"))); //$NON-NLS-1$
			return;
		}

		//	Check if the log file has been modified
		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		}

		// Get log record index file for selected log file - No need to check is exists due to
		// LogFileCacheHandler.hasLogFileChanged(logFile) returning false above
		File logRecordIndexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);

		try {
			// Read/parse log file
			final TestCase tc = model.getTestCase();
			final LogRecordIndex[] logRecordIndexes =
					LogFileCacheHandler.readLogRecordIndexFile(logRecordIndexFile, tc.getStartRecordNumber(), tc.getNumberOfRecords());


			WorkspaceJob job = new WorkspaceJob("Loading log information") {

				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
					ExecutionModel model;
					try {
						model = parseLogFile();
					} catch (Exception e) {
						ErrorReporter.logExceptionStackTrace(e);
						TitanLogExceptionHandler.handleException(new TechnicalException(
								Messages.getString("RefreshMSCViewAction.5") + e.getMessage())); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					}

					final int firstRow = getFirstRow(model, preferences);

					final ExecutionModel finalModel = model;
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							RefreshMSCViewAction.this.mscView.setModel(finalModel, firstRow);
						}
					});

					return Status.OK_STATUS;
				}

				private ExecutionModel parseLogFile() throws TechnicalException {
					ExecutionModel model;
					if (logFileMetaData.getExecutionMode() == null) {
						throw new TechnicalException("Error while parsing of the log file: ExecutionMode is null");
					}

					try {
						// re-parse tc
						Parser parser = new Parser(logFileMetaData);
						model = parser.preParse(tc, logRecordIndexes, preferences, mscView.getFilterPattern(), null);
					} catch (IOException e) {
						throw new TechnicalException("Error while parsing of the log file");
					} catch (ParseException e) {
						throw new TechnicalException("Error while parsing of the log file");
					}
					return model;
				}
			};
			job.schedule();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while parsing of the log file", e);
			TitanLogExceptionHandler.handleException(new TechnicalException(e.getMessage()));
		}
	}

	private int getFirstRow(ExecutionModel model, PreferencesHolder preferences) {
		final int firstRow;
		switch (preferences.getMscViewOpen()) {
			case PreferenceConstants.MSCVIEW_TOP:
				firstRow = 0;
				break;
			case PreferenceConstants.MSCVIEW_BOTTOM:
				firstRow = model.getNumberOfEvents() - 1;
				break;
			case PreferenceConstants.MSCVIEW_FIRST_VERDICT:
				if (model.getSetverdict().length > 0) {
					firstRow = model.getSetverdict()[0];
				} else {
					firstRow = 0;
				}
				break;
			default:
				firstRow = 0;
		}
		return firstRow;
	}

	private IFile getSelectedLogFile(LogFileMetaData logFileMetaData) {
		// Get selected log file
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(logFileMetaData.getProjectName());
		return project.getFile(logFileMetaData.getProjectRelativePath().substring(logFileMetaData.getProjectName().length() + 1));
	}
}
