/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.titan.designer.editors.ColorManager;
import org.eclipse.titan.designer.editors.StringDetectionPatternRule;
import org.eclipse.titan.designer.editors.WhiteSpaceDetector;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public final class CodeScanner extends RuleBasedScanner {
	private static final String[] STRING_PIECES = new String[] { "\"", "#", "'", "'B", "'H", "'O" };

	public static final String[] SECTION_TITLES = new String[] { "[LOGGING]", "[EXECUTE]", "[TESTPORT_PARAMETERS]", "[MODULE_PARAMETERS]",
			"[MAIN_CONTROLLER]", "[EXTERNAL_COMMANDS]", "[GROUPS]", "[COMPONENTS]", "[INCLUDE]", "[ORDERED_INCLUDE]", "[DEFINE]", "[PROFILER]" };

	public static final String[] KEYWORDS = new String[] { "LogFile", "FileMask", "ConsoleMask", "AppendFile", "TimeStampFormat",
			"LogEventTypes", "SourceInfoFormat", "LogEntityName", "LogSourceInfo", "DiskFullAction", "LogFileNumber", "LogFileSize",
			"MatchingHints", "Detailed", "Compact", "SubCategories", "Stack", "Single", "None", "Seconds", "DateTime", "Time", "Stop",
			"Error", "Retry", "Delete", "TCPPort", "KillTimer", "NumHCs", "UnixSocketsEnabled", "LocalAddress",
			"ConsoleTimeStampFormat", "LoggerPlugins","EmergencyLogging","EmergencyLoggingBehaviour","EmergencyLoggingMask",
			"DisableProfiler", "DisableCoverage", "DatabaseFile", "AggregateData", "StatisticsFile", "DisableStatistics",
			"StatisticsFilter", "StartAutomatically", "NetLineTimes", "NetFunctionTimes" };

	public static final String[] MASK_OPTIONS = new String[] { "TTCN_EXECUTOR", "TTCN_ERROR", "TTCN_WARNING", "TTCN_PORTEVENT", "TTCN_TIMEROP",
			"TTCN_VERDICTOP", "TTCN_DEFAULTOP", "TTCN_TESTCASE", "TTCN_ACTION", "TTCN_USER", "TTCN_FUNCTION", "TTCN_STATISTICS",
			"TTCN_PARALLEL", "TTCN_MATCHING", "TTCN_DEBUG", "EXECUTOR", "ERROR", "WARNING", "PORTEVENT", "TIMEROP", "VERDICTOP",
			"DEFAULTOP", "TESTCASE", "ACTION", "USER", "FUNCTION", "STATISTICS", "PARALLEL", "MATCHING", "DEBUG", "LOG_ALL",
			"LOG_NOTHING",

			"ACTION_UNQUALIFIED", "DEBUG_ENCDEC", "DEBUG_TESTPORT", "DEBUG_UNQUALIFIED", "DEFAULTOP_ACTIVATE", "DEFAULTOP_DEACTIVATE",
			"DEFAULTOP_EXIT", "DEFAULTOP_UNQUALIFIED", "ERROR_UNQUALIFIED", "EXECUTOR_COMPONENT", "EXECUTOR_CONFIGDATA",
			"EXECUTOR_EXTCOMMAND", "EXECUTOR_LOGOPTIONS", "EXECUTOR_RUNTIME", "EXECUTOR_UNQUALIFIED", "FUNCTION_RND",
			"FUNCTION_UNQUALIFIED", "MATCHING_DONE", "MATCHING_MCSUCCESS", "MATCHING_MCUNSUCC", "MATCHING_MMSUCCESS",
			"MATCHING_MMUNSUCC", "MATCHING_PCSUCCESS", "MATCHING_PCUNSUCC", "MATCHING_PMSUCCESS", "MATCHING_PMUNSUCC",
			"MATCHING_PROBLEM", "MATCHING_TIMEOUT", "MATCHING_UNQUALIFIED", "PARALLEL_PORTCONN", "PARALLEL_PORTMAP", "PARALLEL_PTC",
			"PARALLEL_UNQUALIFIED", "PORTEVENT_DUALRECV", "PORTEVENT_DUALSEND", "PORTEVENT_MCRECV", "PORTEVENT_MCSEND",
			"PORTEVENT_MMRECV", "PORTEVENT_MMSEND", "PORTEVENT_MQUEUE", "PORTEVENT_PCIN", "PORTEVENT_PCOUT", "PORTEVENT_PMIN",
			"PORTEVENT_PMOUT", "PORTEVENT_PQUEUE", "PORTEVENT_STATE", "PORTEVENT_UNQUALIFIED", "STATISTICS_UNQUALIFIED",
			"STATISTICS_VERDICT", "TESTCASE_FINISH", "TESTCASE_START", "TESTCASE_UNQUALIFIED", "TIMEROP_GUARD", "TIMEROP_READ",
			"TIMEROP_START", "TIMEROP_STOP", "TIMEROP_TIMEOUT", "TIMEROP_UNQUALIFIED", "USER_UNQUALIFIED", "VERDICTOP_FINAL",
			"VERDICTOP_GETVERDICT", "VERDICTOP_SETVERDICT", "VERDICTOP_UNQUALIFIED", "WARNING_UNQUALIFIED",
			
			"NumberOfLines", "LineDataRaw", "FuncDataRaw", "LineAvgRaw", "FuncAvgRaw", "LineTimesSortedByMod",
			"FuncTimesSortedByMod", "LineTimesSortedTotal", "FuncTimesSortedTotal", "LineCountSortedByMod",
			"FuncCountSortedByMod", "LineCountSortedTotal", "FuncCountSortedTotal", "LineAvgSortedByMod",
			"FuncAvgSortedByMod", "LineAvgSortedTotal", "FuncAvgSortedTotal", "Top10LineTimes", "Top10FuncTimes",
			"Top10LineCount", "Top10FuncCount", "Top10LineAvg", "Top10FuncAvg", "UnusedLines", "UnusedFunc",
			"AllRawData", "LineDataSortedByMod", "FuncDataSortedByMod", "LineDataSortedTotal", "FuncDataSortedTotal",
			"LineDataSorted", "FuncDataSorted", "AllDataSorted", "Top10LineData", "Top10FuncData",
			"Top10AllData", "UnusedData", "All" };

	public static final String[] EXTERNAL_COMMAND_TYPES = new String[] { "BeginControlPart", "EndControlPart", "BeginTestCase", "EndTestCase" };

	static final String[] OPTIONS = new String[] { "Yes", "No" };

	static final String[] ASSIGNMENT = new String[] { ":=" };

	public static final String SINGLE_LINE_COMMENT = "__config_single_line_comment";
	public static final String MULTI_LINE_COMMENT = "__config_multi_line_comment";
	static final String[] COMMENT_PIECES = new String[] { "//", "#", "/*", "*/" };

	public CodeScanner(final ColorManager colorManager) {
		IToken singleLineComment = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_COMMENTS);
		IToken multiLineComment = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_COMMENTS);
		IToken sectionTitle = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_SECTION_TITLE);
		IToken keywords = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_CONFIG_KEYWORDS);
		IToken maskOptions = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS);
		IToken externalCommandTypes = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES);
		IToken options = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_CONFIG_KEYWORDS);
		IToken assignment = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_CONFIG_KEYWORDS);

		IToken string = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_STRINGS);

		IToken other = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_NORMAL_TEXT);
		List<IRule> rules = new ArrayList<IRule>();

		rules.add(new EndOfLineRule(COMMENT_PIECES[0], singleLineComment));
		rules.add(new EndOfLineRule(COMMENT_PIECES[1], singleLineComment));
		rules.add(new MultiLineRule(COMMENT_PIECES[2], COMMENT_PIECES[3], multiLineComment, '\0', true));

		rules.add(new WhitespaceRule(new WhiteSpaceDetector()));
		rules.add(new SingleLineRule(STRING_PIECES[0], STRING_PIECES[0], string, '\\'));
		rules.add(new StringDetectionPatternRule(STRING_PIECES[2], new char[][] { { '\'', 'B' }, { '\'', 'H' }, { '\'', 'O' } }, string));

		WordRule wordRule = new WordRule(new WordDetector(), other);
		for (String element : CodeScanner.SECTION_TITLES) {
			wordRule.addWord(element, sectionTitle);
		}
		for (String element : CodeScanner.KEYWORDS) {
			wordRule.addWord(element, keywords);
		}
		for (String element : CodeScanner.MASK_OPTIONS) {
			wordRule.addWord(element, maskOptions);
		}
		for (String element : CodeScanner.EXTERNAL_COMMAND_TYPES) {
			wordRule.addWord(element, externalCommandTypes);
		}
		for (String element : CodeScanner.OPTIONS) {
			wordRule.addWord(element, options);
		}
		for (String element : CodeScanner.ASSIGNMENT) {
			wordRule.addWord(element, assignment);
		}

		rules.add(wordRule);

		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
