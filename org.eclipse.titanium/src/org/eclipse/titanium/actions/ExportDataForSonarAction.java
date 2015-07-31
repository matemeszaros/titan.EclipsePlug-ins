/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.SelectionUtils;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;
import org.eclipse.titanium.utils.SonarDataExporter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class ExportDataForSonarAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPage iwPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		final List<IProject> res = SelectionUtils.getProjectsFromSelection(iwPage.getSelection());

		for (final IProject project : res) {
			new ProjectAnalyzerJob("Exporting sonar data for project " + project.getName()) {
				@Override
				public IStatus doPostWork(IProgressMonitor monitor) {
					try {
						final SonarDataExporter exporter = new SonarDataExporter(project);
						exporter.exportDataForProject();
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace("Error while exporting data for project " + project.getName(), e);
					}
					return Status.OK_STATUS;
				}
			}.quickSchedule(project);
		}
		return null;
	}

}
