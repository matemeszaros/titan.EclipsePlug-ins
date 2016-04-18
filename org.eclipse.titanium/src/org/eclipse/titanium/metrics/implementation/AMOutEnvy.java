/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.ExternalFeatureEnvyDetector;

public class AMOutEnvy extends BaseAltstepMetric {
	public AMOutEnvy() {
		super(AltstepMetric.OUT_ENVY);
	}

	@Override
	public Number measure(final MetricData data, final Def_Altstep altstep) {
		final Counter count = new Counter(0);
		final Module myModule = altstep.getMyScope().getModuleScope();
		altstep.accept(new ExternalFeatureEnvyDetector(myModule, count));
		return count.val();
	}
}
