/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.risk;

import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;
import org.eclipse.titanium.metrics.preferences.RiskMethod;
import org.eclipse.titanium.metrics.utils.RiskLevel;

public abstract class BaseRisk implements IRisk {
	protected final RiskMethod method;
	protected final Number[] limits;

	public BaseRisk(final IMetricEnum metric) {
		method = PreferenceManager.getRiskMethod(metric, false);
		limits = PreferenceManager.getLimits(metric, false);
	}

	@Override
	public RiskLevel getRiskLevel(final Number value) {
		final double v = getRiskValue(value);
		return risk(v);
	}

	public static RiskLevel risk(final double v) {
		RiskLevel color;
		if (v < 1) {
			color = RiskLevel.NO;
		} else if (v < 2) {
			color = RiskLevel.LOW;
		} else {
			color = RiskLevel.HIGH;
		}

		return color;
	}
}
