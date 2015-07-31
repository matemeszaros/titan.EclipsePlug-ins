/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.titanium.error.ConsoleErrorHandler;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.clustering.BaseCluster;
import org.eclipse.titanium.graph.clustering.ClustererBuilder;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.generators.GraphGenerator;
import org.eclipse.titanium.graph.generators.ModuleGraphGenerator;
import org.eclipse.titanium.graph.visualization.GraphHandler;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class <code>implements {@link IApplication}</code>, it is used to export
 * pajek graph without loading the visual features.
 * 
 * @author Gabor Jenei
 */
public class SaveModuleNet extends InformationExporter {
	private DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> g=null;
	
	@Override
	protected boolean checkParameters(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: <output path> [-c<clustering algorithm name>]");
			System.out.println("The possible clustering algorithms are: ");
			System.out.println("\tModuleLocation\n\tFolderName\n\tLinkedLocation\n\tRegularExpression\n\tModuleName\n");
			return false;
		}

		return true;
	}

	@Override
	protected void exportInformationForProject(final String[] args, final IProject project, IProgressMonitor monitor) {
		final ErrorHandler errorHandler = new ConsoleErrorHandler();
		GraphGenerator generator = new ModuleGraphGenerator(project, errorHandler);
		
		try {
			generator.generateGraph();
			String clusterName="";
			for (int i = 1;i < args.length;++i) {
				if (args[i].startsWith("-c")) {
					clusterName=args[i].substring(2);
				}
			}
			
			if(clusterName.isEmpty()){
				g = generator.getGraph();
			} else {
				BaseCluster clusterer = new ClustererBuilder().
						setAlgorithm(clusterName).setGraph(generator.getGraph()).setProject(project).build();
				clusterer.run(monitor, false);
				g = clusterer.getGraph();
			}

			String fileName=args[0] + project.getName() + ".net";
			GraphHandler.saveGraphToPajek(g, fileName);
			errorHandler.reportInformation("The graphs have been successfully saved. See results at "+
					new File(fileName).getAbsolutePath());
		} catch (Exception e) {
			errorHandler.reportException("Error while exporting", e);
		}
	}
}