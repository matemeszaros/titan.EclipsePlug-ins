/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.titanium.markers;

import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.junit.BeforeClass;
import org.junit.Test;

public class CodeSmellChecker {

	static Expectation[] expectations;

	@BeforeClass
	public static void setUpClass() {
		expectations = new Expectation[] {
				new Expectation("altstep coverage", CodeSmellType.ALTSTEP_COVERAGE)
						.shouldHave("altsteps", new Integer[] { 6 }),
				new Expectation("circular importation", CodeSmellType.CIRCULAR_IMPORTATION)
						.shouldHave("import__cycle", new Integer[] {})
						.shouldHave("import__jang", new Integer[] { 2 })
						.shouldHave("import__jin", new Integer[] { 2, 3 }),
				new Expectation("empty statement block", CodeSmellType.EMPTY_STATEMENT_BLOCK)
						.shouldHave("block", new Integer[] { 10 }),
				new Expectation("goto", CodeSmellType.GOTO)
						.shouldHave("say__no__to__goto", new Integer[] { 7 }),
				new Expectation("if instread altguard", CodeSmellType.IF_INSTEAD_ALTGUARD)
						.shouldHave("altguard", new Integer[] { 7, 12, 23 }),
				new Expectation("if instead receive template", CodeSmellType.IF_INSTEAD_RECEIVE_TEMPLATE)
						.shouldHave("receive__template", new Integer[] { 17 }),
				new Expectation("if without else", CodeSmellType.IF_WITHOUT_ELSE)
						.shouldHave("ifelse", new Integer[] { 10 }),
				new Expectation("incorrect shift rotate size", CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE)
						.shouldHave("shiftrotate", new Integer[] { 23, 25, 27, 29, 34, 36, 38, 40, 45, 47, 49, 51, 56, 58, 60, 62,
								66, 67, 69, 70, 71, 72, 74, 75, 76, 77, 79, 80, 82, 83 }),
				new Expectation("infinite loop", CodeSmellType.INFINITE_LOOP)
						.shouldHave("loop", new Integer[] { 6, 7, 8 }),
				new Expectation("logic inversion", CodeSmellType.LOGIC_INVERSION)
						.shouldHave("logic", new Integer[] { 4 }),
				new Expectation("magic number", CodeSmellType.MAGIC_NUMBERS)
						.shouldHave("magic__number", new Integer[] { 4, 5, 5, 5 }),
				new Expectation("magic string", CodeSmellType.MAGIC_STRINGS)
						.shouldHave("magic__string", new Integer[] { 4, 5, 6, 7 }),
				new Expectation("missing friend", CodeSmellType.MISSING_FRIEND)
						.shouldHave("guy", new Integer[] { 3 }),
				new Expectation("missing import", CodeSmellType.MISSING_IMPORT)
						.shouldHave("importer", new Integer[] { 3 }),
				new Expectation("modulename in definition", CodeSmellType.MODULENAME_IN_DEFINITION)
						.shouldHave("modulename", new Integer[] { 4 }),
				new Expectation("nonprivate private", CodeSmellType.NONPRIVATE_PRIVATE)
						.shouldHave("nonprivate", new Integer[] { 3, 6, 7, 8, 9, 10, 11 }),
				new Expectation("readonly in out", CodeSmellType.READONLY_INOUT_PARAM)
						.shouldHave("ro__inout", new Integer[] { 3, 8 }),
				new Expectation("readonly loc variable", CodeSmellType.READONLY_LOC_VARIABLE)
						.shouldHave("ro__loc", new Integer[] { 4 }),
				new Expectation("readonly out param", CodeSmellType.READONLY_OUT_PARAM)
						.shouldHave("ro__out", new Integer[] { 4, 9 }),
				new Expectation("receive any template", CodeSmellType.RECEIVE_ANY_TEMPLATE)
						.shouldHave("any__template", new Integer[] { 7, 9 }),
				new Expectation("setverdict without reason", CodeSmellType.SETVERDICT_WITHOUT_REASON)
						.shouldHave("verdict", new Integer[] { 7 }),
				new Expectation("sizecheck in loop", CodeSmellType.SIZECHECK_IN_LOOP)
						.shouldHave("sizecheck", new Integer[] { 11, 12, 13, 15, 16, 17 }),
				new Expectation("stop in function", CodeSmellType.STOP_IN_FUNCTION)
						.shouldHave("stop__func", new Integer[] { 6 }),
				new Expectation("switch on boolean", CodeSmellType.SWITCH_ON_BOOLEAN)
						.shouldHave("switch__bool", new Integer[] { 4 }),
				new Expectation("too complex expression", CodeSmellType.TOO_COMPLEX_EXPRESSIONS) {
					@Override
					public void setUp() { // we should also set the expression complexity limit for this test
						super.setUp();
						org.eclipse.titanium.Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.TOO_COMPLEX_EXPRESSIONS_SIZE, 10);
					}
				}.shouldHave("complex__expr", new Integer[] { 5, 6, 7, 8, 9 }),
				new Expectation("too many parameters", CodeSmellType.TOO_MANY_PARAMETERS) {
					@Override
					public void setUp() { // we should also set the parameter limit for this test
						super.setUp();
						org.eclipse.titanium.Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.TOO_MANY_PARAMETERS_SIZE, 5);
					}
				}.shouldHave("many__param", new Integer[] { 5 }),
				new Expectation("too many statements", CodeSmellType.TOO_MANY_STATEMENTS) {
					@Override
					public void setUp() { // we should also set the statement size limit for this test
						super.setUp();
						org.eclipse.titanium.Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.TOO_MANY_STATEMENTS_SIZE, 300);
					}
				}.shouldHave("many__statements", new Integer[] { 6 }),
				new Expectation("typename in definition", CodeSmellType.TYPENAME_IN_DEFINITION)
						.shouldHave("type__in__def", new Integer[] { 4, 5, 6, 7 }),
				new Expectation("uncommented function", CodeSmellType.UNCOMMENTED_FUNCTION)
						.shouldHave("no__comment", new Integer[] { 7, 9, 11 }),
				new Expectation("uninitialized variable", CodeSmellType.UNINITIALIZED_VARIABLE)
						.shouldHave("init__var", new Integer[] { 4, 5 }),
				new Expectation("unnecessary control", CodeSmellType.UNNECESSARY_CONTROLS)
						.shouldHave("overcomp", new Integer[] { 8, 11, 14, 18, 21, 22, 26, 26, 33 }), // TODO: why is this duplicated in line 26?
				new Expectation("unnecessary valueof", CodeSmellType.UNNECESSARY_VALUEOF)
						.shouldHave("omit__valueof", new Integer[] { 9, 10 }),
				new Expectation("unused function return values", CodeSmellType.UNUSED_FUNTION_RETURN_VALUES)
						.shouldHave("omit__retval", new Integer[] { 7 }),
				new Expectation("unused global definition", CodeSmellType.UNUSED_GLOBAL_DEFINITION)
						.shouldHave("unused__global", new Integer[] { 4, 10 }),
				new Expectation("unused import", CodeSmellType.UNUSED_IMPORT)
						.shouldHave("unused__import", new Integer[] { 4 }),
				new Expectation("unused local definition", CodeSmellType.UNUSED_LOCAL_DEFINITION)
						.shouldHave("unused__local", new Integer[] { 5 }),
				new Expectation("unused started function return values", CodeSmellType.UNUSED_STARTED_FUNCTION_RETURN_VALUES)
						.shouldHave("omit__started__retval", new Integer[] { 10 }),
				new Expectation("visibility in definition", CodeSmellType.VISIBILITY_IN_DEFINITION)
						.shouldHave("visibility", new Integer[] { 4, 5, 6 })
		};
	}

	@Test
	public void runTest() {

		for (Expectation e : expectations) {
			e.runTest();
		}
	}

}