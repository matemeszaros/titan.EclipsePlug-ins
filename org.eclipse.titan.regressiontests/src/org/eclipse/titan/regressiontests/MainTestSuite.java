/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	Designer_plugin_tests.class
})
public final class MainTestSuite {
	
	//public static String LICENSE_FILE = "/home/earplov/license_13136.dat";
	//public static String LICENSE_FILE = "C:\\Users\\ethbaat\\license_98.dat";
	public static String LICENSE_FILE = System.getenv("TTCN3_LICENSE_FILE");
	
	private static URI pathToWorkspace;

	//public static final String PATH_TO_WORKSPACE = "file:///home/earplov/gerrithub/eclipse/titan.EclipsePlug-ins/";
	//public static final String PATH_TO_WORKSPACE = "file:///c:/Users/ethbaat/git/titan.EclipsePlug-ins/";
	public static final String PATH_TO_WORKSPACE = "file:///" + System.getProperty("user.dir") + "/../";

	static {
		try {
			pathToWorkspace = URIUtil.fromString(PATH_TO_WORKSPACE);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	private MainTestSuite() {
		throw new UnsupportedOperationException();
	}

	@BeforeClass
	public static void setUp() throws Exception {
		if (!licenseFileExists()) {
			final String errorMsg = "The license file can not be found at the location: `" + LICENSE_FILE + "'" + System.getProperty("line.separator")
					+ "The 'org.eclipse.titan.regressiontests.MainTestSuite.LICENSE_FILE' constant should contain the proper location.";
			showError("Cannot find the license file.", errorMsg);
			Assert.fail(errorMsg);
		}

		File workspaceFolder = new File(URIUtil.append(pathToWorkspace, "Semantic_Analizer_Tests"));
		if (!workspaceFolder.exists()) {
			final String errorMsg = "Can not find the workspace at the location: `" + workspaceFolder.toURI() + "'" + System.getProperty("line.separator") 
					+ "The 'org.eclipse.titan.regressiontests.MainTestSuite.PATH_TO_WORKSPACE' constant should contain the proper location.";
			showError("Cannot find the workspace", errorMsg);
			Assert.fail(errorMsg);
		}
	}

	/**
	 * Displays an error message. If the application is running in headless mode,
	 *  the message will be printed to stderr. Otherwise, a messagedialog will appear.
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

	/**
	 * Tries to find the license file.
	 * @return The license file, or null if it does not exist.
	 */
	private static boolean licenseFileExists() {
		if(LICENSE_FILE != null) {
			File licenseFile = new File(LICENSE_FILE);
			if (licenseFile != null && licenseFile.exists()) {
				return true;
			}
		}		
		return false;
	}

	public static void setPathToWorkspace(final URI pathToSet) {
		pathToWorkspace = pathToSet;
	}

	public static URI getPathToWorkspace() {
		return pathToWorkspace;
	}
}
