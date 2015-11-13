/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.titanium.metrics;

import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetricsChecker {

	private static Expectation[] expectations;

	@BeforeClass
	public static void setUpClass() {
		expectations = new Expectation[] {
				new Expectation("AM_Branches", AltstepMetric.BRANCHES)
						.shouldHave("AM__Branches", 2),
				new Expectation("AM_CyclomaticComplexity", AltstepMetric.CYCLOMATIC_COMPLEXITY)
						.shouldHave("AM__CyclomaticComplexity", 6),
				new Expectation("AM_InEnvy", AltstepMetric.IN_ENVY)
						.shouldHave("AM__InEnvy", 2),
				new Expectation("AM_LinesOfCode", AltstepMetric.LINES_OF_CODE)
						.shouldHave("AM__LinesOfCode", 3),
				new Expectation("AM_Nesting", AltstepMetric.NESTING)
						.shouldHave("AM__Nesting", 4),
				new Expectation("AM_NumberOfParams", AltstepMetric.NUMBER_OF_PARAMETERS)
						.shouldHave("AM__NumberOfParams", 1),
				new Expectation("AM_OutEnvy", AltstepMetric.OUT_ENVY)
						.shouldHave("AM__OutEnvy", 4),
				new Expectation("FM_CyclomaticComplexity", FunctionMetric.CYCLOMATIC_COMPLEXITY)
						.shouldHave("FM__CyclomaticComplexity", 4),
				new Expectation("FM_DefaultActivations", FunctionMetric.DEFAULT_ACTIVATIONS)
						.shouldHave("FM__DefaultActivations", 3),
				new Expectation("FM_InEnvy", FunctionMetric.IN_ENVY)
						.shouldHave("FM__InEnvy", 3),
				new Expectation("FM_LinesOfCode", FunctionMetric.LINES_OF_CODE)
						.shouldHave("FM__LinesOfCode", 4),
				new Expectation("FM_Nesting", FunctionMetric.NESTING)
						.shouldHave("FM__Nesting", 4),
				new Expectation("FM_NumberOfParams", FunctionMetric.NUMBER_OF_PARAMETERS)
						.shouldHave("FM__NumberOfParams", 5),
				new Expectation("FM_OutEnvy", FunctionMetric.OUT_ENVY)
						.shouldHave("FM__OutEnvy", 2),
				new Expectation("FM_ReturnPoints", FunctionMetric.RETURN_POINTS)
						.shouldHave("FM__ReturnPoints", 4)
						.shouldHave("FM__ReturnPoints2", 4),
				new Expectation("MM_AfferentCoupling", ModuleMetric.AFFERENT_COUPLING)
						.shouldHave("MM__AfferentCoupling", 4),
				new Expectation("MM_EfferentCoupling", ModuleMetric.EFFERENT_COUPLING)
						.shouldHave("MM__EfferentCoupling", 2),
				new Expectation("MM_InEnvy", ModuleMetric.IN_ENVY)
						.shouldHave("MM__InEnvy", 3),
				new Expectation("MM_Instability", ModuleMetric.INSTABILITY)
						.shouldHave("MM__Instability", 0.5),
				new Expectation("MM_LinesOfCode", ModuleMetric.LINES_OF_CODE)
						.shouldHave("MM__LinesOfCode", 4),
				new Expectation("MM_NofAltsteps", ModuleMetric.NOF_ALTSTEPS)
						.shouldHave("MM__NofAltsteps", 2),
				new Expectation("MM_NofFixme", ModuleMetric.NOF_FIXME)
						.shouldHave("MM__NofFixme", 3),
				new Expectation("MM_NofFunctions", ModuleMetric.NOF_FUNCTIONS)
						.shouldHave("MM__NofFunctions", 3),
				new Expectation("MM_NofImports", ModuleMetric.NOF_IMPORTS)
						.shouldHave("MM__NofImports", 1),
				new Expectation("MM_NofTestcases", ModuleMetric.NOF_TESTCASES)
						.shouldHave("MM__NofTestcases", 2),
				new Expectation("MM_OutEnvy", ModuleMetric.OUT_ENVY)
						.shouldHave("MM__OutEnvy", 3),
				new Expectation("MM_TimesImported", ModuleMetric.TIMES_IMPORTED)
						.shouldHave("MM__TimesImported", 2),
				new Expectation("TM_CyclomaticComplexity", TestcaseMetric.CYCLOMATIC_COMPLEXITY)
						.shouldHave("TM__CyclomaticComplexity", 4),
				new Expectation("TM_InEnvy", TestcaseMetric.IN_ENVY)
						.shouldHave("TM__InEnvy", 3),
				new Expectation("TM_LinesOfCode", TestcaseMetric.LINES_OF_CODE)
						.shouldHave("TM__LinesOfCode", 3),
				new Expectation("TM_Nesting", TestcaseMetric.NESTING)
						.shouldHave("TM__Nesting", 4),
				new Expectation("TM_NumberOfParams", TestcaseMetric.NUMBER_OF_PARAMETERS)
						.shouldHave("TM__NumberOfParams", 3),
				new Expectation("TM_OutEnvy", TestcaseMetric.OUT_ENVY)
						.shouldHave("TM__OutEnvy", 3)
		};
	}

	@Test
	public void runTest() {
		for (Expectation e : expectations) {
			e.runTest();
		}
	}
}
