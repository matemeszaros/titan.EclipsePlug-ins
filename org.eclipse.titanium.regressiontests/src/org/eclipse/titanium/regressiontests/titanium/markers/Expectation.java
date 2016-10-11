/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.titanium.markers;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.markers.utils.Analyzer;
import org.eclipse.titanium.regressiontests.CustomConfigurable;
import org.eclipse.titanium.regressiontests.library.WorkspaceHandlingLibrary;

/**
 * A testcase for one kind of code smell.
 * <p>
 * The purpose of this class is to formulate a testcase for a code smell
 * as concise as possible. For every code smell, the {@link CustomConfigurable#PROJECT_TO_USE}
 * project should contain some module that tests that code smell, i.e. at some lines
 * a marker should show up due to the suspicious code. With this class, a test for a code smell
 * could be specified by:
 * <ul>
 *   <li>the name of the testcase (that is showed in JUnit),</li>
 *   <li>the type of the code smell (given as a {@link CodeSmellType}),</li>
 *   <li>the names of the modules where markers are expected to occur,</li>
 *   <li>and the line numbers of these markers</li>
 * </ul>
 * </p><p> 
 * During the run of the test the followings happen:
 * <ol>
 *   <li>the marking level of the code smell is set to <code>Warning</code></li>
 *   <li>code smell analysis is run on the named modules</li>
 *   <li>the test fails, if we do not find an {@link IMarker} of this code smell at
 *       the line in the module, where we expected to find,</li>
 *   <li>the test also fails, when we find an <code>IMarker</code> of this code smell
 *       at a line where we did not expect this.</li>   
 * </ol>
 * 
 * @author poroszd
 *
 */
class Expectation {
	CodeSmellType type;
	Map<String, Integer[]> expectedMarkers;

	/**
	 * Create a new code smell test.
	 * 
	 * @param testcaseName the name of the test,
	 * @param problemType the code smell type to test 
	 * 
	 * @see BaseCodeSmellSpotter#getProblemType
	 */
	public Expectation(String testcaseName, CodeSmellType problemType) {
		type = problemType;
		expectedMarkers = new HashMap<String, Integer[]>();
	}

	/**
	 * Specify that a given module is devoted to test this code  smell,
	 * marking the given lines as suspicious.
	 * 
	 * @param moduleName the name of the module, that contains problems regarding this smell.
	 *        Note: Use internal module names here (i.e. underscores are duplicated, for example
	 *        a_b.ttcn in the project should be referred here as "a__b".  
	 * @param lineNumbers the lines that should be marked (may contain duplicates).
	 * 
	 * @return <code>this</code>, for method chaining.
	 */
	public Expectation shouldHave(String moduleName, Integer[] lineNumbers) {
		expectedMarkers.put(moduleName, Arrays.copyOf(lineNumbers, lineNumbers.length));
		return this;
	}

	public void setUp() {
		// Set the related problems to mark errors.
	}

	public void runTest() {
		Map<String, List<Integer>> actualMarkers = new HashMap<String, List<Integer>>();

		// analyze the modules, and collect the related markers
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(CustomConfigurable.PROJECT_TO_USE);
		ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
		for (String modName : expectedMarkers.keySet()) {
			Module mod = parser.getModuleByName(modName);
			IResource file = mod.getLocation().getFile();
			MarkerHandler mh = Analyzer.builder().addProblem(type).build().analyzeModule(new NullProgressMonitor(), mod);
			List<Integer> lines = new ArrayList<Integer>();
			for (Marker m : mh.get(file)) {
				if (m.getProblemType() == type && m.getLine() != -1) {
					lines.add(m.getLine());
				}
			}
			// save the line number of markers that were from our problem type
			actualMarkers.put(modName, lines);
		}

		// check whether the reality complies our expectations
		for (String modName : expectedMarkers.keySet()) {
			for (Integer ln: expectedMarkers.get(modName)) {
				if (!actualMarkers.get(modName).remove(ln)) {
					fail("We expected a marker in " + modName + " at line " + ln + ", but have not found it");
				}
			}
		}

		// Check whether we managed to consume all markers that showed up during the analysis
		for (String modName : actualMarkers.keySet()) {
			for (Integer ln : actualMarkers.get(modName)) {
				fail("Unexpected marker in " + modName + " at line " + ln);
			}
		}
	}
}