/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.EnumSet;
import java.util.Set;


public enum ProjectMetric implements IMetricEnum {
	/**
	 * Counts the number of TTCN3 modules in the project.
	 */
	NOF_TTCN3_MODULES("Number of TTCN3 modules", "Number of TTCN3 modules in the project"),
	/**
	 * Counts the number of ASN1 modules in the project.
	 */
	NOF_ASN1_MODULES("Number of ASN1 modules", "Number of ASN1 modules in the project");

	public static final String GROUP_NAME = "Project";

	private String displayName;
	private String hint;
	private Set<StatColumn> requestedStatistics;
	private boolean isInteger;

	private ProjectMetric(final String name, final String hint) {
		this(name, hint, EnumSet.allOf(StatColumn.class), true);
	}

	private ProjectMetric(final String name, final String hint, final Set<StatColumn> requestedStatistics) {
		this(name, hint, requestedStatistics, true);
	}

	private ProjectMetric(final String name, final String hint, final Set<StatColumn> requestedStatistics, final boolean isInteger) {
		this.displayName = name;
		this.hint = hint;
		this.requestedStatistics = requestedStatistics;
		this.isInteger = isInteger;
	}

	@Override
	public String id() {
		return GROUP_NAME + "." + name();
	}

	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public Set<StatColumn> requestedStatistics() {
		return requestedStatistics;
	}

	@Override
	public boolean isInteger() {
		return isInteger;
	}

	@Override
	public String groupName() {
		return GROUP_NAME;
	}
}
