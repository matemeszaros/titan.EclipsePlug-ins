/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titanium.regressiontests.library.WorkspaceHandlingLibrary;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This is the main test suite. The entry point of regression tests. When creating a new launch configuration this should be set as the Test Class.
 */
@RunWith(Suite.class)
@SuiteClasses({
		TitaniumPluginTests.class })
public class MainTestSuite {
	@BeforeClass
	public static void setUp() throws Exception {
		if (CustomConfigurable.LICENSE_FILE != null) {
			File licenseFile = new File(CustomConfigurable.LICENSE_FILE);
			if (!licenseFile.exists()) {
				final String errorMsg = "The license file can not be found at the location: `" + licenseFile.getAbsolutePath() + "'" + System.getProperty("line.separator") +
						"The 'org.eclipse.titan.regressiontests.CustomConfigurable.LICENSE_FILE' constant should contain the proper location.";
				showError("Cannot find the license file.", errorMsg);
				Assert.fail(errorMsg);
			}
		}

		if (CustomConfigurable.LICENSE_FILE != null)
			org.eclipse.titan.designer.Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.LICENSE_FILE_PATH, CustomConfigurable.LICENSE_FILE);

		File projectFolder = new File(new URI(CustomConfigurable.getProjectPath()));
		if (!projectFolder.exists()) {
			final String errorMsg = "Can not find the project at the location: `" + CustomConfigurable.getProjectPath() + System.getProperty("line.separator");
			showError("Cannot find the project", errorMsg);
			Assert.fail(errorMsg);
		}

		WorkspaceHandlingLibrary.setAutoBuilding(false);

		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(CustomConfigurable.PROJECT_TO_USE);
		if (!project.exists()) {
			WorkspaceHandlingLibrary.importProjectIntoWorkspace(CustomConfigurable.PROJECT_TO_USE, new URI(CustomConfigurable.getProjectPath()));
		}
	}

	/**
	 * Displays an error message. If the application is running in headless mode, the message will be printed to stderr. Otherwise, a messagedialog will appear.
	 * 
	 * @param title
	 * @param errorMsg
	 */
	public static void showError(final String title, final String errorMsg) {
		if (PlatformUI.isWorkbenchRunning()) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), title, errorMsg);
		} else {
			System.err.println(errorMsg);
		}
	}
	
	@Test
	public void nothing() {
		
	}
}
