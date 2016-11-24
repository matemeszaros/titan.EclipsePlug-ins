/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.visibility;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This class handles the {@link MinimizeVisibilityRefactoring} class when the operation is
 * called from the package browser for a single or multiple project(s), folder(s) or file(s).
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Viktor Varga
 */
public class MinimizeVisibilityActionFromBrowser extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		performMinimizeVisibility();
		return null;
	}
	@Override
	public void run(IAction action) {
		performMinimizeVisibility();
		
	}
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	private void performMinimizeVisibility() {
		// getting the active editor
		TTCN3Editor targetEditor = Utils.getActiveEditor();
		//find selection
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		final IStructuredSelection structSelection = (IStructuredSelection)selection;
		Set<IProject> projsToUpdate = Utils.findAllProjectsInSelection(structSelection);
		
		//update AST before refactoring
		Utils.updateASTBeforeRefactoring(projsToUpdate, "MinimizeVisibility");
		Activator.getDefault().pauseHandlingResourceChanges();
		
		//create refactoring
		MinimizeVisibilityRefactoring refactoring = new MinimizeVisibilityRefactoring(structSelection);
		//open wizard
		MinimizeVisibilityWizard wiz = new MinimizeVisibilityWizard(refactoring);
		RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wiz);
		try {
			operation.run(targetEditor == null ? null : targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was cancelled
		} catch (Exception e) {
			ErrorReporter.logError("MinimizeVisibilityActionFromBrowser: Error while performing refactoring change! ");
			ErrorReporter.logExceptionStackTrace(e);
		}
		Activator.getDefault().resumeHandlingResourceChanges();


		//update AST after refactoring
		Utils.updateASTAfterRefactoring(wiz, refactoring.getAffectedObjects(), refactoring.getName());
	}
	
	
}
