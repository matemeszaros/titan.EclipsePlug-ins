/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.navigator;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.actions.ExtractTestCasesAction;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
public class LogFileContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		Object[] emptyResult = new Object[] {}; 
		if (!(parentElement instanceof IFile)) {
			return emptyResult;
		}

		final IFile logFile = (IFile) parentElement;
		if (!logFile.exists()) {
			return emptyResult;
		}
		String fileExtension = logFile.getFileExtension();
		if (fileExtension == null || !fileExtension.equals(Constants.LOG_EXTENSION)) {
			return emptyResult;
		}
		
		try {
			Object temp = logFile.getSessionProperty(Constants.EXTRACTION_RUNNING);
			if (temp != null && (Boolean) temp) {
				return emptyResult;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
		}

		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			handleLogFileChange(logFile);
			return emptyResult;
		}

		
		try {
			final TestCaseExtractor extractor = new TestCaseExtractor();
			extractor.extractTestCasesFromIndexedLogFile(logFile);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart view = activePage.findView("org.eclipse.ui.navigator.ProjectExplorer");
					if (view instanceof CommonNavigator) {
						CommonViewer viewer = ((CommonNavigator) view).getCommonViewer();
						for (TestCase testCase : extractor.getTestCases()) {
							viewer.expandToLevel(testCase, AbstractTreeViewer.ALL_LEVELS);
							viewer.refresh(testCase, true);
						}
					}
				}
			});
			return extractor.getTestCases().toArray();
		} catch (Exception e) {
			LogFileCacheHandler.clearCache(logFile);
			ErrorReporter.logExceptionStackTrace(e);
			return emptyResult;
		}
	}

	private void handleLogFileChange(final IFile logFile) {
		WorkspaceJob job = new WorkspaceJob("Testcase extraction from logfile " + logFile.getProjectRelativePath().toString()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				ExtractTestCasesAction extractTestCasesAction = new ExtractTestCasesAction(logFile);
				try {
					extractTestCasesAction.run(monitor);
				} catch (InvocationTargetException e) {
					LogFileCacheHandler.clearCache(logFile);
					ErrorReporter.logExceptionStackTrace(e);
				} catch (InterruptedException e) {
					LogFileCacheHandler.clearCache(logFile);
				}
				return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.setRule(LogFileCacheHandler.getSchedulingRule(logFile));
		job.schedule();
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof TestCase) {
			return ((TestCase) element).getLogFile();
		}
		
		if (element instanceof IResource) {
			return ((IResource) element).getParent();
		}
		
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return element instanceof IFile
				&& "log".equals(((IFile) element).getFileExtension());
	}
}
