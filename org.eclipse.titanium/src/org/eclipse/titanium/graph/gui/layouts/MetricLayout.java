/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts;

import java.awt.Dimension;
import java.util.Collection;

import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.layouts.algorithms.MetricLayoutAlgorithm;
import org.eclipse.titanium.metrics.IMetricEnum;

import edu.uci.ics.jung.graph.Graph;

/**
 * This class implements a layout ordered according to the measured metric values on the modules.
 * It only works on graphs containing nodes of type {@link NodeDescriptor}
 * @author Gabor Jenei
 *
 * @param <E> The edge type
 */
public class MetricLayout<E> extends BaseHierarchicalLayout<NodeDescriptor, E> {

	/**
	 * Constructor
	 * @param g : The graph to be shown
	 * @param size : The size of the canvas
	 * @param metric : The chosen metric type for ordering
	 */
	public MetricLayout(Graph<NodeDescriptor, E> g, Dimension size, IMetricEnum metric) {
		super(g,size,metric);
	}

	@Override
	protected void initAlg() {
		alg = new MetricLayoutAlgorithm(chosenMetric, graph.getVertices());
	}

	@Override
	protected Collection<NodeDescriptor> getNeighbours(NodeDescriptor v) {
		return graph.getPredecessors(v);
	}
	
}