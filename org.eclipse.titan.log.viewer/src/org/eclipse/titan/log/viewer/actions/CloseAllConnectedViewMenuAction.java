/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Menu action for closing the opened views of the selected log file
 *
 */
// TODO this action should handle selected test cases as well
// TODO this action should handle multiple selection
public class CloseAllConnectedViewMenuAction extends Action implements IActionDelegate {

	private static final String NAME = "Close all connected views";  //$NON-NLS-1$
	private IStructuredSelection selection;
	private IFile logFile;
	
	/**
	 * Constructor
	 */
	public CloseAllConnectedViewMenuAction() {
		super(NAME);
	}
	
	@Override
	public void run(final IAction action) {
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

