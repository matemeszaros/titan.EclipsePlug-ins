/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor;

import static org.eclipse.titan.executor.Activator.PLUGIN_ID;

/**
 * @author Kristof Szabados
 * */
public final class GeneralConstants {
	public static final String PASS = "pass";
	public static final String INCONC = "inconc";
	public static final String ERROR = "error";
	public static final String FAIL = "fail";
	public static final String NONE = "none";

	public static final String HOSTCONTROLLER = "Host Controller";
	public static final String HOST = "Host";
	public static final String WORKINGDIRECTORY = "Working directory";
	public static final String EXECUTABLE = "Executable";
	public static final String COMMAND = "Command";

	public static final String PROJECTNAME = PLUGIN_ID + ".projectName";
	public static final String WORKINGDIRECTORYPATH = PLUGIN_ID + ".workingdirectoryPath";
	public static final String EXECUTABLEFILEPATH = PLUGIN_ID + ".executableFilePath";
	public static final String CONFIGFILEPATH = PLUGIN_ID + ".configurationFilePath";
	public static final String SHORTCUTEXECUTION = PLUGIN_ID + ".executeStartedFromShortCut";
	public static final String EXECUTECONFIGFILEONLAUNCH = PLUGIN_ID + ".executeConfigurationFileOnLaunch";
	public static final String HOSTNAMES = PLUGIN_ID + ".hostNames";
	public static final String HOSTWORKINGDIRECTORIES = PLUGIN_ID + ".hostWorkingDirectories";
	public static final String HOSTEXECUTABLES = PLUGIN_ID + ".hostExecutables";
	public static final String HOSTCOMMANDS = PLUGIN_ID + ".hostCommands";

	public static final String CONSOLELOGGING = PLUGIN_ID + ".consoleLogging";
	public static final String TESTCASEREFRESHONSTART = PLUGIN_ID + ".testcaseRefreshOnStart";
	public static final String SEVERITYLEVELEXTRACTION = PLUGIN_ID + ".severityLevelExtraction";
	public static final String MAXIMUMNOTIFICATIONLINECOUNT = PLUGIN_ID + ".maximumNotificationLineCount";
	public static final String MCSTATEREFRESHTIMEOUT = PLUGIN_ID + ".MainControllerStateRefreshTimeout";
	public static final String VERDICTEXTRACTION = PLUGIN_ID + ".verdictExtraction";
	public static final String KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES = PLUGIN_ID + ".keepTemporarilyGeneratedConfigurationFiles";

	public static final String REPLACEABLEHOSTNAME = "%Host";
	public static final String REPLACEABLEHOSTWORKIGNDIRECTORY = "%Workingdirectory";
	public static final String REPLACEABLEHOSTEXECUTABLE = "%Executable";
	public static final String REPLACEABLEMCHOST = "%MCHost";
	public static final String REPLACEABLEMCPORT = "%MCPort";

	public static final String DEFAULT_LOGFILENAME_SINGLE = "%e-part%i.%s";
	public static final String DEFAULT_LOGFILENAME_PARALLEL = "%e.%h-part%i-%r.%s";

	/** private constructor to disable instantiation */
	private GeneralConstants() {
	}
}
