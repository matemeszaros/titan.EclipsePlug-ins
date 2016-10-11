/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titanium.error.ConsoleErrorHandler;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.generators.ComponentGraphGenerator;
import org.eclipse.titanium.graph.generators.GraphGenerator;
import org.eclipse.titanium.graph.visualization.GraphHandler;

/**
 * This class implements the interface for component graph headless export
 * 
 * @author Gabor Jenei
 * 
 */
public class SaveComponentNet extends InformationExporter {
	@Override
	protected boolean checkParameters(final String[] args) {
		if (args.length != 1) {
			System.out.println("This application takes as parameter the location of the resulting .net files");
			return false;
		}

		return true;
	}
	
	@Override
	protected void exportInformationForProject(final String[] args, final IProject project, final IProgressMonitor monitor) {
		final ErrorHandler errorHandler = new ConsoleErrorHandler();
		final GraphGenerator generator = new ComponentGraphGenerator(project, errorHandler);

		try {
			generator.generateGraph();
			GraphHandler.saveGraphToPajek(generator.getGraph(), args[0] + project.getName() + ".net");
		} catch (Exception e) {
			errorHandler.reportException("Error while exporting", e);
		}
	}
}
