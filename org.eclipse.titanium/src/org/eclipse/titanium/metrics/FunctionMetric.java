/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.EnumSet;
import java.util.Set;


public enum FunctionMetric implements IMetricEnum {
	/**
	 * Counts the number of parameters of a function definition.
	 */
	NUMBER_OF_PARAMETERS("Number of parameters", "Number of parameters in the function declaration", EnumSet.of(StatColumn.MAX, StatColumn.MEAN,
			StatColumn.DEV)),
	/**
	 * Counts the lines of code the function body is made up.
	 */
	LINES_OF_CODE("Lines of code", "Number of lines of the function body"),
	/**
	 * Calculates the cyclomatic complexity of the function.
	 */
	CYCLOMATIC_COMPLEXITY("Cyclomatic complexity", "Cyclomatic (or McCabe) complexity of the function"),
	/**
	 * Counts the maximal depth of nesting in the function body.
	 */
	NESTING("Nesting", "Maximal number of nested blocks in the function", EnumSet.of(StatColumn.MAX, StatColumn.MEAN, StatColumn.DEV)),
	/**
	 * Counts the number of return points in the function body.
	 */
	RETURN_POINTS("Return points", "Number of return statements (not the exit points in the CFG)", EnumSet.of(StatColumn.MAX, StatColumn.MEAN,
			StatColumn.DEV)),
	/**
	 * Counts the number of default branches that can be activated during the
	 * execution of the function.
	 */
	DEFAULT_ACTIVATIONS("Default activations", "Number of possible default branch activations in the function"),
	/**
	 * Counts the number of references to entities inside the module of this
	 * function.
	 */
	IN_ENVY("External feature envy", "Number of references to entities inside the module of this function"),
	/**
	 * Counts the number of references to entities outside the module of this
	 * function.
	 */
	OUT_ENVY("Internal feature envy", "Number of references to entities outside the module of this function");

	public static final String GROUP_NAME = "Function";

	private String displayName;
	private String hint;
	private Set<StatColumn> requestedStatistics;
	private boolean isInteger;

	private FunctionMetric(final String name, final String hint) {
		this(name, hint, EnumSet.allOf(StatColumn.class), true);
	}

	private FunctionMetric(final String name, final String hint, final Set<StatColumn> requestedStatistics) {
		this(name, hint, requestedStatistics, true);
	}

	private FunctionMetric(final String name, final String hint, final Set<StatColumn> requestedStatistics, final boolean isInteger) {
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
