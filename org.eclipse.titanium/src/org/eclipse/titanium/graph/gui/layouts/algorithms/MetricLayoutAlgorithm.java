/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.error.ErrorMessage;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;
import org.eclipse.titanium.metrics.utils.ModuleMetricsWrapper;
import org.eclipse.titanium.metrics.utils.WrapperStore;

/**
 * This class implements the ordering of graph nodes needed to
 * generate a layout using metric values measured on TTCN3 modules
 * 
 * @author Gabor Jenei
 */
public class MetricLayoutAlgorithm implements HierarcicalLayoutAlgorithm<NodeDescriptor> {
	
	private static final int LEVELS = 10;
	private static final double EPSILON = Math.pow(10, -6);
	
	private Map<NodeDescriptor, Double> values;
	private Map<NodeDescriptor, Integer> levels;
	private Collection<NodeDescriptor> nodes;
	private Set<NodeDescriptor> badNodes;
	private IMetricEnum chosenMetric;
	private Double minValue;
	private Double maxValue;
	private int filledLevels;
	private int[] nodeCount;
	
	/**
	 * Constructor. It does the initialization and also the needed calculations
	 * @param metric : The chosen metric
	 * @param nodes : The graph nodes that should be ordered
	 */
	public MetricLayoutAlgorithm(final IMetricEnum metric, final Collection<NodeDescriptor> nodes) {
		this.nodes = new HashSet<NodeDescriptor>(nodes);
		chosenMetric = metric;
	}
	
	private void init() {
		values = new HashMap<NodeDescriptor, Double>();
		levels = new HashMap<NodeDescriptor, Integer>();
		badNodes = new HashSet<NodeDescriptor>();
		minValue = Double.POSITIVE_INFINITY;
		maxValue = Double.NEGATIVE_INFINITY;
		filledLevels = 0;
		
		if (!PreferenceManager.isEnabledOnModuleGraph(chosenMetric)) {
			ErrorReporter.logError("Error during metric layout generating: The requested metric is not" + 
								" enabled. Only enabled metrics can be chosen!");
			ErrorMessage.show("Error", "The chosen metric must be enabled for calculation in the properties." + 
								"Have you enabled it?", MessageDialog.ERROR);
			return;
		}
		
		final Iterator<NodeDescriptor> it = nodes.iterator();
		while (it.hasNext()) {
			final NodeDescriptor node = it.next();
			final ModuleMetricsWrapper wrapper = WrapperStore.getWrapper(node.getProject());
			final Number val = wrapper.getValue(chosenMetric, node.getName());
			if (val == null) {
				it.remove();
				badNodes.add(node);
			} else {
				final Double tempVal = Double.valueOf(val.toString());
				if (tempVal != null) {
					values.put(node, tempVal);
					if (minValue > tempVal) {
						minValue = tempVal;
					}
					
					if (maxValue < tempVal) {
						maxValue = tempVal;
					}
				}
			}
		}
	}
	
	private void genLevels() {
		filledLevels = badNodes.isEmpty() ? 0 : 1;
		final double step = (maxValue - minValue) / LEVELS;
		double actBound = minValue + step;
		for (int actLevel = 0; actLevel < LEVELS; ++actLevel) {
			final Iterator<NodeDescriptor> it = nodes.iterator();
			boolean isEmpty = true;
			while (it.hasNext()) {
				final NodeDescriptor node = it.next();
				final Double value = values.get(node);
				if (value != null && (value < actBound || Math.abs(value-actBound) <= EPSILON)) {
					levels.put(node, filledLevels);
					it.remove();
					isEmpty = false;
				}
			}
			if (!isEmpty) {
				filledLevels++;
			}
			actBound += step;
		}
		
		genLevelNumbers();
	}
	
	private void genLevelNumbers() {
		nodeCount = new int[filledLevels];
		
		for (int i = 0; i < filledLevels; ++i) {
			nodeCount[i] = 0;
		}
		
		final int badNodeCount = badNodes.size();
		if (badNodeCount != 0) {
			nodeCount[0] = badNodeCount;
		}
		
		for (final int actIndex : levels.values()) {
			++nodeCount[actIndex];
		}
	}

	@Override
	public int getNumberOfLevels() {
		return filledLevels;
	}
	
	@Override
	public Map<NodeDescriptor, Integer> getLevels() {
		return levels;
	}

	@Override
	public int[] getNumberOfNodesPerLevel() {
		return nodeCount;
	}

	@Override
	public Set<NodeDescriptor> getIsolateNodes() {
		return badNodes;
	}

	@Override
	public void run() {
		init();
		genLevels();
	}
}