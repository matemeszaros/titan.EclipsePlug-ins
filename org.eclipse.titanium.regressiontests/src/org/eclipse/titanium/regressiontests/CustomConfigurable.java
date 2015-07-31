/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests;

/** 
 * This class contains some host-related settings, which
 * might be different for each developer. 
 * 
 * Feel free to change these constants to fit your need,
 * but do not commit those changes; use the following command:
 * <code>
 * git update-index --assume-unchanged org.eclipse.titanium.regressiontests/src/org/eclipse/titanium/regressiontests/CustomConfigurable.java
 * </code>, or commit your changes to a separate branch and rebase them on need.
 * 
 * @author poroszd
 */
public class CustomConfigurable {
	/** Location of the license file. Set to <code>null</code> to omit the regarded checks */
	public static String LICENSE_FILE = "C:\\cygwin\\home\\ekrisza\\license_1484.dat";

	/** The folder, where the project directory resides */
	public static String PROJECT_FOLDER = "file:///C:/Users/ekrisza/runtime-New_configuration/";
	/** The name of the tested project */
	public static final String PROJECT_TO_USE = "Regression_test_project";

	public static String getLicenseFile() {
		return LICENSE_FILE;
	}
	public static void setLicenseFile(String licenseFile) {
		LICENSE_FILE = licenseFile;
	}
	public static String getProjectFolder() {
		return PROJECT_FOLDER;
	}
	public static void setProjectFolder(String projectFolder) {
		PROJECT_FOLDER = projectFolder;
	}
	/** The folder, where the project (i.e. the .project file and the sources) resides */
	public static String getProjectPath() {
		return PROJECT_FOLDER + PROJECT_TO_USE;
	}
}
