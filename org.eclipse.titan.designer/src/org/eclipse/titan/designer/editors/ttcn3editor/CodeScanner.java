/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
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

	public static final String[] KEYWORDS = new String[] { "activate", "address", "alive", "all", "alt", "altstep", "and", "and4b", "any",
			"break", "case", "component", "const", "continue", "control", "deactivate", "display", "do", "else", "encode", "enumerated",
			"except", "exception", "execute", "extends", "extension", "external", "for", "from", "function", "goto", "group", "if",
			"import", "in", "infinity", "inout", "interleave", "label", "language", "length", "log", "match", "message", "mixed", "mod",
			"modifies", "module", "modulepar", "mtc", "noblock", "not", "not4b", "nowait", "of", "omit", "on", "optional", "or", "or4b", "out",
			"override", "param", "pattern", "port", "present", "procedure", "record", "recursive", "rem", "repeat", "return", "runs", "select",
			"self", "sender", "set", "signature", "system", "template", "testcase", "to", "type", "union", "value", "valueof", "var",
			"variant", "while", "with", "xor", "xor4b" };

	public static final String[] TITANSPECIFICKEYWORDS = new String[] { "@try", "@catch" };

	public static final String[] TEMPLATE_MATCH = new String[] { "complement", "ifpresent", "subset", "superset", "permutation" };

	public static final String[] TYPES = new String[] { "anytype", "bitstring", "boolean", "char", "charstring", "default", "float", "hexstring",
			"integer", "objid", "octetstring", "universal", "verdicttype", "timer" };

	public static final String[] TIMER_OPERATIONS = new String[] { "start", "stop", "timeout", "read", "running" };

	public static final String[] PORT_OPERATIONS = new String[] { "call", "catch", "check", "clear", "getcall", "getreply", "halt", "raise",
			"receive", "reply", "send", "trigger" };

	public static final String[] CONFIGURATION_OPERATIONS = new String[] { "create", "connect", "disconnect", "done", "kill", "killed", "map",
			"unmap" };

	public static final String[] VERDICT_OPERATIONS = new String[] { "getverdict", "setverdict" };

	public static final String[] SUT_OPERATION = new String[] { "action" };

	public static final String[] FUNCTION_OPERATIONS = new String[] { "apply", "derefers", "refers" };

	public static final String[] PREDEFINED_OPERATIONS = new String[] { "bit2hex", "bit2int", "bit2oct", "bit2str", "char2int", "char2oct",
			"encvalue", "decomp", "decvalue", "float2int", "float2str", "hex2bit", "hex2int", "hex2oct", "hex2str", "int2bit",
			"int2char", "int2float", "int2hex", "int2oct", "int2str", "int2unichar", "isbound", "ischosen", "ispresent", "isvalue",
			"lengthof", "log2str", "oct2bit", "oct2char", "oct2hex", "oct2int", "oct2str", "regexp", "replace", "rnd", "sizeof",
			"str2bit", "str2float", "str2hex", "str2int", "str2oct", "substr", "unichar2int", "unichar2char", "enum2int",
			"get_stringencoding", "oct2unichar", "remove_bom", "unichar2oct", "encode_base64", "decode_base64" };

	public static final String[] BOOLEAN_CONSTANTS = new String[] { "true", "false" };

	public static final String[] VERDICT_CONSTANT = new String[] { "none", "pass", "inconc", "fail", "error" };

	public static final String[] OTHER_CONSTANT = new String[] { "null", "NULL" };

	public static final String[] MACROS = new String[] { "%moduleId", "%definitionId", "%testcaseId", "%fileName", "%lineNumber", "__MODULE__",
			"__FILE__", "__BFILE__", "__LINE__", "__SCOPE__", "__TESTCASE__" };

	public static final String[] VISIBILITY_MODIFIERS = new String[] { "public", "private", "friend" };

	public CodeScanner(final ColorManager colorManager) {
		List<IRule> rules = getTTCNRules(colorManager);
		// line marker (single line preprocessor directive)
		IToken preprocessor = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_PREPROCESSOR);
		rules.add(new EndOfLineRule("#", preprocessor));
		setRules(rules.toArray(new IRule[rules.size()]));
	}

	public static List<IRule> getTTCNRules(final ColorManager colorManager) {
		IToken singleLineComment = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_COMMENTS);
		IToken multiLineComment = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_COMMENTS);
		IToken keyword = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_TTCN3_KEYWORDS);
		IToken templateMatch = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_TEMPLATE_MATCH);
		IToken types = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_TYPE);
		IToken timerOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_TIMER_OP);
		IToken portOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_PORT_OP);
		IToken configOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_CONFIG_OP);
		IToken verdictOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_VERDICT_OP);
		IToken sutOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_SUT_OP);
		IToken functionOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_FUNCTION_OP);
		IToken predefinedOp = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_PREDEFINED_OP);
		IToken booleanConst = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_BOOLEAN_CONST);
		IToken verdictConst = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST);
		IToken otherConst = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_OTHER_CONST);
		IToken macro = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_PREPROCESSOR);
		IToken visibility = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_VISIBILITY_OP);

		IToken string = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_STRINGS);

		IToken other = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_NORMAL_TEXT);
		List<IRule> rules = new ArrayList<IRule>();

		rules.add(new EndOfLineRule("//", singleLineComment));
		rules.add(new MultiLineRule("/*", "*/", multiLineComment, '\0', true));

		rules.add(new WhitespaceRule(new WhiteSpaceDetector()));
		rules.add(new TTCN3StringDetectionPatternRule(string));
		rules.add(new StringDetectionPatternRule("'", new char[][] { { '\'', 'B' }, { '\'', 'H' }, { '\'', 'O' } }, string));

		WordRule wordRule = new WordRule(new WordDetector(), other);
		for (String element : CodeScanner.KEYWORDS) {
			wordRule.addWord(element, keyword);
		}
		for (String element : CodeScanner.TEMPLATE_MATCH) {
			wordRule.addWord(element, templateMatch);
		}
		for (String element : CodeScanner.TYPES) {
			wordRule.addWord(element, types);
		}
		for (String element : CodeScanner.TIMER_OPERATIONS) {
			wordRule.addWord(element, timerOp);
		}
		for (String element : CodeScanner.PORT_OPERATIONS) {
			wordRule.addWord(element, portOp);
		}
		for (String element : CodeScanner.CONFIGURATION_OPERATIONS) {
			wordRule.addWord(element, configOp);
		}
		for (String element : CodeScanner.VERDICT_OPERATIONS) {
			wordRule.addWord(element, verdictOp);
		}
		for (String element : CodeScanner.SUT_OPERATION) {
			wordRule.addWord(element, sutOp);
		}
		for (String element : CodeScanner.FUNCTION_OPERATIONS) {
			wordRule.addWord(element, functionOp);
		}
		for (String element : CodeScanner.PREDEFINED_OPERATIONS) {
			wordRule.addWord(element, predefinedOp);
		}
		for (String element : CodeScanner.BOOLEAN_CONSTANTS) {
			wordRule.addWord(element, booleanConst);
		}
		for (String element : CodeScanner.VERDICT_CONSTANT) {
			wordRule.addWord(element, verdictConst);
		}
		for (String element : CodeScanner.OTHER_CONSTANT) {
			wordRule.addWord(element, otherConst);
		}
		for (String element : CodeScanner.VISIBILITY_MODIFIERS) {
			wordRule.addWord(element, visibility);
		}

		rules.add(wordRule);

		WordRule macroRule = new WordRule(new MacroDetector(), other);
		for (String element : CodeScanner.MACROS) {
			macroRule.addWord(element, macro);
		}

		rules.add(macroRule);

		WordRule titanSpecificKeywordsRule = new WordRule(new TitanSpecificKeywordDetector(), other);
		for (String element : CodeScanner.TITANSPECIFICKEYWORDS) {
			// looks like a standard keyword
			titanSpecificKeywordsRule.addWord(element, keyword);
		}

		rules.add(titanSpecificKeywordsRule);

		return rules;
	}

}
