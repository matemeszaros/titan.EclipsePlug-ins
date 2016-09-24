/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering.gui;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class provides a layout-like environment for clusters. It transforms
 * node names into their place on the canvas.
 * 
 * @author Gabor Jenei
 */
public final class ClusterTransformer implements Transformer<NodeDescriptor, Point2D> {
	protected AggregateLayout<NodeDescriptor, EdgeDescriptor> mainLayout;
	protected Set<Set<NodeDescriptor>> clusters;

	/**
	 * Constructor
	 * 
	 * @param subLayouts
	 *            : the layout style used inside the clusters
	 * @param clusters
	 *            : the set of clusters to organize
	 * @param extSize
	 *            : the whole size of the canvas
	 */
	public ClusterTransformer(final Layout<NodeDescriptor, EdgeDescriptor> subLayouts, final Set<Set<NodeDescriptor>> clusters, final Dimension extSize) {
		mainLayout = new AggregateLayout<NodeDescriptor, EdgeDescriptor>(subLayouts);
		this.clusters = clusters;
		mainLayout.setSize(extSize);

		for (final Set<NodeDescriptor> clust : clusters) {
			groupCluster(clust);
		}
	}

	/**
	 * Calculate positions for one cluster
	 * 
	 * @param vertices
	 *            : the set of vertices inside the cluster
	 */
	protected void groupCluster(final Set<NodeDescriptor> vertices) {
		if (vertices.size() < mainLayout.getGraph().getVertexCount()) {
			final Point2D center = mainLayout.transform(vertices.iterator().next());
			final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> subGraph = new DirectedSparseGraph<NodeDescriptor, EdgeDescriptor>();
			for (final NodeDescriptor v : vertices) {
				subGraph.addVertex(v);
			}

			final Layout<NodeDescriptor, EdgeDescriptor> subLayout = new CircleLayout<NodeDescriptor, EdgeDescriptor>(subGraph);
			subLayout.setInitializer(mainLayout);
			// TODO Could we calculate the needed space for one cluster?
			final Dimension canvasSize = new Dimension(100, 100);
			subLayout.setSize(canvasSize);

			mainLayout.put(subLayout, center);

		}
	}

	/**
	 * This method is inherited from {@link Transformer} class
	 * 
	 * @param v
	 *            : the node to transform to coordinates
	 */
	@Override
	public Point2D transform(final NodeDescriptor v) {
		return mainLayout.transform(v);
	}
}