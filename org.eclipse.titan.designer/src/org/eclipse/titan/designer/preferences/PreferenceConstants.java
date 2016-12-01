/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
//INTERNAL CONSTANTS HANDLE WITH CARE
public final class PreferenceConstants {
	public static final String TITAN_INSTALLATION_PATH = ProductConstants.PRODUCT_ID_DESIGNER + ".TTCN3_INSTALL_DIR";
	public static final String LICENSE_FILE_PATH = ProductConstants.PRODUCT_ID_DESIGNER + ".LICENSE_FILE";
	public static final String COMPILERMARKERSAFTERANALYZATION = ProductConstants.PRODUCT_ID_DESIGNER + ".compilerMarkersAfterAnalyzation";
	public static final String ONTHEFLYMARKERSAFTERCOMPILER = ProductConstants.PRODUCT_ID_DESIGNER + ".ontheflyMarkersAfterCompiler";
	public static final String REPORTPROGRAMERRORWITHMARKER = ProductConstants.PRODUCT_ID_DESIGNER + ".reportProgramErrorWithMarker";
	public static final String TREATONTHEFLYERRORSFATALFORBUILD = ProductConstants.PRODUCT_ID_DESIGNER + ".treatOnTheFlyErrorsasFatalForBuild";
	public static final String CHECKFORLOWMEMORY = ProductConstants.PRODUCT_ID_DESIGNER + ".checkForLowMemory";
	public static final String USEONTHEFLYPARSING = ProductConstants.PRODUCT_ID_DESIGNER + ".useOnTheFlyParsing";
	public static final String USEINCREMENTALPARSING = ProductConstants.PRODUCT_ID_DESIGNER + ".useIncrementalParsing";
	public static final String DELAYSEMANTICCHECKINGTILLSAVE = ProductConstants.PRODUCT_ID_DESIGNER + ".delaySemantiCheckingTillSave";
	public static final String MINIMISEMEMORYUSAGE = ProductConstants.PRODUCT_ID_DESIGNER + ".minimiseMemoryUsage";
	public static final String RECONCILERTIMEOUT = ProductConstants.PRODUCT_ID_DESIGNER + ".reconcilerTimeout";
	public static final String PROCESSINGUNITSTOUSE = ProductConstants.PRODUCT_ID_DESIGNER + ".processingUnitsToUse";
	//Actions on console before build
	public static final String CONSOLE_ACTION_BEFORE_BUILD = ProductConstants.PRODUCT_ID_DESIGNER + ".consoleActionBeforeBuild";


	// content assistance
	public static final String CONTENTASSISTANT_SINGLE_PROPOSAL_INSERTION = ProductConstants.PRODUCT_ID_DESIGNER + ".singleProposalInsertion";
	public static final String CONTENTASSISTANT_COMMON_PREFIX_INSERTION = ProductConstants.PRODUCT_ID_DESIGNER + ".commonPrefixInsertion";
	public static final String CONTENTASSISTANT_PROPOSAL_SORTING = ProductConstants.PRODUCT_ID_DESIGNER + ".proposalSorting";
	public static final String CONTENTASSISTANT_AUTO_ACTIVATION = ProductConstants.PRODUCT_ID_DESIGNER + ".autoActivation";
	public static final String CONTENTASSISTANT_AUTO_ACTIVATION_DELAY = ProductConstants.PRODUCT_ID_DESIGNER + ".autoActivationDelay";

	//export
	public static final String EXPORT_EXCLUDE_WORKING_DIRECTORY_CONTENTS = ProductConstants.PRODUCT_ID_DESIGNER + ".excludeWorkingDirectoryContents";
	public static final String EXPORT_EXCLUDE_DOT_RESOURCES = ProductConstants.PRODUCT_ID_DESIGNER + ".excludeDotResources";
	public static final String EXPORT_EXCLUDE_LINKED_CONTENTS = ProductConstants.PRODUCT_ID_DESIGNER + ".excludeLinkedContents";
	public static final String EXPORT_SAVE_DEFAULT_VALUES = ProductConstants.PRODUCT_ID_DESIGNER + ".saveDefaultValues";
	public static final String EXPORT_PACK_ALL_PROJECTS_INTO_ONE = ProductConstants.PRODUCT_ID_DESIGNER + ".packAllProjectsIntoOne";
	public static final String USE_TPD_NAME = ProductConstants.PRODUCT_ID_DESIGNER + ".useTpdName";
	public static final String ORIG_TPD_URI = ProductConstants.PRODUCT_ID_DESIGNER + ".origTpdURI";
	
	/**
	 * Flag: Automatic export required
	 */
	public static final String EXPORT_AUTOMATIC_EXPORT = ProductConstants.PRODUCT_ID_DESIGNER + ".automaticExport";
	/**
	 * Flag: Requests new location for the tpds at the first automatic save. "true" if yes, "false" otherwise
	 */
	public static final String EXPORT_REQUEST_LOCATION = ProductConstants.PRODUCT_ID_DESIGNER + ".requestLocation";
	
	// folding
	public static final String FOLDING_ENABLED = ProductConstants.PRODUCT_ID_DESIGNER + ".foldingEnabledPreference";
	public static final String FOLD_COMMENTS = ProductConstants.PRODUCT_ID_DESIGNER + ".foldComments";
	public static final String FOLD_STATEMENT_BLOCKS = ProductConstants.PRODUCT_ID_DESIGNER + ".foldStatementBlocks";
	public static final String FOLD_PARENTHESIS = ProductConstants.PRODUCT_ID_DESIGNER + ".foldParenthesis";
	public static final String FOLD_DISTANCE = ProductConstants.PRODUCT_ID_DESIGNER + ".distance";

	// Indentation
	public static final String INDENTATION_PREFIX = ProductConstants.PRODUCT_ID_DESIGNER + ".indentation";
	public static final String INDENTATION_TAB_POLICY = INDENTATION_PREFIX + ".indentationTabPolicy";
	public static final String INDENTATION_SIZE = INDENTATION_PREFIX + ".indentationSize";

	// Typing
	public static final String TYPING_PREFIX = ProductConstants.PRODUCT_ID_DESIGNER + ".typing";
	public static final String CLOSE_APOSTROPHE = TYPING_PREFIX + ".close_apostrophe";
	public static final String CLOSE_PARANTHESES = TYPING_PREFIX + ".close_parantheses";
	public static final String CLOSE_SQUARE = TYPING_PREFIX + ".close_square";
	public static final String CLOSE_BRACES = TYPING_PREFIX + ".close_braces";
	public static final String AUTOMATICALLY_MOVE_BRACES = TYPING_PREFIX + ".automatically_move_on_new_line_braces";
	// matching brackets
	public static final String MATCHING_BRACKET_ENABLED = ProductConstants.PRODUCT_ID_DESIGNER + ".matchingBracketEnabled";
	// mark occurrences
	public static final String MARK_OCCURRENCES_ENABLED = ProductConstants.PRODUCT_ID_DESIGNER + ".markOccurrencesEnabled";
	public static final String MARK_OCCURRENCES_DELAY = ProductConstants.PRODUCT_ID_DESIGNER + ".markOccurrencesDelay";
	public static final String MARK_OCCURRENCES_KEEP_MARKS = ProductConstants.PRODUCT_ID_DESIGNER + ".markOccurrencesKeepMarks";
	public static final String MARK_OCCURRENCES_TTCN3_ASSIGNMENTS = ProductConstants.PRODUCT_ID_DESIGNER + ".markOccurrencesTtcn3Assignments";
	public static final String MARK_OCCURRENCES_ASN1_ASSIGNMENTS = ProductConstants.PRODUCT_ID_DESIGNER + ".markOccurrencesAsn1Assignments";
	// excluded resources
	public static final String EXCLUDED_RESOURCES = ProductConstants.PRODUCT_ID_DESIGNER + ".excludedResources";
	// on-the-fly checker
	public static final String REPORTUNSUPPORTEDCONSTRUCTS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportUnsupportedConstructs";
	public static final String REPORTMISSINGIMPORTEDMODULE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportMissingImportedModule";
	public static final String REPORTMISSINGFRIENDMODULE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportMissingFriendModule";
	public static final String REPORTUNUSEDMODULEIMPORTATION = ProductConstants.PRODUCT_ID_DESIGNER + ".reportUnusedModuleImportation";
	public static final String REPORTUNUSEDGLOBALDEFINITION = ProductConstants.PRODUCT_ID_DESIGNER + ".reportUnusedGlobalDefinition";
	public static final String REPORTUNUSEDLOCALDEFINITION = ProductConstants.PRODUCT_ID_DESIGNER + ".reportUnusedLocalDefinition";
	public static final String REPORTUNUSEDFUNCTIONRETURNVALUES = ProductConstants.PRODUCT_ID_DESIGNER + ".reportUnusedFunctionReturnValues";
	public static final String DEFAULTASOPTIONAL = ProductConstants.PRODUCT_ID_DESIGNER + ".defaultAsOptional";
	// public static boolean defaultAsOptional = false;
	public static final String REPORTINCORRECTSHIFTROTATESIZE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportIncorrectShiftRotateSize";
	public static final String REPORTUNNECESSARYCONTROLS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportUnnecessaryControls";
	public static final String REPORT_IGNORED_PREPROCESSOR_DIRECTIVES = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportIgnoredPreprocessorDirectives";
	public static final String REPORTINFINITELOOPS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportInifinteLoops";
	public static final String REPORTREADONLY = ProductConstants.PRODUCT_ID_DESIGNER + ".reportReadOnly";
	public static final String REPORTTYPECOMPATIBILITY = ProductConstants.PRODUCT_ID_DESIGNER + ".reportTypeCompatibility";
	public static final String REPORTERRORSINEXTENSIONSYNTAX = ProductConstants.PRODUCT_ID_DESIGNER + ".reportErrorsInExtensionSyntax";
	public static final String REPORT_STRICT_CONSTANTS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportStrictConstants";
	public static final String REPORT_GOTO = ProductConstants.PRODUCT_ID_DESIGNER + ".reportGOTO";
	public static final String REPORT_NONPRIVATE_PRIVATE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNonPrivatePrivate";

	public static final String REPORT_TOOMANY_PARAMETERS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportTooManyParameters";
	public static final String REPORT_TOOMANY_PARAMETERS_SIZE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportTooManyParametersSize";
	public static final String REPORT_EMPTY_STATEMENT_BLOCK = ProductConstants.PRODUCT_ID_DESIGNER + ".reportEmptyStatementBlock";
	public static final String REPORT_TOOMANY_STATEMENTS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportTooManyStatements";
	public static final String REPORT_TOOMANY_STATEMENTS_SIZE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportTooManyStatementsSize";
	public static final String REPORT_IF_WITHOUT_ELSE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportIfWithoutElse";
	public static final String REPORT_SETVERDICT_WITHOUT_REASON = ProductConstants.PRODUCT_ID_DESIGNER + ".reportSetverdictWithoutReason";

	// Naming Conventions
	public static final String REPORTNAMINGCONVENTIONPROBLEMS = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionProblems";
	public static final String ENABLEPROJECTSPECIFICNAMINGCONVENTIONS = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".enableProjectSpecificNamingConventions";
	public static final String ENABLEFOLDERSPECIFICNAMINGCONVENTIONS = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".enableFolderSpecificNamingConventions";
	public static final String REPORTNAMINGCONVENTION_TTCN3MODULE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionTTCN3Module";
	public static final String REPORTNAMINGCONVENTION_ASN1MODULE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionASN1Module";

	public static final String REPORTNAMINGCONVENTION_ALTSTEP = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionAltstep";
	public static final String REPORTNAMINGCONVENTION_GLOBAL_CONSTANT = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionGlobalConstant";
	public static final String REPORTNAMINGCONVENTION_EXTERNALCONSTANT = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionExternalConstant";
	public static final String REPORTNAMINGCONVENTION_FUNCTION = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionFunction";
	public static final String REPORTNAMINGCONVENTION_EXTERNALFUNCTION = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionExternalFunction";
	public static final String REPORTNAMINGCONVENTION_MODULEPAR = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionModuleParameter";
	public static final String REPORTNAMINGCONVENTION_GLOBAL_PORT = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionGlobalPort";
	public static final String REPORTNAMINGCONVENTION_GLOBAL_TEMPLATE = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionGlobalTemplate";
	public static final String REPORTNAMINGCONVENTION_TESTCASE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionTestcase";
	public static final String REPORTNAMINGCONVENTION_GLOBAL_TIMER = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionGlobalTimer";
	public static final String REPORTNAMINGCONVENTION_TYPE = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionType";
	public static final String REPORTNAMINGCONVENTION_GROUP = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionGroup";

	public static final String REPORTNAMINGCONVENTION_LOCAL_CONSTANT = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionLocalConstant";
	public static final String REPORTNAMINGCONVENTION_LOCAL_VARIABLE = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionLocalVariable";
	public static final String REPORTNAMINGCONVENTION_LOCAL_TEMPLATE = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionlocalTemplate";
	public static final String REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionLocalVariableTemplate";
	public static final String REPORTNAMINGCONVENTION_LOCAL_TIMER = ProductConstants.PRODUCT_ID_DESIGNER + ".reportNamingConventionLocalTimer";
	public static final String REPORTNAMINGCONVENTION_FORMAL_PARAMETER = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionFormalParameter";

	public static final String REPORTNAMINGCONVENTION_COMPONENT_CONSTANT = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionComponentConstant";
	public static final String REPORTNAMINGCONVENTION_COMPONENT_VARIABLE = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionComponentVariable";
	public static final String REPORTNAMINGCONVENTION_COMPONENT_TIMER = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".reportNamingConventionComponentTimer";

	public static final String REPORT_MODULENAME_IN_DEFINITION = ProductConstants.PRODUCT_ID_DESIGNER + ".reportModuleNameInDefinition";
	public static final String REPORT_VISIBILITY_IN_DEFINITION = ProductConstants.PRODUCT_ID_DESIGNER + ".reportVisibilityInDefinition";

	// TITAN actions
	public static final String TITANACTIONS_PROCESSEXCLUDEDRESOURCES = ProductConstants.PRODUCT_ID_DESIGNER
			+ ".TITANActions.processExcludedResources";
	public static final String TITANACTIONS_DEFAULT_AS_OMIT = ProductConstants.PRODUCT_ID_DESIGNER + ".TITANActions.default_as_omit";

	// outline
	public static final String OUTLINE_SORTED = ProductConstants.PRODUCT_ID_DESIGNER + ".outline.sortAlphabetically";
	public static final String OUTLINE_CATEGORISED = ProductConstants.PRODUCT_ID_DESIGNER + ".outline.useCategories";
	public static final String OUTLINE_GROUPED = ProductConstants.PRODUCT_ID_DESIGNER + ".outline.useGroups";
	public static final String OUTLINE_HIDE_FUNCTIONS = ProductConstants.PRODUCT_ID_DESIGNER + ".outline.hideFunctions";
	public static final String OUTLINE_HIDE_TEMPLATES = ProductConstants.PRODUCT_ID_DESIGNER + ".outline.hideTemplates";
	public static final String OUTLINE_HIDE_TYPES = ProductConstants.PRODUCT_ID_DESIGNER + ".outline.hideTypes";

	// syntax highlight
	public static final String COLOR_PREFIX = ProductConstants.PRODUCT_ID_DESIGNER + ".color";
	public static final String COLOR_MATCHING_BRACKET = COLOR_PREFIX + ".matchingBracket";

	public static final String FOREGROUND = ".foreground";
	public static final String BACKGROUND = ".background";
	public static final String USEBACKGROUNDCOLOR = ".usebackgroundcolor";
	public static final String BOLD = ".bold";

	// general settings
	public static final String COLOR_NORMAL_TEXT = COLOR_PREFIX + ".normalText";
	public static final String COLOR_COMMENTS = COLOR_PREFIX + ".comments";
	public static final String COLOR_STRINGS = COLOR_PREFIX + ".strings";

	// asn1 specific
	public static final String COLOR_ASN1_KEYWORDS = COLOR_PREFIX + ".asn1keywords";
	public static final String COLOR_CMIP_VERB = COLOR_PREFIX + ".CMIP_Verb";
	public static final String COLOR_COMPARE_TYPE = COLOR_PREFIX + ".compare_type";
	public static final String COLOR_STATUS = COLOR_PREFIX + ".status_type";
	public static final String COLOR_TAG = COLOR_PREFIX + ".tag";
	public static final String COLOR_STORAGE = COLOR_PREFIX + ".storage";
	public static final String COLOR_MODIFIER = COLOR_PREFIX + ".modifier";
	public static final String COLOR_ACCESS_TYPE = COLOR_PREFIX + ".access_type";

	// config specific
	public static final String COLOR_CONFIG_KEYWORDS = COLOR_PREFIX + ".config_keywords";
	public static final String COLOR_SECTION_TITLE = COLOR_PREFIX + ".section_title";
	public static final String COLOR_SECTION_TITLE_BACKGROUND = COLOR_PREFIX + ".section_title_background";
	public static final String COLOR_FILE_AND_CONTROL_MASK_OPTIONS = COLOR_PREFIX + ".file_and_control_mask_options";
	public static final String COLOR_EXTERNAL_COMMAND_TYPES = COLOR_PREFIX + ".external_command_types";

	// TTCN-3 specific
	public static final String COLOR_TTCN3_KEYWORDS = COLOR_PREFIX + ".ttcn3_keywords";
	public static final String COLOR_PREPROCESSOR = COLOR_PREFIX + ".preprocessor";
	public static final String COLOR_VISIBILITY_OP = COLOR_PREFIX + ".visibility_op";
	public static final String COLOR_TEMPLATE_MATCH = COLOR_PREFIX + ".template_match";
	public static final String COLOR_TYPE = COLOR_PREFIX + ".type";
	public static final String COLOR_TIMER_OP = COLOR_PREFIX + ".timer_op";
	public static final String COLOR_PORT_OP = COLOR_PREFIX + ".port_op";
	public static final String COLOR_CONFIG_OP = COLOR_PREFIX + ".config_op";
	public static final String COLOR_VERDICT_OP = COLOR_PREFIX + ".verdict_op";
	public static final String COLOR_SUT_OP = COLOR_PREFIX + ".sut_op";
	public static final String COLOR_FUNCTION_OP = COLOR_PREFIX + ".function_op";
	public static final String COLOR_PREDEFINED_OP = COLOR_PREFIX + ".predefined_op";
	public static final String COLOR_BOOLEAN_CONST = COLOR_PREFIX + ".boolean_const";
	public static final String COLOR_OTHER_CONST = COLOR_PREFIX + ".other_const";
	public static final String COLOR_TTCN3_VERDICT_CONST = COLOR_PREFIX + ".ttcn3_verdict_const";

	public static final String T3DOC_ENABLE = ProductConstants.PRODUCT_ID_DESIGNER + ".T3DocEnable";

	// Find definition
	private static final String FIND_DEF = ProductConstants.PRODUCT_ID_DESIGNER + ".finddef";
	public static final String FIND_DEF_WS = FIND_DEF + ".ws";
	public static final String FIND_DEF_FUNCT = FIND_DEF + ".function";
	public static final String FIND_DEF_TYPES = FIND_DEF + ".types";
	public static final String FIND_DEF_MODULES = FIND_DEF + ".modules";
	public static final String FIND_DEF_GLOBAL = FIND_DEF + ".global";

	// Debug
	private static final String DEBUG = ProductConstants.PRODUCT_ID_DESIGNER + ".debug";
	public static final String DEBUG_PREFERENCE_PAGE_ENABLED = DEBUG + ".preferencePage.enabled";
	// Debug console
	public static final String DISPLAYDEBUGINFORMATION = ProductConstants.PRODUCT_ID_DESIGNER + ".displayDebugInformation";
	private static final String DEBUG_CONSOLE = DEBUG + ".console";
	public static final String DEBUG_CONSOLE_TIMESTAMP = DEBUG_CONSOLE + ".timestamp";
	public static final String DEBUG_CONSOLE_AST_ELEM = DEBUG_CONSOLE + ".astelem";
	public static final String DEBUG_CONSOLE_PARSE_TREE = DEBUG_CONSOLE + ".parsetree";
	public static final String DEBUG_CONSOLE_LOG_TO_SYSOUT = DEBUG_CONSOLE + ".logtosysout";
	public static final String DEBUG_LOAD_TOKENS_TO_PROCESS_IN_A_ROW = DEBUG + ".load.tokensToProcessInARow";
	public static final String DEBUG_LOAD_THREAD_PRIORITY = DEBUG + ".load.threadPriority";
	public static final String DEBUG_LOAD_SLEEP_BETWEEN_FILES = DEBUG + ".load.sleepBetweenFiles";
	public static final String DEBUG_LOAD_YIELD_BETWEEN_CHECKS = DEBUG + ".load.yieldBetweenChecks";
	

	/** private constructor to disable instantiation */
	private PreferenceConstants() {
		// Do nothing
	}
}
