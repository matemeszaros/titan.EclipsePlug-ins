/*******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.text.table;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class TextTableViewHelper {
	/**
	 * Opens a TextTable view for the given log file. If the view is already open
	 * the record with the number recordToSelect will be selected.
	 * @param projectName The name of the logfile's project.
	 * @param projectRelativePath The project relative path of the log file
	 * @param recordToSelect The number of the record that will be initially selected.
	 * @return The newly created view or null if the view can not be opened.
	 */
	public static TextTableView open(final String projectName, final String projectRelativePath, final int recordToSelect) {

		String secondId = projectRelativePath;
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference reference = activePage.findViewReference(Constants.TEXT_TABLE_VIEW_ID, secondId);

		if (reference != null) {
			try {
				TextTableView view = (TextTableView) activePage.showView(Constants.TEXT_TABLE_VIEW_ID, secondId, IWorkbenchPage.VIEW_ACTIVATE);
				view.setSelectedRecord(recordToSelect);
				view.setFocus();
				return view;
			} catch (PartInitException e) {
				// do nothing
			}
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);
		final IFile logFile = project.getFile(projectRelativePath.substring(projectName.length() + 1));

		if (!logFile.exists()) {
			IViewReference[] viewReferences = activePage.getViewReferences();
			ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
			TitanLogExceptionHandler.handleException(new UserException(Messages.getString("OpenMSCViewMenuAction.4"))); //$NON-NLS-1$
			return null;
		}

		try {
			Object temp = logFile.getSessionProperty(Constants.EXTRACTION_RUNNING);
			if (temp != null && (Boolean) temp) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(null, "View can not be opened.", "Test case extraction is already running on " + logFile.getName());
					}
				});
				return null;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
		}

		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return null;
		}

		try {
			TextTableView view = (TextTableView) activePage.showView(Constants.TEXT_TABLE_VIEW_ID, secondId, IWorkbenchPage.VIEW_ACTIVATE);
			view.setInput(logFile, recordToSelect);
			view.setFocus();
			return view;
		} catch (FileNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableTestCasesViewMenuAction.0") + e.getMessage()));
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableTestCasesViewMenuAction.1") + e.getMessage()));
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableTestCasesViewMenuAction.2") + e.getMessage()));
		} catch (ClassNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableTestCasesViewMenuAction.2") + e.getMessage()));
		}
		return null;
	}

	static void updateSelectionInConnectedMscView(int selectedRecord, LogFileMetaData fileMetaData) {
		final IViewReference[] viewReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference viewReference : viewReferences) {
			final IViewPart viewPart = viewReference.getView(false);
			if (!(viewPart instanceof MSCView)) {
				continue;
			}

			final MSCView mscView = (MSCView) viewPart;
			if (mscView.getModel() == null || mscView.getModel().getTestCase() == null) {
				continue;
			}
			final TestCase testCase = mscView.getModel().getTestCase();
			if (fileMetaData.getFilePath().equals((mscView).getLogFileMetaData().getFilePath())
					&& testCase.getStartRecordNumber() <= selectedRecord
					&& testCase.getEndRecordNumber() >= selectedRecord) {
				if (mscView.getSelectedRecordNumber() != selectedRecord) {
					mscView.setSelection(selectedRecord);
				}
				break; // Only one MSC view can be opened for one test case
			}
		}
	}
}
