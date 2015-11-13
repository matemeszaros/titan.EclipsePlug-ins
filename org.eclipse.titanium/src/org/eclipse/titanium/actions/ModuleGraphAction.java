/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.gui.windows.GraphEditor;
import org.eclipse.titanium.graph.gui.windows.ModuleGraphEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This class implements the Action to do on Project dependency graph drawing
 * 
 * @author Gabor Jenei
 * 
 */
public class ModuleGraphAction extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	public ModuleGraphAction() {
	}

	/**
	 * initializing {@link GraphEditor}
	 */
	@Override
	public void run(IAction action) {
		doOpenModuleGraph();
	}

	/**
	 * react upon selection change
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * needless method (actually empty)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		//Do nothing
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		
		doOpenModuleGraph();

		return null;
	}

	/**
	 * Do open the module graph view.
	 * */
	private void doOpenModuleGraph() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		for (Object selected : structSelection.toList()) {
			if (selected instanceof IProject) {
				final IProject project = (IProject) selected;

				Generator generator = new Generator(project);

				generator.schedule();

			}
		}
	}

	/**
	 * Class responsible for generating a module graph for a project.
	 * */
	private static class Generator extends Job {
		final IProject project;

		Generator(final IProject project) {
			super("Generator");
			this.project = project;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Parsing project", 30);
			IFile input = null;
			try {
				IResource[] members = project.members();
				for (IResource res : members) {
					if (res.getType() == IResource.FILE) {
						input = (IFile) res;
						break;
					}
					if (res.getType() == IResource.FOLDER) {
						members = ((IFolder) res).members();
					}
				}
			} catch (CoreException ce) {
				final ErrorHandler errorHandler = new GUIErrorHandler();
				errorHandler.reportException("Error while parsing the project", ce);
			}
			
			final IFile finalInput = input;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						// looking for a file inside the selected
						// project
						final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						final FileEditorInput editorInput = new FileEditorInput(finalInput);
						final IEditorPart editor = page.findEditor(editorInput);
						if (editor instanceof ModuleGraphEditor) {
							((ModuleGraphEditor) editor).refreshGraph();
						} else {
							page.openEditor(editorInput, ModuleGraphEditor.ID, true, IWorkbenchPage.MATCH_ID
									| IWorkbenchPage.MATCH_INPUT);
						}

					} catch (Exception exc) {
						final ErrorHandler errorHandler = new GUIErrorHandler();
						errorHandler.reportException("Error whlie parsing the project", exc);
					}
				}
			});
			monitor.done();
			return Status.OK_STATUS;
		}
	}
}
