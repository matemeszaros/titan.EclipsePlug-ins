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
import org.eclipse.titanium.graph.gui.windows.ComponentGraphEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This class does the generation and show of component graph, it is the
 * superclass of {@link IAction}. Basically it only should be instanced by mouse
 * click on the menu item.
 * 
 * @author Gabor Jenei
 */
public class ComponentGraphAction extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	public ComponentGraphAction() {
		// Do nothing
	}


	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// Do nothing
	}

	@Override
	public void run(final IAction action) {
		doOpenComponentGraphForSelected();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		
		doOpenComponentGraphForSelected();
		
		return null;
	}

	/**
	 * Do open the component graph view.
	 * */
	private void doOpenComponentGraphForSelected() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		for (Object selected : structSelection.toList()) {
			if (selected instanceof IProject) {
				final IProject project = (IProject) selected;

				final Job generator = new Job("Generator") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						monitor.beginTask("Parsing project", 30);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								doOpenGraphForProject(project);
							}


						});
						monitor.done();
						return Status.OK_STATUS;
					}
				};

				generator.schedule();

			}
		}
	}

	/**
	 * @param project the project whose graph is to be displayed
	 */
	private void doOpenGraphForProject(final IProject project) {
		try {
			// looking for a file inside the selected
			// project
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFile input = null;
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

			final IEditorPart editor = page.findEditor(new FileEditorInput(input));
			if (editor instanceof ComponentGraphEditor) {
				((ComponentGraphEditor) editor).refreshGraph();
			} else {
				page.openEditor(new FileEditorInput(input), ComponentGraphEditor.ID, true, IWorkbenchPage.MATCH_ID
						| IWorkbenchPage.MATCH_INPUT);
			}
		} catch (Exception exc) {
			final ErrorHandler errorHandler = new GUIErrorHandler();
			errorHandler.reportException("Error while parsing the project", exc);
		}
	}
}
