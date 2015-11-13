/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;

public class MMNofImports extends BaseModuleMetric {
	public MMNofImports() {
		super(ModuleMetric.NOF_IMPORTS);
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		return module.getImportedModules().size();
	}
}
