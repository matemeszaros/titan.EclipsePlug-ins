/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree corresponding to a function definition.
 * 
 * @author poroszd
 * 
 */
class FunctionNode implements IContentNode, IOpenable {
	private final Def_Function f;
	private final FunctionMetric m;

	public FunctionNode(final FunctionMetric metric, final Def_Function function) {
		m = metric;
		f = function;
	}

	@Override
	public Location getLocation() {
		return f.getLocation();
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
		return data.getRisk(m, f);
	}

	@Override
	public double risk(final MetricData data) {
		return data.getRiskValue(m, f);
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return f.getIdentifier().getDisplayName();
		} else if (i == 1) {
			return data.get(m, f).toString();
		}

		return null;
	}
}
