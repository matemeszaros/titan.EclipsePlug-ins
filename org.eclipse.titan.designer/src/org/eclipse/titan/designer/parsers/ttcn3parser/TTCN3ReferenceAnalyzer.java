/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.Reference;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class TTCN3ReferenceAnalyzer {

	public TTCN3ReferenceAnalyzer() {
	}

	/**
	 * @return the parsed reference or null if the text can not form a reference
	 */
	public Reference parse(IFile file, String code, boolean reportErrors, final int aLine, final int aOffset) {
		Reference reference = null;

		Reader reader = new StringReader( code );
		CharStream charStream = new UnbufferedCharStream( reader );
		Ttcn3Lexer lexer = new Ttcn3Lexer( charStream );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( code.length() );

		lexer.removeErrorListeners();

		TokenStream tokens = new BufferedTokenStream( lexer );
		Ttcn3Parser parser = new Ttcn3Parser(tokens);

		lexer.setActualFile(file);
		parser.setActualFile(file);
		parser.setProject(file.getProject());
		parser.setLine(aLine);
		parser.setOffset(aOffset);
		parser.setBuildParseTree(false);
		parser.setLexer(lexer);

		parser.removeErrorListeners();

		reference = parser.pr_UnifiedReferenceParser().reference;

		return reference;
	}
	
    /**
	 * Parses the provided elements. If the contents of an editor are to be parsed,
	 *  than the file parameter is only used to report the errors to.
	 * 
	 * @param file the file to parse, and report the errors to
	 * @param code the contents of an editor, or null.
	 * */
	public Reference parseForCompletion(IFile file, String code) {
		Reference reference = null;

		Reader reader = new StringReader( code );
		CharStream charStream = new UnbufferedCharStream( reader );
		Ttcn3KeywordlessLexer lexer = new Ttcn3KeywordlessLexer( charStream );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( code.length() );

		lexer.removeErrorListeners();

		TokenStream tokens = new BufferedTokenStream( lexer );
		Ttcn3Parser parser = new Ttcn3Parser(tokens);

		lexer.setActualFile(file);
		parser.setActualFile(file);
		parser.setProject(file.getProject());
		parser.setBuildParseTree(false);
		parser.setLexer(lexer);

		parser.removeErrorListeners();

		reference = parser.pr_UnifiedReferenceParser().reference;

		return reference;
	}
}
