/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.log.viewer.extractors.TestCaseEvent;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.titan.log.viewer.views.text.table.TextTableViewHelper;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Menu action for opening the text table view from the Projects tab in the Navigator view
 *
 */
//TODO remove unnecessary functions after conversion
public class OpenTextTableProjectsViewMenuAction extends AbstractHandler implements IActionDelegate, Observer {

	private IStructuredSelection selection;
	private IProgressMonitor monitor;
	private int lastWorked;
	private IFile logFile;

	/**
	 * Constructor
	 */
	public OpenTextTableProjectsViewMenuAction() {
	}

	@Override
	public void run(final IAction action) {
		run(selection);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection tempSelection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (!(tempSelection instanceof IStructuredSelection)) {
			return null;
		}

		selection = (IStructuredSelection) tempSelection;

		run(selection);

		return null;
	}

	public void run(final IStructuredSelection selection) {
		if (selection == null) {
			return;
		}
		
		int logRecordToSelect = 0;
		if (SelectionUtils.isSelectionALogFile(this.selection)) {
			this.logFile = SelectionUtils.selectionToIFile(this.selection);
		} else if (selection.getFirstElement() instanceof TestCase) {
			TestCase testCase = (TestCase) selection.getFirstElement();
			this.logFile = testCase.getLogFile();
			logRecordToSelect = testCase.getStartRecordNumber();
		} else {
			return;
		}
		
		if (this.logFile == null) {
			return;
		}
		
		final String projectName = logFile.getProject().getName();
		final String projectRelativePath = File.separator + projectName + File.separator + this.logFile.getProjectRelativePath().toOSString();
		if (LogFileCacheHandler.hasLogFileChanged(logFile) && !LogFileCacheHandler.processLogFile(logFile, new NullProgressMonitor(), false)) {
			return;
		}
		TextTableViewHelper.open(projectName, projectRelativePath, logRecordToSelect);
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		this.selection = (IStructuredSelection) selection;
		if (this.selection.size() != 1) {
			setEnabled(false);
			return;
		}
		boolean isSelectionATestCase = this.selection.getFirstElement() instanceof TestCase;
		setEnabled(isSelectionATestCase || SelectionUtils.isSelectionALogFile(selection));
	}
	
	@Override
	public void update(final Observable observable, final Object event) {
		if (event instanceof TestCaseEvent) {
			TestCaseEvent testCaseEvent = (TestCaseEvent) event;
			int worked = testCaseEvent.getProgress();
			this.lastWorked = worked;
		}
	}
	
}
