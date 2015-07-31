/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.definitions;

import java.io.StringReader;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.definitions.SpecialASN1Module;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Listener;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.ModuleLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4Factory;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class SpecialASN1Module_V4 extends SpecialASN1Module {

	/**
	 * Disabled constructor
	 */
	private SpecialASN1Module_V4() {
		super();
	}
	
	/**
	 * Parses the special internal assignments to build their semantic
	 * representation.
	 * 
	 * @param input_code
	 *                the code to parse.
	 * @param identifier
	 *                the identifier for the assignment to be created.
	 * 
	 * @return the parsed assignment.
	 */
	public static ASN1Assignment parseSpecialInternalAssignment(final String input_code, final Identifier identifier) {
		ASN1Assignment assignment = null;
		StringReader reader = new StringReader(input_code);
		CharStream charStream = new UnbufferedCharStream(reader);
		ASN1Lexer2 lexer = new ASN1Lexer2(charStream);
		lexer.setTokenFactory(new TokenWithIndexAndSubTokensV4Factory(true));

		ASN1Listener lexerListener = new ASN1Listener();
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		lexer.addErrorListener(lexerListener);
		ModuleLevelTokenStreamTrackerV4 tracker = new ModuleLevelTokenStreamTrackerV4(lexer);
		tracker.discard(ASN1Lexer2.WS);
		tracker.discard(ASN1Lexer2.MULTILINECOMMENT);
		tracker.discard(ASN1Lexer2.SINGLELINECOMMENT);
		ASN1Parser2 parser = new ASN1Parser2(tracker);
		parser.setBuildParseTree(false);		
		ASN1Listener parserListener = new ASN1Listener(parser);
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);
		assignment = parser.pr_TITAN_special_Assignment(identifier).assignment;
		if (!parser.getErrorStorage().isEmpty()) {
			ErrorReporter.INTERNAL_ERROR(PARSINGFAILED);
			for (SyntacticErrorStorage temp : parser.getErrorStorage()) {
				ErrorReporter.logError(temp.message);
			}
		}
		return assignment;
	}
}
