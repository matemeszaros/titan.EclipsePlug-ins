/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree corresponding to an altstep.
 * 
 * @author poroszd
 * 
 */
class AltstepNode implements IContentNode, IOpenable {
	private final Def_Altstep a;
	private final AltstepMetric m;

	public AltstepNode(final AltstepMetric metric, final Def_Altstep altstep) {
		m = metric;
		a = altstep;
	}

	@Override
	public Location getLocation() {
		return a.getLocation();
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
		return data.getRisk(m, a);
	}

	@Override
	public double risk(final MetricData data) {
		return data.getRiskValue(m, a);
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return a.getIdentifier().getDisplayName();
		} else if (i == 1) {
			return data.get(m, a).toString();
		}

		return null;
	}
}
