/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ProjectMetric;

public class PMNofTTCN3Modules extends BaseProjectMetric {
	public PMNofTTCN3Modules() {
		super(ProjectMetric.NOF_TTCN3_MODULES);
	}

	@Override
	public Number measure(final MetricData data, final IProject p) {
		int count = 0;
		for (final Module module : data.getModules()) {
			if (module instanceof TTCN3Module) {
				++count;
			}
		}
		return count;
	}
}
