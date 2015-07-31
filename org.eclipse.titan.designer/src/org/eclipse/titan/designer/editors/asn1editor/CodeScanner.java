/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.PatternRule;
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

	public static final String[] VERBS = new String[] { "ACTIONS", "ADD", "GET", "NOTIFICATIONS", "REPLACE", "REMOVE" };

	public static final String[] COMPARE_TYPES = new String[] { "OPTIONAL", "DEFAULT", "MANAGED", "MODULE-TYPE", "MODULE_IDENTITY",
			"MODULE-COMPLIANCE", "OBJECT-TYPE", "OBJECT-IDENTITY", "OBJECT-COMPLIANCE", "MODE", "CONFIRMED", "CONDITIONAL",
			"SUBORDINATE", "SUPERIOR", "CLASS", "TRUE", "FALSE", "NULL", "TEXTUAL-CONVENTION" };

	public static final String[] STATUS_TYPE = new String[] { "current", "deprecated", "mandatory", "obsolete" };

	public static final String[] KEYWORDS = new String[] { "DEFINITIONS", "OBJECTS", "IF", "DERIVED", "INFORMATION", "ACTION", "REPLY", "ANY",
			"NAMED", "CHARACTERIZED", "BEHAVIOUR", "REGISTERED", "WITH", "AS", "IDENTIFIED", "CONSTRAINED", "BY", "PRESENT", "BEGIN",
			"IMPORTS", "FROM", "UNITS", "SYNTAX", "MIN-ACCESS", "MAX-ACCESS", "MINACCESS", "MAXACCESS", "REVISION", "STATUS",
			"DESCRIPTION", "SEQUENCE", "SET", "COMPONENTS", "OF", "CHOICE", "DistinguishedName", "ENUMERATED", "SIZE", "OBJECT-TYPE",
			"MODULE", "END", "INDEX", "AUGMENTS", "EXTENSIBILITY", "IMPLIED", "EXPORTS" };

	public static final String[] TAGS = new String[] { "APPLICATION", "AUTOMATIC", "EXPLICIT", "IMPLICIT", "PRIVATE", "TAGS", "UNIVERSAL", };

	public static final String[] STORAGE = new String[] { "BOOLEAN", "INTEGER", "InterfaceIndex", "IANAifType", "CMIP-Attribute", "REAL",
			"PACKAGE", "PACKAGES", "IpAddress", "PhysAddress", "NetworkAddress", "BITS", "BMPString", "TimeStamp", "TimeTicks",
			"TruthValue", "RowStatus", "DisplayString", "GeneralString", "GraphicString", "IA5String", "NumericString",
			"PrintableString", "SnmpAdminAtring", "TeletexString", "UTF8String", "VideotexString", "VisibleString", "ISO646String",
			"T61String", "UniversalString", "Unsigned32", "Integer32", "Gauge", "Gauge32", "Counter", "Counter32", "Counter64" };

	public static final String[] MODIFIER = new String[] { "ATTRIBUTE", "ATTRIBUTES", "MANDATORY-GROUP", "MANDATORY-GROUPS", "GROUP", "GROUPS",
			"ELEMENTS", "EQUALITY", "ORDERING", "SUBSTRINGS", "DEFINED" };

	public static final String[] ACCESS_TYPE = new String[] { "not-accessible", "accessible-for-notify", "read-only", "read-create", "read-write" };

	public static final String SINGLE_LINE_COMMENT = "__asn1_single_line_comment";
	private static final String[] COMMENT_PIECES = new String[] { "--", "/*", "*/" };

	public CodeScanner(final ColorManager colorManager) {
		IToken comment = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_COMMENTS);
		IToken verbs = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_CMIP_VERB);
		IToken compareType = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_COMPARE_TYPE);
		IToken statusType = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_STATUS);
		IToken keywords = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_ASN1_KEYWORDS);
		IToken tags = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_TAG);
		IToken storage = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_STORAGE);
		IToken modifier = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_MODIFIER);
		IToken accesType = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_ACCESS_TYPE);

		IToken string = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_STRINGS);
		IToken other = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_NORMAL_TEXT);

		List<IRule> rules = new ArrayList<IRule>();

		rules.add(new PatternRule(COMMENT_PIECES[0], COMMENT_PIECES[0], comment, '\0', true, true));
		rules.add(new MultiLineRule(COMMENT_PIECES[1], COMMENT_PIECES[2], comment, '\0', true));

		rules.add(new WhitespaceRule(new WhiteSpaceDetector()));
		rules.add(new SingleLineRule(STRING_PIECES[0], STRING_PIECES[0], string, '\\'));
		rules.add(new StringDetectionPatternRule(STRING_PIECES[2], new char[][] { { '\'', 'B' }, { '\'', 'H' }, { '\'', 'O' } }, string));

		// special multi-word keyword like stuff
		rules.add(new SingleLineRule("NAME", "BINDING", keywords));
		rules.add(new SingleLineRule("OBJECT", "IDENTIFIER", keywords));
		rules.add(new SingleLineRule("BIT", "STRING", storage));
		rules.add(new SingleLineRule("OCTET", "STRING", storage));
		rules.add(new SingleLineRule("MATCHES", "FOR", modifier));

		WordRule wordRule = new WordRule(new WordDetector(), other);

		for (String element : CodeScanner.VERBS) {
			wordRule.addWord(element, verbs);
		}
		for (String element : CodeScanner.COMPARE_TYPES) {
			wordRule.addWord(element, compareType);
		}
		for (String element : CodeScanner.STATUS_TYPE) {
			wordRule.addWord(element, statusType);
		}
		for (String element : CodeScanner.KEYWORDS) {
			wordRule.addWord(element, keywords);
		}
		for (String element : CodeScanner.TAGS) {
			wordRule.addWord(element, tags);
		}
		for (String element : CodeScanner.STORAGE) {
			wordRule.addWord(element, storage);
		}
		for (String element : CodeScanner.MODIFIER) {
			wordRule.addWord(element, modifier);
		}
		for (String element : CodeScanner.ACCESS_TYPE) {
			wordRule.addWord(element, accesType);
		}

		rules.add(wordRule);

		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
