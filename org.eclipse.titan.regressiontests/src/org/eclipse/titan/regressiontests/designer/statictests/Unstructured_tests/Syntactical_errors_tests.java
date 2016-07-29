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

public class Syntactical_errors_tests {

	//SemanticErrors2_asn
	//SemanticErrors3_asn
	//ReturnValueTest.ttcn
	
	@Test
	public void SemanticErrors2_asn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(SemanticErrors2_asn_initializer(), "src/Unstructured_tests/SemanticErrors2.asn");
	}

	@Test
	public void SemanticErrors3_asn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(SemanticErrors3_asn_initializer(), "src/Unstructured_tests/SemanticErrors3.asn");
	}
	
	@Test
	public void SyntacticErrors5() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile( ReturnValueTest_ttcn_initializer_syntactic(), "src/Unstructured_tests/ReturnValueTest.ttcn");
	}

	private ArrayList<MarkerToCheck> SemanticErrors2_asn_initializer() {
		//SemanticErrors2.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 5;
		markersToCheck.add(new MarkerToCheck("`Semantic_errors3' is not a valid ASN.1 identifier. Did you mean `Semantic-errors3' ?",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> SemanticErrors3_asn_initializer() {
		//SemanticErrors3.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 20;
		markersToCheck.add(new MarkerToCheck("Duplicate named bit `first'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}
	
	private ArrayList<MarkerToCheck> ReturnValueTest_ttcn_initializer_syntactic() {
		//ReturnValueTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(5);
		int lineNum = 37;
		markersToCheck.add(new MarkerToCheck("mismatched input '..' expecting {'action', 'activate', 'all', 'alt', 'and', 'and4b', 'any', 'break', 'connect', 'const', 'continue', 'deactivate', 'disconnect', 'do', 'execute', 'for', 'goto', 'if', 'ifpresent', 'interleave', 'kill', 'label', 'length', 'log', 'map', 'mod', 'mtc', 'or', 'or4b', 'rem', 'repeat', 'return', 'select', 'self', 'setverdict', 'stop', 'template', 'testcase', 'timer', 'unmap', 'var', 'while', 'xor', 'xor4b', 'int2enum', 'string2ttcn', IDENTIFIER, '@try', ';', '{', '}', '<', '>', '!=', '>=', '<=', '==', '+', '-', '*', '/', '<<', '>>', '<@', '@>', '&'}",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 39;
		markersToCheck.add(new MarkerToCheck("mismatched input '..' expecting {'action', 'activate', 'all', 'alt', 'and', 'and4b', 'any', 'break', 'connect', 'const', 'continue', 'deactivate', 'disconnect', 'do', 'execute', 'for', 'goto', 'if', 'ifpresent', 'interleave', 'kill', 'label', 'length', 'log', 'map', 'mod', 'mtc', 'or', 'or4b', 'rem', 'repeat', 'return', 'select', 'self', 'setverdict', 'stop', 'template', 'testcase', 'timer', 'unmap', 'var', 'while', 'xor', 'xor4b', 'int2enum', 'string2ttcn', IDENTIFIER, '@try', ';', '{', '}', '<', '>', '!=', '>=', '<=', '==', '+', '-', '*', '/', '<<', '>>', '<@', '@>', '&'}",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("mismatched input '..' expecting {'and', 'and4b', 'ifpresent', 'length', 'mod', 'or', 'or4b', 'rem', 'xor', 'xor4b', ',', ')', '<', '>', '!=', '>=', '<=', '==', '+', '-', '*', '/', '<<', '>>', '<@', '@>', '&'}",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 145;
		markersToCheck.add(new MarkerToCheck("no viable alternative at input '?'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 61;
		markersToCheck.add(new MarkerToCheck("no viable alternative at input '?'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}
	


}
