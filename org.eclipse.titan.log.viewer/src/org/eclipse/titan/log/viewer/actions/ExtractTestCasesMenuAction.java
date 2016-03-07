/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.console.ConsoleWriter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Extracts test cases from a TITAN log file and shows the test cases
 * in the test cases viewer in the navigator view
 */
//TODO remove unnecessary functions after conversion
public class ExtractTestCasesMenuAction extends AbstractHandler implements IActionDelegate {

	private ISelection selection;
		
	public ExtractTestCasesMenuAction() {
	}
	
	public void run(final ISelection selection) {
		if (selection == null) {
			return;
		}
		if (!SelectionUtils.isSelectionALogFile(selection)) {
			return;
		}
		final IFile logFile = SelectionUtils.selectionToIFile(selection);
		try {
			Object temp = logFile.getSessionProperty(Constants.EXTRACTION_RUNNING);
			if (temp != null && (Boolean) temp) {
				return;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		WorkspaceJob op = new WorkspaceJob("Testcase extraction from logfile " + logFile.getProjectRelativePath().toString()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				final ExtractTestCasesAction extractTestCasesAction = new ExtractTestCasesAction(logFile);
				try {
					final long start = System.currentTimeMillis();
					extractTestCasesAction.run(monitor);
					final long stop = System.currentTimeMillis();

					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							int numTestCases = extractTestCasesAction.getTestCases().size();
							String projectName = logFile.getProject().getName();
							ConsoleWriter.getInstance().writeToConsole(
									Messages.getString("ExtractTestCasesAction.3") + numTestCases
											+ Messages.getString("ExtractTestCasesAction.1") + logFile.getName()
											+ Messages.getString("ExtractTestCasesAction.2") + (stop - start) / 1000.0
											+ Messages.getString("ExtractTestCasesAction.4"), projectName);
							ConsoleWriter.getInstance().writeToConsole("", projectName); //$NON-NLS-1$
						}
					});
				} catch (InvocationTargetException e) {
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(new TechnicalException(
							Messages.getString("OpenWithLogViewer.2") + e.getTargetException().getMessage()));
				} catch (InterruptedException e) {
					// cancel button is de-activated, this exception can occur when the extraction fails
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenWithLogViewer.3") + e.getMessage()));
				}

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.LONG);
		op.setUser(true);
		op.setRule(logFile.getProject());
		op.schedule();
	}
	
	@Override
	public void run(final IAction action) {
		run(selection);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		run(selection);

		return null;
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		this.selection = (IStructuredSelection) selection;
		setEnabled(SelectionUtils.isSelectionALogFile(this.selection));
	}
}
