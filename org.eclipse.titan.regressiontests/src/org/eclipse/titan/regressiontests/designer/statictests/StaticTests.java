/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.designer.statictests.Basic_tests.Basic_tests;
import org.eclipse.titan.regressiontests.designer.statictests.Unstructured_tests.Unstructured_tests;
import org.eclipse.titan.regressiontests.library.ProjectHandlingLibrary;
import org.eclipse.titan.regressiontests.library.WorkspaceHandlingLibrary;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	Basic_tests.class, 
	Unstructured_tests.class
	})
public final class StaticTests {

	private final static Logger LOGGER = Logger.getLogger(StaticTests.class.getName());

	private StaticTests() {
		throw new UnsupportedOperationException();
	}

	@BeforeClass
	public static void setUp() {
		//=== 1. Analyze all projects  ===
		//Perhaps "Semantic_Analizer_Tests" is enough
		IProject[] projects = WorkspaceHandlingLibrary.getProjectsInWorkspace();
		ProjectHandlingLibrary projectLibrary = null;
		for (IProject project : projects) {

			projectLibrary = new ProjectHandlingLibrary(project);
			try {
				projectLibrary.clearMarkers(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
				projectLibrary.clearMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.info("Analyzing project: " + project.getName());
			projectLibrary.analyzeProject();
			LOGGER.info("Analyzation done: " + project.getName());
		}
		//=== 2.Designer_plugin test based check, uses the result of the analysis, stored in the workspace ===
		try {
			LOGGER.info("Collecting markers");
			Designer_plugin_tests.collectorTransformator();
			LOGGER.info(Designer_plugin_tests.getSyntacticMarkers().size() + " resources have syntactic markers");
			LOGGER.info(Designer_plugin_tests.getSemanticMarkers().size() + " resources have semantic markers");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
