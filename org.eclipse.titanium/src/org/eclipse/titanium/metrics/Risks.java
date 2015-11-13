/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.titanium.metrics.risk.IRisk;
import org.eclipse.titanium.metrics.risk.LinearRisk;
import org.eclipse.titanium.metrics.risk.TriangleRisk;

/**
 * Encapsulates a mapping from metric enums to {@link IRisk}
 * implementations.
 * 
 * @author poroszd
 * 
 */
public class Risks {
	final Map<IMetricEnum, IRisk> risks;

	/**
	 * Create new <code>IRisk</code> instances for all metrics.
	 */
	public Risks() {
		risks = new HashMap<IMetricEnum, IRisk>();
		for (IMetricEnum metric : MetricGroup.knownMetrics()) {
			risks.put(metric, new LinearRisk(metric));
		}
		risks.put(ModuleMetric.INSTABILITY, new TriangleRisk(ModuleMetric.INSTABILITY));
	}

	/**
	 * Access the risk calculator of a metric
	 */
	public IRisk getRisk(IMetricEnum metric) {
		return risks.get(metric);
	}
}