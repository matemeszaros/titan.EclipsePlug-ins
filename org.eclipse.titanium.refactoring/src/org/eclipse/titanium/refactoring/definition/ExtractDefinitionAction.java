/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * This class handles the {@link ExtractDefinitionRefactoring} class.
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Viktor Varga
 */
public class ExtractDefinitionAction extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		
		Utils.updateASTForProjectActiveInEditor("ExtractDefinition");
		//getting current text selection in editor
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection sel = selectionService.getSelection();
		
		if (sel == null) {
			//no selection
			return null;
		} else if (!(sel instanceof TextSelection)) {
			ErrorReporter.logError("ExtractDefinitionAction: Selection is not a TextSelection!");
			return null;
		}
		TextSelection textSelection = (TextSelection)sel;
		
		//getting selected def
		ExtractDefinitionRefactoring refactoring = new ExtractDefinitionRefactoring();
		Definition selectedDef = refactoring.getSelection();
		
		//create wizard and ask for the project name, only if the selection is valid & create project
		if (selectedDef == null) {
			ErrorReporter.logError("ExtractDefinitionAction: Selected definition is null.");
			return null;
		}
		
		ExtractDefinitionWizard wiz = new ExtractDefinitionWizard(selectedDef.getIdentifier().getName());
		//
		StructuredSelection ssel = new StructuredSelection(textSelection);
		wiz.init(PlatformUI.getWorkbench(), ssel);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
		dialog.open();
		IProject newProj = wiz.getProject();
		if (newProj == null) {
			ErrorReporter.logError("ExtractDefinitionAction: Wizard returned a null project. ");
			return null;
		}
		refactoring.setTargetProject(newProj);
		//copy project settings to new project
		IProject sourceProj = refactoring.getSourceProject();
		ProjectFileHandler pfh = new ProjectFileHandler(sourceProj);
		if (pfh.projectFileExists()) {
			//IResource.copy(...) is used because ProjectFileHandler.getDocumentFromFile(...) is not working
			final IFile settingsFile = sourceProj.getFile("/" + ProjectFileHandler.XML_TITAN_PROPERTIES_FILE);
			final IFile settingsCopy = newProj.getFile("/" + ProjectFileHandler.XML_TITAN_PROPERTIES_FILE);
			try {
				if (settingsCopy.exists()) {
					settingsCopy.delete(true, new NullProgressMonitor());
				}
				settingsFile.copy(settingsCopy.getFullPath(), true, new NullProgressMonitor());
			} catch (CoreException ce) {
				ErrorReporter.logError("ExtractDefinitionAction: Copying project settings to new project failed.");
			}
		}
		
		//performing the refactor operation
		refactoring.perform();
		//reanalyze project
		WorkspaceJob job = GlobalParser.getProjectSourceParser(newProj).analyzeAll();
		if (job != null) {
			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		return null;
	}
	
}
