/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.wizards.GUIProjectImporter;

public class ImportProjectsFromPrj implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		if (!GeneralConstants.DEBUG) {
			ErrorReporter.INTERNAL_ERROR("Loading prj files in headless mode is in prototype mode "
					+ "and so should not be available in released versions yet");
		}

		Platform.getBundle("org.eclipse.titan.designer").start();

		final String[] projectFiles = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

		if (projectFiles.length != 1) {
			System.out.println("This application takes as parameter the location of the prj file it should load projects from.");
			return Integer.valueOf(-1);
		}

		boolean result = true;

		try {
			if (Activator.getDefault() != null) {
				Activator.getDefault().pauseHandlingResourceChanges();
			}
			System.out.println("Importing from prj");
			result = GUIProjectImporter.importProjectfromPrj(projectFiles[0]);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while importing from prj " + projectFiles[0],e);
		}

		try {
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while closing workspace", e);
			result = false;
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
