/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.regressiontests.MainTestSuite;
import org.eclipse.titan.regressiontests.common.CommonTestSuite;
import org.eclipse.titan.regressiontests.designer.dynamictests.ChangeTests;
import org.eclipse.titan.regressiontests.designer.statictests.StaticTests;
import org.eclipse.titan.regressiontests.designer.unittest.DesignerUnitTestSuite;
import org.eclipse.titan.regressiontests.library.MarkerHandlingLibrary;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.eclipse.titan.regressiontests.library.ProjectHandlingLibrary;
import org.eclipse.titan.regressiontests.library.WorkspaceHandlingLibrary;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ChangeTests.class,
	StaticTests.class,
	DesignerUnitTestSuite.class,
	CommonTestSuite.class })
public class Designer_plugin_tests {

	public static final String PROJECT_NAME = "Semantic_Analizer_Tests";

	private static Map<IResource, List<Map<?, ?>>> semanticMarkers;
	private static Map<IResource, List<Map<?, ?>>> syntacticMarkers;

	public static void collectorTransformator() throws Exception {
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(PROJECT_NAME);
		ProjectHandlingLibrary projectLibrary = new ProjectHandlingLibrary(project);

		semanticMarkers = MarkerHandlingLibrary.transformMarkers(projectLibrary.getMarkers(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER));
		syntacticMarkers = MarkerHandlingLibrary.transformMarkers(projectLibrary.getMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER));
	}

	@BeforeClass
	public static void setUp() throws Exception {
		Locale.setDefault(new Locale("en", "EN")); // the number format used is the english one

		/**
		 * The options that could be set can be fount in the Designer plug-in.
		 * Those options which would be assigned their default value, should not be set, but left as they are initialized.
		 * */
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.LICENSE_FILE_PATH, MainTestSuite.LICENSE_FILE);
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTUNUSEDMODULEIMPORTATION, "warning");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, "warning");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTUNUSEDGLOBALDEFINITION, "ignore");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTUNUSEDLOCALDEFINITION, "ignore");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTINFINITELOOPS, "ignore");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTREADONLY, "ignore");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTTYPECOMPATIBILITY, "warning");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORTNAMINGCONVENTIONPROBLEMS, "warning");
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORT_STRICT_CONSTANTS, true);
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.T3DOC_ENABLE, true);
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.DISPLAYDEBUGINFORMATION, true);
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.DEBUG_CONSOLE_LOG_TO_SYSOUT, true);

		WorkspaceHandlingLibrary.setAutoBuilding(false);
		boolean found = false;
		IProject[] projects = WorkspaceHandlingLibrary.getProjectsInWorkspace();
		for (IProject project : projects) {
			if (PROJECT_NAME.equals(project.getName())) {
				found = true;
				break;
			}
		}

		if (!found) {
			WorkspaceHandlingLibrary.importProjectIntoWorkspace(PROJECT_NAME, URIUtil.append(MainTestSuite.getPathToWorkspace(), "Semantic_Analizer_Tests"));
		}
	}


	public void markerCollection() throws Exception {
		Designer_plugin_tests.collectorTransformator();
	}

	public void unProcessedMarkerCheck() throws Exception {
		int sum = 0;
		for (IResource resource : syntacticMarkers.keySet()) {
			List<Map<?, ?>> temp = syntacticMarkers.get(resource);

			MarkerHandlingLibrary.printMarkerArray(resource.getProjectRelativePath().toString(), temp);
			sum += temp.size();
		}

		for (IResource resource : semanticMarkers.keySet()) {
			List<Map<?, ?>> temp = semanticMarkers.get(resource);

			MarkerHandlingLibrary.printMarkerArray(resource.getProjectRelativePath().toString(), temp);
			sum += temp.size();
		}
		assertEquals(0, sum);
	}

	public static void checkZeroMarkersOnFile(final String projectRelativePath) {
		Designer_plugin_tests.checkRealZeroSemanticMarkersOnFile(projectRelativePath);
		Designer_plugin_tests.checkZeroSyntaxMarkersOnFile(projectRelativePath);
	}

	public static void checkZeroSyntaxMarkersOnFile(final String projectRelativePath) {
		IFile file = getAccessibleFile(projectRelativePath);
		List<Map<?, ?>> fileMarkerList = syntacticMarkers.get(file);

		assertNotNull("Couldn't find syntax markers on file: " + file.getName(), fileMarkerList);

		if (!fileMarkerList.isEmpty()) {
			MarkerHandlingLibrary.printMarkerArray(projectRelativePath, fileMarkerList);
			fail("Invalid markers found");
		}
	}
	
	public static void checkRealZeroSyntaxMarkersOnFile(final String projectRelativePath) {
		IFile file = getAccessibleFile(projectRelativePath);
		List<Map<?, ?>> fileMarkerList = syntacticMarkers.get(file);

		assertNull("Invalid syntax markers found on file: " + file.getName(), fileMarkerList);

	}

	/**
	 * Gets the file marker list. If the list is not null and it is empty prints marker onto the file
	 * @param projectRelativePath
	 */
	public static void checkZeroSemanticMarkersOnFile(final String projectRelativePath) {
		List<Map<?, ?>> fileMarkerList = semanticMarkers.get(getAccessibleFile(projectRelativePath));

		assertNotNull(fileMarkerList);

		if (!fileMarkerList.isEmpty()) {
			MarkerHandlingLibrary.printMarkerArray(projectRelativePath, fileMarkerList);
			fail("Invalid semantic markers found");
		}
	}

	//
	public static void checkRealZeroSemanticMarkersOnFile(final String projectRelativePath) {
		IFile file = getAccessibleFile(projectRelativePath);

		List<Map<?, ?>> fileMarkerList = semanticMarkers.get(file);
		assertNull("Invalid semantic markers found on file: " + file.getName(), fileMarkerList);
	}


	public static void checkSyntaxMarkersOnFile(final List<MarkerToCheck> expected, final String projectRelativePath) {
		IFile file = getAccessibleFile(projectRelativePath);
		List<Map<?, ?>> fileMarkerList = syntacticMarkers.get(file);

		assertNotNull("Couldn't find syntax markers on file: " + file.getName(), fileMarkerList);

		for (int i = expected.size() - 1; i >= 0; i--) {
			MarkerHandlingLibrary.searchNDestroyFittingMarker(fileMarkerList, expected.get(i).getMarkerMap(), true);
		}

		expected.clear();
	}

	public static void checkSemanticMarkersOnFile(final List<MarkerToCheck> expected, final String projectRelativePath) {
		IFile file = getAccessibleFile(projectRelativePath);
		List<Map<?, ?>> fileMarkerList = semanticMarkers.get(file);

		assertNotNull("Couldn't find semantic markers on file: " + file.getName(), fileMarkerList);

		for (int i = expected.size() - 1; i >= 0; i--) {
			MarkerHandlingLibrary.searchNDestroyFittingMarker(fileMarkerList, expected.get(i).getMarkerMap(), true);
		}

		expected.clear();
	}

	private static IFile getAccessibleFile(String projectRelativePath) {
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(PROJECT_NAME);

		IFile file = project.getFile(projectRelativePath);
		assertTrue("Cannot access file: " + file.getFullPath(), file.isAccessible());
		return file;
	}

	public static Map<IResource, List<Map<?, ?>>> getSemanticMarkers() {
		return semanticMarkers;
	}

	public static Map<IResource, List<Map<?, ?>>> getSyntacticMarkers() {
		return syntacticMarkers;
	}
}
