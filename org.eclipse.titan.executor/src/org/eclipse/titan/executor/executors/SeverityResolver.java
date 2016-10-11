/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

/**
 * @author Kristof Szabados
 * */
public final class SeverityResolver {

	private static final String[] SEVERITY_STRINGS = new String[] {
		"NOTHING_TO_LOG",
		"ACTION_UNQUALIFIED",
		"DEFAULTOP_ACTIVATE", "DEFAULTOP_DEACTIVATE",
		"DEFAULTOP_EXIT", "DEFAULTOP_UNQUALIFIED",
		"ERROR_UNQUALIFIED",
		"EXECUTOR_RUNTIME", "EXECUTOR_CONFIGDATA", "EXECUTOR_EXTCOMMAND",
		"EXECUTOR_COMPONENT", "EXECUTOR_LOGOPTIONS", "EXECUTOR_UNQUALIFIED",
		"FUNCTION_RND", "FUNCTION_UNQUALIFIED",
		"PARALLEL_PTC", "PARALLEL_PORTCONN", "PARALLEL_PORTMAP", "PARALLEL_UNQUALIFIED",
		"TESTCASE_START", "TESTCASE_FINISH", "TESTCASE_UNQUALIFIED",
		"PORTEVENT_PQUEUE", "PORTEVENT_MQUEUE", "PORTEVENT_STATE", "PORTEVENT_PMIN",
		"PORTEVENT_PMOUT", "PORTEVENT_PCIN",  "PORTEVENT_PCOUT", "PORTEVENT_MMRECV",
		"PORTEVENT_MMSEND", "PORTEVENT_MCRECV", "PORTEVENT_MCSEND", "PORTEVENT_DUALRECV",
		"PORTEVENT_DUALSEND", "PORTEVENT_UNQUALIFIED",
		"STATISTICS_VERDICT", "STATISTICS_UNQUALIFIED",
		"TIMEROP_READ", "TIMEROP_START", "TIMEROP_GUARD", "TIMEROP_STOP",
		"TIMEROP_TIMEOUT", "TIMEROP_UNQUALIFIED",
		"USER_UNQUALIFIED",
		"VERDICTOP_GETVERDICT", "VERDICTOP_SETVERDICT", "VERDICTOP_FINAL", "VERDICTOP_UNQUALIFIED",
		"WARNING_UNQUALIFIED",
		"MATCHING_DONE", "MATCHING_TIMEOUT", "MATCHING_PCSUCCESS", "MATCHING_PCUNSUCC",
		"MATCHING_PMSUCCESS",  "MATCHING_PMUNSUCC", "MATCHING_MCSUCCESS", "MATCHING_MCUNSUCC",
		"MATCHING_MMSUCCESS", "MATCHING_MMUNSUCC", "MATCHING_PROBLEM", "MATCHING_UNQUALIFIED",
		"DEBUG_ENCDEC", "DEBUG_TESTPORT", "DEBUG_UNQUALIFIED",
		"NUMBER_OF_LOGSEVERITIES",
		"LOG_ALL_IMPORTANT" };

	private static final String UNKNOWN_SEVERITY = "Unknown";

	/** private constructor to disable instantiation */
	private SeverityResolver() {
	}

	/**
	 * Decodes the severity value into a user readable text.
	 *
	 * @param severity the severity value to decode
	 * @return the user readable text
	 * */
	public static String getSeverityString(final int severity) {
		if (severity < 0 || severity > SEVERITY_STRINGS.length) {
			return UNKNOWN_SEVERITY;
		}

		return SEVERITY_STRINGS[severity];
	}
}
