/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.debug.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public final class ProjectReAnalyzer extends Action implements IObjectActionDelegate {
	private ISelection selection;

	public void run(IAction action) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		IStructuredSelection structSelection = (IStructuredSelection) selection;
		ArrayList<IProject> projects = new ArrayList<IProject>();

		for (Object selected : structSelection.toList()) {
			if (!(selected instanceof IProject)) {
				continue;
			}

			projects.add((IProject) selected);
		}

		for (IProject project : projects) {
			GlobalParser.clearAllInformation(project);
		}

		for (IProject project : projects) {
			GlobalParser.getProjectSourceParser(project).analyzeAll();
		}
	}

	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}
}
