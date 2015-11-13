/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Class for viewer constants
 *
 */
public final class Constants {
	
	// Plug-in id's
	// The TITAN Log Viewer Project Nature ID
	public static final String NATURE_ID = "org.eclipse.titan.log.viewer.TitanLogProject"; //$NON-NLS-1$
	// The TITAN Log Viewer Perspective ID
	public static final String PERSPECTIVE_ID = "org.eclipse.titan.log.viewer.factories.PerspectiveFactory"; //$NON-NLS-1$
	// The TITAN Log Viewer Projects Viewer ID 
	public static final String PROJECTS_VIEWER_ID = "org.eclipse.titan.log.viewer.views.navigator.ProjectsViewer"; //$NON-NLS-1$
	// The TITAN Log Viewer MSC View ID 
	public static final String MSC_VIEW_ID = "org.eclipse.titan.log.viewer.views.MSCView"; //$NON-NLS-1$
	// The Value View View ID
	public static final String DETAILS_VIEW_ID = "org.eclipse.titan.log.viewer.views.DetailsView"; //$NON-NLS-1$
	// The Text Table View ID
	public static final String TEXT_TABLE_VIEW_ID = "org.eclipse.titan.log.viewer.views.text.table.TextTableView"; //$NON-NLS-1$
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.titan.log.viewer"; //$NON-NLS-1$
	// The Console View ID
	public static final String CONSOLE_ID = "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$
	// The Statistical View ID
	public static final String STATISTICAL_VIEW_ID = "org.eclipse.titan.log.viewer.views.StatisticalView"; //$NON-NLS-1$
	// The LogSearchPage ID
	public static final String LOG_SEARCH_PAGE_ID = "org.eclipse.titan.log.viewer.search.LogSearchPage"; //$NON-NLS-1$

	
	// Debug flag
	public static final boolean DEBUG = false;
	
	// Version
	//public static final String LOG_VIEWER_PREVIOUS_VERSION = "1.0"; //$NON-NLS-1$
	public static final String LOG_VIEWER_CURRENT_VERSION = "1.2"; //$NON-NLS-1$
	
	// Id of the switch view action bar group
	public static final String ID_SWITCH_VIEW_GROUP = "SWITCH_VIEW_GROUP"; //$NON-NLS-1$
	
	// Icon for decorators
	public static final String ICONS_HOUR_GLASS = "icons/hourGlass.gif"; //$NON-NLS-1$
	
	// Icons for test cases
	public static final String ICONS_PASS = "icons/testpass.gif"; //$NON-NLS-1$
	public static final String ICONS_FAIL = "icons/testfail.gif"; //$NON-NLS-1$
	public static final String ICONS_ERROR = "icons/testerr.gif"; //$NON-NLS-1$
	public static final String ICONS_NONE = "icons/testnone.gif"; //$NON-NLS-1$
	public static final String ICONS_INCONCLUSIVE = "icons/testincon.gif"; //$NON-NLS-1$
	public static final String ICONS_CRASHED = "icons/crashed.gif"; //$NON-NLS-1$
	
	// Icons for Navigator view actions
	public static final String ICONS_NEW_PROJ = "icons/newprj_wiz.gif"; //$NON-NLS-1$
	public static final String ICONS_ADD_LOG = "icons/add_log.gif"; //$NON-NLS-1$
	public static final String ICONS_ADD_LOG_FOLDER = "icons/add_log_folder.gif"; //$NON-NLS-1$
	public static final String ICONS_DELETE = "icons/delete.gif"; //$NON-NLS-1$
	public static final String ICONS_REFRESH = "icons/refresh.gif"; //$NON-NLS-1$
	public static final String ICONS_MSC_VIEW = "icons/msc.gif"; //$NON-NLS-1$
	public static final String ICONS_FETCH_COMP = "icons/fetch_components.gif"; //$NON-NLS-1$
	public static final String ICONS_SORT_BY_NAME = "icons/sort_name.gif"; //$NON-NLS-1$
	public static final String ICONS_SORT_BY_VERDICT = "icons/sort_verdict.gif"; //$NON-NLS-1$
	
	// Icons for Navigator view tab folder 
	public static final String ICONS_PROJECTS_TAB = "icons/projects.gif"; //$NON-NLS-1$
	public static final String ICONS_TESTCASES_TAB = "icons/testcases.gif"; //$NON-NLS-1$
	
	// Icons for MSC view
	public static final String ICONS_MSC_DELETE = "icons/delete.gif"; //$NON-NLS-1$
	public static final String ICONS_MSC_JUMP_TO_NEXT = "icons/arrow_down.gif"; //$NON-NLS-1$
	public static final String ICONS_MSC_JUMP_TO_PREVIOUS = "icons/arrow_up.gif"; //$NON-NLS-1$
	public static final String ICONS_MSC_DECIPHERING = "icons/deciphering.gif"; //$NON-NLS-1$

	public static final int SCROLL_INCREMENT = 20; 
	
	// Icons for Text Table view
	public static final String ICONS_TEXT_TABLE_VIEW = "icons/table_view.gif"; //$NON-NLS-1$
	public static final String ICONS_FILTER = "icons/filter2.gif";
	
	// Icons for Statistical view
	public static final String ICONS_STATISTICAL_VIEW = "icons/statistics.gif"; //$NON-NLS-1$
	
	// Icons for Details view
	public static final String ICONS_DETAILS_VIEW = "icons/details.gif"; //$NON-NLS-1$
	public static final String ICONS_CHILD_OBJ = "icons/child_obj.gif"; //$NON-NLS-1$
	public static final String ICONS_PARENT_OBJ = "icons/parent_obj.gif"; //$NON-NLS-1$
	public static final String ICONS_TREE_TEXT_OBJ = "icons/tree_text_obj.gif"; //$NON-NLS-1$
	public static final String ICONS_EMPTY_PARENT = "icons/empty_parent.gif"; //$NON-NLS-1$
	public static final String ICONS_TEXT_VIEW = "icons/text_view.gif"; //$NON-NLS-1$
	public static final String ICONS_GRAPH_VIEW = "icons/tree_view.gif"; //$NON-NLS-1$

	// Icons for Overview
	public static final String ICONS_OVERVIEW = "icons/overview.gif"; //$NON-NLS-1$
	
	// Icons for Search
	public static final String ICONS_SEARCH = "icons/search.gif"; //$NON-NLS-1$
	public static final String ICONS_INFORMATION = "icons/tree_text_obj.gif"; //$NON-NLS-1$
	public static final String ICONS_INVALID_DATA = "icons/testerr.gif"; //$NON-NLS-1$
	
	// Test Case string verdicts
	public static final String TEST_CASE_VERDICT_PASS = "pass"; //$NON-NLS-1$
	public static final String TEST_CASE_VERDICT_FAIL = "fail"; //$NON-NLS-1$
	public static final String TEST_CASE_VERDICT_INCONCLUSIVE = "inconc"; //$NON-NLS-1$
	public static final String TEST_CASE_VERDICT_NONE = "none"; //$NON-NLS-1$
	public static final String TEST_CASE_VERDICT_ERROR = "error"; //$NON-NLS-1$
	
	// Test Case constants
	public static final int VERDICT_PASS = 0;
	public static final int VERDICT_NONE = 1;
	public static final int VERDICT_INCONCLUSIVE = 2;
	public static final int VERDICT_FAIL = 3;
	public static final int VERDICT_ERROR = 4;
	public static final int VERDICT_CRASHED = 5;
	
	// Test Case Extractor constants
	public static final QualifiedName EXTRACTION_RUNNING =
			new QualifiedName("org.eclipse.titan.log.viewer.actions.ExtractTestCasesAction", "EXTRACTING");
	public static final String CACHE_DIRECTORY = ".cache"; //$NON-NLS-1$
	public static final String INDEX_EXTENSION = ".index"; //$NON-NLS-1$
	public static final String RECORD_INDEX_EXTENSION = ".lrindex"; //$NON-NLS-1$
	public static final char LF = '\n';
	public static final int K = 1024;
	public static final int INITIAL_BUFFER_SIZE = 8 * K;
	public static final char[] TEST_CASE = "Test case".toCharArray(); //$NON-NLS-1$
	public static final char[] TEST_CASE_STARTED = "started".toCharArray(); //$NON-NLS-1$
	public static final char[] TEST_CASE_FINISHED = "finished".toCharArray(); //$NON-NLS-1$
	public static final char[] VERDICT = "Verdict:".toCharArray(); //$NON-NLS-1$
	public static final char[] LOG_FORMAT = "TTCN Logger v2".toCharArray(); //$NON-NLS-1$
	public static final char[] LOG_FORMAT_OPTION = "options: ".toCharArray(); //$NON-NLS-1$
	public static final Map<String, Integer> TEST_CASE_VERDICTS = new HashMap<String, Integer>();
	static {
		TEST_CASE_VERDICTS.put(Constants.TEST_CASE_VERDICT_PASS, VERDICT_PASS);
		TEST_CASE_VERDICTS.put(Constants.TEST_CASE_VERDICT_FAIL, VERDICT_FAIL);
		TEST_CASE_VERDICTS.put(Constants.TEST_CASE_VERDICT_INCONCLUSIVE, VERDICT_INCONCLUSIVE);
		TEST_CASE_VERDICTS.put(Constants.TEST_CASE_VERDICT_NONE, VERDICT_NONE);
		TEST_CASE_VERDICTS.put(Constants.TEST_CASE_VERDICT_ERROR, VERDICT_ERROR);
	}	
	
	// Perspective factory constants
	public static final String LAYOUT_LEFT = "org.eclipse.titan.log.viewer.perspective.LEFT"; //$NON-NLS-1$
	public static final String LAYOUT_TOP = "org.eclipse.titan.log.viewer.perspective.TOP"; //$NON-NLS-1$
	public static final String LAYOUT_BOTTOM = "org.eclipse.titan.log.viewer.perspective.BOTTOM"; //$NON-NLS-1$
	
	// Navigator View constants
	public static final String LOG_EXTENSION = "log"; //$NON-NLS-1$

	//Statistical View constants
	public static final String STATISTICAL_VIEW = "Statistical View"; //$NON-NLS-1$
	
	// File size decorator constants
	public static final long KILO_FACTOR = 1024;
	public static final long MEGA_FACTOR = KILO_FACTOR * KILO_FACTOR;
	public static final long GIGA_FACTOR = KILO_FACTOR * KILO_FACTOR * KILO_FACTOR;
	
	// Log File Cache Handler constants
	public static final String PROPERTY_EXTENSION = ".property"; //$NON-NLS-1$
	public static final String VERSION = "Version"; //$NON-NLS-1$
	public static final String CURRENT_VERSION = "4.0.0"; //$NON-NLS-1$
	public static final String FILE_SIZE = "FileSize"; //$NON-NLS-1$
	public static final String LAST_MODIFIED = "LastModified"; //$NON-NLS-1$
	public static final String INDEX_FILE_TYPE = "TITAN Log Viewer index file"; //$NON-NLS-1$

	// Component name length
	public static final int MAX_COMP_NAME = 255;
	
	// Reserved Keywords
	public static final String SUT = "System Under Test"; //$NON-NLS-1$
	public static final String MTC = "Main Test Component"; //$NON-NLS-1$
	public static final String HC = "Host Controller";
	public static final String SUT_REFERENCE = "system"; //$NON-NLS-1$
	public static final String HC_REFERENCE = "hc";
	public static final String MTC_REFERENCE = "mtc"; //$NON-NLS-1$
	
	// LogFileHandler
	public static final String DATETIME_FORMAT = "yyyy/MMM/dd HH:mm:ss.SSSSSS"; //$NON-NLS-1$
	public static final String TIME_FORMAT = "HH:mm:ss.SSSSSS"; //$NON-NLS-1$
	public static final String SECONDS_FORMAT = "s.SSSSSS"; //$NON-NLS-1$
	public static final int DATETIME_FORMAT_LENGTH = 27;
	public static final int TIME_FORMAT_LENGTH = 15;
	public static final int SECONDS_FORMAT_LENGTH = 8;

	/** TITAN 7.0 format */
	public static final int FILEFORMAT_1 = 1;
	/** TITAN 7.1 format */
	public static final int FILEFORMAT_2 = 2;
	 
	// Execution mode
	public static final String EXECUTION_MODE_SINGLE = "SINGLE"; //$NON-NLS-1$
	public static final String EXECUTION_MODE_PARALLEL = "PARALLEL"; //$NON-NLS-1$
	public static final String EXECUTION_MODE_PARALLEL_MERGED = "PARALLEL_MERGED"; //$NON-NLS-1$
	
	// Log message valid start single mode
	static final String[] MESSAGE_START_LINES_SINGLE = {
		"TTCN-3 Test Executor started in single mode."}; //$NON-NLS-1$
	
	// Log message valid start parallel mode	
	static final String[] MESSAGE_START_LINES_PARALLEL = {
		"TTCN-3 Host Controller started on", //$NON-NLS-1$
		"The address of MC was set to ", //this line is sample log specific ? //$NON-NLS-1$
		"TTCN-3 Main Test Component started on", //$NON-NLS-1$
		"TTCN-3 Parallel Test Component started on" //$NON-NLS-1$
		};
	
	// Regular expressions for verifying dates
	private static final String REGEXP_YEAR =   "[12][0-9]{3}"; //$NON-NLS-1$
	private static final String REGEXP_MONTH =  "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)"; //$NON-NLS-1$
	private static final String REGEXP_DATE =   "([012][0-9]|30|31)"; //$NON-NLS-1$
	private static final String REGEXP_HOUR =   "([01][0-9]|2[0-3])"; //$NON-NLS-1$
	private static final String REGEXP_MIN =    "[0-5][0-9]"; //$NON-NLS-1$
	private static final String REGEXP_SEC =    "[0-5][0-9]"; //$NON-NLS-1$
	private static final String REGEXP_US =     "[0-9]{6}"; //$NON-NLS-1$
	private static final String REGEXP_NUMBER = "(0|[1-9][0-9]*)"; //$NON-NLS-1$
	private static final String REGEXP_COLON =  "\\:"; //$NON-NLS-1$
	private static final String REGEXP_DOT =    "\\."; //$NON-NLS-1$
	private static final String REGEXP_SLASH =  "\\/"; //$NON-NLS-1$
	private static final String REGEXP_SPACE =  "\\ "; //$NON-NLS-1$ 
	
	public static final String REGEXP_TIME_FORMAT = REGEXP_HOUR + REGEXP_COLON + REGEXP_MIN + REGEXP_COLON + REGEXP_SEC + REGEXP_DOT + REGEXP_US;
	public static final String REGEXP_DATETIME_FORMAT =
			REGEXP_YEAR + REGEXP_SLASH + REGEXP_MONTH + REGEXP_SLASH + REGEXP_DATE + REGEXP_SPACE + REGEXP_TIME_FORMAT;
	public static final String REGEXP_SECONDS_FORMAT = REGEXP_NUMBER + REGEXP_DOT + REGEXP_US;
	
	// Silent Event types: categories
	public static final String EVENTTYPE_ACTION = "ACTION"; //$NON-NLS-1$
	public static final String EVENTTYPE_DEBUG = "DEBUG"; //$NON-NLS-1$
	public static final String EVENTTYPE_DEFAULTOP = "DEFAULTOP"; //$NON-NLS-1$
	public static final String EVENTTYPE_ERROR = "ERROR"; //$NON-NLS-1$
	public static final String EVENTTYPE_EXECUTOR = "EXECUTOR"; //$NON-NLS-1$
	public static final String EVENTTYPE_FUNCTION = "FUNCTION"; //$NON-NLS-1$
	public static final String EVENTTYPE_MATCHING = "MATCHING"; //$NON-NLS-1$
	public static final String EVENTTYPE_PARALLEL = "PARALLEL"; //$NON-NLS-1$
	public static final String EVENTTYPE_PORTEVENT = "PORTEVENT"; //$NON-NLS-1$
	public static final String EVENTTYPE_STATISTICS = "STATISTICS"; //$NON-NLS-1$
	public static final String EVENTTYPE_TESTCASE = "TESTCASE"; //$NON-NLS-1$
	public static final String EVENTTYPE_TIMEROP = "TIMEROP"; //$NON-NLS-1$
	public static final String EVENTTYPE_UNKNOWN = "UNKNOWN"; //$NON-NLS-1$
	public static final String EVENTTYPE_USER = "USER"; //$NON-NLS-1$
	public static final String EVENTTYPE_VERDICTOP = "VERDICTOP"; //$NON-NLS-1$
	public static final String EVENTTYPE_WARNING = "WARNING"; //$NON-NLS-1$
	
	// Silent Event types: sub categories
	private static final String SUB_EVENTTYPE_ACTIVATE = "ACTIVATE"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_DEACTIVATE = "DEACTIVATE"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_COMPONENT = "COMPONENT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_CONFIGDATA = "CONFIGDATA"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_DONE = "DONE"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_DUALRECV = "DUALRECV"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_DUALSEND = "DUALSEND"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_ENCDEC = "ENCDEC"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_EXIT = "EXIT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_EXTCOMMAND = "EXTCOMMAND"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_FINAL = "FINAL"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_FINISH = "FINISH"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_GETVERDICT = "GETVERDICT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_GUARD = "GUARD"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_LOGOPTIONS = "LOGOPTIONS"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MCRECV = "MCRECV"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MCSEND = "MCSEND"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MCSUCCESS = "MCSUCCESS"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MCUNSUCC = "MCUNSUCC"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MMRECV = "MMRECV"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MMSEND = "MMSEND"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MMSUCCESS = "MMSUCCESS"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MMUNSUCC = "MMUNSUCC"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_MQUEUE = "MQUEUE"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PCIN = "PCIN"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PCOUT = "PCOUT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PCSUCCESS = "PCSUCCESS"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PCUNSUCC = "PCUNSUCC"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PMIN = "PMIN"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PMOUT = "PMOUT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PMSUCCESS = "PMSUCCESS"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PMUNSUCC = "PMUNSUCC"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PQUEUE = "PQUEUE"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PORTCONN = "PORTCONN"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PORTMAP = "PORTMAP"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PTC = "PTC"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_PROBLEM = "PROBLEM"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_READ = "READ"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_SETVERDICT = "SETVERDICT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_START = "START"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_STATE = "STATE"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_STOP = "STOP"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_TIMEOUT = "TIMEOUT"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_RND = "RND"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_RUNTIME = "RUNTIME"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_UNQUALIFIED = "UNQUALIFIED"; //$NON-NLS-1$
	private static final String SUB_EVENTTYPE_VERDICT = "VERDICT"; //$NON-NLS-1$
	
	// Categories and sub-categories for silent events (new TITAN log format)
	public static final SortedMap<String, String[]> EVENT_CATEGORIES = new TreeMap<String, String[]>();
	static {
		EVENT_CATEGORIES.put(EVENTTYPE_ACTION, 
				new String[]{SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_DEBUG, 
				new String[]{SUB_EVENTTYPE_ENCDEC, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_DEFAULTOP, 
				new String[]{SUB_EVENTTYPE_ACTIVATE, SUB_EVENTTYPE_DEACTIVATE, SUB_EVENTTYPE_EXIT, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_ERROR, 
				new String[]{SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_EXECUTOR, 
				new String[]{SUB_EVENTTYPE_COMPONENT, SUB_EVENTTYPE_CONFIGDATA, SUB_EVENTTYPE_EXTCOMMAND, SUB_EVENTTYPE_LOGOPTIONS,
							 SUB_EVENTTYPE_RUNTIME, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_FUNCTION, 
				new String[]{SUB_EVENTTYPE_RND, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_MATCHING, 
				new String[]{SUB_EVENTTYPE_DONE, SUB_EVENTTYPE_MCSUCCESS, SUB_EVENTTYPE_MCUNSUCC, SUB_EVENTTYPE_MMSUCCESS, 
							 SUB_EVENTTYPE_MMUNSUCC, SUB_EVENTTYPE_PCSUCCESS, SUB_EVENTTYPE_PCUNSUCC, SUB_EVENTTYPE_PMSUCCESS,
							 SUB_EVENTTYPE_PMUNSUCC, SUB_EVENTTYPE_PROBLEM, SUB_EVENTTYPE_TIMEOUT, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_PARALLEL, 
				new String[]{SUB_EVENTTYPE_PTC,	SUB_EVENTTYPE_PORTCONN, SUB_EVENTTYPE_PORTMAP, SUB_EVENTTYPE_UNQUALIFIED}); 
		EVENT_CATEGORIES.put(EVENTTYPE_PORTEVENT, 
				new String[]{SUB_EVENTTYPE_DUALRECV, SUB_EVENTTYPE_DUALSEND, SUB_EVENTTYPE_MCRECV, SUB_EVENTTYPE_MCSEND,
							 SUB_EVENTTYPE_MMRECV, SUB_EVENTTYPE_MMSEND, SUB_EVENTTYPE_MQUEUE, SUB_EVENTTYPE_PCIN,
							 SUB_EVENTTYPE_PCOUT, SUB_EVENTTYPE_PMIN, SUB_EVENTTYPE_PMOUT, SUB_EVENTTYPE_PQUEUE,
							 SUB_EVENTTYPE_STATE, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_STATISTICS, 
				new String[]{SUB_EVENTTYPE_VERDICT, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_TESTCASE, 
				new String[]{SUB_EVENTTYPE_START, SUB_EVENTTYPE_FINISH, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_TIMEROP, 
				new String[]{SUB_EVENTTYPE_GUARD, SUB_EVENTTYPE_READ, SUB_EVENTTYPE_START, SUB_EVENTTYPE_STOP,
							 SUB_EVENTTYPE_TIMEOUT, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_UNKNOWN, 
				new String[]{});
		EVENT_CATEGORIES.put(EVENTTYPE_USER, 
				new String[]{SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_VERDICTOP, 
				new String[]{SUB_EVENTTYPE_FINAL, SUB_EVENTTYPE_GETVERDICT, SUB_EVENTTYPE_SETVERDICT, SUB_EVENTTYPE_UNQUALIFIED});
		EVENT_CATEGORIES.put(EVENTTYPE_WARNING, 
				new String[]{SUB_EVENTTYPE_UNQUALIFIED});
	}

	private Constants() {
	}
}
