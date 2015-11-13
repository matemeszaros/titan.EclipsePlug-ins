/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This enum helps to manage displaying and setting semantic problems on the
 * preference page.
 * <p>
 * Each enum is mapped to one or more {@link CodeSmellType}, and determines
 * their behavior.
 * </p>
 * 
 * @author poroszd
 * 
 */
public enum ProblemTypePreference {
	ALTSTEP_COVERAGE("Report insufficient altstep coverage", EnumSet.of(CodeSmellType.ALTSTEP_COVERAGE)),
	CIRCULAR_IMPORTATION("Report circular module dependencies", EnumSet.of(CodeSmellType.CIRCULAR_IMPORTATION)),
	CONSECUTIVE_ASSIGNMENTS("Report consecutive assignments", EnumSet.of(CodeSmellType.CONSECUTIVE_ASSIGNMENTS)),
	CONVERT_TO_ENUM("Report usage of non-enumeration types in select statements", EnumSet.of(CodeSmellType.CONVERT_TO_ENUM)),
	EMPTY_STATEMENT_BLOCK("Report empty statement blocks", EnumSet.of(CodeSmellType.EMPTY_STATEMENT_BLOCK)),
	GOTO("Report the usage of label and goto statements", EnumSet.of(CodeSmellType.GOTO)),
	IF_INSTEAD_ALTGUARD("Report alt branches that should use alt guards", EnumSet.of(CodeSmellType.IF_INSTEAD_ALTGUARD)),
	IF_INSTEAD_RECEIVE_TEMPLATE("Report alt branches that should use receive template", EnumSet.of(CodeSmellType.IF_INSTEAD_RECEIVE_TEMPLATE)),
	IF_WITHOUT_ELSE("Report conditional statements without else block", EnumSet.of(CodeSmellType.IF_WITHOUT_ELSE)),
	INCORRECT_SHIFT_ROTATE_SIZE("Report too big or too small shift and rotation sizes", EnumSet.of(CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE)),
	INFINITE_LOOP("Report infinite loops", EnumSet.of(CodeSmellType.INFINITE_LOOP)),
	ISBOUND_WITHOUT_ELSE("Report the usage of isbound without else branch", EnumSet.of(CodeSmellType.ISBOUND_WITHOUT_ELSE)),
	ISVALUE_WITH_VALUE("Report the usage of isvalue with a value as parameter", EnumSet.of(CodeSmellType.ISVALUE_WITH_VALUE)),
	ITERATE_ON_WRONG_ARRAY("Report possible iteration on wrong array", EnumSet.of(CodeSmellType.ITERATE_ON_WRONG_ARRAY)),
	MAGIC_CONSTANTS("Report magic constants", EnumSet.of(CodeSmellType.MAGIC_NUMBERS, CodeSmellType.MAGIC_STRINGS)),
	MISSING_FRIEND("Report friend declarations with missing modules", EnumSet.of(CodeSmellType.MISSING_FRIEND)),
	MISSING_IMPORT("Missing imported module", EnumSet.of(CodeSmellType.MISSING_IMPORT)),
	MODULENAME_IN_DEFINITION("Report if the name of the module is mentioned in the name of the definition", EnumSet
			.of(CodeSmellType.MODULENAME_IN_DEFINITION)),
	LAZY("Report proper usage of @lazy modifier", EnumSet.of(CodeSmellType.LAZY)),
	LOGIC_INVERSION("Report unnecessary negations in if statements.", EnumSet.of(CodeSmellType.LOGIC_INVERSION)),
	NONPRIVATE_PRIVATE("Report TTCN-3 definitions that could be private, but are not set so", EnumSet.of(CodeSmellType.NONPRIVATE_PRIVATE)),
	PRIVATE_FIELD_VIA_PUBLIC("Report when use private field via public definition", EnumSet.of(CodeSmellType.PRIVATE_FIELD_VIA_PUBLIC)),
	PRIVATE_VALUE_VIA_PUBLIC("Report when parameterize private field via public definition", EnumSet.of(CodeSmellType.PRIVATE_VALUE_VIA_PUBLIC)),
	READING_OUT_PAR_BEFORE_WRITTEN("Report reading out parameter before assigning a value to it", EnumSet.of(CodeSmellType.READING_OUT_PAR_BEFORE_WRITTEN)),
	READONLY_VARIABLE("Report read only variables", EnumSet.of(CodeSmellType.READONLY_LOC_VARIABLE, CodeSmellType.READONLY_OUT_PARAM,
			CodeSmellType.READONLY_INOUT_PARAM)),
	SELECT_COVERAGE("Report insufficient coverage of select statements", EnumSet.of(CodeSmellType.SELECT_COVERAGE)),
	SELECT_WITH_NUMBERS_SORTED("Report disordered cases of select statements", EnumSet.of(CodeSmellType.SELECT_WITH_NUMBERS_SORTED)),
	SETVERDICT_WITHOUT_REASON("Report setverdict without reason", EnumSet.of(CodeSmellType.SETVERDICT_WITHOUT_REASON)),
	SHORTHAND("Report the usage of shorthand statements", EnumSet.of(CodeSmellType.SHORTHAND)),
	SIZECHECK_IN_LOOP("Report size check in loop condition", EnumSet.of(CodeSmellType.SIZECHECK_IN_LOOP)),
	RECEIVE_ANY_TEMPLATE("Report receive statements accepting any value", EnumSet.of(CodeSmellType.RECEIVE_ANY_TEMPLATE)),
	STOP_IN_FUNCTION("Report stop statement in functions", EnumSet.of(CodeSmellType.STOP_IN_FUNCTION)),
	SWITCH_ON_BOOLEAN("Report switching on boolean value", EnumSet.of(CodeSmellType.SWITCH_ON_BOOLEAN)),
	TOO_COMPLEX_EXPRESSIONS("Report TTCN-3 expressions that are too complex", EnumSet.of(CodeSmellType.TOO_COMPLEX_EXPRESSIONS)),
	TOO_MANY_PARAMETERS("Report TTCN-3 definitions that have too many parameters", EnumSet.of(CodeSmellType.TOO_MANY_PARAMETERS)),
	TOO_MANY_STATEMENTS("Report statement blocks that have too many statements", EnumSet.of(CodeSmellType.TOO_MANY_STATEMENTS)),
	TYPENAME_IN_DEFINITION("Report if the name of the type is mentioned in the name of the definition", EnumSet
			.of(CodeSmellType.TYPENAME_IN_DEFINITION)),
	UNCOMMENTED_FUNCTION("Report uncommented functions", EnumSet.of(CodeSmellType.UNCOMMENTED_FUNCTION)),
	UNINITIALIZED_VARIABLE("Report uninitialized variable", EnumSet.of(CodeSmellType.UNINITIALIZED_VARIABLE)),
	UNNECESSARY_CONTROLS("Report unnecessary controls", EnumSet.of(CodeSmellType.UNNECESSARY_CONTROLS)),
	UNNECESSARY_VALUEOF("Report unnecessary 'valueof' operation", EnumSet.of(CodeSmellType.UNNECESSARY_VALUEOF)),
	UNUSED_FUNTION_RETURN_VALUES("Report unused function return values", EnumSet.of(CodeSmellType.UNUSED_FUNTION_RETURN_VALUES,
			CodeSmellType.UNUSED_STARTED_FUNCTION_RETURN_VALUES)),
	UNUSED_GLOBAL_DEFINITION("Report unused module level definitions", EnumSet.of(CodeSmellType.UNUSED_GLOBAL_DEFINITION)),
	UNUSED_IMPORT("Report unused module importation", EnumSet.of(CodeSmellType.UNUSED_IMPORT)),
	UNUSED_LOCAL_DEFINITION("Report unused local definition", EnumSet.of(CodeSmellType.UNUSED_LOCAL_DEFINITION)),
	VISIBILITY_IN_DEFINITION("Report visibility settings mentioned in the name of definitions", EnumSet.of(CodeSmellType.VISIBILITY_IN_DEFINITION));

	public static final String PREFIX = "CODE_SMELL.";

	private String preferenceName;
	private String description;
	private Set<CodeSmellType> related;

	private ProblemTypePreference(String description, Set<CodeSmellType> related) {
		preferenceName = PREFIX + name();
		this.description = description;
		this.related = related;
	}

	/**
	 * @return the string that is used as an identifier for this problem in the
	 *         preference store.
	 */
	public String getPreferenceName() {
		return preferenceName;
	}

	/**
	 * @return the string that should be displayed on the preference page
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return The (Enum)Set of the {@link CodeSmellType}s that are affected by
	 *         the state of this preference.
	 */
	public Set<CodeSmellType> getRelatedProblems() {
		return related;
	}
}
