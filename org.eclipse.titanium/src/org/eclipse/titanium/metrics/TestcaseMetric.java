/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.EnumSet;
import java.util.Set;


public enum TestcaseMetric implements IMetricEnum {
	/**
	 * Counts the lines of code the testcase body is made up.
	 */
	LINES_OF_CODE("Lines of code", "Number of lines in the testcase body"),
	/**
	 * Calculates the cyclomatic complexity of the testcase.
	 */
	CYCLOMATIC_COMPLEXITY("Cyclomatic complexity", "Cyclomatic (or McCabe) complexity"),
	/**
	 * Counts the maximal depth of nesting in the testcase body.
	 */
	NESTING("Nesting", "Maximal number of nested blocks in a testcase"),
	/**
	 * Counts the number of parameters of a testcase definition.
	 */
	NUMBER_OF_PARAMETERS("Number of parameters", "Number of parameters in the testcase declaration", EnumSet.of(StatColumn.MAX, StatColumn.MEAN,
			StatColumn.DEV)),
	/**
	 * Counts the number of references to entities inside the module of this
	 * testcase.
	 */
	IN_ENVY("External feature envy", "Number of references to entities inside the module of this testcase"),
	/**
	 * Counts the number of references to entities outside the module of this
	 * testcase.
	 */
	OUT_ENVY("Internal feature envy", "Number of references to entities outside the module of this testcase");

	public static final String GROUP_NAME = "Testcase";

	private String displayName;
	private String hint;
	private Set<StatColumn> requestedStatistics;
	private boolean isInteger;

	private TestcaseMetric(String name, String hint) {
		this(name, hint, EnumSet.allOf(StatColumn.class), true);
	}

	private TestcaseMetric(String name, String hint, Set<StatColumn> requestedStatistics) {
		this(name, hint, requestedStatistics, true);
	}

	private TestcaseMetric(String name, String hint, Set<StatColumn> requestedStatistics, boolean isInteger) {
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
