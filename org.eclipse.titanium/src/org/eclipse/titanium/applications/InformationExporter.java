/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;

/**
 * Generic application for extracting the contents of the problems view into
 * a file in headless mode. It will analyze every project in the
 * workspace, and save the reports for each project into a file.
 * 
 * Should be extended to provide the actual export functionality
 * */
public abstract class InformationExporter implements IApplication {

	/**
	 * Checks the command line provided parameters for validity.
	 * 
	 * @param args the command line arguments the application received to be checked
	 * @return true if the arguments are ok, false if the application must quit.
	 */
	protected abstract boolean checkParameters(final String[] args);

	/**
	 * Exports the ode smells reported on a project, in the export format specific way.
	 * 
	 * @param args the command line arguments passed to the application
	 * @param project the project whose code smells are to be extracted
	 * @param monitor a monitor object for progress reporting.
	 */
	protected abstract void exportInformationForProject(final String[] args, final IProject project, IProgressMonitor monitor);
	
	@Override
	public Object start(final IApplicationContext context) throws Exception {
		if (!GeneralConstants.DEBUG) {
			ErrorReporter.INTERNAL_ERROR("Experimental functionaility for the Titanium project");
		}

		Platform.getBundle("org.eclipse.titan.designer").start();
		final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if(!checkParameters(args)) {
			return Integer.valueOf(-1);
		}
		
		if (args.length >= 1){
			final File path = new File(args[0].substring(0, args[0].lastIndexOf(File.separator)));
			if (!path.exists() && !path.mkdirs()) {
				System.err.println("Couldn't create output directory!");
				return Integer.valueOf(-1);
			}
		}

		final List<IProject> existingProjects = getProjectsToHandle();

		for (final IProject project : existingProjects) {
			final  ProjectAnalyzerJob job = new ProjectAnalyzerJob("Exporting information for project " + project.getName()) {
				@Override
				public IStatus doPostWork(final IProgressMonitor monitor) {
					System.out.println("Exporting information for " + getProject().getName());
					exportInformationForProject(args, getProject(), monitor);
					return Status.OK_STATUS;
				}
			}.quickSchedule(project);
			job.join();
		}

		boolean result = true;

		try {
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while closing workspace",e);
			result = false;
		}

		if (result) {
			if (args.length >= 1){
				System.out.println("All informations are successfully exported to " + args[0].substring(0, args[0].lastIndexOf(File.separator)));
			} else {
				System.out.println("All information is succesfully exported.");
			}
			return EXIT_OK;
		} else {
			System.err.println("The export wasn't successfull, see zour workspace1s errorlog for details");
			return Integer.valueOf(-1);
		}
	}

	protected List<IProject> getProjectsToHandle() {
		return getAllAccessibleProjects();
	}

	protected List<IProject> getAllAccessibleProjects() {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final List<IProject> existingProjects = new ArrayList<IProject>();
		for (final IProject project : projects) {
			if (project.isAccessible()) {
				existingProjects.add(project);
			}
		}
		return existingProjects;
	}

	@Override
	public void stop() {
		// nothing to be done
	}
}
