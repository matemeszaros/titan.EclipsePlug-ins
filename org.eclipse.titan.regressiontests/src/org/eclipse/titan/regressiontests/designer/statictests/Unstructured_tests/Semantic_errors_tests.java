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

public class Semantic_errors_tests {
	//SemanticErrors1_asn
	//SemanticErrors2_asn
	//SemanticErrors3_asn
	//Semantic_errors_ttcn
	//Semantic_errors2_ttcn
	//Semantic_errors3_ttcn
	//Semantic_errors4_ttcn
	//Syntax_warnings_ttcn
	//ReturnValueTest_ttcn
	//ITS_bug1.ttcn
	//ForBug.ttcn
	
	@Test
	public void SemanticErrors1_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(SemanticErrors1_asn_initializer(), "src/Unstructured_tests/SemanticErrors1.asn");
	}

	@Test
	public void SemanticErrors2_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(SemanticErrors2_asn_initializer(), "src/Unstructured_tests/SemanticErrors2.asn");
	}

	@Test
	public void SemanticErrors3_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(SemanticErrors3_asn_initializer(), "src/Unstructured_tests/SemanticErrors3.asn");
	}

	@Test
	public void Semantic_errors_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Semantic_errors_ttcn_initializer(), "src/Unstructured_tests/Semantic_errors.ttcn");
	}

	@Test
	public void Semantic_errors2_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(Semantic_errors2_ttcn_initializer(), "src/Unstructured_tests/Semantic_errors2.ttcn");
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
	public void Semantic_errors5_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(ReturnValueTest_ttcn_initializer(), "src/Unstructured_tests/ReturnValueTest.ttcn");
	}
	
	@Test
	public void ITS_bug1_Test() throws Exception {
		checkZeroMarkersOnFile("src/Unstructured_tests/ITS_bug1.ttcn");
	}

	@Test
	public void ForBug_Test() throws Exception {
		checkZeroMarkersOnFile("src/Unstructured_tests/ForBug.ttcn");
	}
	
	private static void checkZeroMarkersOnFile(final String fileName) {
 		Designer_plugin_tests.checkRealZeroSemanticMarkersOnFile(fileName);
		Designer_plugin_tests.checkRealZeroSyntaxMarkersOnFile(fileName);
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//	Helper functions                                                                //
	//////////////////////////////////////////////////////////////////////////////////////

	private ArrayList<MarkerToCheck> SemanticErrors1_asn_initializer() {
		//SemanticErrors1.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 7;
		markersToCheck.add(new MarkerToCheck("Duplicate symbol with name `Duplicate-symbol' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate symbol with name `Duplicate-symbol' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `Nonexi' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no ASN.1 module with name `Module-B'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate symbol with name `Duplicate-symbol-2' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate symbol with name `Duplicate-symbol-2' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `nonexi' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 19;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `nonexi' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 136;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `NONEXI' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `NONEXI' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `NONEXI' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `NONEXI' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 32;
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `NONEXI' in module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> SemanticErrors2_asn_initializer() {
		//SemanticErrors2.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 3;
		markersToCheck.add(new MarkerToCheck("A module cannot import from itself",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate import from module `SemanticErrors1' was first declared here",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Symbol `X10' is not exported from module `SemanticErrors1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("An ASN.1 module cannot import from a TTCN-3 module",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate import from module `SemanticErrors1' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `Duplicate-symbol-3' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `Duplicate-symbol-3' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Cannot recognise this assignment",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There are more imported symbols with name `A' in module `SemanticErrors2'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> SemanticErrors3_asn_initializer() {
		//SemanticErrors3.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 11;
		markersToCheck.add(new MarkerToCheck("BOOLEAN value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `b' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("BOOLEAN value was expected",  ++lineNum, IMarker.SEVERITY_ERROR));
		int i = 0;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Duplicate definition with name `b' was declared here again", lineNum++, IMarker.SEVERITY_ERROR)); }
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `ize' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `ize' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate number 1 for name `ize'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate number 1 for name `ize2'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Number 1 is already assigned to name `ize'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Bit 1 is already assigned to name `ize'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `ize' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `ize' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate value 1 for named bit `ize'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate value 1 for named bit `ize2'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("No named bits are defined in type `bitstring'",  lineNum, IMarker.SEVERITY_ERROR));
		//			markersToCheck.add(new MarkerToCheck("Duplicate named bit `first'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("No named bit with name `forth' is defined in type `bitstring'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `ize' was first declared here",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `ize2' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Duplicate ENUMERATE identifier: `ize' was declared here again", lineNum, IMarker.SEVERITY_ERROR)); }
		markersToCheck.add(new MarkerToCheck("Duplicate ENUMERATE identifier: `ize2' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate numeric value 1 for enumeration `ize'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate numeric value 1 for enumeration `ize2'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Value 1 is already assigned to `ize'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Alternative `map-open' was first defined here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 1;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Duplicate Alternative identifier in CHOICE: `map-open' was declared here again", lineNum++, IMarker.SEVERITY_ERROR)); }
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Component `eventTypeBCSM' was first defined here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate Component identifier in SEQUENCE: `eventTypeBCSM' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Component `eventTypeBCSM' was first defined here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate Component identifier in SET: `eventTypeBCSM' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("COMPONENTS OF in a SET type shall refer to another SET type instead of `@SemanticErrors3.Bearerservicecode6'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("COMPONENTS OF in a SEQUENCE type shall refer to another SEQUENCE type instead of `@SemanticErrors3.Bearerservicecode7'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("There is no imported module with name `Bar-mi'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `BearerServiceCode-1' in module `SemanticErrors3'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no imported module with name `Non-exi'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: Bearerservicecode11 -> type reference: Bearerservicecode10 -> type reference: Bearerservicecode11'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 3;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: Bearerservicecode10 -> type reference: Bearerservicecode11 -> type reference: Bearerservicecode10'", lineNum++, IMarker.SEVERITY_ERROR)); }
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Cannot recognise this assignment",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Parameterized assignment expected",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Too few parameters: `2' was expected instead of `1'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Too many parameters: `2' was expected instead of `3'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Cannot recognise this assignment",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no assignment or imported symbol with name `NON-EXI' in module `SemanticErrors3'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("General string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("INTEGER value was expected", lineNum++, IMarker.SEVERITY_ERROR)); }
		markersToCheck.add(new MarkerToCheck("Mandatory component `field2' is missing from SEQUENCE value",  --lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a non-existent component `field3' of SEQUENCE type `@SemanticErrors3.ASNSequenceType'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("General string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("INTEGER value was expected", lineNum++, IMarker.SEVERITY_ERROR)); }
		markersToCheck.add(new MarkerToCheck("Mandatory component `field2' is missing from SET value",  --lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to a non-existent component `field3' of SET type `@SemanticErrors3.ASNSetType'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Mantissa `99,999,999,999,999' should be less than `2,147,483,647'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Base of the REAL must be 2 or 10",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Exponent `9,999,999,999,999,999' should be less than `2,147,483,647'",  ++lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> Semantic_errors_ttcn_initializer() {
		//Semantic_errors.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 11;
		markersToCheck.add(new MarkerToCheck("There is no module with name `nonexiModule'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("There is no module with name `nonExistent1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no module with name `nonExistent2'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("There is no module with name `nonExistent3'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 15;
		markersToCheck.add(new MarkerToCheck("Duplicate field name `x' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Duplicate field name `x' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("`myf' is not a reference to a type",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to parameterized definition `myf' without actual parameter list",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Duplicate enumeration identifier `enum_val1' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Duplicate enumeration identifier `enum_val1' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: xx -> type reference: yy -> type reference: xx'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: yy -> type reference: xx -> type reference: yy'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `myfuncref' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `myfuncref' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Duplicate parameter with name `par' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate parameter with name `par' was declared here again",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `x' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `x' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.no_choice -> type reference: @Semantic_errors.no_choice -> type reference: no_choice'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.no_choice -> type reference: no_choice -> type reference: @Semantic_errors.no_choice'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 1;
		int i = 0;
		for (i = 0; i < 2; i++) { markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: no_choice -> type reference: @Semantic_errors.no_choice -> type reference: @Semantic_errors.no_choice'", lineNum++, IMarker.SEVERITY_ERROR)); }
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: @Semantic_errors.MyUnion0'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: @Semantic_errors.MyUnion1 -> type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Circular reference chain: `type reference: MyUnion0 -> type reference: @Semantic_errors.MyUnion0 -> type reference: MyUnion1 -> type reference: @Semantic_errors.MyUnion1 -> type reference: @Semantic_errors.MyUnion1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 23;
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Version template must be exactly <RnXnn>",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Wrong format for product version information: The accepted formats resemble CRL 113 200/1 R9A",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Wrong format for version information: The accepted formats resemble R2D02 and R2D",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Wrong format for version information: The accepted formats resemble R2D02 and R2D",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("The minor version number 2,147,483,648 is unexpectedly large, right now we can not handle such large numbers",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate version attribute",  ++lineNum, IMarker.SEVERITY_ERROR));
		//markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("The build version number 2,147,483,648 is unexpectedly large, right now we can not handle such large numbers",  lineNum, IMarker.SEVERITY_ERROR));
		++lineNum; //markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no module with name `nonExi'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 1;
		for (i = 0; i < 4; i++) lineNum++; //{ markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types", lineNum++, IMarker.SEVERITY_ERROR)); }
		markersToCheck.add(new MarkerToCheck("Wrong format for product version information: The accepted formats resemble CRL 113 200/1 R9A",  --lineNum, IMarker.SEVERITY_ERROR));
		++lineNum; //markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Wrong format for version information: The accepted formats resemble R2D02 and R2D",  lineNum, IMarker.SEVERITY_ERROR));
		++lineNum; //markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Wrong format for version information: The accepted formats resemble R2D02 and R2D",  lineNum, IMarker.SEVERITY_ERROR));
		++lineNum; //markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("The minor version number 2,147,483,648 is unexpectedly large, right now we can not handle such large numbers",  lineNum, IMarker.SEVERITY_ERROR));
		++lineNum; //markersToCheck.add(new MarkerToCheck("Extension attributes are not supported for types",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("The build version number 2,147,483,648 is unexpectedly large, right now we can not handle such large numbers",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> Semantic_errors2_ttcn_initializer() {
		//Semantic_errors2.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 8;
		markersToCheck.add(new MarkerToCheck("Modules must be unique, but `Semantic_errors2' was declared multiple times",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> Semantic_errors3_ttcn_initializer() {
		//Semantic_errors3.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 18;
		markersToCheck.add(new MarkerToCheck("Previous definition with identifier `cx1' in higher scope unit is here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("Definition `cx1' inherited from component type `@Semantic_errors3.mycomp2' collides with definition inherited from `@Semantic_errors.mycomp1'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("The name of the inherited definition `cx1' is not unique in the scope hierarchy",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("Definition `xc1' inherited from component type `@Semantic_errors3.mycomp5' collides with definition inherited from `@Semantic_errors3.mycomp4'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("The name of the inherited definition `cx1' is not unique in the scope hierarchy",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `akarmi' in module `Semantic_errors3'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("`myfunc' is not a reference to a type",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Reference to parameterized definition `myfunc' without actual parameter list",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("Port type can not be used as `out' value parameter",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Port type can not be used as value parameter",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `ccc' in module `Semantic_errors3'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `nonexiModule' in module `Semantic_errors3'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `myInt' in module `Semantic_errors'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `ccc' in module `Semantic_errors3'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("`param1' is not a reference to a type",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Variable can not be defined for port type `@Semantic_errors3.myport1'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `iter' was declared here again",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Duplicate definition with name `iter' was first declared here",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("Reference to parameterized definition `myfunc' without actual parameter list",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Type reference expected",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("component type expected",  ++lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> Semantic_errors4_ttcn_initializer() {
		//Semantic_errors4.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 8;
		markersToCheck.add(new MarkerToCheck("Modules must be unique, but `Semantic_errors2' was declared multiple times",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 16;
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `x' in module `Semantic_errors2'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `myport' in module `Semantic_errors2'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `x' in module `Semantic_errors2'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> ReturnValueTest_ttcn_initializer() {
		// ReturnValueTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(29);
		int lineNum = 11;
		markersToCheck.add(new MarkerToCheck("Missing return value. The function should return a value of type `integer'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 30;
		markersToCheck.add(new MarkerToCheck("A specific value without matching symbols was expected as return value",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 11;
		markersToCheck.add(new MarkerToCheck("A specific value without matching symbols was expected as return value",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("A specific value without matching symbols was expected as return value",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 52;
		markersToCheck.add(new MarkerToCheck("integer value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("integer value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("integer value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("A specific value without matching symbols was expected as return value",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Reference to a value was expected instead of template `t_roi'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 14;
		markersToCheck.add(new MarkerToCheck("integer value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 25;
		markersToCheck.add(new MarkerToCheck("Character string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("A specific value without matching symbols was expected as return value",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("Character string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 15;
		markersToCheck.add(new MarkerToCheck("Character string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 15;
		markersToCheck.add(new MarkerToCheck("Character string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 17;
		markersToCheck.add(new MarkerToCheck("Character string value was expected",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 10;
		markersToCheck.add(new MarkerToCheck("`omit' value is not allowed in this context",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 55;
		markersToCheck.add(new MarkerToCheck("An altstep cannot return a value",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}



	private ArrayList<MarkerToCheck> Syntax_warnings_ttcn_initializer() {
		//Syntax_warnings.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 17;
		markersToCheck.add(new MarkerToCheck("There is no visible definition with name `anyport' in module `Syntax_warnings'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}


}
