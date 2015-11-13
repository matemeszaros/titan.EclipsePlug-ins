/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.utils;

import javax.swing.JRadioButtonMenuItem;

import org.eclipse.titanium.metrics.IMetricEnum;

/**
 * A child class of {@link JRadioButtonMenuItem}, it stores also a
 * {@link MetricsEnum} to handle metric changes more easily. These items are
 * used inside the graph window
 * 
 * @author Gabor Jenei
 */
public class MetricsEntry extends JRadioButtonMenuItem {
	private static final long serialVersionUID = 3727330194438493431L;
	protected IMetricEnum metric;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            : The name to show in the menu
	 * @param metric
	 *            : The associated metric
	 */
	public MetricsEntry(String name, IMetricEnum metric) {
		super(name);
		this.metric = metric;
	}

	/**
	 * @return returns the metric associated with the menu entry
	 */
	public IMetricEnum getMetric() {
		return metric;
	}
}
