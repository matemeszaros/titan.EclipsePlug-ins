/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.generators;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.titanium.applications.SaveModuleNet;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.error.PrimitiveErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class represents an abstract frame. It also can be used to generate a graph (see
 * {@link SaveModuleNet})
 * 
 * @author Gabor Jenei
 */
public abstract class GraphGenerator {
	protected DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph;
	protected Map<String, NodeDescriptor> labels;
	protected final Transformer<NodeDescriptor, String> labeler;
	protected Job graphGenerator;
	protected IProject project;
	protected final ErrorHandler errorHandler;
	protected static final String LOG_ENTRY_NOTE = " (see error log for further information)";

	/**
	 * Constructor, note that after this method you must call
	 * {@link #generateGraph()} to make the graph. The constructor only does
	 * initialization.
	 * 
	 * @param project
	 *            : The project to generate graph for
	 * @param eHandler
	 *            : An object that implements error handling capabilities
	 */
	public GraphGenerator(final IProject project, ErrorHandler eHandler) {
		labeler = new Transformer<NodeDescriptor, String>() {
			@Override
			public String transform(NodeDescriptor v) {
				return v.getDisplayName();
			}
		};

		this.project = project;
		if (eHandler == null) {
			errorHandler = new PrimitiveErrorHandler();
		} else {
			errorHandler = eHandler;
		}


		graphGenerator = new Job("Graph generator") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					createGraph();
				} catch (Exception ex) {
					errorHandler.reportException("Error while generating graph", ex);
				}

				return Status.OK_STATUS;
			}
		};
	}

	/**
	 * This method should be called when we need to refresh the graph (because
	 * of possible new edges/vertices) It only does refresh in the inner
	 * representation, you can obtain the newly generated graph by calling
	 * {@link #getGraph()}
	 * 
	 * @throws InterruptedException
	 */
	public void generateGraph() throws InterruptedException {
		graphGenerator.join();
		graph = new DirectedSparseGraph<NodeDescriptor, EdgeDescriptor>();
		labels = new HashMap<String, NodeDescriptor>();
		graphGenerator.schedule();
	}

	/**
	 * This method returns the generated graph, it's useful if you use this
	 * class without the visual features. This method first synchronizes with
	 * the generating thread, therefore it may take longer to return.
	 * 
	 * @return The generated graph
	 * @throws InterruptedException
	 */
	public DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> getGraph() throws InterruptedException {
		graphGenerator.join();
		return graph;
	}

	/**
	 * This method returns a node ID -> node label transformer, it's useful if
	 * you use this class without the visual features. This method first
	 * synchronizes with the generating thread, therefore it may take longer to
	 * return.
	 * 
	 * @return A {@link NodeLabeler} that transforms node IDs into node names.
	 * @throws InterruptedException
	 */
	public Transformer<NodeDescriptor, String> getLabeler() throws InterruptedException {
		graphGenerator.join();
		return labeler;
	}

	/**
	 * In the subclasses this method should implement the actual graph
	 * generation. Here we should allocate space for the graph, and add
	 * edges/nodes. In the end a complete graph should be generated to the
	 * {@link #graph} attribute, that represents the current state of the
	 * project.
	 * 
	 * @throws Exception
	 *             In case of any problem occurred during the generation
	 */
	protected abstract void createGraph() throws Exception;
}