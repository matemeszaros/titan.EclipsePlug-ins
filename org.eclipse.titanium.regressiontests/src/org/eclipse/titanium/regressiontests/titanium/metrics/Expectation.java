/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.titanium.metrics;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.utils.ModuleMetricsWrapper;
import org.eclipse.titanium.metrics.utils.WrapperStore;
import org.eclipse.titanium.regressiontests.CustomConfigurable;
import org.eclipse.titanium.regressiontests.library.WorkspaceHandlingLibrary;

class Expectation {
	ModuleMetricsWrapper metricProvider;
	IMetricEnum type;
	Map<String, Number> expectedMetrics;

	public Expectation(String testcaseName, IMetricEnum metric) {
		type = metric;
		expectedMetrics = new HashMap<String, Number>();
	}

	public Expectation shouldHave(String moduleName, Number value) {
		expectedMetrics.put(moduleName, value);
		return this;
	}

	public void runTest() {
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(CustomConfigurable.PROJECT_TO_USE);
		metricProvider = WrapperStore.getWrapper(project);

		for (String modName : expectedMetrics.keySet()) {
			Number val = metricProvider.getValue(type, modName);
			Number expVal = expectedMetrics.get(modName);
			if (val == null) {
				fail("In the module " + modName + " these is nothing measurable for metric " + type);
			} else if (Math.abs(expVal.floatValue() - val.floatValue()) > 0.01) {
				fail("We expected that " + type + " in " + modName + " is " + expVal +
						", but it is actually " + val + " according to the metric.");
			}
		}
	}
}