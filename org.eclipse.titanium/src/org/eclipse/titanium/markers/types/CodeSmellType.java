/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.types;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titanium.Activator;

/**
 * Types of the code smell markers.
 * 
 * @author poroszd
 * 
 */
public enum CodeSmellType implements ProblemType{
	ALTSTEP_COVERAGE("Altstep coverage", 1.0, 5.0, 76.0),
	CIRCULAR_IMPORTATION("Circular importation", 2.0, 12.0, 80.0),
	CONSECUTIVE_ASSIGNMENTS("Consecutive assignments", 0.0, 1.0, 6.0),
	CONVERT_TO_ENUM("Convert to enumeration", 0.5, 3.0, 8.0),
	EMPTY_STATEMENT_BLOCK("Empty statement block", 0.0 ,2.0, 5.0),
	GOTO("Goto", 1.0, 5.5, 26.0),
	IF_INSTEAD_ALTGUARD("If instead altguard", 1.0, 2.0, 8.0),
	IF_INSTEAD_RECEIVE_TEMPLATE("If instead receive template", 1.0, 2.0, 8.0),
	IF_WITHOUT_ELSE("If without else", 0.5, 1.0, 8.0),
	INCORRECT_SHIFT_ROTATE_SIZE("Incorrect shift or rotation size", 1.0, 2.0, 8.0),
	INFINITE_LOOP("Infinite loop", 0.0, 1.0, 3.5),
	ISBOUND_WITHOUT_ELSE("IsBound without else", 0.5, 1.0, 8.0),
	ISVALUE_WITH_VALUE("IsValue with value", 0.5, 1.5, 5.0),
	ITERATE_ON_WRONG_ARRAY("Iterate on wrong array", 1.0, 5.0, 20.0),
	MAGIC_NUMBERS("Magic numbers", 0.0, 0.5, 3.0),
	MAGIC_STRINGS("Magic strings", 0.0, 0.5, 3.0),
	MISSING_FRIEND("Missing friend module", 0.0, 0.5, 3.5),
	MISSING_IMPORT("Missing import", 0.0, 0.5, 3.5),
	MODULENAME_IN_DEFINITION("Module name in definition", 0.0, 1.0, 3.5),
	LAZY("Lazy formalparameter", 0.5, 0.5, 1.0),
	LOGIC_INVERSION("Logic inversion", 0.0, 0.5, 3.5),
	NONPRIVATE_PRIVATE("Definition should be private", 0.0, 0.5, 4.5),
	PRIVATE_FIELD_VIA_PUBLIC("Private field wrapped into public definition", 1.0, 2.0, 6.5),
	PRIVATE_VALUE_VIA_PUBLIC("Private field wrapped into public definition", 1.0, 2.0, 6.5),
	READING_OUT_PAR_BEFORE_WRITTEN("Reading out parameter before written", 0.0, 0.5, 2.0),
	READONLY_LOC_VARIABLE("Readonly local variable", 0.0, 2.0, 5.0),
	READONLY_OUT_PARAM("Readonly out formal parameter", 0.0, 2.0, 5.0),
	READONLY_INOUT_PARAM("Readonly inout formal parameter", 0.0, 2.0, 5.0),
	RECEIVE_ANY_TEMPLATE("Receive any template", 0.5, 1.0, 6.0),
	SELECT_COVERAGE("Select coverage", 1.0, 5.0, 15.0),
	// numbers copied from select_coverage assuming they are of the same difficulty
	SELECT_WITH_NUMBERS_SORTED("Select with numbers not sorted", 1.0, 5.0, 15.0),
	SETVERDICT_WITHOUT_REASON("Setverdict without reason", 0.5, 1.0, 2.0),
	SHORTHAND("Shorthand statement", 0.5, 5.0, 50.0),
	SIZECHECK_IN_LOOP("Size check in loop", 0.0, 1.0, 5.0),
	STOP_IN_FUNCTION("Stop in function", 0.5, 2.5, 50.0),
	SWITCH_ON_BOOLEAN("Switch on boolean", 0.5, 1.0, 2.0),
	TOO_COMPLEX_EXPRESSIONS("Too complex expression", 1.0, 2.0, 8.0),
	TOO_MANY_PARAMETERS("Too many parameters", 1.0, 3.0, 37.0),
	TOO_MANY_STATEMENTS("Too many statements", 2.0, 6.0, 50.0),
	TYPENAME_IN_DEFINITION("Typename in definition", 0.0, 1.0, 3.5),
	UNCOMMENTED_FUNCTION("Uncommented function", 0.5, 1.0, 3.5),
	UNINITIALIZED_VARIABLE("Uninitialize variable", 0.0, 0.5, 2.0),
	UNNECESSARY_CONTROLS("Unnecessary control",0.5, 1.5, 5.0),
	UNNECESSARY_VALUEOF("Unnecessary valueof operation",0.5, 1.0, 5.0),
	UNUSED_FUNTION_RETURN_VALUES("Unused function return values", 0.0, 0.5, 9.5),
	UNUSED_STARTED_FUNCTION_RETURN_VALUES("Unused started function return values", 0.0, 0.5, 9.5),
	UNUSED_GLOBAL_DEFINITION("Unused global definition", 0.5, 4.5, 18.0),
	UNUSED_IMPORT("Unused import", 0.0, 0.5, 1.0),
	UNUSED_LOCAL_DEFINITION("Unused local definition",0.0, 0.5, 1.5),
	VISIBILITY_IN_DEFINITION("Visibility in definition", 0.0, 0.5, 4.5);

	public static final String MARKER_ID = "org.eclipse.titanium.markers.CodeSmellMarker";
	public static final String PROBLEM = "problem";

	private double defaultMinTime;
	private double defaultAvgTime;
	private double defaultMaxTime;
	private String innerName;
	private String readableName;
	
	private CodeSmellType(final String name, final Double minTime, final Double avgTime, final Double maxTime) {
		this.defaultMinTime = minTime;
		this.defaultAvgTime = avgTime;
		this.defaultMaxTime = maxTime;
		innerName = name();
		readableName = name;
	}

	@Override
	public int getBaseLine() {
		return getInt(ProblemNameToPreferenceMapper.nameSmellBaseLine(innerName), 43);
	}
	
	@Override
	public int getImpact() {
		return getInt(ProblemNameToPreferenceMapper.nameSmellImpact(innerName), 43);
	}
	
	@Override
	public double getAvgRepairTime() {
		return getDouble(ProblemNameToPreferenceMapper.nameSmellAvgTime(innerName), defaultAvgTime);
	}
	
	@Override
	public double getMinRepairTime() {
		return getDouble(ProblemNameToPreferenceMapper.nameSmellMinTime(innerName), defaultMinTime);
	}
	
	@Override
	public double getMaxRepairTime() {
		return getDouble(ProblemNameToPreferenceMapper.nameSmellMaxTime(innerName), defaultMaxTime);
	}
	
	 @Override
	public double getAvgDefaultTime() {
		 return defaultAvgTime;
	 }
	
	 @Override
	public double getMinDefaultTime() {
		 return defaultMinTime;
	 }
	
	 @Override
	public double getMaxDefaultTime() {
		 return defaultMaxTime;
	 }
	
	@Override
	public String getHumanReadableName() {
		return readableName;
	}
	
	@Override
	public String toString() {
		return innerName;
	}
	
	
	private int getInt(final String id, final int defaultValue) {
		final int val = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID, id, -1, null);
		if (val == -1) {
			throw new IllegalArgumentException("The requested field for " + readableName + " is not found in the preference store. "
					+ "Probably you forgot to add it in the PreferenceInitializer or in the RiskFactorPreferencePage.");
		} else {
			return val;
		}
	}
	
	private double getDouble(final String id, final double defaultValue) {
		return Platform.getPreferencesService().getDouble(Activator.PLUGIN_ID, id, defaultValue, null);
	}

}
