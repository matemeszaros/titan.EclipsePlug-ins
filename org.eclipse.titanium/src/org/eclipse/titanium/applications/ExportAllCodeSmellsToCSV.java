/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.util.Calendar;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.markers.export.BaseProblemExporter;
import org.eclipse.titanium.markers.export.CsvProblemExporter;
/**
 * Prototype application for extracting the contents of the problems view into
 * CSV files in headless mode. It will analyze every project in the workspace,
 * and save the reports for each project into CSV files.
 * 
 * It awaits one single parameter, the path prefix where the files should be
 * created.
 * */
public class ExportAllCodeSmellsToCSV extends InformationExporter {

	@Override
	protected boolean checkParameters(final String[] args) {
		if (args.length != 1) {
			System.out.println("This application takes as parameter the location of the resulting CSV files.");
			return false;
		}

		return true;
	}

	@Override
	protected void exportInformationForProject(final String[] args, final IProject project, final IProgressMonitor monitor) {
		final BaseProblemExporter exporter = new CsvProblemExporter(project);

		try {
			exporter.exportMarkers(monitor, args[0] + project.getName(), Calendar.getInstance().getTime());
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting to CSV " + args[0] + project.getName(),e);
		}
	}
}
