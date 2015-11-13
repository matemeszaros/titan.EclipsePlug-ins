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


public enum ModuleMetric implements IMetricEnum {

	/** Count the number of statements in the module.*/
	NOF_STATEMENTS("Number of statements", "Number of statements in the module"),
	/**
	 * Count the number of function definitions in the module.
	 */
	NOF_FUNCTIONS("Number of functions", "Number of functions in the module"),
	/**
	 * Counts the number of testcase definitions in the module.
	 */
	NOF_TESTCASES("Number of testcases", "Number of testcases in the module"),
	/**
	 * Counts the altstep definitions in the module.
	 */
	NOF_ALTSTEPS("Number of altsteps", "Number of altsteps in a module"),
	/**
	 * Counts the number of references to entities inside the module.
	 */
	IN_ENVY("Internal feature envy", "Number of references to entities inside this module"),
	/**
	 * Counts the number of references to entities outside the module.
	 */
	OUT_ENVY("External feature envy", "Number of references to entities outside this module"),
	/**
	 * Counts the fixme comments in the module.
	 */
	NOF_FIXME("Fixme comments", "Number of fixme comments"),
	/**
	 * Counts how many times this module is imported by other modules.
	 */
	TIMES_IMPORTED("Imported", "The times this module is imported by other modules"),
	/**
	 * Counts the import statements in the module.
	 */
	NOF_IMPORTS("Imports", "Number of module importations"),
	/**
	 * Counts the referred assignments that are defined outside this module.
	 */
	EFFERENT_COUPLING("Efferent coupling", "Number of referred assignments that are defined outside this module"),
	/**
	 * Counts the assignments defined in this module, that are referred by
	 * outside modules.
	 */
	AFFERENT_COUPLING("Afferent coupling", "Number of assignments in this module that are referred by outside module"),
	/**
	 * The ratio of the efferent and afferent coupling.
	 */
	INSTABILITY("Instability", "Practically the efferent : (afferent plus efferent) coupling ratio", EnumSet.of(StatColumn.MEAN, StatColumn.DEV),
			false),
	/**
	 * Counts lines of code in the module.
	 */
	LINES_OF_CODE("Lines of code", "Number of lines in the altstep body");

	public static final String GROUP_NAME = "Module";

	private String displayName;
	private String hint;
	private Set<StatColumn> requestedStatistics;
	private boolean isInteger;

	private ModuleMetric(String name, String hint) {
		this(name, hint, EnumSet.allOf(StatColumn.class), true);
	}

	private ModuleMetric(String name, String hint, Set<StatColumn> requestedStatistics) {
		this(name, hint, requestedStatistics, true);
	}

	private ModuleMetric(String name, String hint, Set<StatColumn> requestedStatistics, boolean isInteger) {
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
