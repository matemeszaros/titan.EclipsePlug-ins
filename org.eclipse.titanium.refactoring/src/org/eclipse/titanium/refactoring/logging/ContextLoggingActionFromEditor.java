/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * This class handles the {@link ContextLoggingRefactoring} class when the operation is
 * called from the editor for a part of a single file.
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Viktor Varga
 */
public class ContextLoggingActionFromEditor extends AbstractHandler {
	private static final String ERR_MSG_NO_SELECTION = "Empty selection! ";
	
	private TextSelection selection;
	private IStatusLineManager statusLineManager;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		//update AST
		Utils.updateASTForProjectActiveInEditor("ContextLogging");
		Activator.getDefault().pauseHandlingResourceChanges();

		// getting the active editor
		TTCN3Editor targetEditor = Utils.getActiveEditor();
		if (targetEditor == null) {
			return null;
		}
		// getting status line manager
		statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();

		// getting selected file
		IFile selectedFile = Utils.getSelectedFileInEditor("MinimizeVisibility");
		if (selectedFile == null) {
			return null;
		}
		
		// getting selection
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection sel = selectionService.getSelection();
		if (sel == null) {
			setStatusLineMsg(ERR_MSG_NO_SELECTION, statusLineManager);
			return null;
		} else if (!(sel instanceof TextSelection)) {
			ErrorReporter.logError("ContextLoggingActionFromEditor:" +
					" selection is not a TextSelection");
			return null;
		}
		selection = (TextSelection)sel;
		
		//
		ContextLoggingRefactoring refactoring;
		if (selection.getLength() == 0) {
			IStructuredSelection ssel = new StructuredSelection(new Object[]{selectedFile});
			refactoring = new ContextLoggingRefactoring(ssel, null);
		} else {
			refactoring = new ContextLoggingRefactoring(selectedFile, selection, null);
		}
		
		//open wizard
		ContextLoggingWizard wiz = new ContextLoggingWizard(refactoring);
		RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wiz);
		try {
			operation.run(targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was cancelled
		} catch (Exception e) {
			ErrorReporter.logError("ContextLoggingActionFromEditor: Error while performing refactoring change! ");
			ErrorReporter.logExceptionStackTrace(e);
		}
		
		//update AST again
		Activator.getDefault().resumeHandlingResourceChanges();

		IProject project = selectedFile.getProject();
		GlobalParser.getProjectSourceParser(project).reportOutdating(selectedFile);
		GlobalParser.getProjectSourceParser(project).analyzeAll();
		
		return null;
	}

	static void setStatusLineMsg(final String msg, final IStatusLineManager toSet) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				toSet.setErrorMessage(msg);
				
			}
		});
	}
	
}
