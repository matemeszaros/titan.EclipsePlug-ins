/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.console.ConsoleWriter;
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
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OpenMSCViewMenuAction extends Action implements IActionDelegate, ISelectionChangedListener {
	
	private static final String NAME = Messages.getString("OpenMSCViewMenuAction.0"); //$NON-NLS-1$
	private IStructuredSelection selection;
	private LogFileMetaData logFileMetaData;
	private int recordToSelect = -1;
	
	/**
	 * Constructor
	 */
	public OpenMSCViewMenuAction() {
		super(NAME);
	}

	@Override
	public void run() {
		if (!isEnabled()) {
			return;
		}
		Object element = this.selection.getFirstElement();
		if (!(element instanceof TestCase)) {
			return;
		}

		final TestCase tc = (TestCase) element;
		final IFile logFile = tc.getLogFile();
		try {
			logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(LogFileCacheHandler.getPropertyFileForLogFile(logFile));
		} catch (IOException e1) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		} catch (ClassNotFoundException e1) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		}

		try {
			if (!PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName()).getVisualOrderComponents().isEmpty()) {
				// Get start time
				final long start = new Date().getTime();
				
				if (!logFile.exists()) {
					final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewReference[] viewReferences = activePage.getViewReferences();
					ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
					TitanLogExceptionHandler.handleException(new UserException(Messages.getString("OpenMSCViewMenuAction.4"))); //$NON-NLS-1$
					return;
				}
				
				//	Check if the file has been modified
				if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
					LogFileCacheHandler.handleLogFileChange(logFile);
					return;
				}

				// Get log record index file for selected log file - No need to check is exists due to 
				// LogFileCacheHandler.hasLogFileChanged(logFile) returning false above
				File logRecordIndexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);
				final LogRecordIndex[] logRecordIndexes = LogFileCacheHandler.readLogRecordIndexFile(logRecordIndexFile, tc.getStartRecordNumber(), tc.getNumberOfRecords());
				final PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName());

				WorkspaceJob job = new WorkspaceJob("Loading log information") {

					@Override
					public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
						if (OpenMSCViewMenuAction.this.logFileMetaData == null || OpenMSCViewMenuAction.this.logFileMetaData.getExecutionMode() == null) {
							return Status.CANCEL_STATUS;
						}

						final Parser parser;
						final ExecutionModel model;
						try {
							parser = new Parser(OpenMSCViewMenuAction.this.logFileMetaData);
							parser.setStart(start);
							model = parser.preParse(tc, logRecordIndexes, preferences, null, monitor);
						} catch (TechnicalException e) {
							ErrorReporter.logExceptionStackTrace(e);
							TitanLogExceptionHandler.handleException(new TechnicalException(
									Messages.getString("OpenMSCViewMenuAction.3") + e.getMessage())); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						} catch (ParseException e) {
							ErrorReporter.logExceptionStackTrace(e);
							TitanLogExceptionHandler.handleException(new TechnicalException(
									Messages.getString("OpenMSCViewMenuAction.2") + e.getMessage())); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						} catch (IOException e) {
							ErrorReporter.logExceptionStackTrace("Error while parsing of the log file", e);
							TitanLogExceptionHandler.handleException(new TechnicalException(e.getMessage()));
							return Status.CANCEL_STATUS;
						}

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								showView(model, parser, tc);

								// Write data to the console
								final long end = new Date().getTime();
								parser.setEnd(end);
								ConsoleWriter.getInstance().writeModelData(OpenMSCViewMenuAction.this.logFileMetaData.getProjectName(), 
										parser, 
										model, 
										OpenMSCViewMenuAction.this.logFileMetaData.getFilePath().toString());
							}
						});

						return Status.OK_STATUS;
					}
				};
				job.schedule();

			} else {
				String userE = Messages.getString("OpenMSCViewMenuAction.1"); //$NON-NLS-1$
				TitanLogExceptionHandler.handleException(new UserException(userE));
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while parsing of the log file", e);
			TitanLogExceptionHandler.handleException(new TechnicalException(e.getMessage()));
		}
	}

	private static String generateSecondaryViewId(final TestCase testCase, final LogFileMetaData metaData) {
		// Secondary ID can not contain any ":" char, so replace with "_"
		final String secondId = testCase.getTestCaseName()
			// Since one log file can several tc:s with the same name a counter is needed
			+ testCase.getTestCaseNumber()
			// Even if log file has same name, it can still be in a different path
			+ metaData.getFilePath().toString().replaceAll(":", "_") //$NON-NLS-1$//$NON-NLS-2$
			// Needed if log file is in another project (otherwise header will not be updated) 
			+ metaData.getProjectRelativePath();
		return secondId;
	}

	private int getFirstRowToSelect(final PreferencesHolder preferences,
			final ExecutionModel model) {
		final int firstRow;
		if (recordToSelect == -1) {
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
		} else {
			firstRow = model.getRecordsPosition(recordToSelect) + 2; 
			// + 2 == difference between the log records position in the events vector
			//			and the position on the screen
		}
		return firstRow;
	}
	
	private void showView(final ExecutionModel model, final Parser parser, final TestCase tc) {
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final String viewId = Constants.MSC_VIEW_ID;
		final String secondId = generateSecondaryViewId(tc, this.logFileMetaData);
		IViewReference reference = activePage.findViewReference(viewId, secondId);
		MSCView part = null;

		// Get the view
		if (reference != null) {
			part = (MSCView) reference.getView(false);
		}
		// If something is wrong, close the view
		if (part != null) {
			activePage.hideView(part);
		}

		// Create the new view
		try {
			part = (MSCView) activePage.showView(viewId, secondId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(
					Messages.getString("OpenMSCViewMenuAction.3") + e.getMessage())); //$NON-NLS-1$
			return;
		}

		part.setLogFileMetaData(OpenMSCViewMenuAction.this.logFileMetaData);
		final PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName());
		part.setModel(model, getFirstRowToSelect(preferences, model));
	}

	@Override
	public void run(final IAction action) {
		run();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			setEnabled(false);
			return;
		}
		
		this.selection = (IStructuredSelection) selection;
		if (this.selection.size() != 1 || !(this.selection.getFirstElement() instanceof TestCase)) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		selectionChanged(null, event.getSelection());
	}

	/**
	 * The given record will be selected in the opening MSC view.
	 * @param recordNumber the recordNumber of the record
	 */
	public void setFirstRow(final int recordNumber) {
		this.recordToSelect = recordNumber;
	}
}
