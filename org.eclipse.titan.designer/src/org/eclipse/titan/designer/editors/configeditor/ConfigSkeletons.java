/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

/**
 * @author Kristof Szabados
 * */
public final class ConfigSkeletons {

	/** private constructor to disable instantiation */
	private ConfigSkeletons() {
	}

	private static final String NEWLINE = System.getProperty("line.separator");
	public static final String CONFIG_FILE_SKELETON =
		"[MODULE_PARAMETERS]" + NEWLINE
		+ "# This section shall contain the values of all parameters that are defined in your TTCN-3 modules." + NEWLINE
		+ NEWLINE
		+ "[LOGGING]" + NEWLINE
		+ "# In this section you can specify the name of the log file and the classes of events" + NEWLINE
		+ "# you want to log into the file or display on console (standard error)." + NEWLINE
		+ NEWLINE
		+ "LogFile := \"logs/%e.%h-%r.%s\"" + NEWLINE
		+ "FileMask := LOG_ALL | DEBUG | MATCHING" + NEWLINE
		+ "ConsoleMask := ERROR | WARNING | TESTCASE | STATISTICS | PORTEVENT" + NEWLINE
		+ "LogSourceInfo := Yes" + NEWLINE
		+ "AppendFile := No" + NEWLINE
		+ "TimeStampFormat := DateTime" + NEWLINE
		+ "LogEventTypes := Yes" + NEWLINE
		+ "SourceInfoFormat := Single" + NEWLINE
		+ "LogEntityName := Yes" + NEWLINE
		+ NEWLINE
		+ "[TESTPORT_PARAMETERS]" + NEWLINE
		+ "# In this section you can specify parameters that are passed to Test Ports." + NEWLINE
		+ NEWLINE
		+ "[DEFINE]" + NEWLINE
			+ "# In this section you can create macro definitions," + NEWLINE
		+ "# that can be used in other configuration file sections except [INCLUDE] and [ORDERED_INCLUDE]." + NEWLINE
		+ NEWLINE
		+ "[INCLUDE]" + NEWLINE
			+ "# To use configuration settings given in other configuration files," + NEWLINE
			+ "# the configuration files just need to be listed in this section, with their full or relative pathnames." + NEWLINE
		+ NEWLINE
		+ "[ORDERED_INCLUDE]" + NEWLINE
		+ "# To use configuration settings given in other configuration files," + NEWLINE
		+ "# the configuration files just need to be listed in this section, with their full or relative pathnames." + NEWLINE
		+ NEWLINE
		+ "[EXTERNAL_COMMANDS]" + NEWLINE
			+ "# This section can define external commands (shell scripts) to be executed by the ETS" + NEWLINE
		+ "# whenever a control part or test case is started or terminated." + NEWLINE
		+ NEWLINE
		+ "#BeginTestCase := \"\"" + NEWLINE
		+ "#EndTestCase := \"\"" + NEWLINE
		+ "#BeginControlPart := \"\"" + NEWLINE
		+ "#EndControlPart := \"\"" + NEWLINE
		+ NEWLINE
		+ "[EXECUTE]" + NEWLINE
		+ "# In this section you can specify what parts of your test suite you want to execute." + NEWLINE
		+ NEWLINE
		+ "[GROUPS]" + NEWLINE
		+ "# In this section you can specify groups of hosts. These groups can be used inside the" + NEWLINE
		+ "# [COMPONENTS] section to restrict the creation of certain PTCs to a given set of hosts." + NEWLINE
		+ NEWLINE
		+ "[COMPONENTS]" + NEWLINE
		+ "# This section consists of rules restricting the location of created PTCs." + NEWLINE
		+ NEWLINE
		+ "[MAIN_CONTROLLER]" + NEWLINE
		+ "# The options herein control the behavior of MC." + NEWLINE
		+ NEWLINE
		+ "TCPPort := 0" + NEWLINE
		+ "KillTimer := 10.0" + NEWLINE
		+ "# NumHCs := 0" + NEWLINE
		+ "# LocalAddress := " + NEWLINE;
}
