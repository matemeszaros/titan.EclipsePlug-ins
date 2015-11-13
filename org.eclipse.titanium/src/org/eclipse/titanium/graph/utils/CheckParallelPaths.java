/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

/**
 * This class checks for parallel paths in a Jung graph.
 * 
 * @author Gobor Daniel
 * @param <V>
 *            node type
 * @param <E>
 *            edge type
 */
public class CheckParallelPaths<V, E> {
	protected Graph<V, E> graph;
	protected Map<V, E> predArc;
	protected Set<Deque<E>> paths;
	protected V root;
	private boolean hasRoot;

	/**
	 * Initialize the data to find all parallel paths.
	 * 
	 * @param graph
	 *            The graph to be tested
	 */
	public CheckParallelPaths(Graph<V, E> graph) {
		this.graph = graph;
		paths = new HashSet<Deque<E>>();
		hasRoot = false;
	}

	/**
	 * Initialize the date to find all parallel paths from s.
	 * 
	 * @param graph
	 *            The graph to be tested
	 * @param s
	 *            The source node
	 */
	public CheckParallelPaths(Graph<V, E> graph, V s) {
		this(graph);
		root = s;
		hasRoot = true;
	}
	
	/**
	 * Initializes one dfs search.
	 */
	public void init() {
		predArc = new HashMap<V, E>();
		for (V v : graph.getVertices()) {
			predArc.put(v, null);
		}
	}

	/**
	 * Checks if the graph contains parallel paths.
	 * 
	 * @return True if there are parallel paths in the graph
	 */
	public boolean hasParallelPaths() {
		if (hasRoot) {
			init();
			dfs(root);
		} else {
			for (V s : graph.getVertices()) {
				init();
				dfs(s);
			}
		}
		return !paths.isEmpty();
	}

	/**
	 * Performs a dfs algorithm to find the parallel paths.
	 * 
	 * @param source
	 *            The starting node
	 */
	protected void dfs(V source) {
		Set<V> set = new HashSet<V>();
		set.add(source);
		Deque<V> current = new LinkedList<V>();
		current.add(source);
		while (!current.isEmpty()) {
			V v = current.removeLast();
			set.add(v);
			for (E e : graph.getOutEdges(v)) {
				V w = graph.getDest(e);
				if (set.contains(w)) {
					addPaths(v, w, e);
				} else {
					predArc.put(w, e);
					current.add(w);
				}
			}
		}
	}

	/**
	 * Checks if the found paths are indeed parallel. Trims the paths to
	 * appropriate size if needed. Add the paths to the set if they are correct.
	 * 
	 * @param v
	 *            End node of the parallel path
	 * @param w
	 *            End node of the path
	 * @param e
	 *            Edge from which we arrive at w
	 */
	protected void addPaths(V v, V w, E e) {
		Deque<E> path = new LinkedList<E>();
		Deque<E> parallelpath = new LinkedList<E>();
		buildPath(w, path);
		buildPath(v, parallelpath);
		parallelpath.add(e);
		if (path.isEmpty() || parallelpath.isEmpty()) {
			return;
		}
		if (hasRoot) {
			// it is a cycle
			if (parallelpath.contains(path.peekLast())) {
				return;
			}
		} else {
			// trim the paths
			while (path.peek() == parallelpath.peek()) {
				path.poll();
				parallelpath.poll();
			}
			// it is a cycle
			if (path.isEmpty()) {
				return;
			}
		}
		paths.add(path);
		paths.add(parallelpath);
	}

	/**
	 * Creates the path from the root to s recursively.
	 * 
	 * @param s
	 *            The current node
	 * @param path
	 *            The list to contain the path
	 */
	protected void buildPath(V s, Deque<E> path) {
		E e = predArc.get(s);
		while (e != null) {
			path.add(e);
			e = predArc.get(graph.getSource(e));
		}
	}

	/**
	 * The function should be called after calling
	 * {@link CheckParallelPaths#hasParallelPaths()}. Returns the set of found
	 * parallel paths.
	 * 
	 * @return The found paths, which have a parallel path
	 */
	public Set<Deque<E>> getPaths() {
		return paths;
	}
}