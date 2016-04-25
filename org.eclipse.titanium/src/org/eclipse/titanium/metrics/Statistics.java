/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.titanium.metrics.risk.IRisk;

/**
 * Statistical information about a metric.
 * <p>
 * This class is provides statistics about a metric in a module or in the whole
 * project.
 * 
 * @author poroszd
 * 
 */
public class Statistics {
	private final Map<StatColumn, Double> columns;
	private final Double highestRisk;

	/**
	 * Calculate statistics on <code>nums</code>.
	 * <p>
	 * The supplied <code>metric</code> parameter is used to decide which
	 * statistics to calculate, and how to display them (integer or double), and
	 * <code>risk</code> is used to decide which value represents the highest
	 * risk.
	 * 
	 * @param nums
	 *            The data for calculations (e.g. to calculate module
	 *            statistics, provide the metric results of all entity in the
	 *            module).
	 * @param metric
	 *            The metric which owns the subtree from where the data is
	 *            collected.
	 * @param risk
	 *            is used for finding the value with the highest risk
	 */
	public Statistics(final double[] nums, final IMetricEnum metric, final IRisk risk) {
		final int n = nums.length;
		double total = 0;
		double max;
		double dev;
		double mean;
		final Set<StatColumn> stats = metric.requestedStatistics();
		final boolean isInt = metric.isInteger();

		columns = new EnumMap<StatColumn, Double>(StatColumn.class);

		if (n == 0) {
			max = 0;
			mean = 0;
			dev = 0;
			highestRisk = null;
		} else {
			double ssum = 0;
			double highest = nums[0];
			max = nums[0];

			for (final double i : nums) {
				total += i;
				ssum += i * i;
				max = (max < i) ? i : max;
				highest = risk.getRiskValue(i) > risk.getRiskValue(highest) ? i : highest;
			}

			mean = total / n;
			dev = Math.sqrt((ssum / n) - (mean * mean));
			highestRisk = metric.isInteger() ? new Double(Math.round(highest)) : new Double(highest);
		}

		if (stats.contains(StatColumn.TOTAL)) {
			// Note that this three line is NOT equivalent to
			// Number tmp = isInt ? new Long(.) : new Double(.);
			// due to the evaluation rules of the ternary operator in java.
			Double tmpT;
			if (isInt) {
				tmpT = new Double(Math.round(total));
			} else {
				tmpT = new Double(total);
			}
			columns.put(StatColumn.TOTAL, tmpT);
		}
		if (stats.contains(StatColumn.MAX)) {
			Double tmpM;
			if (isInt) {
				tmpM = new Double(Math.round(max));
			} else {
				tmpM = new Double(max);
			}
			columns.put(StatColumn.MAX, tmpM);
		}
		if (stats.contains(StatColumn.MEAN)) {
			columns.put(StatColumn.MEAN, new Double(mean));
		}
		if (stats.contains(StatColumn.DEV)) {
			columns.put(StatColumn.DEV, new Double(dev));
		}
	}

	/**
	 * Get the value of a specific statistic.
	 * <p>
	 * The value is returned a {@link Number}. More specifically it will be
	 * {@link Double} or {@link Integer}, depending on the metric who was used
	 * to contruct this statistic.
	 * <p>
	 * NOTE: the returned value is <code>null</code> if the requested column is
	 * not chosen at construction by the metric.
	 * </p>
	 * 
	 * @param column
	 *            The enum value for that statistic.
	 * 
	 * @return The value of the statistic, or <code>null</code> if the column is
	 *         omitted.
	 */
	public Number get(final StatColumn column) {
		return columns.get(column);
	}

	/**
	 * Get the value with the highest risk.
	 * <p>
	 * If the stat was constructed with linear risk, it is just the max of the
	 * numbers, but for example {@link ModuleMetric#INSTABILITY} uses different
	 * risk dimension.
	 * 
	 * @return the value with the highest risk
	 */
	public Number getHighestRisk() {
		return highestRisk;
	}
}
