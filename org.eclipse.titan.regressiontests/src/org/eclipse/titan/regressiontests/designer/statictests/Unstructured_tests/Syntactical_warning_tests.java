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

public class Syntactical_warning_tests {

	//Semantic_errors_ttcn
	//SemanticErrors3_asn
	//Syntax_warnings_ttcn
	//config_cfg

	@org.junit.Test
	public void Semantic_errors_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(Semantic_errors_ttcn_initializer(), "src/Unstructured_tests/Semantic_errors.ttcn");
	}

	@org.junit.Test
	public void SemanticErrors3_asn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(SemanticErrors3_asn_initializer(), "src/Unstructured_tests/SemanticErrors3.asn");
	}

	@org.junit.Test
	public void Syntax_warnings_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(Syntax_warnings_ttcn_initializer(), "src/Unstructured_tests/Syntax_warnings.ttcn");
	}

	@org.junit.Test
	public void Syntax_warnings2_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(Syntax_warnings2_ttcn_initializer(), "src/Unstructured_tests/Syntax_warnings2.ttcn");
	}

	private ArrayList<MarkerToCheck> Semantic_errors_ttcn_initializer() {
		//Semantic_errors.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(29);
		int lineNum = 15;
		markersToCheck.add(new MarkerToCheck("Recursive importation is deprecated and may be fully removed in a future edition of the TTCN-3 standard",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 1;
		int i = 0;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 1;
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> SemanticErrors3_asn_initializer() {
		//SemanticErrors3.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 0;
		markersToCheck.add(new MarkerToCheck("Missing IMPORTS clause is interpreted as `IMPORTS ; (import nothing) instead of importing all symbols from all modules.",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Syntax_warnings_ttcn_initializer() {
		//Syntax_warnings.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(3);
		int lineNum = 10;
		markersToCheck.add(new MarkerToCheck("Obsolete type `char' is taken as `charstring' ",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Obsolete type `universal char' is taken as `universal charstring' ",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 10;

		markersToCheck.add(new MarkerToCheck("Obsolete statement `goto alt' will be substituted with `repeat' ", lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> Syntax_warnings2_ttcn_initializer() {
		//Syntax_warnings2.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 10;
		markersToCheck.add(new MarkerToCheck("Selective importation is not yet supported, importing all definitions",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
