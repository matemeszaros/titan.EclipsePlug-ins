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
import org.eclipse.titanium.metrics.visitors.ExternalFeatureEnvyDetector;

public class MMOutEnvy extends BaseModuleMetric {
	public MMOutEnvy() {
		super(ModuleMetric.OUT_ENVY);
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		final Counter externalReferences = new Counter(0);
		final ExternalFeatureEnvyDetector detector = new ExternalFeatureEnvyDetector(module, externalReferences);
		module.accept(detector);
		return externalReferences.val();
	}
}
