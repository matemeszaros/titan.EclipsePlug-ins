/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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

public class AST_warnings_tests {

	//ASNValues_asn
	//attribute_tests_ttcn
	//expression_tests_ttcn
	//namingConvention_ttcn
	//specificValue_template_tests_ttcn
	//statement_tests_ttcn
	//subtype_tests_ttcn
	//t3doc_explicit_negative_test_ttcn
	//t3doc_explicit_test_ttcn
	//ttcnpp_test_main_ttcnpp
	//template_assignment_tests_ttcn
	//value_assignment_tests_ttcn
	//value_tests_ttcn
	
	@org.junit.Test
	public void ASNValues_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(ASNValues_asn_initializer(), "src/Basic_tests/ASNValues.asn");
	}

	@org.junit.Test
	public void attribute_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(attribute_tests_ttcn_initializer(), "src/Basic_tests/attribute_tests.ttcn");
	}

	@org.junit.Test
	public void expression_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(expression_tests_ttcn_initializer(), "src/Basic_tests/expression_tests.ttcn");
	}

	@org.junit.Test
	public void namingConvention_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(namingConvention_ttcn_initializer(), "src/Basic_tests/namingConvention.ttcn");
	}

	@org.junit.Test
	public void specificValue_template_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(specificValue_template_tests_ttcn_initializer(), "src/Basic_tests/specificValue_template_tests.ttcn");
	}

	@org.junit.Test
	public void statement_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(statement_tests_ttcn_initializer(), "src/Basic_tests/statement_tests.ttcn");
	}

	@org.junit.Test
	public void subtype_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(subtype_tests_ttcn_initializer(), "src/Basic_tests/subtype_tests.ttcn");
	}

	@org.junit.Test
	public void t3doc_explicit_test_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(t3doc_explicit_test_ttcn_initializer(), "src/Basic_tests/t3doc_explicit_test.ttcn");
	}

	@org.junit.Test
	public void t3doc_explicit_negative_test_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(t3doc_explicit_negative_test_ttcn_initializer(), "src/Basic_tests/t3doc_explicit_negative_test.ttcn");
	}

	@org.junit.Test
	public void ttcnpp_test_main_ttcnpp() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(ttcnpp_test_main_ttcnpp_initializer(), "src/Basic_tests/preprocessor_test/ttcnpp_test_main.ttcnpp");
	}

	@org.junit.Test
	public void template_assignment_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(template_assignment_tests_ttcn_initializer(), "src/Basic_tests/template_assignment_tests.ttcn");
	}

	@org.junit.Test
	public void template_specific_test_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(template_specific_test_ttcn_initializer(), "src/Basic_tests/template_specific_test.ttcn");
	}

	@org.junit.Test
	public void value_assignment_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(value_assignment_tests_ttcn_initializer(), "src/Basic_tests/value_assignment_tests.ttcn");
	}

	@org.junit.Test
	public void value_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(value_tests_ttcn_initializer(), "src/Basic_tests/value_tests.ttcn");
	}

	private ArrayList<MarkerToCheck> ASNValues_asn_initializer() {
		//ASNValues.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 65;
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `qw' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `er' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier 99 was expected instead of `ty' for number 3 in the NameAndNumberForm as the third OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> attribute_tests_ttcn_initializer() {
		//attribute_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 39;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 24;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `f' with name f breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 22;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `cs' with name cs breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 14;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `i' with name i breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 31;
		markersToCheck.add(new MarkerToCheck("The `inout' value parameter `pdu' with name pdu breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 44;
		markersToCheck.add(new MarkerToCheck("Duplicate attribute `internal'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Duplicate attribute `address'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Duplicate attribute `provider'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 138;
		markersToCheck.add(new MarkerToCheck("Local variable `v3' has initial value, but the variable inherited from component type `@attribute_tests.original' does not",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Local variable `v4' does not have initial value, but the variable inherited from component type `@attribute_tests.original' has",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Local variable `v5' and the variable inherited from component type `@attribute_tests.original' have different values",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Local template variable `vt4' has initial value, but the template variable inherited from component type `@attribute_tests.original' does not",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Local template variable `vt5' does not have initial value, but the template variable inherited from component type `@attribute_tests.original' has",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Local timer `t7' does not have default duration, but the timer inherited from component type `@attribute_tests.original' has",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Local timer `t8' has default duration, but the timer inherited from component type `@attribute_tests.original' does not",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Local timer `t10' and the timer inherited from component type `@attribute_tests.original' have different default durations",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 88;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 14;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 11;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 45;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not currently supported", lineNum, IMarker.SEVERITY_WARNING));
		}

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> expression_tests_ttcn_initializer() {
		//expression_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 118;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 53;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 30;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 34;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 34;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 34;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 36;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 26;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 26;
		int i = 0;
		for (i = 0; i < 5; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 23;
		for (i = 0; i < 5; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 22;
		for (i = 0; i < 5; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 25;
		for (i = 0; i < 13; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 29;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 36;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 36;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 38;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 2;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("Shifting to the right should be used instead of shifting to the left with a negative value", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("Shifting to the left with 0 will not change the original value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Shifting a 4 long string to the left with 5 will always result in a string of same size, but filled with 0 -s.",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Shifting a 1 long string to the left with 5 will always result in a string of same size, but filled with 0 -s.", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 30;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 2;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("Shifting to the left should be used instead of shifting to the right with a negative value", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("Shifting to the right with 0 will not change the original value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Shifting a 4 long string to the right with 5 will always result in a string of same size, but filled with 0 -s.",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Shifting a 1 long string to the right with 5 will always result in a string of same size, but filled with 0 -s.", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 30;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 2;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating to the right should be used instead of rotating to the left with a negative value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating to the right should be used instead of rotating to the left with a negative value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating to the left with 0 will not change the original value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating to the left with 0 will not change the original value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating a 4 long value to the left with 5 will have the same effect as rotating by 1",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating a 6 long value to the left with 10 will have the same effect as rotating by 4",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Rotating a 2 long value to the left with 10 will have the same effect as rotating by 0",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating a 4 long value to the left with 5 will have the same effect as rotating by 1", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating to the right should be used instead of rotating to the left with a negative value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 23;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 2;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating to the left should be used instead of rotating to the right with a negative value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating to the left should be used instead of rotating to the right with a negative value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating to the right with 0 will not change the original value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating to the right with 0 will not change the original value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating a 4 long value to the right with 5 will have the same effect as rotating by 1",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating will not change the value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating a 6 long value to the right with 10 will have the same effect as rotating by 4",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Rotating a 2 long value to the right with 10 will have the same effect as rotating by 0",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating a 4 long value to the right with 5 will have the same effect as rotating by 1", lineNum++, IMarker.SEVERITY_WARNING)); }
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Rotating to the left should be used instead of rotating to the right with a negative value", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 33;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 50;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 60;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.c11' and `@expression_tests.c22'", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.recR' and `@expression_tests.recofR'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.recofR' and `@expression_tests.recR'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 61;
		for (i = 0; i < 5; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 1;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 4; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 2;
		for (i = 0; i < 12; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrec1' and `@expression_tests.myrec2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrec2' and `@expression_tests.myrec1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrec1' and `@expression_tests.myrec2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrec2' and `@expression_tests.myrec1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrecof1' and `@expression_tests.myrecof2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrecof2' and `@expression_tests.myrecof1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrecof1' and `@expression_tests.myrecof2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myrecof2' and `@expression_tests.myrecof1'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Type compatibility between `integer[1]' and `integer[1]'", lineNum, IMarker.SEVERITY_WARNING)); }
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Type compatibility between `integer[1]' and `integer[1]'", lineNum, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myset1' and `@expression_tests.myset2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myset2' and `@expression_tests.myset1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myset1' and `@expression_tests.myset2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myset2' and `@expression_tests.myset1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.mysetof1' and `@expression_tests.mysetof2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.mysetof2' and `@expression_tests.mysetof1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.mysetof1' and `@expression_tests.mysetof2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.mysetof2' and `@expression_tests.mysetof1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myuni1' and `@expression_tests.myuni2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myuni2' and `@expression_tests.myuni1'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myuni1' and `@expression_tests.myuni2'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Type compatibility between `@expression_tests.myuni2' and `@expression_tests.myuni1'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 110;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 85;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 32;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 32;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 32;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 30;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 29;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 26;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 29;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 3; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 29;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 61;
		for (i = 0; i < 7; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 31;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 31;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 33;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 29;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 27;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 30;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 28;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 53;
		for (i = 0; i < 5; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 57;
		//markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		//markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2573
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));//2578
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2579
		lineNum += 5;
		//markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2584
		lineNum += 4;
		//markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2588
		lineNum += 9;
		//markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));//2597
		lineNum += 5;
		for (i = 0; i < 6; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); } //2602
		lineNum += 2; 
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		//markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2614: TODO: Write TR!!!
		lineNum += 23;
		lineNum += 2;
		//for (i = 0; i < 3; i++) { 
		//TODO: Write TR for 2637-2638!
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); //2639
		//}
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING)); //2640? FIXME: Write TR !
		lineNum += 23;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }//2663-64
		lineNum += 21;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2686
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//2708
		lineNum += 421;
		for (i = 0; i < 18; i++) { markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true", lineNum++, IMarker.SEVERITY_WARNING)); }
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));//3155
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The variable `minus_zero' with name minus_zero breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));//3165
		markersToCheck.add(new MarkerToCheck("The variable `plus_zero' with name plus_zero breaks the naming convention  `vl.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 23;
		markersToCheck.add(new MarkerToCheck("The variable `plus_inf' with name plus_inf breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));//3189
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("The variable `minus_inf' with name minus_inf breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("The variable `NaN' with name NaN breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The constant `minus_zero' with name minus_zero breaks the naming convention  `cl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The constant `plus_zero' with name plus_zero breaks the naming convention  `cl.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The constant `plus_inf' with name plus_inf breaks the naming convention  `cl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The constant `minus_inf' with name minus_inf breaks the naming convention  `cl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The constant `NaN' with name NaN breaks the naming convention  `cl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> namingConvention_ttcn_initializer() {
		//namingConvention.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 18;
		markersToCheck.add(new MarkerToCheck("The constant `@namingConvention.cbad' with name cbad breaks the naming convention  `cg_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The external constant `@namingConvention.e_bad' with name e_bad breaks the naming convention  `ec_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The module parameter `@namingConvention.bad_modulepar' with name bad_modulepar breaks the naming convention  `tsp.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template `@namingConvention.bad_template' with name bad_template breaks the naming convention  `t.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("The constant `@namingConvention.good_CT.bad_const' with name bad_const breaks the naming convention  `c_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The variable `@namingConvention.good_CT.bad_var' with name bad_var breaks the naming convention  `v_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The timer `@namingConvention.good_CT.bad_timer' with name bad_timer breaks the naming convention  `T_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The port `@namingConvention.good_CT.pbad' with name pbad breaks the naming convention  `.*_PT'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The group with name g_bad breaks the naming convention  `[A-Z].*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("The function `@namingConvention.fbad' with name fbad breaks the naming convention  `f_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The external function `@namingConvention.ebad' with name ebad breaks the naming convention  `ef_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The value parameter `badParameter' with name badParameter breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("The altstep `@namingConvention.altstep_bad' with name altstep_bad breaks the naming convention  `as_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("The testcase `@namingConvention.bad_testcase' with name bad_testcase breaks the naming convention  `tc_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The constant `c_bad' with name c_bad breaks the naming convention  `cl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The variable `v_bad' with name v_bad breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The template `bad_localtemplate' with name bad_localtemplate breaks the naming convention  `t.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The template variable `bad_vartemplate' with name bad_vartemplate breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The timer `T_bad' with name T_bad breaks the naming convention  `TL_.*'",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> specificValue_template_tests_ttcn_initializer() {
		//specificValue_template_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 152;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 124;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 134;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 112;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 125;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1025;
		markersToCheck.add(new MarkerToCheck("`*'' in subset. This template will match everything",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("`*'' in superset has no effect during matching",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 246;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@specific_template_tests.ASNSequenceType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2948;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@specific_template_tests.ASNSequenceType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> statement_tests_ttcn_initializer() {
		//statement_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 134;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the final condition evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the final condition evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the final condition evaluates to true",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the final condition evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 14;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective condition(s)",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 127;
		markersToCheck.add(new MarkerToCheck("If the first statement of the [else] branch is a repeat statement, it will result in busy waiting",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		int i = 0;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Control never reaches this branch of alternative because of a previous [else] branch", lineNum++, IMarker.SEVERITY_WARNING)); }
		lineNum += 45;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because of previous effective cases(s)",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 372;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 113;
		markersToCheck.add(new MarkerToCheck("Function `@statement_tests.f_runsonothercomponent' returns a template of type `integer', which cannot be retrieved when the test component terminates",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 39;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 27;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 99;
		markersToCheck.add(new MarkerToCheck("Broadcast communication is not currently supported",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 105;
		markersToCheck.add(new MarkerToCheck("Broadcast communication is not currently supported",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 155;
		markersToCheck.add(new MarkerToCheck("A variable entry for parameter `par1' is already given here",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 54;
		markersToCheck.add(new MarkerToCheck("A variable entry for parameter `par1' is already given here",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 143;
		markersToCheck.add(new MarkerToCheck("The call operation has a timer, but the timeout expection is not cought",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 92;
		markersToCheck.add(new MarkerToCheck("The value returned by @statement_tests.f_functionwith_return is not used",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template returned by @statement_tests.f_functionwith_returntemplate is not used",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The value returned by @statement_tests.ef_f_externalfunctionwith_return is not used",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template returned by @statement_tests.ef_f_externalfunctionwith_returntemplate is not used",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 48;
		markersToCheck.add(new MarkerToCheck("The value returned by function type `@statement_tests.t_function' is not used",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 14;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Control never reaches this statement",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> subtype_tests_ttcn_initializer() {
		//subtype_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 21;
		markersToCheck.add(new MarkerToCheck("The subtype of type `boolean' is a full set, it does not constrain the root type.",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 54;
		markersToCheck.add(new MarkerToCheck("The subtype of type `integer' is a full set, it does not constrain the root type.",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("The subtype of type `integer' is a full set, it does not constrain the root type.",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1040;
		markersToCheck.add(new MarkerToCheck("The subtype of type `float' is a full set, it does not constrain the root type.",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> t3doc_explicit_test_ttcn_initializer() {
		//t3doc_explicit_test.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 25;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> t3doc_explicit_negative_test_ttcn_initializer() {
		//t3doc_explicit_negative_test.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 14;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 14;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 66;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 19;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Comment `@config' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@exception' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@member' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@param' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@priority' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@purpose' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@requirement' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@return' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Comment `@verdict' cannot be used with this type!",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}


	private ArrayList<MarkerToCheck> template_assignment_tests_ttcn_initializer() {
		//template_assignment_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 147;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 125;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 140;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 114;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 129;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1051;
		markersToCheck.add(new MarkerToCheck("`*'' in subset. This template will match everything",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("`*'' in superset has no effect during matching",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 251;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@template_assignment_tests.myrecordType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 115;
		markersToCheck.add(new MarkerToCheck("Length restriction is useless for an array template",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2725;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@ASNTypes.ASNSequenceType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 306;
		markersToCheck.add(new MarkerToCheck("Identifier `itu_r' should not be used as NameForm",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `r_recommendation' should not be used as NumberForm",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `presentation' should not be used as NumberForm",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `itu_t' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `standard' was expected instead of `recommendation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `ccitt' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `standard' was expected instead of `recommendation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `iso' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `standard' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `joint_iso_itu_t' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `presentation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `joint_iso_ccitt' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `presentation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `itu_t' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `iso' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> ttcnpp_test_main_ttcnpp_initializer() {
		//ttcnpp_test_main.ttcnpp
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 15;
		markersToCheck.add(new MarkerToCheck("Preprocessor directive #line is ignored",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Preprocessor directive #line is ignored",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> template_specific_test_ttcn_initializer() {
		//template_specific_test.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 205;
		markersToCheck.add(new MarkerToCheck("The function `@template_specific_test.fsi_charstrings' with name fsi_charstrings breaks the naming convention  `f_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template parameter `vtc' with name vtc breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The function `@template_specific_test.fsi_bitstrings' with name fsi_bitstrings breaks the naming convention  `f_.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template parameter `vtb' with name vtb breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The function `@template_specific_test.fsi_hexstrings' with name fsi_hexstrings breaks the naming convention  `f_.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template parameter `vth' with name vth breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The function `@template_specific_test.fsi_octetstrings' with name fsi_octetstrings breaks the naming convention  `f_.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template parameter `vto' with name vto breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The function `@template_specific_test.fsi_universal_charstrings' with name fsi_universal_charstrings breaks the naming convention  `f_.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The template parameter `vtu' with name vtu breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> value_assignment_tests_ttcn_initializer() {
		//value_assignment_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 148;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 127;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 132;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 110;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 125;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1271;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@assignment_tests.myrecordType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2707;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@ASNTypes.ASNSequenceType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 306;
		markersToCheck.add(new MarkerToCheck("Identifier `itu_r' should not be used as NameForm",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `r_recommendation' should not be used as NumberForm",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `presentation' should not be used as NumberForm",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `itu_t' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `standard' was expected instead of `recommendation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `ccitt' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `standard' was expected instead of `recommendation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `iso' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `standard' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `joint_iso_itu_t' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `presentation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `joint_iso_ccitt' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `presentation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `itu_t' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `iso' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> value_tests_ttcn_initializer() {
		//value_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 198;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 136;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 137;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 115;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 132;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1377;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@value_tests.myrecordType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 929;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 39;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Rotating will not change the value",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 75;
		markersToCheck.add(new MarkerToCheck("Control never reaches this code because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to false",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2213;
		markersToCheck.add(new MarkerToCheck("All elements of value list notation for type `@ASNTypes.ASNSequenceType' are not used symbols (`-')",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 255;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 139;
		markersToCheck.add(new MarkerToCheck("This control is unnecessary because the conditional expression evaluates to true",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 205;
		markersToCheck.add(new MarkerToCheck("Identifier `itu_r' should not be used as NameForm",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `r_recommendation' should not be used as NumberForm",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `presentation' should not be used as NumberForm",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `itu_t' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `standard' was expected instead of `recommendation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `ccitt' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `standard' was expected instead of `recommendation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `iso' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `standard' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `joint_iso_itu_t' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `presentation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `joint_iso_ccitt' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `recommendation' was expected instead of `presentation' for number 0 in the NameAndNumberForm as the second OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Identifier  or `iso' was expected instead of `itu_t' for number 1 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Identifier `itu_t' or `ccitt' was expected instead of `iso' for number 0 in the NameAndNumberForm as the first OBJECT IDENTIFIER component",  ++lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

}
