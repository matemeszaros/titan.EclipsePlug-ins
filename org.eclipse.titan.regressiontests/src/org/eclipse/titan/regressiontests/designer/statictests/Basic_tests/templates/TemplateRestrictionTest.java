/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests.templates;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;
//import org.junit.Ignore;

//@Ignore("The tested feature is not ready yet")
public class TemplateRestrictionTest {
	//templateRestrictionTest_ttcn
	@Test
	public void templateRestrictionTest_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(templateRestrictionTest_ttcn_initializer_errors(), "src/Basic_tests/templates/templateRestrictionTest.ttcn");
		Designer_plugin_tests.checkSemanticMarkersOnFile(templateRestrictionTest_ttcn_initializer_warnings(), "src/Basic_tests/templates/templateRestrictionTest.ttcn");
	}

	private ArrayList<MarkerToCheck> templateRestrictionTest_ttcn_initializer_errors() {
		//templateRestrictionTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(8);
		int lineNum = 29;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Formal parameter with template restriction `present' not allowed here", lineNum, IMarker.SEVERITY_ERROR));
		}
		lineNum += 22;
		markersToCheck.add(new MarkerToCheck("Restriction 'value' on reference template does not allow usage of this template",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Restriction 'value' on template parameter does not allow usage of this template",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Restriction 'value' on template parameter does not allow usage of this template", lineNum++, IMarker.SEVERITY_ERROR));
		}
		markersToCheck.add(new MarkerToCheck("Restriction 'value' on template parameter does not allow usage of this template",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Restriction 'present' on template variable does not allow usage of `ifpresent'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}
	
	private ArrayList<MarkerToCheck> templateRestrictionTest_ttcn_initializer_warnings() {
		//templateRestrictionTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 51;
		markersToCheck.add(new MarkerToCheck("Inadequate restriction on the referenced template `t_f', this may cause a dynamic test case error at runtime",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
