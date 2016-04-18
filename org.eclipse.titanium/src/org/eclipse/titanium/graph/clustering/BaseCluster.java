/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * The base class for the clustering algorithms.
 * 
 * @author Gobor Daniel
 */
public abstract class BaseCluster {

	protected static final String ERRORTITLE = "Clustering failure";
	protected static final String ERRORBUTTON = "Open Clustering Preferences";
	
	/**
	 * The module graph to be clustered
	 */
	protected DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> moduleGraph;
	/**
	 * The simplified cluster graph
	 */
	protected DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> clusterGraph;
	/**
	 * The clusters of the modules
	 */
	protected Set<Set<NodeDescriptor>> clusters;
	/**
	 * A map containing the clusters with their names
	 */
	protected Map<String, Set<NodeDescriptor>> mapNameCluster;
	/**
	 * Indicates, whether the clustering was successful <br>
	 * If not, a pop-up will be displayed with the {@link #msg}
	 */
	protected boolean successful;
	/**
	 * The error message to display if something is wrong
	 */
	protected String msg;
	protected final GUIErrorHandler errorHandler = new GUIErrorHandler();

	/**
	 * Create the clusters.
	 * 
	 * @return True if the partitioning was successful, false otherwise
	 */
	public abstract boolean createClusters(IProgressMonitor monitor);

	/**
	 * @return Returns a set of set of nodes, that represents the set of
	 *         clusters
	 */
	public Set<Set<NodeDescriptor>> getClusters() {
		return clusters;
	}

	/**
	 * Creates the cluster graph from the clustering.
	 */
	protected void createGraph() {
		clusterGraph = ClusteringTools.generateClusterGraph(moduleGraph, mapNameCluster);
	}

	/**
	 * @return Returns the simplified graph, where each node represents just one
	 *         cluster
	 */
	public DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> getGraph() {
		return clusterGraph;
	}

	/**
	 * Runs the algorithm.
	 * 
	 * @param monitor
	 *            The progress monitor of the Job
	 * @param group
	 *            True if the nodes will be grouped, false if the cluster graph
	 *            will be drawn
	 */
	public void run(final IProgressMonitor monitor, final boolean group) {
		final IProgressMonitor progress = (monitor == null) ? new NullProgressMonitor() : monitor;

		try {

			progress.beginTask(getType(), getTotalWork());

			progress.subTask("Creating clusters");
			createClusters(new SubProgressMonitor(progress, getClusteringWork()));
			progress.worked(getClusteringWork());
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			if (successful && !group) {
				progress.subTask("Creating cluster graph");
				createGraph();
				progress.worked(1);
				if (progress.isCanceled()) {
					throw new OperationCanceledException();
				}
			}

		} finally {
			progress.done();
			if (!successful) {
				reportError();
			}
		}
	}

	/**
	 * Prevent the creation of the cluster graph, e.g. because of user
	 * interruption.
	 * 
	 * @param msg
	 *            The string to display in an error message.
	 */
	protected void setErronous(final String msg) {
		successful = false;
		this.msg = msg;
	}
	
	/**
	 * Should be overridden for the specific clusterer to show a dialog.
	 */
	protected abstract void reportError();
	
	/**
	 * @return The string that will be written on the process monitor
	 */
	protected String getType() {
		return "Clustering";
	}
	
	/**
	 * @return The total amount of work. Default is 3.
	 */
	protected int getTotalWork() {
		return 3;
	}
	
	/**
	 * @return Amount of work to create the clusters. Default is 1.
	 */
	protected int getClusteringWork() {
		return 1;
	}
	
	/**
	 * @return True, if the clustering was successful
	 */
	public boolean isOK() {
		return successful;
	}

}
