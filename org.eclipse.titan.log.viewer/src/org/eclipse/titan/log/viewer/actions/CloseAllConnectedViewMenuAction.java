/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Menu action for closing the opened views of the selected log file
 *
 */
// TODO this action should handle selected test cases as well
// TODO this action should handle multiple selection
public class CloseAllConnectedViewMenuAction extends AbstractHandler implements IActionDelegate {

	private IStructuredSelection selection;
	private IFile logFile;
	
	/**
	 * Constructor
	 */
	public CloseAllConnectedViewMenuAction() {
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
		if (this.selection == null) {
			return;
		}
		if (!SelectionUtils.isSelectionALogFile(this.selection)) {
			return;
		}
		this.logFile = SelectionUtils.selectionToIFile(this.selection);
		if (this.logFile == null) {
			return;
		}
		
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] viewReferences = activePage.getViewReferences();
		ActionUtils.closeAssociatedViews(activePage, viewReferences, logFile);
	}
	
	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		this.selection = (IStructuredSelection) selection;
		setEnabled(SelectionUtils.isSelectionALogFile(selection));
	}
	
}

