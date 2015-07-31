/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ProjectMetric;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree for a project in a project-metric subtree.
 * 
 * @author poroszd
 * 
 */
class ProjectNode implements IContentNode {
	private final ProjectMetric metric;

	public ProjectNode(final ProjectMetric metric) {
		this.metric = metric;
	}

	@Override
	public Object[] getChildren(final MetricData data) {
		return new Object[]{};
	}

	@Override
	public boolean hasChildren(final MetricData data) {
		return false;
	}

	@Override
	public RiskLevel getRiskLevel(final MetricData data) {
		return data.getRisk(metric);
	}

	@Override
	public double risk(final MetricData data) {
		return data.getRiskValue(metric);
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return metric.getName();
		} else if (i == 1) {
			return data.get(metric).toString();
		}

		return null;
	}
}
