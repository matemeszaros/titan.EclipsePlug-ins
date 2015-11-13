/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.StatColumn;
import org.eclipse.titanium.metrics.Statistics;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree for a project, with child nodes.
 * 
 * @author poroszd
 * 
 */
class ProjectStatNode implements IContentNode {
	private final IMetricEnum metric;
	private boolean initialized;
	private Object[] children;

	public ProjectStatNode(final IMetricEnum metric) {
		this.metric = metric;
		initialized = false;
	}

	@Override
	public Object[] getChildren(final MetricData data) {
		if (initialized) {
			return children;
		}

		final List<? super IContentNode> c = new ArrayList<IContentNode>();
		if (metric instanceof FunctionMetric || metric instanceof AltstepMetric || metric instanceof TestcaseMetric) {
			for (final Module m : data.getModules()) {
				final IContentNode n = new ModuleStatNode(metric, m);
				if (n.hasChildren(data)) {
					c.add(n);
				}
			}
		} else if (metric instanceof ModuleMetric) {
			for (final Module m : data.getModules()) {
				c.add(new ModuleNode((ModuleMetric) metric, m));
			}
		} else {
			throw new AssertionError("ProjectStatNode should have a subProject-metric");
		}
		children = c.toArray();
		initialized = true;
		return children;
	}

	@Override
	public boolean hasChildren(final MetricData data) {
		if (!initialized) {
			getChildren(data);
		}

		return children.length != 0;
	}

	@Override
	public RiskLevel getRiskLevel(final MetricData data) {
		if (metric instanceof AltstepMetric) {
			return data.getRisk((AltstepMetric) metric);
		} else if (metric instanceof FunctionMetric) {
			return data.getRisk((FunctionMetric) metric);
		} else if (metric instanceof TestcaseMetric) {
			return data.getRisk((TestcaseMetric) metric);
		} else if (metric instanceof ModuleMetric) {
			return data.getRisk((ModuleMetric) metric);
		}

		throw new IllegalArgumentException(metric.getName() + " is not a subproject metric");
	}

	@Override
	public double risk(final MetricData data) {
		if (metric instanceof AltstepMetric) {
			return data.getRiskValue((AltstepMetric) metric);
		} else if (metric instanceof FunctionMetric) {
			return data.getRiskValue((FunctionMetric) metric);
		} else if (metric instanceof TestcaseMetric) {
			return data.getRiskValue((TestcaseMetric) metric);
		} else if (metric instanceof ModuleMetric) {
			return data.getRiskValue((ModuleMetric) metric);
		}

		throw new IllegalArgumentException(metric.getName() + " is not a subproject metric");
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return metric.getName();
		} else {
			StatColumn c = null;
			switch (i) {
			case 1:
				c = StatColumn.TOTAL;
				break;
			case 2:
				c = StatColumn.MAX;
				break;
			case 3:
				c = StatColumn.MEAN;
				break;
			case 4:
				c = StatColumn.DEV;
				break;
			default:
				return null;
			}
			Statistics s;
			if (metric instanceof AltstepMetric) {
				s = data.getStatistics((AltstepMetric) metric);
			} else if (metric instanceof FunctionMetric) {
				s = data.getStatistics((FunctionMetric) metric);
			} else if (metric instanceof TestcaseMetric) {
				s = data.getStatistics((TestcaseMetric) metric);
			} else if (metric instanceof ModuleMetric) {
				s = data.getStatistics((ModuleMetric) metric);
			} else {
				throw new IllegalArgumentException(metric.getName() + " is not a subproject metric");
			} 

			final Number n = s.get(c);
			return n == null ? null : n.toString();
		}
	}
}
