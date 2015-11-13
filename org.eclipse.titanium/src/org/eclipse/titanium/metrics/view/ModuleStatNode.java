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

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.StatColumn;
import org.eclipse.titanium.metrics.Statistics;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree of a module, with child nodes.
 * 
 * @TODO: should be split to three separate class for altstep, function and
 *        testcase metrics
 * @author poroszd
 * 
 */
class ModuleStatNode implements IContentNode, IOpenable {
	private final Module module;
	private final IMetricEnum metric;
	private boolean initialized;
	private Object[] children;

	public ModuleStatNode(final IMetricEnum metric, final Module module) {
		this.metric = metric;
		this.module = module;
		initialized = false;
	}

	@Override
	public Location getLocation() {
		return module.getLocation();
	}

	@Override
	public Object[] getChildren(final MetricData data) {
		if (initialized) {
			return children;
		}

		final List<? super IContentNode> c = new ArrayList<IContentNode>();
		if (metric instanceof FunctionMetric) {
			for (final Def_Function f : data.getFunctions().get(module)) {
				c.add(new FunctionNode((FunctionMetric) metric, f));
			}
		} else if (metric instanceof TestcaseMetric) {
			for (final Def_Testcase t : data.getTestcases().get(module)) {
				c.add(new TestcaseNode((TestcaseMetric) metric, t));
			}
		} else if (metric instanceof AltstepMetric) {
			for (final Def_Altstep a : data.getAltsteps().get(module)) {
				c.add(new AltstepNode((AltstepMetric) metric, a));
			}
		} else {
			throw new AssertionError("ModuleStatNode should have a subModule-metric");
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
			return data.getRisk((AltstepMetric) metric, module);
		} else if (metric instanceof FunctionMetric) {
			return data.getRisk((FunctionMetric) metric, module);
		} else if (metric instanceof TestcaseMetric) {
			return data.getRisk((TestcaseMetric) metric, module);
		}

		throw new AssertionError("ModuleStatNode should have a subModule-metric");
	}

	@Override
	public double risk(final MetricData data) {
		if (metric instanceof AltstepMetric) {
			return data.getRiskValue((AltstepMetric) metric, module);
		} else if (metric instanceof FunctionMetric) {
			return data.getRiskValue((FunctionMetric) metric, module);
		} else if (metric instanceof TestcaseMetric) {
			return data.getRiskValue((TestcaseMetric) metric, module);
		}

		throw new AssertionError("ModuleStatNode should have a subModule-metric");
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return module.getName();
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
			Statistics stat;
			if (metric instanceof AltstepMetric) {
				stat = data.getStatistics((AltstepMetric) metric, module);
			} else if (metric instanceof FunctionMetric) {
				stat = data.getStatistics((FunctionMetric) metric, module);
			} else if (metric instanceof TestcaseMetric) {
				stat = data.getStatistics((TestcaseMetric) metric, module);
			} else {
				throw new AssertionError("ModuleStatNode should have a subModule-metric");
			}

			final Number n = stat.get(c);
			return n == null ? null : n.toString();
		}
	}
}
