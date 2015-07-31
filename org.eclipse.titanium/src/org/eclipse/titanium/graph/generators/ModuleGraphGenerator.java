/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.generators;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.GlobalProjectStructureTracker;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ProjectStructureDataCollector;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * @author Gabor Jenei 
 * 			This class implements {@link #createGraph()} for module
 *         graph.
 * @see GraphGenerator
 */
public class ModuleGraphGenerator extends GraphGenerator {

	/**
	 * Constructor
	 * 
	 * @param project
	 *            : The project to create graph for
	 * @param eHandler
	 *            : An object that implements error reporting capabilities
	 */
	public ModuleGraphGenerator(IProject project, ErrorHandler eHandler) {
		super(project, eHandler);
		if (eHandler == null) {
			errorHandler.reportErrorMessage("The referenced error handler mustn't be null (source: ModuleGraphGenerator)");
		}
	}

	@Override
	protected void createGraph() {

		// analyze the project if needed
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		if (sourceParser.getLastTimeChecked() == null) {
			WorkspaceJob job = sourceParser.analyzeAll();

			while (job == null) {
				try {
					Thread.sleep(500);
					job = sourceParser.analyzeAll();
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace("Error while waiting for analyzis result", e);
				}
			}

			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("Error while parsing the project", e);
			}
		}

		List<IProject> visitedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
		Map<String, Identifier> globalKnownModules = new HashMap<String, Identifier>();

		for (int i = 0; i < visitedProjects.size(); ++i) {
			IProject currentProject = visitedProjects.get(i);
			ProjectStructureDataCollector collector = GlobalProjectStructureTracker.getDataCollector(currentProject);
			collector.evaulateMissingModules();

			// adding known modules
			for (Identifier moduleName : collector.knownModules.values()) {
				NodeDescriptor actNode = new NodeDescriptor(moduleName.getDisplayName(), moduleName.getName(), currentProject,
						false, moduleName.getLocation());
				globalKnownModules.put(moduleName.getName(), moduleName);
				if (!graph.containsVertex(actNode)) {
					graph.addVertex(actNode);
					labels.put(actNode.getName(), actNode);
				}
			}

			// adding missing modules
			for (Identifier moduleName : collector.missingModules.values()) {
				if (!globalKnownModules.containsKey(moduleName.getName())) {
					NodeDescriptor actNode = new NodeDescriptor(moduleName.getDisplayName(), moduleName.getName(),
							currentProject, true, moduleName.getLocation());
					if (!graph.containsVertex(actNode)) {
						graph.addVertex(actNode);
						labels.put(actNode.getName(), actNode);
					}
				}
			}

			// building edges
			for (String from : collector.importations.keySet()) {
				for (String to : collector.importations.get(from)) {
					EdgeDescriptor edge = new EdgeDescriptor(from + "->" + to, Color.black);
					// if(!graph.containsEdge(edge))
					graph.addEdge(edge, labels.get(from), labels.get(to), EdgeType.DIRECTED);
				}
			}
		}
	}

}
