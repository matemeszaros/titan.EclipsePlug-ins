/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.application;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.wizards.projectFormat.TpdImporter;

/**
 * Prototype application for loading Tpd files in headless mode.
 * 
 * @author Kristof Szabados
 * */
public class LoadFromTpd implements IApplication {
	private boolean result;

	private void reportResult(final boolean result) {
		this.result = result;
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		if (!GeneralConstants.DEBUG) {
			ErrorReporter.INTERNAL_ERROR("Loading Tpd files in headless mode is in prototype mode and so should not be available in released versions yet");
		}

		Platform.getBundle("org.eclipse.titan.designer").start();

		final Object arguments = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (!(arguments instanceof String[])) {
			System.out.println("A list of strings was expected as argument.");
			return Integer.valueOf(-1);
		}
		final String[] projectFiles = (String[]) arguments;
		
		if (projectFiles.length != 1) {
			System.out.println("This application takes as parameter the location of the Tpd file it should load projects from.");
			return Integer.valueOf(-1);
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (final IProject project: projects) {
					try {
						project.delete(true, true, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}

				try {
					new ProgressMonitorDialog(null).run(true, false, new IRunnableWithProgress() {

						@Override
						public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							boolean result = true;
							monitor.beginTask("Importing the data of the project", 1);

							try {
								final TpdImporter tpdImporter = new TpdImporter(null, true);
								final List<IProject> projectsCreated = new ArrayList<IProject>();
								result = tpdImporter.internalFinish(projectFiles[0], false, false, projectsCreated, monitor, null);
							} catch (Exception e) {
								ErrorReporter.logExceptionStackTrace(e);
								result = false;
							}

							monitor.done();
							reportResult(result);
						}
					});
				} catch (Exception e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		});

		if (result) {
			return EXIT_OK;
		}

		return Integer.valueOf(-1);
	}
	
	@Override
	public void stop() {
		// nothing to be done
		
	}
}
