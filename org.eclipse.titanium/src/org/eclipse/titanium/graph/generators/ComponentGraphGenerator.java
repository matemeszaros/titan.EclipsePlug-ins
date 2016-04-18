/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.generators;

import java.awt.Color;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeReferenceList;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * This is a {@link GraphGenerator} class for the component graph
 * 
 * @see GraphGenerator
 * @author Gabor Jenei
 */
public class ComponentGraphGenerator extends GraphGenerator {

	/**
	 * Constructor
	 * 
	 * @param project
	 *            : The project to create graph for
	 * @param editor
	 *            : the creator editor
	 * @param eHandler
	 *            : An object that implements error reporting capabilities
	 */
	public ComponentGraphGenerator(final IProject project, final ErrorHandler eHandler) {
		super(project, eHandler);
		if (eHandler == null) {
			errorHandler.reportErrorMessage("The referenced error handler mustn't be null"
					+ "(source: ComponentGraphGenerator)");
		}
	}

	@Override
	protected void createGraph() {

		analyzeProject();

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final List<IProject> visitedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();

		for (final IProject currentProject : visitedProjects) {
			for (String moduleName : projectSourceParser.getKnownModuleNames()) {
				handleModule(currentProject, projectSourceParser.getModuleByName(moduleName));
			}
		}
	}

	private void handleModule(final IProject currentProject, final Module mod) {
		mod.accept(new ASTVisitor() {
			@Override
			public int visit(final IVisitableNode node) {
				if (!(node instanceof Def_Type)) {
					return super.visit(node);
				}

				final Def_Type defType = (Def_Type) node;
				final Type type = defType.getType(CompilationTimeStamp.getBaseTimestamp());
				if (!(type instanceof Component_Type)) {
					return super.visit(node);
				}

				final Component_Type componentType = (Component_Type) type;

				final Identifier id = defType.getIdentifier();

				final NodeDescriptor sourceNode = new NodeDescriptor(id.getDisplayName(), id.getName(), NodeColours.LIGHT_GREEN,
						currentProject, false, id.getLocation());
				if (!graph.containsVertex(sourceNode)) {
					graph.addVertex(sourceNode);
					labels.put(sourceNode.getName(), sourceNode);
				}

				final ComponentCollector componentCollector = new ComponentCollector(currentProject, sourceNode.getName());
				final ComponentTypeReferenceList compReferenceList = componentType.getComponentBody().getExtensions();
				compReferenceList.accept(componentCollector);

				final ComponentTypeReferenceList compReferenceListAttribute = componentType.getComponentBody().getAttributeExtensions();
				if (compReferenceListAttribute != null) {
					compReferenceListAttribute.accept(componentCollector);
				}

				return ASTVisitor.V_SKIP;
			}

		});
	}

	private void analyzeProject() {
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		if (projectSourceParser.getLastTimeChecked() == null) {
			WorkspaceJob job = projectSourceParser.analyzeAll();

			while (job == null) {
				try {
					Thread.sleep(500);
					job = projectSourceParser.analyzeAll();
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace("Error while waiting for analyzis result", e);
				}
			}

			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("Error while waiting for analyzis result", e);
			}
		}
	}

	private class ComponentCollector extends ASTVisitor {
		final IProject currentProject;
		final String sourceName;

		public ComponentCollector(final IProject currentProject, final String sourceName) {
			this.currentProject = currentProject;
			this.sourceName = sourceName;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				final Reference reference = (Reference) node;
				final ISetting setting = reference.getRefdSetting(CompilationTimeStamp.getBaseTimestamp());
				final Identifier id = reference.getId();

				if (!labels.containsKey(id.getName())) {
					final NodeDescriptor destinationNode = new NodeDescriptor(id.getDisplayName(), id.getName(),
							NodeColours.LIGHT_GREEN, currentProject, false, setting.getLocation());
					graph.addVertex(destinationNode);
					labels.put(destinationNode.getName(), destinationNode);
				}
				final EdgeDescriptor edge = new EdgeDescriptor(sourceName + "->" + id.getName(), Color.black);
				graph.addEdge(edge, labels.get(sourceName), labels.get(id.getName()), EdgeType.DIRECTED);
			}
			return super.visit(node);
		}
	}
}