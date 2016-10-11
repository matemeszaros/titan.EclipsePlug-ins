/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree, which corresponds to a module in a module-metric subtree.
 * 
 * @author poroszd
 * 
 */
class ModuleNode implements IContentNode, IOpenable {
	private final Module module;
	private final ModuleMetric metric;

	public ModuleNode(final ModuleMetric metric, final Module module) {
		this.metric = metric;
		this.module = module;
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
	public Location getLocation() {
		return module.getLocation();
	}

	@Override
	public RiskLevel getRiskLevel(final MetricData data) {
		return data.getRisk(metric, module);
	}

	@Override
	public double risk(final MetricData data) {
		return data.getRiskValue(metric, module);
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return module.getName();
		} else if (i == 1) {
			return data.get(metric, module).toString();
		}

		return null;
	}
}
