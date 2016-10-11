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


public enum AltstepMetric implements IMetricEnum {
	/**
	 * Count the lines of code the altstep body is made up.
	 */
	LINES_OF_CODE("Lines of code", "Number of lines in the altstep body"),
	/**
	 * Calculates the cyclomatic complexity of the altstep.
	 */
	CYCLOMATIC_COMPLEXITY("Cyclomatic complexity", "Cyclomatic (or McCabe) complexity"),
	/**
	 * Counts the maximal depth of block nesting in the altstep body.
	 */
	NESTING("Nesting", "Maximum depth of nested statements in the altstep body", EnumSet.of(StatColumn.MAX, StatColumn.MEAN, StatColumn.DEV)),
	/**
	 * Counts the branches of the altstep.
	 */
	BRANCHES("Branches", "Number of branches in the altstep body", EnumSet.of(StatColumn.MAX, StatColumn.MEAN, StatColumn.DEV)),
	/**
	 * Count the number of parameters in the altstep definition.
	 */
	NUMBER_OF_PARAMETERS("Number of parameters", "Number of parameters in the altstep declaration", EnumSet.of(StatColumn.MAX, StatColumn.MEAN,
			StatColumn.DEV)),
	/**
	 * Counts the number of references to entities inside the module of this
	 * altstep.
	 */
	IN_ENVY("External feature envy", "Number of references to entities inside the module of this altstep"),
	/**
	 * Counts the number of references to entities outside the module of this
	 * altstep.
	 */
	OUT_ENVY("Internal feature envy", "Number of references to entities outside the module of this altstep");


	public static final String GROUP_NAME = "Altstep";
	
	private String displayName;
	private String hint;
	private Set<StatColumn> requestedStatistics;
	private boolean isInteger;

	private AltstepMetric(final String name, final String hint) {
		this(name, hint, EnumSet.allOf(StatColumn.class), true);
	}

	private AltstepMetric(final String name, final String hint, final Set<StatColumn> requestedStatistics) {
		this(name, hint, requestedStatistics, true);
	}

	private AltstepMetric(final String name, final String hint, final Set<StatColumn> requestedStatistics, final boolean isInteger) {
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
