/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.parsers.asn1parser.ModuleLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4Factory;
import org.eclipse.titan.designer.editors.asn1editor.ASN1ReferenceParser;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Listener;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1ReferenceParser_V4 extends ASN1ReferenceParser {

	public ASN1ReferenceParser_V4() {
	}

	@Override
	protected Reference parseReference(final IFile file, final String input, final int line, final int offset) {
		Reference reference = null;
		StringReader reader = new StringReader(input);
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
		parser.setProject(file.getProject());
		parser.setActualFile(file);
		parser.setLine(line);
		parser.setOffset(offset);
		parser.setBuildParseTree(false);
		ASN1Listener parserListener = new ASN1Listener();
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);
		reference = parser.pr_parseReference().reference;
		return reference;
	}
	
	@Override
	protected ASN1ReferenceParser newInstance() {
		return new ASN1ReferenceParser_V4();
	}
}
