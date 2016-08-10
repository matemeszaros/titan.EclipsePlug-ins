/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;
//import org.junit.Ignore;

//@Ignore("The tested feature is not ready yet")
public class ExpectedValueTypeTest {

	public ExpectedValueTypeTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void ExpectedValueTypeTest_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(ExpectedValueTypeTest_ttcn_initializer(), "src/Basic_tests/ExpectedValueTypeTest.ttcn");
	}

	//ExpectedValueTypeTest_ttcn

	private ArrayList<MarkerToCheck> ExpectedValueTypeTest_ttcn_initializer() {
		//ExpectedValueTypeTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(26);
		int lineNum = 14;
		markersToCheck.add(new MarkerToCheck("sequence value was expected for type `@ExpectedValueTypeTest.Rec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 21;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template module parameter `@ExpectedValueTypeTest.tsp_i'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Formal parameter without template restriction not allowed here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template module parameter `@ExpectedValueTypeTest.tsp_trec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template `@ExpectedValueTypeTest.t_rec1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template `@ExpectedValueTypeTest.t_rec2'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Type mismatch: a value or template of type `@ExpectedValueTypeTest.Rec' was expected instead of `integer'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Type mismatch: a value or template of type `charstring' was expected instead of `integer'", lineNum++, IMarker.SEVERITY_ERROR));
		}
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template parameter `tpl_rec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template parameter `tpl_rec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of `out' template parameter `tpl_rec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of `inout' template parameter `tpl_rec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template variable `vt_rec_'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template `vt_rec'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The second operand of the `&' operation should be a string, `record of', or a `set of' value",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template variable `vt_cs'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template parameter `tpl_rec'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template module parameter `@ExpectedValueTypeTest.tsp_trec'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template parameter `tpl_recin'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of `inout' template parameter `tpl_recinout'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of `out' template parameter `tpl_recout'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of a call of function `@ExpectedValueTypeTest.frt', which return a template",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template variable `vl_rec9'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 17;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template module parameter `@ExpectedValueTypeTest.tsp_i'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 19;
		markersToCheck.add(new MarkerToCheck("Reference to a template variable or template parameter was expected for an `out' template parameter instead of template `vlt_trec3'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}


}
