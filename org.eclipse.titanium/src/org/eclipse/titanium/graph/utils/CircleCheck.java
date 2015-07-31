/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class checks for circle in a given Jung graph
 * 
 * @author Gabor Jenei
 * @param <V>
 *            node type
 * @param <E>
 *            edge type
 */
public class CircleCheck<V, E> {
	private DirectedSparseGraph<V, E> g;
	private List<Deque<E>> circles;
	private Deque<E> circleEdges;
	private Deque<V> circleVertices;
	private List<V> remaining;

	/**
	 * This class checks whether a graph is cyclic or non-cyclic
	 * 
	 * @param graph
	 *            : The tested graph
	 */
	public CircleCheck(DirectedSparseGraph<V, E> graph) {
		g = graph;
		circles = new LinkedList<Deque<E>>();
		circleEdges = new LinkedList<E>();
		circleVertices = new LinkedList<V>();
	}

	/**
	 * @return boolean value indicates whether the given graph contains a cycle
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isCyclic() {
		remaining = new ArrayList<V>(g.getVertices());
		Collections.sort((List)remaining);
		while (!remaining.isEmpty()) {
			V v = remaining.get(remaining.size() - 1);
			circleVertices.push(v);
			recursiveWalk(v);
			circleVertices.pop();
		}

		return !circles.isEmpty();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void recursiveWalk(V u) {
		List<E> neighbours = new ArrayList<E>(g.getOutEdges(u));
		Collections.sort((List)neighbours);
		for (E e : neighbours) {
			V v = g.getDest(e);
			circleEdges.push(e);
			if (circleVertices.contains(v)) {
				final int index = ((LinkedList<V>)circleVertices).indexOf(v);
				Deque<E> circle = new LinkedList<E>();
				for (int i = index; i >= 0; i--) {
					circle.push(((LinkedList<E>)circleEdges).get(i));
				}
				circles.add(circle);
			} else if (remaining.contains(v)) {
				circleVertices.push(v);
				recursiveWalk(v);
				circleVertices.pop();
			}
			circleEdges.pop();
		}
		remaining.remove(u);
	}

	/**
	 * This function should be called after calling
	 * {@link CircleCheck#isCyclic()}, it returns a list of cycles
	 * 
	 * @return the list of cycles found in the graph
	 */
	public List<Deque<E>> getCircles() {
		return circles;
	}
}