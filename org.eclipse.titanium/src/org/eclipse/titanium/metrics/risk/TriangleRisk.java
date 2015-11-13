/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.risk;

import org.eclipse.titanium.metrics.IMetricEnum;

public class TriangleRisk extends BaseRisk {
	public TriangleRisk(IMetricEnum metric) {
		super(metric);
	}

	@Override
	public double getRiskValue(final Number value) {
		double risk = 0;
		double l1, l2;
		final double dist = 0.5 - Math.abs(value.doubleValue() - 0.5);
		switch (method) {
		case NEVER:
			// cheat to never reach 1.0
			risk = dist * 1.999;
			break;
		case NO_HIGH:
			l1 = limits[0].doubleValue();
			if (dist >= l1) {
				risk = 2 + (dist - l1) / (0.5 - l1);
			} else {
				risk = dist / l1;
			}
			break;
		case NO_LOW:
			l1 = limits[0].doubleValue();
			if (dist >= l1) {
				// 0.501 instead of 0.5 is again a cheat, like above.
				risk = 1 + (dist - l1) / (0.501 - l1);
			} else {
				risk = dist / l1;
			}
			break;
		case NO_LOW_HIGH:
			l1 = limits[0].doubleValue();
			l2 = limits[1].doubleValue();
			if (dist < l1) {
				risk = dist / l1;
			} else if (dist >= l2) {
				risk = 2 + (dist - l2) / (0.5 - l2);
			} else {
				risk = 1 + (dist - l1) / (l2 - l1);
			}
			break;
		}
		return risk;
	}
}
