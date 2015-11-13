/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.application;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.designer.core.TITANBuilder;

/**
 * Externally callable application, that takes exactly one argument: the name of the project it should build.
 * 
 * @author Kristof Szabados
 * */
public final class InvokeBuild implements IApplication {
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Platform.getBundle("org.eclipse.titan.designer").start();
		final String[] projectNames = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

		if (projectNames.length != 1) {
			System.out.println("This application takes as parameter the name of the project it should build.");
			return Integer.valueOf(-1);
		}

		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (final IProject project : projects) {
			if (project.getName().equals(projectNames[0])) {
				TITANBuilder.invokeBuild(project);
				return EXIT_OK;
			}
		}

		System.out.println("The project with name `" + projectNames[0] + "' could not be found.");

		return Integer.valueOf(-1);
	}

	@Override
	public void stop() {
		// nothing to be done
		
	}
}
