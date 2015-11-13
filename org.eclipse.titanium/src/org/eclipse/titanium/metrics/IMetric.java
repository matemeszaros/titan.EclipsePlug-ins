/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

/**
 * The common interface of all metrics.
 * <p>
 * Metrics are stateless classes, that can provide information about a TTCN3
 * project. Basically they are used like:
 * <ol>
 * <li>Create a {@link MetricData} object on a TTCN3 project</li>
 * <li>query the result of the metrics.</li>
 * </ol>
 * <p>
 * Metrics store any information collected during the measurement in the
 * <code>MetricData</code> objects.
 * 
 * @author poroszd
 * 
 */
public interface IMetric<ENTITY, METRIC extends IMetricEnum> {
	METRIC getMetric();

	/**
	 * Initialize the data structures used by this metric.
	 */
	void init(MetricData data);

	/**
	 * Measure an entity.
	 * 
	 * During the measuring, the metric can use the values measured by the
	 * previously used metrics to save computing those results themselves. Those
	 * values can be accessed via the supplied {@link MetricData} instance.
	 * 
	 * @param data
	 *            measured values of previous metrics
	 * @param entity
	 *            the entity to measure
	 * @return the result of the measurement
	 */
	Number measure(MetricData data, ENTITY entity);
}
