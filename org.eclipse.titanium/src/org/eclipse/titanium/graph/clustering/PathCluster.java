/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public abstract class PathCluster extends BaseCluster {

	protected IProject project;
	protected ProjectSourceParser parser;

	protected Map<String, NodeDescriptor> mapNameNode;

	/**
	 * Initialize the variables for the clustering.
	 * 
	 * @param graph
	 *            The graph to be partitioned
	 * @param project
	 *            The project the graph belongs to
	 */
	public PathCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph, final IProject project) {
		moduleGraph = graph;
		this.project = project;
		successful = true;

		mapNameNode = new HashMap<String, NodeDescriptor>();

		clusters = new HashSet<Set<NodeDescriptor>>();
		mapNameCluster = new HashMap<String, Set<NodeDescriptor>>();
		parser = GlobalParser.getProjectSourceParser(project);
	}

	@Override
	public boolean createClusters(final IProgressMonitor monitor) {
		final IProgressMonitor progress = (monitor == null) ? new NullProgressMonitor() : monitor;
		progress.beginTask("Creating clusters", IProgressMonitor.UNKNOWN);
		init();
		try {
			checkProject(progress);
			addMissing();
			progress.worked(1);
			progress.done();
			return checkClustering();
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while creating clusters", e);
			setErronous("Couldn't process every resource");
			return false;
		}
	}

	/**
	 * Adds the missing modules to a separate cluster.
	 */
	protected void addMissing() {
		final Set<NodeDescriptor> missing = new HashSet<NodeDescriptor>();
		for (final NodeDescriptor v : moduleGraph.getVertices()) {
			if (v.getCluster() == null) {
				missing.add(v);
				v.setCluster(missing);
			}
		}
		if (!missing.isEmpty()) {
			clusters.add(missing);
			mapNameCluster.put("missing", missing);
		}
	}

	/**
	 * Checks if we have enough clusters.
	 */
	protected boolean checkClustering() {
		if (clusters.size() > 1) {
			return true;
		} else {
			setErronous("Less than two clusters were found.\n" + "Perhaps there are no folders in the project.");
			return false;
		}
	}

	/**
	 * Initializes the algorithm.
	 */
	protected void init() {
		for (final NodeDescriptor v : moduleGraph.getVertices()) {
			v.setCluster(null);
			mapNameNode.put(v.getName(), v);
		}
	}

	/**
	 * Adds the node to the given cluster.
	 * 
	 * @param name
	 *            The name of the node
	 * @param cluster
	 *            The cluster
	 */
	protected void addNodeToCluster(final String name, final Set<NodeDescriptor> cluster) {
		final NodeDescriptor v = mapNameNode.get(name);
		cluster.add(v);
		v.setCluster(cluster);
	}

	@Override
	protected void reportError() {
		errorHandler.reportErrorMessage(msg);
	}

	/**
	 * Begins the recursive search from the project folder.
	 * 
	 * @param progress
	 *            A progress monitor
	 * @throws CoreException
	 */
	protected abstract void checkProject(IProgressMonitor progress) throws CoreException;
}
