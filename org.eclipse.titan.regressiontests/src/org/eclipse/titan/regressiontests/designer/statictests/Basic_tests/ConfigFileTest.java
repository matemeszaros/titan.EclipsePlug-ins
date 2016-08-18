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

public class ConfigFileTest {

	@Test
	public void configFileParserTest() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(config_cfg_initializer_errors(), "cfgFile/config.cfg");
		Designer_plugin_tests.checkSyntaxMarkersOnFile(config_cfg_initializer_warnings(), "cfgFile/config.cfg");
	}

	//config_cfg
	
	private ArrayList<MarkerToCheck> config_cfg_initializer_errors() {
		//config.cfg
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(3);
		int lineNum = 329;
		markersToCheck.add(new MarkerToCheck("Could not resolve integer definition: d_integer_nonexi using 0 as replacement.",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 37;
		markersToCheck.add(new MarkerToCheck("Could not resolve definition: d_boolean_nonexi using \"true\" as a replacement.",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 238;
		markersToCheck.add(new MarkerToCheck("Could not resolve definition: d_hostname_nonexi using \"\" as a replacement.",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> config_cfg_initializer_warnings() {
		//config.cfg
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(30);
		int lineNum = 277;
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_ACTION",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_DEBUG",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_DEFAULTOP",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_ERROR",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_EXECUTOR",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_FUNCTION",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_MATCHING",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_PARALLEL",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_PORTEVENT",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_STATISTICS",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_TESTCASE",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_TIMEROP",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_USER",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_VERDICTOP",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_WARNING",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 14;
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_ACTION",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_DEBUG",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_DEFAULTOP",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_ERROR",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_EXECUTOR",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_FUNCTION",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_MATCHING",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_PARALLEL",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_PORTEVENT",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_STATISTICS",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_TESTCASE",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_TIMEROP",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_USER",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_VERDICTOP",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("Deprecated logging option TTCN_WARNING",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
