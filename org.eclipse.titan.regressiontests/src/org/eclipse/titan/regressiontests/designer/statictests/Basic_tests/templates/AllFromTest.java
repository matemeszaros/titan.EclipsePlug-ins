/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests.templates;

import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
//import org.junit.Ignore;
import org.junit.Test;

//@Ignore("The tested feature is not ready yet")
public class AllFromTest {

	private static final String DIR_PATH = "src/Basic_tests/templates/all_from/positive/";

	@Test
	public void allFromComplement() throws Exception {
		checkZeroMarkersOnFile("all_from_complement.ttcn");
	}

	@Test
	public void allFromPermutation() throws Exception {
		checkZeroMarkersOnFile("all_from_permutation.ttcn");
	}

	@Test
	public void allFromSubset() throws Exception {
		checkZeroMarkersOnFile("all_from_subset.ttcn");
	}

	@Test
	public void allFromSuperset() throws Exception {
		checkZeroMarkersOnFile("all_from_superset.ttcn");
	}

	@Test
	public void allFromAllFrom() throws Exception {
		checkZeroMarkersOnFile("all_from.ttcn");
	}

	@Test
	public void allFromEverything() throws Exception {
		checkZeroMarkersOnFile("everything.ttcn");
	}

	@Test
	public void allFromFunctions() throws Exception {
		checkZeroMarkersOnFile("functions.ttcn");
	}

	//@Test
	public void allFromSapc() throws Exception {
		checkZeroMarkersOnFile("sapc.ttcn");
	}

	private static void checkZeroMarkersOnFile(final String fileName) {
		final String filePath = DIR_PATH + fileName;
		Designer_plugin_tests.checkRealZeroSemanticMarkersOnFile(filePath);
	}

}
