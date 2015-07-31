/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.preferences.PreferenceConstants;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class partitions a module graph automatically.
 * 
 * @see The <a
 *      href="http://www.mcs.drexel.edu/~bmitchel/research/iwpc98.pdf">The
 *      article </a> the algorithm is based on.
 * 
 * @author Gobor Daniel
 */
public class AutomaticCluster extends BaseCluster {

	private int maxclusters;
	private int maxiterations;

	private boolean checkFolder;
	private boolean checkModulename;
	private boolean checkRegexp;

	private IProject project;

	private Set<Set<NodeDescriptor>> clustersToCheck;
	/**
	 * The index of the cluster the node belongs to
	 */
	private Map<NodeDescriptor, Integer> mapClusterIndex;
	/**
	 * The cluster belonging to the index
	 */
	private Map<Integer, Set<NodeDescriptor>> mapIndexCluster;
	/**
	 * Size of the cluster with the given index
	 */
	private Map<Integer, Integer> size;
	private Set<Integer> indices;
	/**
	 * The matrix containing the edge numbers between the clusters
	 */
	private Map<Integer, Map<Integer, Integer>> mapBetweenArcs;
	/**
	 * The next free index for a cluster
	 */
	private int index;
	private int nodenum;
	private int clusternum;
	private double mq;
	private double maxmq;

	/**
	 * @param graph
	 *            The module graph.
	 */
	public AutomaticCluster(
			DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph,
			IProject project) {
		this.project = project;
		successful = true;
		moduleGraph = graph;
		nodenum = graph.getVertexCount();
		loadSettings();
	}

	/**
	 * Load clustering settings from preference store.
	 */
	private void loadSettings() {
		maxiterations = Platform.getPreferencesService().getInt(
				Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_ITERATION, 20,
				null);
		maxclusters = Platform.getPreferencesService().getInt(
				Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_SIZE_LIMIT, 7,
				null);

		checkFolder = Platform.getPreferencesService().getBoolean(
				Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_AUTO_FOLDER,
				true, null);
		checkModulename = Platform.getPreferencesService().getBoolean(
				Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_AUTO_NAME,
				true, null);
		checkRegexp = Platform.getPreferencesService().getBoolean(
				Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_AUTO_REGEXP,
				true, null);
	}

	/**
	 * Initializes the clustering algorithm.
	 */
	private void init() {
		mapBetweenArcs = new HashMap<Integer, Map<Integer, Integer>>();
		indices = new HashSet<Integer>();
		mapIndexCluster = new HashMap<Integer, Set<NodeDescriptor>>();
		mapClusterIndex = new HashMap<NodeDescriptor, Integer>();
		size = new HashMap<Integer, Integer>();
		
		index = 0;
		clusternum = 0;

		// remove unneeded clusters, create indices for the clusters
		Set<Set<NodeDescriptor>> clustersToRemove = new HashSet<Set<NodeDescriptor>>();
		for (Set<NodeDescriptor> cluster : clustersToCheck) {
			if (cluster == null || cluster.isEmpty()) {
				clustersToRemove.add(cluster);
			} else {
				indices.add(index);
				mapIndexCluster.put(index, cluster);
				size.put(index, cluster.size());
				for (NodeDescriptor v : cluster) {
					mapClusterIndex.put(v, index);
				}
				index++;
				clusternum++;
			}
		}
		for (Set<NodeDescriptor> cluster : clustersToRemove) {
			clustersToCheck.remove(cluster);
		}

		// create matrix of components
		for (int i : indices) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int j : indices) {
				map.put(j, 0);
			}
			mapBetweenArcs.put(i, map);
		}
		for (EdgeDescriptor e : moduleGraph.getEdges()) {
			NodeDescriptor v = moduleGraph.getSource(e);
			NodeDescriptor w = moduleGraph.getDest(e);
			int i = mapClusterIndex.get(v);
			int j = mapClusterIndex.get(w);
			changeCell(i, j, 1);
		}

		mq = MQ();
	}

	/**
	 * Increases the cell (i,j) by k. The order of i,j does not matter.
	 * 
	 * @param i
	 *            The "row"
	 * @param j
	 *            The "column"
	 * @param k
	 *            The change
	 */
	private void changeCell(int i, int j, int k) {
		final int row = i <= j ? i : j;
        final int column = i <= j ? j : i;
		
		int a = mapBetweenArcs.get(row).get(column);
		a += k;
		mapBetweenArcs.get(row).put(column, a);
	}

	/**
	 * Update the size of the cluster.
	 * 
	 * @param i
	 *            Which cluster
	 * @param k
	 *            By how much
	 */
	private void changeSize(int i, int k) {
		int a = size.get(i);
		a += k;
		size.put(i, a);
	}

	/**
	 * Calculates the priorities of all the nodes.
	 */
	private Queue<NodeDescriptor> calculatePriorities() {
		final Map<NodeDescriptor, Integer> priority = new HashMap<NodeDescriptor, Integer>();
		PriorityQueue<NodeDescriptor> queue = new PriorityQueue<NodeDescriptor>(nodenum, new Comparator<NodeDescriptor>() {
			@Override
			public int compare(NodeDescriptor v, NodeDescriptor w) {
				final int priorv = priority.get(v);
				final int priorw = priority.get(w);
				if (priorv < priorw) {
					return -1;
				} else if (priorv == priorw) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		
		for (NodeDescriptor v : moduleGraph.getVertices()) {
			int prio = calculatePriority(v);
			priority.put(v, prio);
			queue.offer(v);
		}
		
		return queue;
	}

	/**
	 * Calculate the priority of the node. The lower it is, the higher change is
	 * possible in the MQ.
	 * 
	 * @param v
	 *            The node
	 */
	private int calculatePriority(NodeDescriptor v) {
		final int indexv = mapClusterIndex.get(v);
		int prior = 0;
		for (NodeDescriptor w : moduleGraph.getNeighbors(v)) {
			final int indexw = mapClusterIndex.get(w);
			if (indexv == indexw) {
				prior++;
			} else {
				prior--;
			}
		}
		prior *= size.get(indexv);
		return prior;
	}

	/**
	 * The intra-connectivity of the ith cluster.<br>
	 * Number of edges inside / possible edge number inside the cluster.<br>
	 * See pages 3-4 in the article for more information.
	 * 
	 * @param i
	 *            The index of the cluster
	 * @return The intra-connectivity of the cluster
	 */
	private double A(int i) {
		final double a = mapBetweenArcs.get(i).get(i);
		int nodes = size.get(i);
		if (nodes > 1) {
			return a / (nodes * (nodes));
		}

		return 0;
	}

	/**
	 * The inter-connectivity of the ith and jth clusters.<br>
	 * Number of edges between cluster i and cluster j / possible edge number.<br>
	 * Should be called with i,j and j,i and the result summed.<br>
	 * See page 4 in the article for more information.
	 * 
	 * @param i
	 *            First cluster
	 * @param j
	 *            Second cluster
	 * @return The inter-connectivity of the clusters
	 */
	private double E(int i, int j) {
		final double a = mapBetweenArcs.get(i).get(j);
		return a / (2 * size.get(i) * size.get(j));
	}

	/**
	 * The modularization quality of the current clustering.<br>
	 * This is the measure we need to maximize.<br>
	 * The connectivity measures are weighted to discourage very small clusters
	 * in contrast to the algorithm described in the article.
	 * 
	 * @return The MQ value
	 */
	private double MQ() {
		int k = 0;
		int e = 0;
		if (clusternum == 1) {
			double edgenum = moduleGraph.getEdgeCount();
			return edgenum / (nodenum * (nodenum));
		}
		double sumA = 0;
		double sumE = 0;
		for (int i : indices) {
			if (size.get(i) == 0) {
				continue;
			}
			for (int j : indices) {
				if (size.get(j) == 0) {
					continue;
				}
				if (i == j) {
					sumA += A(i) * size.get(i);
					k += size.get(i);
				} else {
					sumE += E(i, j) * (nodenum - size.get(i))
							* (nodenum - size.get(j));
					e += (nodenum - size.get(i)) * (nodenum - size.get(j));
				}
			}
		}
		return sumA / k - sumE / e;
	}

	/**
	 * Moves v from its original cluster to the one indexed by 'to'. It is much
	 * faster than calculating the whole matrix again.
	 * 
	 * @param v
	 *            The node to be moved
	 * @param to
	 *            The index of the cluster
	 */
	private void moveNode(NodeDescriptor v, int to) {
		final int from = mapClusterIndex.get(v);
		if (from == to) {
			return;
		}
		if (size.get(to) == 0) {
			clusternum++;
		}
		if (size.get(from) == 1) {
			clusternum--;
		}
		for (EdgeDescriptor e : moduleGraph.getInEdges(v)) {
			final NodeDescriptor u = moduleGraph.getSource(e);
			int indexu = mapClusterIndex.get(u);
			changeCell(from, indexu, -1);
			changeCell(to, indexu, 1);
		}
		for (EdgeDescriptor e : moduleGraph.getOutEdges(v)) {
			final NodeDescriptor w = moduleGraph.getDest(e);
			int indexw = mapClusterIndex.get(w);
			changeCell(indexw, from, -1);
			changeCell(indexw, to, 1);
		}
		changeSize(from, -1);
		changeSize(to, 1);
		mapClusterIndex.put(v, to);
	}

	/**
	 * Finds a better cluster for the given node.
	 * 
	 * @param v
	 *            The node to be moved for better clustering
	 * @return True if the clustering did not change
	 */
	private boolean checkNode(NodeDescriptor v) {
		int originalIndex = mapClusterIndex.get(v);
		int bestIndex = mapClusterIndex.get(v);
		double bestmq = MQ();
		// if the cluster is too small, then move the node
		if (size.get(originalIndex) == 1) {
			bestmq = -1;
		}
		double currentmq;

		// check current neighbouring clusters
		for (NodeDescriptor w : moduleGraph.getNeighbors(v)) {
			final int newIndex = mapClusterIndex.get(w);
			moveNode(v, newIndex);
			currentmq = MQ();
			if (currentmq > bestmq) {
				bestmq = currentmq;
				bestIndex = newIndex;
			}
			moveNode(v, originalIndex);
		}

		// check, if in a new cluster
		if (clusternum < maxclusters) {

			mapIndexCluster.put(index, new HashSet<NodeDescriptor>());
			indices.add(index);
			size.put(index, 0);
			// add new row and column to matrix
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int j : indices) {
				map.put(j, 0);
			}
			mapBetweenArcs.put(index, map);
			for (int j : indices) {
				mapBetweenArcs.get(j).put(index, 0);
			}

			moveNode(v, index);
			currentmq = MQ();
			if (currentmq > bestmq) {
				bestmq = currentmq;
				bestIndex = index;
			}
			moveNode(v, originalIndex);

			// if not in a new empty cluster we can use the same index again
			if (bestIndex != index) {
				indices.remove(index);
			} else {
				index++;
			}

		}

		// the new cluster of the node
		moveNode(v, bestIndex);
		mq = MQ();

		return originalIndex == bestIndex;
	}

	/**
	 * Tries to improve the current clustering by iterating over all nodes.
	 * 
	 * @return false if there was a change
	 */
	private boolean improve() {
		Queue<NodeDescriptor> queue = calculatePriorities();
		boolean optimal = true;
		while (!queue.isEmpty()) {
			NodeDescriptor v = queue.poll();
			boolean check = checkNode(v);
			optimal = optimal && check;
		}
		return optimal;
	}

	/**
	 * Runs the algorithm. Should only be run after running {@link #init()}.
	 */
	private void run() {
		boolean optimal = false;
		int i = 0;
		while (!optimal && i < maxiterations) {
			optimal = improve();
			++i;
		}
	}

	/**
	 * Check the given clustering tool.
	 * 
	 * @param ct
	 *            The clustering tool
	 * @param progress
	 *            A progress monitor
	 * @return True if the clustering was successful
	 */
	private boolean checkCulesteringTool(BaseCluster ct,
			IProgressMonitor progress) {
		if (ct.createClusters(new SubProgressMonitor(progress, 1))) {
			progress.worked(1);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
			check(ct.getClusters(), new SubProgressMonitor(progress, 3));
			progress.worked(3);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean createClusters(IProgressMonitor monitor) {

		IProgressMonitor progress = (monitor == null) ? new NullProgressMonitor() : monitor;
		progress.beginTask("Creating clusters", 13);

		if (!(checkFolder || checkRegexp || checkModulename)) {
			setErronous("No clustering tool is given in the settings.\n"
					+ "Please enable at least one.");
			return false;
		}

		boolean ok = false;

		maxmq = -2;

		if (checkFolder) {
			ok = ok	|| checkCulesteringTool(
					new ModuleLocationCluster(moduleGraph, project), progress);
		} else {
			progress.worked(4);
		}

		if (checkRegexp) {
			ok = ok	|| checkCulesteringTool(
					new RegexpCluster(moduleGraph),	progress);
		} else {
			progress.worked(4);
		}

		if (checkModulename) {
			ok = ok	|| checkCulesteringTool(
					new ModuleNameCluster(moduleGraph),	progress);
		} else {
			progress.worked(4);
		}

		progress.done();

		if (!ok) {
			setErronous("All checked clustering tools failed.\n"
					+ "Please enable more, or check the other clustering tools for errors.");
			return false;
		}

		return true;
	}

	/**
	 * Run the algorithm on the given clustering. If there are too many clusters
	 * it merges them, and runs the algorithm again.
	 */
	private void check(Set<Set<NodeDescriptor>> clustering,
			IProgressMonitor monitor) {
		clustersToCheck = clustering;
		init();
		monitor.beginTask("Improving clustering", maxclusters-clusternum);
		monitor.subTask("First improvement");
		run();
		monitor.worked(1);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		while (clusternum > maxclusters) {
			monitor.subTask("Reducing number of clusters: "
					+ (clusternum - maxclusters));
			if (mergeBest()) {
				createCurrentClusters();
				init();
				run();
			} else {
				break;
			}
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		end();
		monitor.done();
	}

	/**
	 * Check if the resulting clustering is better than the previous best.
	 */
	private void end() {
		mq = MQ();
		if (mq > maxmq) {
			createCurrentClusters();
			maxmq = mq;
			clusters = clustersToCheck;
		}
	}

	/**
	 * Creates the clusters using the current state of the algorithm.
	 */
	private void createCurrentClusters() {
		clustersToCheck = new HashSet<Set<NodeDescriptor>>();
		for (int i : indices) {
			Set<NodeDescriptor> cluster = mapIndexCluster.get(i);
			cluster.clear();
		}
		for (NodeDescriptor v : moduleGraph.getVertices()) {
			Set<NodeDescriptor> cluster = mapIndexCluster.get(mapClusterIndex
					.get(v));
			cluster.add(v);
			v.setCluster(cluster);
		}
		for (int i : indices) {
			Set<NodeDescriptor> cluster = mapIndexCluster.get(i);
			if (!cluster.isEmpty()) {
				clustersToCheck.add(cluster);
			}
		}
	}

	/**
	 * Merges the two clusters.
	 */
	private void mergeClusters(int from, int to) {
		for (NodeDescriptor v : moduleGraph.getVertices()) {
			if (mapClusterIndex.get(v) == from) {
				mapClusterIndex.put(v, to);
			}
		}
	}

	/**
	 * Merges two clusters which have the most edges between them.
	 * {@link #createCurrentClusters()} should be called after to create the new
	 * clusters.
	 * 
	 * @return
	 */
	private boolean mergeBest() {
		int max = 0;
		int besti = -1;
		int bestj = -1;
		for (int i : indices) {
			for (int j : indices) {
				int a = mapBetweenArcs.get(i).get(j);
				if (i != j && a > max) {
					max = a;
					besti = i;
					bestj = j;
				}
			}
		}
		if (besti != -1) {
			mergeClusters(besti, bestj);
			return true;
		}

		return false;

	}

	@Override
	public void createGraph() {
		clusterGraph = ClusteringTools.generateClusterGraph(moduleGraph,
				clusters);
	}

	@Override
	protected void reportError() {
		errorHandler.reportBadSetting(ERRORTITLE, msg, ERRORBUTTON,
				"org.eclipse.titanium.preferences.pages.GraphClusterAutoPage");
	}

	@Override
	protected String getType() {
		return "Clustering automatically";
	}

	@Override
	protected int getTotalWork() {
		return 9;
	}

	@Override
	protected int getClusteringWork() {
		return 7;
	}

}
