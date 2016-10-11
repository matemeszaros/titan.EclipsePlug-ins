/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts.algorithms;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

/**
 * This class assigns a level for the nodes of a graph for the DAG layout.<br>
 * If the graph contains circles, the nodes of the circles will be grouped
 * together.
 * 
 * @author Gobor Daniel
 * @param <V>
 *            node type
 * @param <E>
 *            edge type
 */
public class DAGLayoutAlgorithm<V, E> implements HierarcicalLayoutAlgorithm<V> {

	protected Graph<V, E> g;
	protected int nodenum;

	protected Map<V, Integer> level;
	protected Set<V> isolateNodes;

	protected Integer minusLevels;

	protected Map<V, Integer> arcs;
	protected Map<Set<V>, Integer> mapCircleArcs;
	protected PriorityQueue<V> queue;

	/**
	 * Set of finalized nodes.<br>
	 * If a node is finalized, then every in-neighbor of the node is also
	 * finalized.
	 */
	protected Set<V> checked;
	protected Map<V, Set<V>> mapNodeCircle;

	protected int[] nodesOnLevel;
	protected int levelnumber;

	public static final String ALG_ID = "TDAG";

	/**
	 * Initialize the variables.
	 * 
	 * @param graph
	 *            The graph whose layout we want to construct
	 */
	public DAGLayoutAlgorithm(final Graph<V, E> graph) {
		g = graph;

		minusLevels = 0;

		nodenum = g.getVertexCount();
		checked = new HashSet<V>();
		mapNodeCircle = new HashMap<V, Set<V>>();

		level = new HashMap<V, Integer>();
		isolateNodes = new HashSet<V>();

		arcs = new HashMap<V, Integer>();
		mapCircleArcs = new HashMap<Set<V>, Integer>();
		queue = new PriorityQueue<V>(nodenum, new Comparator<V>() {
			@Override
			public int compare(final V v, final V w) {
				final int priorv = arcs.get(v);
				final int priorw = arcs.get(w);
				if (priorv < priorw) {
					return -1;
				} else if (priorv == priorw) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		levelnumber = 0;

	}

	/**
	 * Run the algorithm. This method should be called before making checking
	 * levels.
	 */
	@Override
	public void run() {
		init();
		runAlgorithm();
		createLevelNumbers();
	}

	/**
	 * Create a array of the levels and how many nodes belong to them.
	 */
	protected void createLevelNumbers() {

		nodesOnLevel = new int[levelnumber + 1];
		for (int i = 0; i < levelnumber + 1; ++i) {
			nodesOnLevel[i] = 0;
		}

		if (!isolateNodes.isEmpty()) {
			nodesOnLevel[0] = isolateNodes.size();
		}

		for (final int i : level.values()) {
			nodesOnLevel[i] += 1;
		}
	}

	/**
	 * Initialize default values. Find isolated nodes.
	 */
	protected void init() {
		final Collection<V> vertices = g.getVertices();
		final Set<V> complexVerticses = new HashSet<V>();

		for (final V v : vertices) {
			if (getInDegree(v) == 0 && getOutDegree(v) == 0) {
				isolateNodes.add(v);
				minusLevels = 1;
			} else {
				complexVerticses.add(v);
			}
		}

		for (final V v : complexVerticses) {
			level.put(v, minusLevels);
			arcs.put(v, getInDegree(v));
			queue.offer(v);
		}

	}

	/**
	 * Run the algorithm.<br>
	 * Check the node with the least number of in-arcs.<br>
	 * If the node has no in-arcs, then it does not belong to a strongly
	 * connected component.<br>
	 * If the node has in-arcs, we can be sure there is a circle in the
	 * remaining graph.
	 * 
	 * @see #checkNonCircle(Object)
	 * @see #checkCircle(Object)
	 */
	protected void runAlgorithm() {
		while (!queue.isEmpty()) {
			final V v = queue.poll();
			if (arcs.get(v) == 0) {
				checkNonCircle(v);
			} else {
				checkCircle(v);
				// What if the node is not in a circle?
				// Since a circle is finalized only when there are no more
				// in-arcs to the circle, we won't actually finalize this node
				// until the not yet found circle is finalized.
				//
				// A small example:
				// The graph: a->b->a; b->c->d
				// Suppose we get node c out of the queue first
				// (all nodes have priority 1).
				// Then node c will be considered a circle, but won't be
				// finalized, since we still have the b->c edge.
				// This way, the level of node d will be updated properly when
				// necessary.
			}
		}
	}

	/**
	 * Check a node that does not belong to a circle.<br>
	 * The node will be finalized, and its neighbors updated.
	 * 
	 * @param v
	 *            The node to be checked
	 * @see #updateNode(Object, int)
	 */
	protected void checkNonCircle(final V v) {
		checked.add(v);
		for (final E e : getOutEdges(v)) {
			final V w = getDest(e);
			updateNode(w, level.get(v));
		}
	}

	/**
	 * Check a node that belongs to a circle.<br>
	 * Start a dfs from v. Check every seen node if there is a path back to v.<br>
	 * Assume the found strongly connected component to be a node.<br>
	 * Calculate the in-arcs of the component. If it is 0, finalize the circle.
	 * 
	 * @param v
	 *            The node to be checked
	 * @see #finalizeCircle(Set, int)
	 */
	protected void checkCircle(final V v) {
		// find the circles
		final Set<V> circle = detectCircles(v);

		// calculate the level for the circle, which is the maximum of the
		// level of nodes contained in it.
		int circleLevel = 0;
		for (final V w : circle) {
			final int currentLevel = level.get(w);
			if (currentLevel > circleLevel) {
				circleLevel = currentLevel;
			}
		}

		// administer the changes to all nodes in the circle
		int foundArcs = 0;
		for (final V u : circle) {
			level.put(u, circleLevel);
			mapNodeCircle.put(u, circle);

			for (final E e : getInEdges(u)) {
				final V w = getSource(e);
				if (!(checked.contains(w) || circle.contains(w))) {
					++foundArcs;
				}
			}

			queue.remove(u);
		}

		if (foundArcs == 0) {
			finalizeCircle(circle, circleLevel);
		}
		mapCircleArcs.put(circle, foundArcs);
	}

	/**
	 * Detects all of the circles the parameter is a part of.
	 * 
	 * @param v
	 *            The node to be checked
	 */
	protected Set<V> detectCircles(final V v) {

		final Set<V> seenForward = new HashSet<V>();
		final Set<V> seenBackward = new HashSet<V>();

		dfsSeeForward(v, seenForward);
		dfsSeeBackward(v, seenBackward);

		seenForward.retainAll(seenBackward);
		seenForward.add(v);

		return seenForward;
	}

	/**
	 * Finalizes the circle on the given level.<br>
	 * Update the out-neighbors of the circle.
	 * 
	 * @param circle
	 *            The set of nodes belonging to the circle
	 * @param circleLevel
	 *            The level of the circle
	 */
	protected void finalizeCircle(final Set<V> circle, final int circleLevel) {
		for (final V u : circle) {
			checked.add(u);
			for (final E e : getOutEdges(u)) {
				final V w = getDest(e);
				if (!circle.contains(w)) {
					updateNode(w, circleLevel);
				}
			}
		}
	}

	/**
	 * Update the values of a node.<br>
	 * Increase the level of the node, if necessary.<br>
	 * Decrease in-arcs of the node by one.
	 * 
	 * @param w
	 *            The node whose values will be updated
	 * @param parentLevel
	 *            The level of the parent node
	 */
	protected void updateNode(final V w, final int parentLevel) {
		boolean isnewlevel;

		final int oldLevel = level.get(w);
		int newLevel;
		if (oldLevel > parentLevel + 1) {
			isnewlevel = false;
			newLevel = oldLevel;
		} else {
			isnewlevel = true;
			newLevel = parentLevel + 1;
		}
		if (newLevel > levelnumber) {
			levelnumber = newLevel;
		}

		if (mapNodeCircle.containsKey(w)) {
			final Set<V> circle = mapNodeCircle.get(w);
			mapCircleArcs.put(circle, mapCircleArcs.get(circle) - 1);
			if (isnewlevel) {
				for (final V u : circle) {
					level.put(u, newLevel);
				}
			}
			if (mapCircleArcs.get(circle) == 0) {
				finalizeCircle(circle, newLevel);
			}
		} else {
			level.put(w, newLevel);
			arcs.put(w, arcs.get(w) - 1);
			queue.remove(w);
			queue.offer(w);
		}
	}

	/**
	 * A dfs algorithm to traverse the graph from the given node, and collect
	 * the reachable nodes in the set.
	 * 
	 * @param source
	 *            The source node
	 * @param set
	 *            The reachable nodes will be collected in this set
	 */
	protected void dfsSee(final V source, final Set<V> set, final boolean forward) {
		final Deque<V> current = new LinkedList<V>();
		current.add(source);
		while (!current.isEmpty()) {
			final V v = current.removeLast();
			set.add(v);
			for (final E e : getDirectedEdges(v, forward)) {
				final V w = getNodeOfDirectedEdge(e, forward);
				if (set.contains(w)) {
					continue;
				}
				current.add(w);
			}
		}
		set.remove(source);
	}

	/**
	 * Find the reachable nodes from a start node in the graph.
	 * 
	 * @param source
	 *            The start node
	 * @param set
	 *            The set that will contain the reachable nodes
	 */
	protected void dfsSeeForward(final V source, final Set<V> set) {
		dfsSee(source, set, true);
	}

	/**
	 * Find the reachable nodes from a start node in the reverse graph.
	 * 
	 * @param source
	 *            The start node
	 * @param set
	 *            The set that will contain the reachable nodes
	 */
	protected void dfsSeeBackward(final V source, final Set<V> set) {
		dfsSee(source, set, false);
	}

	/**
	 * @param v
	 *            The node
	 * @param forward
	 *            False if we search in the reverse graph
	 * @return The edges incident to the node in the (reverse) graph
	 */
	protected Collection<E> getDirectedEdges(final V v, final boolean forward) {
		if (forward) {
			return g.getOutEdges(v);
		} else {
			return g.getInEdges(v);
		}
	}

	/**
	 * @param e
	 *            The edge
	 * @param forward
	 *            False if we search in the reverse graph
	 * @return The endpoint of the edge
	 */
	protected V getNodeOfDirectedEdge(final E e, final boolean forward) {
		if (forward) {
			return g.getDest(e);
		} else {
			return g.getSource(e);
		}
	}

	/**
	 * @return The map containing the nodes and their levels.
	 */
	@Override
	public Map<V, Integer> getLevels() {
		return level;
	}

	/**
	 * @return The number of levels. Level numbering begins from 0.
	 */
	@Override
	public int getNumberOfLevels() {
		return levelnumber + 1;
	}

	/**
	 * @return The array of levels: how many nodes belong to them.
	 */
	@Override
	public int[] getNumberOfNodesPerLevel() {
		return nodesOnLevel;
	}

	/**
	 * @return Returns the set of isolate nodes
	 */
	@Override
	public Set<V> getIsolateNodes() {
		return isolateNodes;
	}

	/**
	 * @param v
	 *            The node
	 * @return The in-degree of the node
	 */
	protected int getInDegree(final V v) {
		return g.inDegree(v);
	}

	/**
	 * @param v
	 *            The node
	 * @return The out-degree of the node
	 */
	protected int getOutDegree(final V v) {
		return g.outDegree(v);
	}

	/**
	 * @param v
	 *            The node
	 * @return The in-edges of the node
	 */
	protected Collection<E> getInEdges(final V v) {
		return g.getInEdges(v);
	}

	/**
	 * @param v
	 *            The node
	 * @return The out-edges of the node
	 */
	protected Collection<E> getOutEdges(final V v) {
		return g.getOutEdges(v);
	}

	/**
	 * @param e
	 *            The edge
	 * @return The source node of the edge
	 */
	protected V getSource(final E e) {
		return g.getSource(e);
	}

	/**
	 * @param e
	 *            The edge
	 * @return The tail of the edge
	 */
	protected V getDest(final E e) {
		return g.getDest(e);
	}
}
