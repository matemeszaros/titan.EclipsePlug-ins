/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;

public class MMTimesImported extends BaseModuleMetric {
	final Map<Module, Integer> imported = new HashMap<Module, Integer>();

	public MMTimesImported() {
		super(ModuleMetric.TIMES_IMPORTED);
	}

	@Override
	public void init(MetricData data) {
		imported.clear();
		for (Module module : data.getModules()) {
			imported.put(module, 0);
		}
		for (Module module : data.getModules()) {
			for (final Module imp : module.getImportedModules()) {
				Integer count = imported.get(imp);
				if (count != null) {
					imported.put(imp, count + 1);
				}
			}
		}
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		return imported.get(module);
	}
}
