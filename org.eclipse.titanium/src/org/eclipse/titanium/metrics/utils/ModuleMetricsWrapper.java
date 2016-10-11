/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.ProjectMetric;
import org.eclipse.titanium.metrics.StatColumn;
import org.eclipse.titanium.metrics.Statistics;
import org.eclipse.titanium.metrics.TestcaseMetric;

/**
 * A wrapper class on {@link MetricData}.
 * 
 * This class supplies similar functionality as that, but is cleaner, simpler,
 * dumber.
 * 
 * @author poroszd
 */
public class ModuleMetricsWrapper {
	private final MetricData data;
	private final Map<String, Module> modules;

	/**
	 * Create a new wrapper object.
	 * <p>
	 * Actually, it creates a new <code>MetricData</code> instance. To do this,
	 * it locks the project. Beware deadlocks.
	 * <p>
	 * NOTE: Creating this object is costly. Cache when you can.
	 * 
	 * @param project
	 *            the target project of the metrics
	 * @param user_metrics
	 *            metrics to use in the measurement
	 */
	protected ModuleMetricsWrapper(final IProject project) {
		modules = new HashMap<String, Module>();
		data = MetricData.measure(project);
		for (final Module module : data.getModules()) {
			modules.put(module.getName(), module);
		}
	}

	/**
	 * Query the {@link Statistics} of a metric on a module.
	 * <p>
	 * If there is no module with the given name, <code>null</code> is returned.
	 * <p>
	 * Note that if the metric is not a submodule metric, an
	 * IllegalArgumentException is thrown. Also note, that the returned
	 * Statistics contains only those {@link StatColumn}s that are requested by
	 * the metric.
	 * 
	 * @param metric
	 *            the metric of which you need statistics.
	 * @param moduleName
	 *            The name of the module in question.
	 * 
	 * @return The stats of the metric measured on entities of the module, or
	 *         null.
	 * 
	 * @throws IllegalArgumentException
	 *             if metric is a {@link ProjectMetric}
	 */
	public Statistics getStats(final IMetricEnum metric, final String moduleName) {
		final Module module = modules.get(moduleName);
		if (module == null) {
			return null;
		}

		if (metric instanceof AltstepMetric) {
			return data.getStatistics((AltstepMetric) metric, module);
		} else if (metric instanceof FunctionMetric) {
			return data.getStatistics((FunctionMetric) metric, module);
		} else if (metric instanceof TestcaseMetric) {
			return data.getStatistics((TestcaseMetric) metric, module);
		} else {
			throw new IllegalArgumentException("The metric " + metric.getName() + " does not have statistics for modules");
		}
	}

	/**
	 * Query a value of a metric on a given module.
	 * <p>
	 * If there is no module with the given name, <code>null</code> is returned.
	 * <p>
	 * Otherwise the result depends on the type of the requested metric. If it
	 * is a {@link ModuleMetric}, then the value of the metric on this module is
	 * returned. If the requested metric is a submodule metric (
	 * {@link AltstepMetric}, {@link FunctionMetric} of {@link TestcaseMetric}),
	 * then the returned value is the one that means the highest risk according
	 * to the metric among the entities of the module, or <code>null</code> if
	 * there are no such entities.<br>
	 * For example, if you request {@link FunctionMetric#LINES_OF_CODE}, then
	 * this method will return the highest result for the functions in that
	 * module, or <code>null</code> if the module doesn't contain any function
	 * definitions.
	 * 
	 * @param metric
	 *            the metric from which you need an indicator
	 * @param moduleName
	 *            the name of the module in question.
	 * 
	 * @return the measured value for module metrics, or the max value in the
	 *         module for submodule metrics, or <code>null</code>
	 * 
	 * @throws IllegalArgumentException
	 *             if metric is a {@link ProjectMetric}
	 */
	public Number getValue(final IMetricEnum metric, final String moduleName) {
		final Module module = modules.get(moduleName);
		if (module == null) {
			return null;
		}

		if (metric instanceof ModuleMetric) {
			return data.get((ModuleMetric) metric, module);
		} else if (metric instanceof AltstepMetric) {
			return data.getHighestRiskCauser((AltstepMetric) metric, module);
		} else if (metric instanceof FunctionMetric) {
			return data.getHighestRiskCauser((FunctionMetric) metric, module);
		} else if (metric instanceof TestcaseMetric) {
			return data.getHighestRiskCauser((TestcaseMetric) metric, module);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * The {@link RiskLevel} suggested by the metric for the given module.
	 * <p>
	 * This method is guaranteed not to return <code>null</code>. If something
	 * fails (e.g. module_name is not a module in the target project), then
	 * {@link RiskLevel}.UNKNOWN is returned.
	 * 
	 * @param metric
	 *            subproject metric
	 * @param moduleName
	 *            a name of a module in the target project
	 * 
	 * @return the level of risk in the module according to the metric
	 * 
	 * @throws IllegalArgumentException
	 *             if metric is a {@link ProjectMetric}
	 */
	public RiskLevel getRisk(final IMetricEnum metric, final String moduleName) {
		final Module module = modules.get(moduleName);
		if (module == null) {
			return RiskLevel.UNKNOWN;
		}

		if (metric instanceof ModuleMetric) {
			return data.getRisk((ModuleMetric) metric, module);
		} else if (metric instanceof AltstepMetric) {
			return data.getRisk((AltstepMetric) metric, module);
		} else if (metric instanceof FunctionMetric) {
			return data.getRisk((FunctionMetric) metric, module);
		} else if (metric instanceof TestcaseMetric) {
			return data.getRisk((TestcaseMetric) metric, module);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * This method is a generalizated version of
	 * {@link #getRisk(MetricsEnum, String) getRisk}.
	 * <p>
	 * When one want to compare risks of two modules
	 *  one might find the other <code>getRisk</code> method
	 * too coarse-grained, too discrete. In these cases, this method comes
	 * handy.
	 * </p>
	 * <p>
	 * It is totally consistent with the discrete version:
	 * <ul>
	 * <li>return values under 1.0 corresponds to {@link RiskLevel#NO}</li>
	 * <li>between 1.0 and 2.0 corresponds to {@link RiskLevel#LOW}</li>
	 * <li>greater or equals to 2.0 corresponds to {@link RiskLevel#HIGH}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param metric
	 *            a metric, requested at construction time
	 * @param moduleName
	 *            the name of a module in the project
	 * @return the (continuous) risk value of the module (in the [0.0, 3.0)
	 *         interval).
	 */
	public double getRiskValue(final IMetricEnum metric, final String moduleName) {
		final Module module = modules.get(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("module_name does not correspond to a module");
		}

		if (metric instanceof ModuleMetric) {
			return data.getRiskValue((ModuleMetric) metric, module);
		} else if (metric instanceof AltstepMetric) {
			return data.getRiskValue((AltstepMetric) metric, module);
		} else if (metric instanceof FunctionMetric) {
			return data.getRiskValue((FunctionMetric) metric, module);
		} else if (metric instanceof TestcaseMetric) {
			return data.getRiskValue((TestcaseMetric) metric, module);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public MetricData getMetricData() {
		return data;
	}
}
