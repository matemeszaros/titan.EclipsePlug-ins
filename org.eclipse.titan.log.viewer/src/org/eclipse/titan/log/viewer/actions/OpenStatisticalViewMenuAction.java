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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.extractors.TestCaseEvent;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.readers.CachedLogReader;
import org.eclipse.titan.log.viewer.readers.LogFileReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.LogFileHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.StatisticalView;
import org.eclipse.titan.log.viewer.views.details.StatisticalData;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Menu action for opening the statistical view from the Projects tab in the Navigator view
 *
 */
public class OpenStatisticalViewMenuAction extends Action implements IActionDelegate, Observer {

	private static final String NAME = Messages.getString("OpenStatisticalViewMenuAction.6");  //$NON-NLS-1$
	private IStructuredSelection selection;
	private IProgressMonitor monitor;
	private int lastWorked;
	private IFile logFile;
	private LogFileMetaData logFileMetaData;
	private boolean logFileIsSupported;	
	private TestCaseExtractor testCaseExtractor;

	public OpenStatisticalViewMenuAction() {
		super(NAME);
		testCaseExtractor = new TestCaseExtractor();
	}

	@Override
	public void run(final IAction action) {
		run();
	}

	@Override
	public void run() {
		this.logFileMetaData = null;
		this.logFileIsSupported = true;
		if (this.selection == null) {
			return;
		}

		Set<IFile> logFiles = new HashSet<IFile>(selection.size());

		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof IFile) {
				logFiles.add((IFile) object);
			} else if (object instanceof TestCase) {
				logFiles.add(((TestCase) object).getLogFile());
			}
		}

		if (logFiles.isEmpty()) {
			return;
		}

		List<StatisticalData> statisticalDataVector = createStatisticalData(logFiles);
		if (statisticalDataVector == null || statisticalDataVector.isEmpty()) {
			return;
		}

		String secondId = Constants.STATISTICAL_VIEW;
		if (statisticalDataVector.size() < 2) {
			secondId = File.separator + this.logFile.getProject().getName() + File.separator + this.logFile.getProjectRelativePath().toOSString();
		}
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference reference = activePage.findViewReference(Constants.STATISTICAL_VIEW_ID, secondId);

		// get the view
		if (reference != null) {
			StatisticalView part = (StatisticalView) reference.getView(false);
		}

		// create a new view
		try {
			StatisticalView part = (StatisticalView) activePage.showView(Constants.STATISTICAL_VIEW_ID, secondId, IWorkbenchPage.VIEW_ACTIVATE);
			part.setData(statisticalDataVector);
			part.setFocus();
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(
					new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage()));
		}
	}

	private List<StatisticalData> createStatisticalData(Set<IFile> logFiles) {
		List<StatisticalData> statisticalDataVector = new ArrayList<StatisticalData>();
		for (IFile file : logFiles) {
			this.logFile = file;

			if (!this.logFile.exists()) {
				TitanLogExceptionHandler.handleException(new UserException(Messages.getString("OpenStatisticalViewMenuAction.5"))); //$NON-NLS-1$
				return null;
			}

			File logRecordIndexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(this.logFile);
			File propertyFile = LogFileCacheHandler.getPropertyFileForLogFile(this.logFile);
			if (!logRecordIndexFile.exists() || !propertyFile.exists() || LogFileCacheHandler.hasLogFileChanged(this.logFile)) {
				processLogFile();
			} else {
				// Get log file meta data
				try {
					this.logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);
					// Extract test cases from the index file
					this.testCaseExtractor = new TestCaseExtractor();
					this.testCaseExtractor.extractTestCasesFromIndexedLogFile(this.logFile);
				} catch (IOException e) {
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage())); //$NON-NLS-1$
				} catch (ClassNotFoundException e) {
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage())); //$NON-NLS-1$

				}
			}

			if (this.logFileIsSupported) {
				try {
					if (this.logFileMetaData == null) {
						this.logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);
					}

					List<TestCase> testCases = this.testCaseExtractor.getTestCases();
					// //Create data for the statistical view
					final CachedLogReader reader = new CachedLogReader(LogFileReader.getReaderForLogFile(this.logFile));
					StatisticalData statisticalData = new StatisticalData(this.logFileMetaData, testCases, reader);
					statisticalDataVector.add(statisticalData);

				} catch (IOException e) {
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(
							new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage()));
				} catch (ClassNotFoundException e) {
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(
							new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage()));
				}
			}
		}
		return statisticalDataVector;
	}

	private void processLogFile() {
		try {
			new ProgressMonitorDialog(new Shell(Display.getDefault())).run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					OpenStatisticalViewMenuAction.this.monitor = monitor;
					OpenStatisticalViewMenuAction.this.testCaseExtractor = null;
					try {
						LogFileHandler logFileHandler = new LogFileHandler(logFile);

						// First of all, verify that the file is a TITAN supported log file
						try {
							OpenStatisticalViewMenuAction.this.logFileMetaData = logFileHandler.autoDetect();
						} catch (final TechnicalException e) {
							ErrorReporter.logExceptionStackTrace(e);
							OpenStatisticalViewMenuAction.this.logFileIsSupported = false;
							TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
							return;
						}

						OpenStatisticalViewMenuAction.this.testCaseExtractor = new TestCaseExtractor();
						OpenStatisticalViewMenuAction.this.testCaseExtractor.addObserver(OpenStatisticalViewMenuAction.this);

						if (OpenStatisticalViewMenuAction.this.monitor != null) {
							OpenStatisticalViewMenuAction.this.monitor.beginTask(
									Messages.getString("OpenStatisticalViewMenuAction.4")
											+ OpenStatisticalViewMenuAction.this.logFile.getName()
											+ Messages.getString("OpenStatisticalViewMenuAction.3"), 100);
						}
						// Extract test cases from log file
						OpenStatisticalViewMenuAction.this.testCaseExtractor.extractTestCasesFromLogFile(
								OpenStatisticalViewMenuAction.this.logFileMetaData, monitor);

						LogFileCacheHandler.fillCache(logFile, logFileMetaData, testCaseExtractor.getTestCases(), testCaseExtractor.getLogRecordIndexes());

						if (OpenStatisticalViewMenuAction.this.testCaseExtractor.failedDuringExtraction()) {
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(new Shell(Display.getDefault()),
											Messages.getString("OpenStatisticalViewMenuAction.2"),
											Messages.getString("OpenStatisticalViewMenuAction.1"));
								}
							});
						}
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace(e);
						TitanLogExceptionHandler.handleException(
								new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage()));
					} finally {
						if (OpenStatisticalViewMenuAction.this.testCaseExtractor != null) {
							OpenStatisticalViewMenuAction.this.testCaseExtractor.deleteObserver(OpenStatisticalViewMenuAction.this);
						}
						if (OpenStatisticalViewMenuAction.this.monitor != null) {
							OpenStatisticalViewMenuAction.this.monitor.done();
						}
					}
				}
			});

		} catch (InvocationTargetException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(
					new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getTargetException()));
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(
					new TechnicalException(Messages.getString("OpenStatisticalViewMenuAction.0") + e.getMessage()));
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		this.selection = (IStructuredSelection) selection;
		for (Iterator<?> iterator = this.selection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (!(object instanceof IFile || object instanceof TestCase)) {
				setEnabled(false);
				return;
			}
		}
		setEnabled(true);
	}

	@Override
	public void update(final Observable observable, final Object event) {
		if (event instanceof TestCaseEvent) {
			TestCaseEvent testCaseEvent = (TestCaseEvent) event;
			int worked = testCaseEvent.getProgress();
			if (this.monitor != null) {
				this.monitor.worked(worked - this.lastWorked);
			}
			this.lastWorked = worked;
		}
	}

}
