/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.PlatformUI;

/**
 * This class handles the {@link ExtractModuleParRefactoring} class when the operation is
 * called from the editor for a part of a single file.
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Viktor Varga
 */
public class ExtractModuleParActionFromEditor extends AbstractHandler {

	private IProject sourceProj;
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		// getting the active editor
		TTCN3Editor targetEditor = Utils.getActiveEditor();
		if (targetEditor == null) {
			return null;
		}
		IFile selectedFile = Utils.getSelectedFileInEditor("ExtractModulePar");
		if (selectedFile == null) {
			return null;
		}
		
		//getting current project
		sourceProj = selectedFile.getProject();
		if (sourceProj == null) {
			ErrorReporter.logError("ExtractModuleParActionFromEditor: Source project is null. ");
			return null;
		}
		
		//update AST
		Set<IProject> projsToUpdate = new HashSet<IProject>();
		projsToUpdate.add(sourceProj);
		Utils.updateASTBeforeRefactoring(projsToUpdate, "ExtractModulePar");
		
		//create refactoring
		ExtractModuleParRefactoring refactoring = new ExtractModuleParRefactoring(sourceProj);
		
		ExtractModuleParWizard wiz = new ExtractModuleParWizard();
		//
		StructuredSelection ssel = new StructuredSelection(sourceProj);
		wiz.init(PlatformUI.getWorkbench(), ssel);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
		dialog.open();
		boolean saveModuleParsOption = wiz.getSaveModuleParsOption();
		IProject newProj = wiz.getProject();
		if (newProj == null) {
			ErrorReporter.logError("ExtractModuleParActionFromEditor: Wizard returned a null project. ");
			return null;
		}
		refactoring.setTargetProject(newProj);
		refactoring.setOption_saveModuleParList(saveModuleParsOption);
		//copy project settings to new project
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
				ErrorReporter.logError("ExtractModuleParActionFromEditor: Copying project settings to new project failed.");
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
