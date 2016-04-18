/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.markers.export.BaseProblemExporter;
import org.eclipse.titanium.markers.export.XlsProblemExporter;

/**
 * Prototype application for extracting the contents of the problems view into
 * an excel file in headless mode. It will analyze every project in the
 * workspace, and save the reports for each project into an excel file with the
 * name <project_name>.xls
 * 
 * It awaits one single parameter, the folder to place to excel files into.
 * */
public class ExportAllCodeSmells extends InformationExporter {

	@Override
	protected boolean checkParameters(final String[] args) {
		if (args.length == 0 || args.length > 2) {
			System.out.println("This application takes as parameter the location of the resulting .XLS files "
					+ "and optionally the date to be inserted into the file.");
			return false;
		}

		return true;
	}

	@Override
	protected void exportInformationForProject(final String[] args, final IProject project, final IProgressMonitor monitor) {
		BaseProblemExporter exporter = new XlsProblemExporter(project);
		try {
			Date date;
			if (args.length == 1) {
				date = Calendar.getInstance().getTime();
			} else {
				date = new SimpleDateFormat("yyyy_MM_dd").parse(args[1]);
			}
			exporter.exportMarkers(monitor, args[0] + project.getName() + ".xls", date);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting to excel " + args[0] + project.getName() + ".xls",e);
		}
	}
}
