/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ASN1.definitions.ASN1Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ProjectMetric;

public class PMNofASN1Modules extends BaseProjectMetric {
	public PMNofASN1Modules() {
		super(ProjectMetric.NOF_ASN1_MODULES);
	}

	@Override
	public Number measure(final MetricData data, IProject p) {
		int c = 0;
		for (Module m : data.getModules()) {
			if (m instanceof ASN1Module) {
				++c;
			}
		}
		return c;
	}
}
