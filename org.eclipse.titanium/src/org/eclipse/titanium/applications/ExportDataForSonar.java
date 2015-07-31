/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.utils.SonarDataExporter;

public class ExportDataForSonar extends InformationExporter {

	private List<IProject> projectsToExport = new ArrayList<IProject>();

	@Override
	protected boolean checkParameters(String[] args) {
		// Use Apache CLI if more functionality is needed
		if (args.length == 0) {
			projectsToExport = getAllAccessibleProjects();
			return true;
		}

		if (args.length != 2) {
			printUsage();
			return false;
		}

		if (!("-p".equals(args[0]) || !"--projects".equals(args[0]))) {
			printUsage();
			return false;
		}

		List<String> projectNames = Arrays.asList(args[1].split(","));
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (String name : projectNames) {
			IProject project = wsRoot.getProject(name);
			if (!project.isAccessible()) {
				System.out.println("Project '" + name + "' is not accessible.");
				return false;
			}
			projectsToExport.add(project);
		}
		return true;
	}

	private void printUsage() {
		String applicationName = ExportDataForSonar.class.getCanonicalName();
		System.out.println("Usage: ./eclipse " + applicationName + " [-p project1,project2,...,projectN]");
	}

	@Override
	protected void exportInformationForProject(String[] args, IProject project, IProgressMonitor monitor) {
		SonarDataExporter exporter = new SonarDataExporter(project);
		try {
			exporter.exportDataForProject();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting data for project " + project.getName(), e);
		}
	}

	@Override
	protected List<IProject> getProjectsToHandle() {
		return projectsToExport;
	}

}
