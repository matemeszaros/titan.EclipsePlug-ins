/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.risk;

import org.eclipse.titanium.metrics.IMetricEnum;

public class LinearRisk extends BaseRisk {
	public LinearRisk(IMetricEnum metric) {
		super(metric);
	}

	@Override
	public double getRiskValue(final Number value) {
		double risk = 0;
		double l1, l2;
		double val = value == null ? 0.0 : value.doubleValue();
		switch (method) {
		case NEVER:
			risk = 1 - (1 / (val + 1));
			break;
		case NO_HIGH:
			l1 = limits[0].doubleValue();
			if (val >= l1) {
				risk = 3 - (1 / ((val - l1) + 1));
			} else {
				risk = val / l1;
			}
			break;
		case NO_LOW:
			l1 = limits[0].doubleValue();
			if (val >= l1) {
				risk = 2 - (1 / ((val - l1) + 1));
			} else {
				risk = val / l1;
			}
			break;
		case NO_LOW_HIGH:
			l1 = limits[0].doubleValue();
			l2 = limits[1].doubleValue();
			if (val < l1) {
				risk = val / l1;
			} else if (val >= l2) {
				risk = 3 - (1 / (val - l2 + 1));
			} else {
				risk = 1 + (val - l1) / (l2 - l1);
			}
			break;
		}
		return risk;
	}
}
