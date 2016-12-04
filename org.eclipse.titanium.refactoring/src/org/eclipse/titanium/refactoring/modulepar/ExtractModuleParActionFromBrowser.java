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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This class handles the {@link ExtractModuleParRefactoring} class when the operation is
 * called from the package browser for a single or multiple project(s), folder(s) or file(s).
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Viktor Varga
 */
public class ExtractModuleParActionFromBrowser extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		performExtractModulePar();
		return null;
	}
	@Override
	public void run(final IAction action) {
		performExtractModulePar();
		
	}
	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}
	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}

	private void performExtractModulePar() {
		//find selection: only a single project can be refactored
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		Set<IProject> sourceProjs = Utils.findAllProjectsInSelection((IStructuredSelection)selection);
		if (sourceProjs.isEmpty()) {
			return;
		}
		IProject sourceProj = sourceProjs.iterator().next();
		final IStructuredSelection ssel = new StructuredSelection(sourceProj); 
		
		//update AST before refactoring
		Set<IProject> projsToUpdate = new HashSet<IProject>();
		projsToUpdate.add(sourceProj);
		Utils.updateASTBeforeRefactoring(projsToUpdate, "ExtractModulePar");
		Activator.getDefault().pauseHandlingResourceChanges();
		
		//create refactoring
		final ExtractModuleParRefactoring refactoring = new ExtractModuleParRefactoring(sourceProj);
		//open wizard
		ExtractModuleParWizard wiz = new ExtractModuleParWizard();

		wiz.init(PlatformUI.getWorkbench(), ssel);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
		dialog.open();
		boolean saveModuleParsOption = wiz.getSaveModuleParsOption();
		IProject newProj = wiz.getProject();
		if (newProj == null) {
			ErrorReporter.logError("ExtractModuleParActionFromBrowser: Wizard returned a null project. ");
			return;
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
		
		WorkspaceJob job = new WorkspaceJob("ExtractModulePar: writing to target project") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				refactoring.perform();
				Activator.getDefault().resumeHandlingResourceChanges();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

}
