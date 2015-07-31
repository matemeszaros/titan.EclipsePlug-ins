/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.MetricData;

public class AMNumberOfParams extends BaseAltstepMetric {
	public AMNumberOfParams() {
		super(AltstepMetric.NUMBER_OF_PARAMETERS);
	}

	@Override
	public Number measure(final MetricData data, final Def_Altstep altstep) {
		return altstep.getFormalParameterList().getNofParameters();
	}
}
