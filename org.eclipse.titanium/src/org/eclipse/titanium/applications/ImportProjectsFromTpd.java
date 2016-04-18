/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.wizards.projectFormat.TpdImporter;

/**
 * Prototype application for loading Tpd files in headless mode.
 *
 * It awaits one single parameter, the location of the Tpd file to load.
 * */
public class ImportProjectsFromTpd implements IApplication {
	private boolean result = true;

	private void reportResult(final boolean result) {
		this.result = result;
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		if (!GeneralConstants.DEBUG) {
			ErrorReporter.INTERNAL_ERROR("Loading Tpd files in headless mode is in prototype mode "
					+ "and so should not be available in released versions yet");
		}

		Platform.getBundle("org.eclipse.titan.designer").start();

		final String[] projectFiles = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

		if (projectFiles.length != 1) {
			System.out.println("This application takes as parameter the location of the Tpd file it should load projects from.");
			return Integer.valueOf(-1);
		}

		Activator.getDefault().pauseHandlingResourceChanges();
		System.out.println("Importing from Tpd");
		TpdImporter tpdImporter = new TpdImporter(null, true);
		final List<IProject> projectsCreated = new ArrayList<IProject>();
		result = tpdImporter.internalFinish(projectFiles[0], false, false, projectsCreated, new NullProgressMonitor(), null);
		Activator.getDefault().resumeHandlingResourceChanges();

		try {
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while closing workspace", e);
			reportResult(false);
		}
		if (result) {
			return EXIT_OK;
		}

		return Integer.valueOf(-1);
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
