/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A grouping of metrics working on the same entity.
 * 
 * @author poroszd
 * 
 */
public enum MetricGroup {
	PROJECT("Project", Arrays.asList(ProjectMetric.values())),
	MODULE("Module", Arrays.asList(ModuleMetric.values())),
	FUNCTION("Function", Arrays.asList(FunctionMetric.values())),
	TESTCASE("Testcase", Arrays.asList(TestcaseMetric.values())),
	ALTSTEP("Altstep", Arrays.asList(AltstepMetric.values()));

	private List<IMetricEnum> metrics;
	private String groupName;

	<T extends IMetricEnum> MetricGroup(String groupName, List<T> metrics) {
		this.metrics = new ArrayList<IMetricEnum>();
		this.metrics.addAll(metrics);
		this.groupName = groupName;
	}

	public List<IMetricEnum> getMetrics() {
		return metrics;
	}

	public String getGroupName() {
		return groupName;
	}

	public static List<IMetricEnum> knownMetrics() {
		List<IMetricEnum> enums = new ArrayList<IMetricEnum>();
		enums.addAll(PROJECT.metrics);
		enums.addAll(MODULE.metrics);
		enums.addAll(FUNCTION.metrics);
		enums.addAll(TESTCASE.metrics);
		enums.addAll(ALTSTEP.metrics);
		return enums;
	}
}
