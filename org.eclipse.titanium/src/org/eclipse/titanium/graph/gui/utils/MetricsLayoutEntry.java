/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.utils;

import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.metrics.IMetricEnum;

/**
 * This class implements a {@link LayoutEntry} that is used for metric layouts
 * @author Gabor Jenei
 */
public class MetricsLayoutEntry extends LayoutEntry {
	private static final long serialVersionUID = 6482227185818686580L;
	protected IMetricEnum metric;

	/**
	 * Constructor
	 * @param metric : The metric to be used for ordering
	 */
	public MetricsLayoutEntry(final IMetricEnum metric) {
		super(Layouts.METRIC_LAYOUT_CODE, metric.getName());
		this.metric = metric;
	}
	
	/**
	 * @return The used metric object
	 */
	public IMetricEnum getMetric() {
		return metric;
	}
}
