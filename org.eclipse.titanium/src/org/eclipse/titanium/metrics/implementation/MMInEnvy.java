/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.InternalFeatureEnvyDetector;

public class MMInEnvy extends BaseModuleMetric {
	public MMInEnvy() {
		super(ModuleMetric.IN_ENVY);
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		final Counter innerReferences = new Counter(0);
		final InternalFeatureEnvyDetector detector = new InternalFeatureEnvyDetector(module, innerReferences);
		module.accept(detector);
		return innerReferences.val();
	}
}
