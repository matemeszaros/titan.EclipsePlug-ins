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
import org.eclipse.titanium.metrics.view.MetricsView;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class MetricsViewOpener extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public void run(final IAction action) {
		doOpenMetricsView();
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

		doOpenMetricsView();

		return null;
	}

	/**
	 * Do open the metrics view.
	 * */
	private void doOpenMetricsView() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		if (structSelection.isEmpty()) {
			return;
		}

		final Object firstElement = structSelection.getFirstElement();
		if (!(firstElement instanceof IProject)) {
			ErrorReporter.logError("The open metrics view command needs to be called on a project ");
			return;
		}

		final IProject project = (IProject) firstElement;
		try {
			final IViewPart activeView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.titanium.metrics.view");
			if (activeView instanceof MetricsView) {
				final MetricsView metricsView = (MetricsView) activeView;
				metricsView.setSelectedProject(project);
				metricsView.startMeasuring();

			}
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace("Error while initializing the metrics view", e);
		}
	}
}
