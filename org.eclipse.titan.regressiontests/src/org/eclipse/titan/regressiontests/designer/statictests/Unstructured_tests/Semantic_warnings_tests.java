/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Unstructured_tests;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;

public class Semantic_warnings_tests {
	
	//SemanticErrors1_asn
	//SemanticErrors2_asn
	//Semantic_errors_ttcn
	//Semantic_errors3_ttcn
	//Semantic_errors4_ttcn
	//Syntax_warnings_ttcn
	//Syntax_warnings2_ttcn

	@Test
	public void SemanticErrors1_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(SemanticErrors1_asn_initializer(), "src/Unstructured_tests/SemanticErrors1.asn");
	}

	@Test
	public void SemanticErrors2_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(SemanticErrors2_asn_initializer(), "src/Unstructured_tests/SemanticErrors2.asn");
	}

	@Test
	public void Semantic_errors_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Semantic_errors_ttcn_initializer(), "src/Unstructured_tests/Semantic_errors.ttcn");
	}

	@Test
	public void Semantic_errors3_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Semantic_errors3_ttcn_initializer(), "src/Unstructured_tests/Semantic_errors3.ttcn");
	}

	@Test
	public void Semantic_errors4_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Semantic_errors4_ttcn_initializer(), "src/Unstructured_tests/Semantic_errors4.ttcn");
	}

	@Test
	public void Syntax_warnings_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Syntax_warnings_ttcn_initializer(), "src/Unstructured_tests/Syntax_warnings.ttcn");
	}

	@Test
	public void Syntax_warnings2_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Syntax_warnings2_ttcn_initializer(), "src/Unstructured_tests/Syntax_warnings2.ttcn");
	}
	
	
	private ArrayList<MarkerToCheck> SemanticErrors1_asn_initializer() {
		//SemanticErrors1.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 8;
		int i = 0;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Possibly unused importation", lineNum++, IMarker.SEVERITY_WARNING)); }

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> SemanticErrors2_asn_initializer() {
		//SemanticErrors2.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 3;
		int i = 0;
		for (i = 0; i < 5; i++) { markersToCheck.add(new MarkerToCheck("Possibly unused importation", lineNum++, IMarker.SEVERITY_WARNING)); }

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Semantic_errors_ttcn_initializer() {
		//Semantic_errors.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(21);
		int lineNum = 11;
		markersToCheck.add(new MarkerToCheck("Possibly unused importation",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Circular import chain is not recommended: Semantic_errors -> Semantic_errors3 -> Semantic_errors",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Possibly unused importation",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Possibly unused importation", lineNum++, IMarker.SEVERITY_WARNING));
		}
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Possibly unused importation",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 20;
		markersToCheck.add(new MarkerToCheck("The value parameter `b' with name b breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The value parameter `h' with name h breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("Definition `cx1' inherited from component type `@Semantic_errors.mycomp1' is here",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `@Semantic_errors.mycomp1.cx1' with name cx1 breaks the naming convention  `v_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Inherited definition with name `Semantic_errors3' hides a module identifier",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `@Semantic_errors.mycomp1.Semantic_errors3' with name Semantic_errors3 breaks the naming convention  `v_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("The function `@Semantic_errors.myfunc1' with name myfunc1 breaks the naming convention  `f_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("The value parameter `par' with name par breaks the naming convention  `pl_.*'", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("Definition with name `Syntax_warnings' hides a module identifier",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `Syntax_warnings' with name Syntax_warnings breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("The variable `x' with name x breaks the naming convention  `vl.*'", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Definition with name `nonexiModule' hides a module identifier",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `nonexiModule' with name nonexiModule breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Semantic_errors3_ttcn_initializer() {
		//Semantic_errors3.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(22);
		int lineNum = 10;
		markersToCheck.add(new MarkerToCheck("Circular import chain is not recommended: Semantic_errors3 -> Semantic_errors -> Semantic_errors3",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 16;
		markersToCheck.add(new MarkerToCheck("Definition `cx1' inherited from component type `@Semantic_errors3.mycomp2' is here",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `@Semantic_errors3.mycomp2.cx1' with name cx1 breaks the naming convention  `v_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Definition `xc1' inherited from component type `@Semantic_errors3.mycomp4' is here",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `@Semantic_errors3.mycomp4.xc1' with name xc1 breaks the naming convention  `v_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Definition `xc1' inherited from component type `@Semantic_errors3.mycomp5' is here",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `@Semantic_errors3.mycomp5.xc1' with name xc1 breaks the naming convention  `v_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The port `@Semantic_errors3.mycomp7.myport' with name myport breaks the naming convention  `.*_PT'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 16;
		markersToCheck.add(new MarkerToCheck("The `out' value parameter `cool' with name cool breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The function `@Semantic_errors3.myfunc' with name myfunc breaks the naming convention  `f_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The value parameter `param1' with name param1 breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The variable `cc' with name cc breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `mytype' with name mytype breaks the naming convention  `vl.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `myInteger' with name myInteger breaks the naming convention  `vl.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `hehe' with name hehe breaks the naming convention  `vl.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `partt' with name partt breaks the naming convention  `vl.*'",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The variable `ehhh' with name ehhh breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The variable `huhhhuhuh' with name huhhhuhuh breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("The variable `iter' with name iter breaks the naming convention  `vl.*'", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The testcase `@Semantic_errors3.myTC1' with name myTC1 breaks the naming convention  `tc_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The testcase `@Semantic_errors3.myTC2' with name myTC2 breaks the naming convention  `tc_.*'",  ++lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Semantic_errors4_ttcn_initializer() {
		//Semantic_errors4.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(9);
		int lineNum = 10;
		markersToCheck.add(new MarkerToCheck("Type parameterization is not yet supported",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Reference to multiple definitions in attribute qualifiers is not yet supported",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The function `@Semantic_errors2.myf' with name myf breaks the naming convention  `f_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The value parameter `z' with name z breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Reference to parameterized type is not yet supported ",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The variable `akarmi' with name akarmi breaks the naming convention  `vl.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Broadcast communication is not yet supported",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Disconnect operation on multiple connections is not yet supported",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Unmap operation on multiple mappings is not yet supported",  ++lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Syntax_warnings_ttcn_initializer() {
		//Syntax_warnings.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(2);
		int lineNum = 13;
		markersToCheck.add(new MarkerToCheck("The function `@Syntax_warnings.myf' with name myf breaks the naming convention  `f_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("The value parameter `z' with name z breaks the naming convention  `pl_.*'",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Syntax_warnings2_ttcn_initializer() {
		//Syntax_warnings2.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 10;
		markersToCheck.add(new MarkerToCheck("Possibly unused importation",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}


}
