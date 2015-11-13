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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.log.viewer.console.TITANDebugConsole;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.extractors.TestCaseEvent;
import org.eclipse.titan.log.viewer.extractors.TestCaseEventDispatcher;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.LogFileHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Class for the action which extracts test cases
 * from a TITAN Log Viewer log file 
 *
 */
public class ExtractTestCasesAction implements IRunnableWithProgress, Observer {

	public static final String PROJECT_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
	private IFile logFile;
	private final TestCaseExtractor testCaseExtractor;
	private LogFileHandler logFileHandler;
	private LogFileMetaData logFileMetaData;
	private IProgressMonitor monitor;
	private int lastWorked;
	private TestCaseEventDispatcher testCaseEventDispatcher;

	/**
	 * Extracts test cases from a log file
	 * @param logFile The input file full path
	 */
	public ExtractTestCasesAction(final IFile logFile) {
		this.logFile = logFile;
		this.testCaseExtractor = new TestCaseExtractor();
		this.testCaseEventDispatcher = new TestCaseEventDispatcher();
		this.testCaseExtractor.addObserver(this.testCaseEventDispatcher);
		this.logFileHandler = new LogFileHandler(logFile);
		this.logFileMetaData = null;
		this.lastWorked = 0;
		// Add this viewer as an observer on ProxyBroker
		this.testCaseEventDispatcher.addObserver(this);
	}

	@Override
	public void run(final IProgressMonitor pMonitor) throws InvocationTargetException, InterruptedException {
		this.monitor = pMonitor == null ? new NullProgressMonitor() : pMonitor;

		// First of all, verify that the file is a TITAN supported log file
		try {
			this.logFileMetaData = this.logFileHandler.autoDetect();
		} catch (TechnicalException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
			return;
		}

		try {
			Object temp = logFile.getSessionProperty(Constants.EXTRACTION_RUNNING);
			if (temp != null && (Boolean) temp) {
				monitor.done();
				return;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
		}
		
		try {
			this.monitor.beginTask(Messages.getString("ExtractTestCasesAction.0"), 100); //$NON-NLS-1$

			// Check if a property file exists for the log file (if log file has changed or not)
			boolean logFileHasChanged = LogFileCacheHandler.hasLogFileChanged(this.logFile);
			
			// Get the property file
			File propertyFile = LogFileCacheHandler.getPropertyFileForLogFile(this.logFile);

			if (logFileHasChanged) {
				if (updateLogFileIndex()) {
					return;
				}
			} else {
				if (Constants.DEBUG) {
					TITANDebugConsole.getConsole().newMessageStream().println("Log file has NOT changed -> extracting from index file!"); //$NON-NLS-1$
				}
				// Get log file meta data 
				this.logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);
				// Extract test cases from the index file
				this.testCaseExtractor.extractTestCasesFromIndexedLogFile(this.logFile);
			}
		} catch (final IOException e) {
			handleExtractingError(e);
		} catch (final ClassNotFoundException e) {
			handleExtractingError(e);
		} catch (final CoreException e) {
			handleExtractingError(e);
		}

		setExtractionRunningProperty(false);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart view = activePage.findView(PROJECT_EXPLORER_VIEW_ID);

				if (view instanceof CommonNavigator) {
					CommonViewer viewer = ((CommonNavigator) view).getCommonViewer();
					viewer.refresh(logFile, true);
					viewer.expandToLevel(logFile, AbstractTreeViewer.ALL_LEVELS);
				}
			}
		});
		this.testCaseEventDispatcher.deleteObserver(this);
		this.testCaseExtractor.deleteObserver(this.testCaseEventDispatcher);
		this.monitor.done();
	}

	private boolean updateLogFileIndex() throws CoreException, IOException {
		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("Log file has changed -> extracting from log file!"); //$NON-NLS-1$
		}
		logFile.setSessionProperty(Constants.EXTRACTION_RUNNING, true);

		ResourceUtils.refreshResources(Arrays.asList(logFile));

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart view = activePage.findView(PROJECT_EXPLORER_VIEW_ID);
				if (view instanceof CommonNavigator) {
					CommonViewer viewer = ((CommonNavigator) view).getCommonViewer();
					viewer.refresh(logFile, true);
					viewer.collapseToLevel(logFile, AbstractTreeViewer.ALL_LEVELS);
				}
			}
		});

		// Extract test cases from log file
		this.testCaseExtractor.extractTestCasesFromLogFile(this.logFileMetaData, monitor);

		if (monitor.isCanceled()) {
			LogFileCacheHandler.clearCache(logFile);
			monitor.done();
			setExtractionRunningProperty(false);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart view = activePage.findView("org.eclipse.ui.navigator.ProjectExplorer");
					if (view instanceof CommonNavigator) {
						CommonViewer viewer = ((CommonNavigator) view).getCommonViewer();
						viewer.refresh(logFile, true);
						viewer.collapseToLevel(logFile, AbstractTreeViewer.ALL_LEVELS);
					}
				}
			});
			return true;
		}

		LogFileCacheHandler.fillCache(logFile, this.logFileMetaData,
				this.testCaseExtractor.getTestCases(), this.testCaseExtractor.getLogRecordIndexes());
		return false;
	}

	private void setExtractionRunningProperty(boolean on) {
		try {
			logFile.setSessionProperty(Constants.EXTRACTION_RUNNING, on);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	private void handleExtractingError(final Exception e) throws InterruptedException {
		ErrorReporter.logExceptionStackTrace(e);
		// Generate Technical Error
		String errorMsg = Messages.getString("ExtractTestCasesAction.5")
				+ this.logFile.getName() + Messages.getString("ExtractTestCasesAction.6") + e.getMessage();
		TitanLogExceptionHandler.handleException(new TechnicalException(errorMsg));
		// Clear cache so index file is regenerated next time
		LogFileCacheHandler.clearCache(this.logFile);
		throw new InterruptedException(errorMsg);
	}

	@Override
	public void update(final Observable observable, final Object event) {
		if (event instanceof TestCaseEvent) {
			TestCaseEvent testCaseEvent = (TestCaseEvent) event;
			this.monitor.subTask(testCaseEvent.getTestCaseName());
			int worked = testCaseEvent.getProgress();
			this.monitor.worked(worked - this.lastWorked);
			this.lastWorked = worked;
		}
	}
	
	/**
	 * Returns the extracted testcases from the testCaseExtractor
	 *  @return the testcases
	 */
	public List<TestCase> getTestCases() {
		return this.testCaseExtractor.getTestCases();
	}
}
