/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;


/**
 * This class is used for initializing the internal values to their default state.
 * 
 * @author Kristof Szabados
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public final void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = getPreference();

		final String ttcn3Dir = System.getenv("TTCN3_DIR");
		if (ttcn3Dir != null) {
			preferenceStore.setDefault(PreferenceConstants.TITAN_INSTALLATION_PATH, ttcn3Dir);
		}

		final String licenseFile = System.getenv("TTCN3_LICENSE_FILE");
		if (licenseFile != null) {
			preferenceStore.setDefault(PreferenceConstants.LICENSE_FILE_PATH, licenseFile);
		}

		preferenceStore.setDefault(PreferenceConstants.COMPILERMARKERSAFTERANALYZATION,  PreferenceConstantValues.COMPILEROPTIONOUTDATE);
		preferenceStore.setDefault(PreferenceConstants.ONTHEFLYMARKERSAFTERCOMPILER,  PreferenceConstantValues.ONTHEFLYOPTIONSTAY);
		preferenceStore.setDefault(PreferenceConstants.REPORTPROGRAMERRORWITHMARKER, false);
		preferenceStore.setDefault(PreferenceConstants.TREATONTHEFLYERRORSFATALFORBUILD, false);
		preferenceStore.setDefault(PreferenceConstants.USEONTHEFLYPARSING, true);
		preferenceStore.setDefault(PreferenceConstants.ENABLERISKYREFACTORING, false);
		preferenceStore.setDefault(PreferenceConstants.USEINCREMENTALPARSING, false);
		preferenceStore.setDefault(PreferenceConstants.DELAYSEMANTICCHECKINGTILLSAVE, true);
		preferenceStore.setDefault(PreferenceConstants.MINIMISEMEMORYUSAGE, false);
		preferenceStore.setDefault(PreferenceConstants.RECONCILERTIMEOUT, 1);
		preferenceStore.setDefault(PreferenceConstants.PROCESSINGUNITSTOUSE, PreferenceConstantValues.AVAILABLEPROCESSORS);
		preferenceStore.setDefault(PreferenceConstants.CONSOLE_ACTION_BEFORE_BUILD, PreferenceConstantValues.BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS);

		//		content assistance
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_SINGLE_PROPOSAL_INSERTION, false);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_COMMON_PREFIX_INSERTION, false);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_PROPOSAL_SORTING, PreferenceConstantValues.SORT_BY_RELEVANCE);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION, true);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION_DELAY, 100);

		//		folding
		preferenceStore.setDefault(PreferenceConstants.FOLDING_ENABLED, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_COMMENTS, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_STATEMENT_BLOCKS, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_PARENTHESIS, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_DISTANCE, 3);

		preferenceStore.setDefault(PreferenceConstants.MATCHING_BRACKET_ENABLED, true);

		// 		indentation
		preferenceStore.setDefault(PreferenceConstants.INDENTATION_TAB_POLICY, PreferenceConstantValues.TAB_POLICY_2);
		preferenceStore.setDefault(PreferenceConstants.INDENTATION_SIZE, "2");

		//		typing
		preferenceStore.setDefault(PreferenceConstants.CLOSE_APOSTROPHE, true);
		preferenceStore.setDefault(PreferenceConstants.CLOSE_PARANTHESES, true);
		preferenceStore.setDefault(PreferenceConstants.CLOSE_SQUARE, true);
		preferenceStore.setDefault(PreferenceConstants.CLOSE_BRACES, true);
		preferenceStore.setDefault(PreferenceConstants.AUTOMATICALLY_MOVE_BRACES, true);
		//		matching brackets
		preferenceStore.setDefault(PreferenceConstants.COLOR_MATCHING_BRACKET, StringConverter.asString(PreferenceConstantValues.GREY20));
		preferenceStore.setDefault(PreferenceConstants.EXCLUDED_RESOURCES, "");
		preferenceStore.setDefault(PreferenceConstants.T3DOC_ENABLE, false);

		markOccurrences(preferenceStore);
		onTheFlyChecker(preferenceStore);
		titanActions(preferenceStore);
		outline(preferenceStore);
		namingConventionPreferences(preferenceStore);
		color(preferenceStore);
		debug(preferenceStore);
		findDefinition(preferenceStore);
	}

	private void markOccurrences(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_ENABLED, true);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_DELAY, 300);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_KEEP_MARKS, true);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS, true);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS, true);
	}

	private void onTheFlyChecker(final IPreferenceStore preferenceStore) {
		//on-the-fly checker
		preferenceStore.setDefault(PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, GeneralConstants.ERROR);
		preferenceStore.setDefault(PreferenceConstants.REPORTUNUSEDMODULEIMPORTATION, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTUNUSEDGLOBALDEFINITION, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORTUNUSEDLOCALDEFINITION, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTUNUSEDFUNCTIONRETURNVALUES, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTMISSINGIMPORTEDMODULE, GeneralConstants.ERROR);
		preferenceStore.setDefault(PreferenceConstants.REPORTMISSINGFRIENDMODULE, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.DEFAULTASOPTIONAL, false);
		preferenceStore.setDefault(PreferenceConstants.REPORTINCORRECTSHIFTROTATESIZE, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORT_IGNORED_PREPROCESSOR_DIRECTIVES, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTINFINITELOOPS, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORTREADONLY, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.ERROR);
		preferenceStore.setDefault(PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORT_STRICT_CONSTANTS, false);
		preferenceStore.setDefault(PreferenceConstants.REPORT_GOTO, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_NONPRIVATE_PRIVATE, GeneralConstants.IGNORE);

		preferenceStore.setDefault(PreferenceConstants.REPORT_TOOMANY_PARAMETERS, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_TOOMANY_PARAMETERS_SIZE, 7);
		preferenceStore.setDefault(PreferenceConstants.REPORT_EMPTY_STATEMENT_BLOCK, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_TOOMANY_STATEMENTS, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_TOOMANY_STATEMENTS_SIZE, 150);
		preferenceStore.setDefault(PreferenceConstants.REPORT_IF_WITHOUT_ELSE, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_SETVERDICT_WITHOUT_REASON, GeneralConstants.IGNORE);
	}

	private void titanActions(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.TITANACTIONS_PROCESSEXCLUDEDRESOURCES, false);
		preferenceStore.setDefault(PreferenceConstants.TITANACTIONS_DEFAULT_AS_OMIT, false);
	}

	private void outline(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_SORTED, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_CATEGORISED, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_GROUPED, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_HIDE_FUNCTIONS, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_HIDE_TEMPLATES, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_HIDE_TYPES, false);
	}

	private void namingConventionPreferences(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTIONPROBLEMS, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_TTCN3MODULE, ".*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_ASN1MODULE, ".*");

		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_ALTSTEP, "as_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_CONSTANT, "cg_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALCONSTANT, "ec_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_FUNCTION, "f_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALFUNCTION, "ef_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_MODULEPAR, "tsp.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_PORT, ".*_PT");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TEMPLATE, "t.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_TESTCASE, "tc_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TIMER, "T.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_TYPE, ".*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GROUP, "[A-Z].*");

		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_CONSTANT, "cl.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARIABLE, "vl.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TEMPLATE, "t.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE, "vt.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TIMER, "TL_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_FORMAL_PARAMETER, "pl_.*");

		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_CONSTANT, "c_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_VARIABLE, "v_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_TIMER, "T_.*");

		preferenceStore.setDefault(PreferenceConstants.REPORT_MODULENAME_IN_DEFINITION, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_VISIBILITY_IN_DEFINITION, GeneralConstants.IGNORE);
	}

	private void color(final IPreferenceStore preferenceStore) {
		//		general settings
		preferenceStore.setDefault(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.VIOLETRED4));
		preferenceStore.setDefault(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.GREY20));
		preferenceStore.setDefault(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.DARKGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));

		//		 asn1 specific
		preferenceStore.setDefault(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLACK));
		preferenceStore.setDefault(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.BOLD, true);

		preferenceStore.setDefault(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.VIOLETRED4));
		preferenceStore.setDefault(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.ROYALBLUE4));
		preferenceStore.setDefault(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_STATUS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.SADDLE_BROWN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_STATUS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_STATUS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TAG + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.DARKGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TAG + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TAG + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.SADDLE_BROWN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.CHOCOLATE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.ROYALBLUE4));
		preferenceStore.setDefault(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));

		// config specific
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLACK));
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.BOLD, true);

		preferenceStore.setDefault(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.SEAGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.USEBACKGROUNDCOLOR, true);
		preferenceStore.setDefault(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.BOLD, true);

		preferenceStore.setDefault(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.SEAGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.BOLD, true);

		preferenceStore.setDefault(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.SADDLE_BROWN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));

		// TTCN-3 specific
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLACK));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BOLD, true);

		preferenceStore.setDefault(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.ROYALBLUE4));
		preferenceStore.setDefault(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLACK));
		preferenceStore.setDefault(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.BOLD, true);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.ROYALBLUE4));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TYPE + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BROWN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TYPE + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TYPE + PreferenceConstants.BOLD, true);

		preferenceStore.setDefault(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLUE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLUE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLUE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLUE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLUE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLUE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.BLACK));
		preferenceStore.setDefault(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.DARKGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.DARKGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
		preferenceStore.setDefault(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.FOREGROUND,
				StringConverter.asString(PreferenceConstantValues.DARKGREEN));
		preferenceStore.setDefault(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		preferenceStore.setDefault(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.BACKGROUND,
				StringConverter.asString(PreferenceConstantValues.WHITE));
	}

	private void debug(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.DISPLAYDEBUGINFORMATION, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_PREFERENCE_PAGE_ENABLED, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_TIMESTAMP, true);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_AST_ELEM, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_ANTLR_V4, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_LOG_TO_SYSOUT, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_LOAD_TOKENS_TO_PROCESS_IN_A_ROW, 100);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_LOAD_SLEEP_BETWEEN_FILES, 10);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_LOAD_YIELD_BETWEEN_CHECKS, true);
	}

	private void findDefinition(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_WS, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_FUNCT, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_GLOBAL, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_MODULES, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_TYPES, true);
	}

	public IPreferenceStore getPreference() {
		return Activator.getDefault().getPreferenceStore();
	}
}
