/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titanium.metrics.AltstepMetric;

/**
 * Specific interface for metrics working on altsteps.
 * 
 * @author poroszd
 * 
 */
abstract class BaseAltstepMetric extends BaseMetric<Def_Altstep, AltstepMetric> {
	BaseAltstepMetric(final AltstepMetric metric) {
		super(metric);
	}
}
