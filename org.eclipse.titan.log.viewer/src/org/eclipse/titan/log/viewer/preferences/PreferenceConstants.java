/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences;

import java.io.File;

/**
 * Constant definitions for plug-in preferences
 */
public final class PreferenceConstants {

	public static final String PREF_KEY_LV_VERSION = "org.eclipse.titan.log.viewer.version"; //$NON-NLS-1$
	public static final String PREFERENCE_DELIMITER = ";"; //$NON-NLS-1$

	public static final String PREF_VERBOSE_ID = "org.eclipse.titan.log.viewer_verbose_printout_id"; //$NON-NLS-1$
	public static final String PREF_SUT_ID = "org.eclipse.titan.log.viewer_sut_id"; //$NON-NLS-1$

	public static final String PREF_FILTER_COMPONENT_ID = "org.eclipse.titan.log.viewer_filter_component_id"; //$NON-NLS-1$
	public static final String PREF_FILTER_SIGNAL_ID = "org.eclipse.titan.log.viewer_filter_signals_id"; //$NON-NLS-1$
	public static final String PREF_FILTER_FUNCTION_ID = "org.eclipse.titan.log.viewer_filter_functions_id"; //$NON-NLS-1$
	public static final String PREF_COMPONENT_ORDER_ID = "org.eclipse.titan.log.viewer_component_order_id"; //$NON-NLS-1$
	public static final String PREF_ASN1_DEFAULT_FORMAT = "org.eclipse.titan.log.viewer_asn11_default"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_ID = "org.eclipse.titan.log.viewer_filter_silentevent_id"; //$NON-NLS-1$
	public static final String PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID = "org.eclipse.titan.log.viewer_add_comp_to_vis_order_rep_app_id"; //$NON-NLS-1$
	public static final String PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID = "org.eclipse.titan.log.viewer_add_comp_to_vis_order_open_prop_id"; //$NON-NLS-1$
	public static final String PREF_OPEN_MSCVIEW_DISPLAY = "org.eclipse.titan.log.viewer_open_mscview_display"; //$NON-NLS-1$
	public static final String PREF_MSCVIEW_DEFAULT = "org.eclipse.titan.log.viewer_mscview_default"; //$NON-NLS-1$
	public static final String PREF_TESTCASETAB_DEFAULT = "org.eclipse.titan.log.viewer_testcasetab_default"; //$NON-NLS-1$
	public static final String PREF_PROJECTTAB_DEFAULT = "org.eclipse.titan.log.viewer_projecttab_default"; //$NON-NLS-1$
	public static final String PREF_HIGHLIGHT_KEYWORD_ID = "org.eclipse.titan.log.viewer_highlight_keyword_id"; //$NON-NLS-1$
	public static final String PREF_USE_HIGHLIGHT_ID = "org.eclipse.titan.log.viewer_use_highlight_id"; //$NON-NLS-1$
	
	public static final String PREF_SETVERDICT_ERROR_ID = "org.eclipse.titan.log.setverdict_error_id"; //$NON-NLS-1$
	public static final String PREF_SETVERDICT_FAIL_ID = "org.eclipse.titan.log.setverdict_fail_id"; //$NON-NLS-1$
	public static final String PREF_SETVERDICT_INCONC_ID = "org.eclipse.titan.log.setverdict_inconc_id"; //$NON-NLS-1$
	public static final String PREF_SETVERDICT_NONE_ID = "org.eclipse.titan.log.setverdict_none_id"; //$NON-NLS-1$
	public static final String PREF_SETVERDICT_PASS_ID = "org.eclipse.titan.log.setverdict_pass_id"; //$NON-NLS-1$
	public static final String PREF_ERROR_CAUSED_BY_ID = "org.eclipse.titan.log.error_caused_by_id"; //$NON-NLS-1$
	public static final String PREF_FAIL_CAUSED_BY_ID = "org.eclipse.titan.log.fail_caused_by_id"; //$NON-NLS-1$
	public static final String PREF_CONNECTING_PORTS_ID = "org.eclipse.titan.log.connecting_ports_id"; //$NON-NLS-1$
	public static final String PREF_MAPPING_PORTS_ID = "org.eclipse.titan.log.mapping_ports_id"; //$NON-NLS-1$

	
	// Silent Events
	public static final String PREF_SILENT_EVENTS_CATEGORIES = "org.eclipse.titan.log.viewer_filter_silent_event_categories"; //$NON-NLS-1$
	public static final String SILENT_EVENTS_DEFAULT_CATEGORIES = "ACTION=false;ACTION_UNQUALIFIED=false;" +
			"DEBUG=false;DEBUG_ENCDEC=false;DEBUG_UNQUALIFIED=false;" +
			"DEFAULTOP=false;DEFAULTOP_ACTIVATE=false;DEFAULTOP_DEACTIVATE=false;DEFAULTOP_EXIT=false;DEFAULTOP_UNQUALIFIED=false;" +
			"ERROR=false;" +
			"ERROR_UNQUALIFIED=false;EXECUTOR=false;EXECUTOR_COMPONENT=false;EXECUTOR_CONFIGDATA=false;EXECUTOR_EXTCOMMAND=false;" +
			"EXECUTOR_LOGOPTIONS=false;EXECUTOR_RUNTIME=false;EXECUTOR_UNQUALIFIED=false;" +
			"FUNCTION=false;FUNCTION_RND=false;FUNCTION_UNQUALIFIED=false;" +
			"MATCHING=false;MATCHING_DONE=false;MATCHING_MCSUCCESS=false;MATCHING_MCUNSUCC=false;MATCHING_MMSUCCESS=false;" +
			"MATCHING_MMUNSUCC=false;MATCHING_PCSUCCESS=false;MATCHING_PCUNSUCC=false;MATCHING_PMSUCCESS=false;MATCHING_PMUNSUCC=false;" +
			"MATCHING_PROBLEM=false;MATCHING_TIMEOUT=false;MATCHING_UNQUALIFIED=false;" +
			"PARALLEL=false;PARALLEL_PTC=false;PARALLEL_PORTCONN=false;PARALLEL_PORTMAP=false;PARALLEL_UNQUALIFIED=false;" +
			"PORTEVENT=false;PORTEVENT_DUALRECV=false;PORTEVENT_DUALSEND=false;PORTEVENT_MCRECV=false;PORTEVENT_MCSEND=false;" +
			"PORTEVENT_MMRECV=false;PORTEVENT_MMSEND=false;PORTEVENT_MQUEUE=false;PORTEVENT_PCIN=false;PORTEVENT_PCOUT=false;" +
			"PORTEVENT_PMIN=false;PORTEVENT_PMOUT=false;PORTEVENT_PQUEUE=false;PORTEVENT_STATE=false;PORTEVENT_UNQUALIFIED=false;" +
			"STATISTICS=false;STATISTICS_VERDICT=false;STATISTICS_UNQUALIFIED=false;" +
			"TESTCASE=false;TESTCASE_START=false;TESTCASE_FINISH=false;TESTCASE_UNQUALIFIED=false;" +
			"TIMEROP=false;TIMEROP_GUARD=false;TIMEROP_READ=false;TIMEROP_START=false;TIMEROP_STOP=false;TIMEROP_TIMEOUT=false;" +
			"TIMEROP_UNQUALIFIED=false;UNKNOWN=false;USER=false;USER_UNQUALIFIED=false;" +
			"VERDICTOP=false;VERDICTOP_FINAL=false;VERDICTOP_GETVERDICT=false;VERDICTOP_SETVERDICT=false;" +
			"VERDICTOP_UNQUALIFIED=false;WARNING=false;WARNING_UNQUALIFIED=false"; //$NON-NLS-1$

	public static final String SILENT_EVENTS_KEY_VALUE_DELIM = "="; //$NON-NLS-1$
	public static final String SILENT_EVENTS_UNDERSCORE = "_"; //$NON-NLS-1$
	
	// Old silent events
	public static final String PREF_FILTER_SILENTEVENT_0 = "org.eclipse.titan.log.viewer_filter_silentevent_error"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_1 = "org.eclipse.titan.log.viewer_filter_silentevent_function"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_2 = "org.eclipse.titan.log.viewer_filter_silentevent_action"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_3 = "org.eclipse.titan.log.viewer_filter_silentevent_user"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_4 = "org.eclipse.titan.log.viewer_filter_silentevent_warning"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_5 = "org.eclipse.titan.log.viewer_filter_silentevent_unknown"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_6 = "org.eclipse.titan.log.viewer_filter_silentevent_executor"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_7 = "org.eclipse.titan.log.viewer_filter_silentevent_timerop"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_8 = "org.eclipse.titan.log.viewer_filter_silentevent_verdictop"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_9 = "org.eclipse.titan.log.viewer_filter_silentevent_deafultop"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_10 = "org.eclipse.titan.log.viewer_filter_silentevent_portevent"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_11 = "org.eclipse.titan.log.viewer_filter_silentevent_testcase"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_12 = "org.eclipse.titan.log.viewer_filter_silentevent_statistics"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_13 = "org.eclipse.titan.log.viewer_filter_silentevent_parallel"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_14 = "org.eclipse.titan.log.viewer_filter_silentevent_matching"; //$NON-NLS-1$
	public static final String PREF_FILTER_SILENTEVENT_15 = "org.eclipse.titan.log.viewer_filter_silentevent_debug"; //$NON-NLS-1$
	
	// Default values
	public static final String SUT_DEFAULT = ""; //$NON-NLS-1$
	public static final String SUT_DESCRIPTION = "System Under Test"; //$NON-NLS-1$
	public static final String MTC_DESCRIPTION = "Main Test Component"; //$NON-NLS-1$
	public static final String MTC_NAME = "mtc"; //$NON-NLS-1$
	public static final String COMP_ORDER_DEFAULT = MTC_DESCRIPTION + File.pathSeparator + SUT_DESCRIPTION + File.pathSeparator;
	
	// Date and time formats
	public static final String DATETIME_FORMAT = "yyyy/MMM/dd HH:mm:ss.SSSSSS"; //$NON-NLS-1$
	public static final String TIME_FORMAT = "HH:mm:ss.SSSSSS"; //$NON-NLS-1$
	public static final String SECONDS_FORMAT = "s.SSSSSS"; //$NON-NLS-1$
	public static final int TIME_STAMP_OFFSET = 0;
	
	// FieldEditors (Add File and Add Folder)
	public static final String PREF_ADD_LOG_FILE_LAST_DIR = "org.eclipse.titan.log.viewer_log_file_last_dir_id"; //$NON-NLS-1$	
	public static final String PREF_ADD_LOG_FOLDER_LAST_DIR = "org.eclipse.titan.log.viewer_log_folder_last_dir_id"; //$NON-NLS-1$
	public static final String PREF_ASN1_TREEVIEW = "org.eclipse.titan.log.viewer_asn1_treeview"; //$NON-NLS-1$
	public static final String PREF_ASN1_TEXTVIEW = "org.eclipse.titan.log.viewer_asn1_textview"; //$NON-NLS-1$
	
	//The display start of the MSCView
	public static final String PREF_MSCVIEW_TOP = "org.eclipse.titan.log.viewer_mscview_top"; //$NON-NLS-1$
	public static final String PREF_MSCVIEW_BOTTOM = "org.eclipse.titan.log.viewer_mscview_bottom"; //$NON-NLS-1$
	public static final String PREF_MSCVIEW_FIRST_SETVERDICT = "org.eclipse.titan.log.viewer_mscview_first_setverdict"; //$NON-NLS-1$
	public static final int MSCVIEW_TOP = 0;
	public static final int MSCVIEW_BOTTOM = 1;
	public static final int MSCVIEW_FIRST_VERDICT = 2;
	
	// default behavior 
	public static final String PREF_MSCVIEW_DEFAULT_TEXT = "org.eclipse.titan.log.viewer_mscview_default_text";
	public static final String PREF_MSCVIEW_DEFAULT_VALUEVIEW = "org.eclipse.titan.log.viewer_mscview_default_valueview";
	public static final String PREF_TESTCASETAB_DEFAULT_TEXT = "org.eclipse.titan.log.viewer_testcasetab_default_text";
	public static final String PREF_TESTCASETAB_DEFAULT_OPEN_MSCVIEW = "org.eclipse.titan.log.viewer_testcasetab_default_open_mscview";
	public static final String PREF_PROJECTTAB_DEFAULT_TEXT = "org.eclipse.titan.log.viewer_projecttab_default_text";
	public static final String PREF_PROJECTTAB_DEFAULT_EXTRACT_TESTCASES = "org.eclipse.titan.log.viewer_projecttab_default_extract_testcases";
	public static final int DEFAULT_TEXT = 0;
	public static final int MSCVIEW_DEFAULT_VALUEVIEW = 1;
	public static final int TESTCASETAB_DEFAULT_OPEN_MSCVIEW = 2;
	public static final int PROJECTTAB_DEFAULT_EXTRACT_TESTCASES = 3;

	
	// Last directory for import/export
	public static final String PREF_IMPORT_LAST_DIR = "org.eclipse.titan.log.viewer_log_import_last_dir_id"; //$NON-NLS-1$ 
	public static final String PREF_EXPORT_LAST_DIR = "org.eclipse.titan.log.viewer_log_export_last_dir_id"; //$NON-NLS-1$	
	
	public static final String ASN1_TREEVIEW = "tree"; //$NON-NLS-1$
	public static final String ASN1_TEXTVIEW = "text"; //$NON-NLS-1$
	
	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String FALSE = "false"; //$NON-NLS-1$
	
	public static final String PREF_ADD_COMP_TO_VIS_ORDER_REPLACE = "org.eclipse.titan.log.viewer_add_to_comp_replace"; //$NON-NLS-1$
	public static final String PREF_ADD_COMP_TO_VIS_ORDER_APPEND = "org.eclipse.titan.log.viewer_add_to_comp_append"; //$NON-NLS-1$
	
	// Preference/Properties pages ID:s
	public static final String PAGE_ID_GENERAL_PAGE = "org.eclipse.titan.log.viewer.properties.GeneralPage"; //$NON-NLS-1$
	public static final String PAGE_ID_COMP_VIS_ORDER_PAGE = "org.eclipse.titan.log.viewer.properties.CompVisOrderPage"; //$NON-NLS-1$
	public static final String PAGE_ID_FILTERED_COMP_PAGE = "org.eclipse.titan.log.viewer.properties.FilteredCompPage"; //$NON-NLS-1$	
	public static final String PAGE_ID_FILTERED_SIGNALS_PAGE = "org.eclipse.titan.log.viewer.properties.FilteredSignalsPage"; //$NON-NLS-1$
	public static final String PAGE_ID_FILTERED_FUNCTIONS_PAGE = "org.eclipse.titan.log.viewer.properties.FilteredFunctionsPage"; //$NON-NLS-1$
	public static final String PAGE_ID_FILTERED_SILENTEVENT_PAGE = "org.eclipse.titan.log.viewer.properties.FilteredSilentEventsPage"; //$NON-NLS-1$
	public static final String PAGE_ID_HIGHLIGHT_KEYWORDS_PAGE = "org.eclipse.titan.log.viewer.properties.HighlightKeywordsPage"; //$NON-NLS-1$
	public static final String PAGE_ID_SETVERDICT_PAGE = "org.eclipse.titan.log.viewer.properties.SetverdictPreferencePage"; //$NON-NLS-1$
	public static final String PAGE_ID_FILTERED_PORTS_PAGE = "org.eclipse.titan.log.viewer.properties.FilteredPortsPreferencePage"; //$NON-NLS-1$
	public static final String PAGE_ID_MESSAGE_DECIPHERING_PAGE =
			"org.eclipse.titan.log.viewer.properties.MessageDecipheringPreferencePage"; //$NON-NLS-1$


	// Filter Constants
	public static final int FILTER_EQUALS = 0;
	public static final int FILTER_START_WITH = 1;
	public static final int FILTER_END_WITH = 2;
	public static final int FILTER_CONTAINS = 3;
	
	//Default values setverdict
	
	public static final String SETVERDICT_ERROR_DEFAULT = "Dynamic test case error" + File.pathSeparator; //$NON-NLS-1$
	public static final String SETVERDICT_FAIL_DEFAULT = 
		"check failed" + File.pathSeparator + //$NON-NLS-1$
		"problems" + File.pathSeparator + //$NON-NLS-1$
		"nothing received (fail)" + File.pathSeparator + //$NON-NLS-1$
		"failed VP" + File.pathSeparator + //$NON-NLS-1$
		"is done." + File.pathSeparator + //$NON-NLS-1$
		"does not match" + File.pathSeparator + //$NON-NLS-1$
		"Setting verdict to fail" + File.pathSeparator + //$NON-NLS-1$
		"Incorrect result checking " + File.pathSeparator + //$NON-NLS-1$
		"mismatch" + File.pathSeparator + //$NON-NLS-1$
		"No match" + File.pathSeparator + //$NON-NLS-1$
		"unmatched" + File.pathSeparator; //$NON-NLS-1$
	
	public static final String KEYWORD_COLOR_SEPARATOR = "#"; //$NON-NLS-1$
	public static final String RGB_COLOR_SEPARATOR = ","; //$NON-NLS-1$

	private PreferenceConstants() {
	}
}
