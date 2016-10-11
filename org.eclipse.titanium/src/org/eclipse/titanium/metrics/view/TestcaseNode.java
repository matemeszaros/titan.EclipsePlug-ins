/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node in the tree corresponding to a testcase.
 * 
 * @author poroszd
 * 
 */
class TestcaseNode implements IContentNode, IOpenable {
	private final Def_Testcase t;
	private final TestcaseMetric m;

	public TestcaseNode(final TestcaseMetric metric, final Def_Testcase testcase) {
		m = metric;
		t = testcase;
	}

	@Override
	public Location getLocation() {
		return t.getLocation();
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
		return data.getRisk(m, t);
	}

	@Override
	public double risk(final MetricData data) {
		return data.getRiskValue(m, t);
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return t.getIdentifier().getDisplayName();
		} else if (i == 1) {
			return data.get(m, t).toString();
		}

		return null;
	}
}
