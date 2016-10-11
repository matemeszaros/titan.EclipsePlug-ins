/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.metrics.topview.TopView;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class TopViewOpener extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public void run(final IAction action) {
		doOpenTopView();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		//Do nothing
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		doOpenTopView();
		
		return null;
	}
	
	/**
	 * Open the view of the top metrics.
	 * */
	private void doOpenTopView() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		if (structSelection.isEmpty()) {
			return;
		}

		final Object firstElement = structSelection.getFirstElement();
		if (!(firstElement instanceof IProject)) {
			ErrorReporter.logError("The open top risk view command needs to be called on a project ");
			return;
		}

		final IProject project = (IProject) firstElement;
		try {
			final IViewPart activeView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.titanium.metrics.topview");
			if (activeView instanceof TopView) {
				final TopView topView = (TopView) activeView;
				topView.setSelectedProject(project);
				topView.startMeasuring();

			}
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace("Error while initializing the top metrics view", e);
		}
	}

}
