/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.titanium.graph.clustering.visualization.ClusterEdge;
import org.eclipse.titanium.graph.clustering.visualization.ClusterNode;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class partitions a module graph by patterns in the module names. Creates the whole "package" tree.
 * 
 * @author Gobor Daniel
 */
public class FullModuleNameCluster extends ModuleNameCluster {

	private Map<String, ClusterNode> mapNameNode;
	private Deque<String> stack;

	/**
	 * Initialize the variables for the clustering.
	 */
	public FullModuleNameCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph) {
		super(graph);
	}

	@Override
	public void createGraph() {
		mapNameNode = new HashMap<String, ClusterNode>();
		clusterGraph = new DirectedSparseGraph<NodeDescriptor, EdgeDescriptor>();
		stack = new LinkedList<String>();

		ClusterNode root = new ClusterNode(ALL, mapNameCluster.get(ALL));
		clusterGraph.addVertex(root);
		mapNameNode.put(ALL, root);

		traverseListOfNames();
	}

	/**
	 * Check the names of the clusters in a sorted fashion
	 */
	private void traverseListOfNames() {
		// ALL is lost but not forgotten
		knownNames.remove(ALL);
		// if we sort the splitted names, and iterate over it, hopefully we get a DFS traversal of the cluster graph
		final List<String> sortedNames = new ArrayList<String>(knownNames);
		Collections.sort(sortedNames);

		for (final String name : sortedNames) {
			check(name);
		}
	}

	/**
	 * Finds the parent of the node belonging to the given name.
	 * 
	 * @param name
	 *            The cluster / node name
	 */
	protected void check(final String name) {
		while (!stack.isEmpty()) {
			final String prev = stack.peek();
			if (name.startsWith(prev)) {
				addEdge(prev, name);
				stack.push(name);
				break;
			} else {
				stack.pop();
			}
		}
		if (stack.isEmpty()) {
			addEdge(ALL, name);
			stack.push(name);
		}
	}

	/**
	 * Grows the tree.
	 * 
	 * @param prev
	 *            Name of the parent node
	 * @param next
	 *            Name of the child node
	 */
	private void addEdge(final String prev, final String next) {
		final ClusterNode parent = mapNameNode.get(prev);
		final ClusterNode child = new ClusterNode(next, mapNameCluster.get(next));

		clusterGraph.addVertex(child);
		mapNameNode.put(next, child);

		final 	ClusterEdge ce = new ClusterEdge(prev + "-" + next, 1);
		clusterGraph.addEdge(ce, parent, child);
	}
}
