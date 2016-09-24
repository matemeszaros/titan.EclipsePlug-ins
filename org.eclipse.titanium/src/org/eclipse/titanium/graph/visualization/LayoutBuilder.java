/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.visualization;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.clustering.gui.ClusterTransformer;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.graph.gui.layouts.MetricLayout;
import org.eclipse.titanium.graph.gui.layouts.ReverseDAGLayout;
import org.eclipse.titanium.graph.gui.layouts.TitaniumDAGLayout;
import org.eclipse.titanium.graph.gui.layouts.TitaniumISOMLayout;
import org.eclipse.titanium.graph.gui.utils.LayoutEntry;
import org.eclipse.titanium.graph.gui.utils.MetricsLayoutEntry;
import org.eclipse.titanium.preferences.PreferenceConstants;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class can create jung Layouts via a builder pattern aspect.
 * 
 * @author Gabor Jenei
 */
public class LayoutBuilder {
	private final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> g;
	private final LayoutEntry layoutEntry;
	private final Dimension size;
	
	private Set<Set<NodeDescriptor>> clusters;
	private Transformer<NodeDescriptor, Point2D> pointTransformer;
	
	/**
	 * Constructor
	 * @param graph : The graph to be shown
	 * @param entry : The code of the claimed layout (see {@link LayoutEntry#getCode()})
	 * @param size : The size of the canvas to work on
	 */
	public LayoutBuilder(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph, final LayoutEntry entry, final Dimension size) {
		g = graph;
		layoutEntry = entry;
		this.size = size;
	}
	
	/**
	 * This method sets a clustering in the layout, it should be used when the user claimed
	 * a clustering layout
	 * @param clusters : The clustering to set
	 * @return This object, in order to implement builder pattern correctly
	 */
	public LayoutBuilder clusters(final Set<Set<NodeDescriptor>> clusters) {
		this.clusters = clusters;
		return this;
	}
	
	/**
	 * This method sets a point transformer, it is needed when a clustering layout is used
	 * @param trf : The transformer to set
	 * @return This object, in order to implement builder pattern correctly
	 */
	public LayoutBuilder transformer(final Transformer<NodeDescriptor, Point2D> trf) {
		pointTransformer = trf;
		return this;
	}
	
	/**
	 * This method implements the building of the chosen layout with chosen parameters
	 * @return The built layout
	 * @throws BadLayoutException If the chosen layout doesn't exist (invalid code was provided)
	 */
	public Layout<NodeDescriptor, EdgeDescriptor> build() throws BadLayoutException {
		Layout<NodeDescriptor, EdgeDescriptor> layout;
		
		final String layoutCode = layoutEntry.getCode();
		if (layoutCode.equals(Layouts.LAYOUT_ISOM.getCode())) {
			layout = new TitaniumISOMLayout<NodeDescriptor, EdgeDescriptor>(g);
			((TitaniumISOMLayout<NodeDescriptor, EdgeDescriptor>)layout).setMaxIterations(Activator.getDefault().
					getPreferenceStore().getInt(PreferenceConstants.NO_ITERATIONS));
		} else if (layoutCode.equals(Layouts.LAYOUT_KK.getCode())) {
			layout = new KKLayout<NodeDescriptor, EdgeDescriptor>(g);
			((KKLayout<NodeDescriptor, EdgeDescriptor>) layout).setMaxIterations(Activator.getDefault().getPreferenceStore()
					.getInt(PreferenceConstants.NO_ITERATIONS));
		} else if (layoutCode.equals(Layouts.LAYOUT_FR.getCode())) {
			layout = new FRLayout<NodeDescriptor, EdgeDescriptor>(g);
			((FRLayout<NodeDescriptor, EdgeDescriptor>) layout).setAttractionMultiplier(0.6);
			((FRLayout<NodeDescriptor, EdgeDescriptor>) layout).setRepulsionMultiplier(0.8);
			((FRLayout<NodeDescriptor, EdgeDescriptor>) layout).setMaxIterations(Activator.getDefault().getPreferenceStore()
					.getInt(PreferenceConstants.NO_ITERATIONS));
		} else if (layoutCode.equals(Layouts.LAYOUT_SPRING.getCode())) {
			layout = new SpringLayout<NodeDescriptor, EdgeDescriptor>(g);
		} else if (layoutCode.equals(Layouts.LAYOUT_CIRCLE.getCode())) {
			layout = new CircleLayout<NodeDescriptor, EdgeDescriptor>(g);
		} else if (layoutCode.equals(Layouts.LAYOUT_RTDAG.getCode())) {
			layout = new ReverseDAGLayout<NodeDescriptor, EdgeDescriptor>(g, size);
		} else if (layoutCode.equals(Layouts.LAYOUT_TDAG.getCode())) {
			layout = new TitaniumDAGLayout<NodeDescriptor, EdgeDescriptor>(g, size);
		} else if (layoutCode.equals(Layouts.METRIC_LAYOUT_CODE)) {
			if (!(layoutEntry instanceof MetricsLayoutEntry)) {
				throw new IllegalStateException("A metric must be chosen before using metric layout!");
			}
			layout = new MetricLayout<EdgeDescriptor>(g, size, ((MetricsLayoutEntry)layoutEntry).getMetric());
		} else if (layoutCode.equals(Layouts.S_LAYOUT_CLUSTER)) {
			if (clusters == null) {
				throw new IllegalStateException("A clustering must be set before using cluster layout!");
			}
			final ClusterTransformer trf = new ClusterTransformer(new FRLayout<NodeDescriptor, EdgeDescriptor>(g), clusters, size);
			layout = new StaticLayout<NodeDescriptor, EdgeDescriptor>(g, trf);
		} else if ("STATIC".equals(layoutCode)) {
			if (pointTransformer == null) {
				throw new IllegalStateException("A point transformer must be set before using static layout!");
			}
			layout=new StaticLayout<NodeDescriptor, EdgeDescriptor>(g,pointTransformer);
		} else {
			throw new BadLayoutException("There is no such layout! (Layout=" + layoutCode + ")", ErrorType.NOT_EXISITING_LAYOUT);
		}
		layout.setSize(size);
		
		return layout;
	}
}
