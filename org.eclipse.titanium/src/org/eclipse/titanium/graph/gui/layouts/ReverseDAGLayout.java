/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts;

import java.awt.Dimension;
import java.util.Collection;

import org.eclipse.titanium.graph.gui.layouts.algorithms.DAGLayoutReverseAlgorithm;

import edu.uci.ics.jung.graph.Graph;
/**
 * This class implements the reverse DAG layout for jung graphs.
 * It can be used even on cyclic graphs
 * @author Gabor Jenei
 *
 * @param <V> Node type
 * @param <E> Edge type
 */
public class ReverseDAGLayout<V,E> extends BaseHierarchicalLayout<V,E> {

	/**
	 * Constructor
	 * @param g : The graph to show
	 * @param size : The size of the canvas to draw on
	 */
	public ReverseDAGLayout(Graph<V,E> g, Dimension size) {
		super(g, size);
	}

	@Override
	protected Collection<V> getNeighbours(V v) {
		return graph.getSuccessors(v);
	}

	@Override
	protected void initAlg() {
		alg = new DAGLayoutReverseAlgorithm<V, E>(graph);
	}


}