/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Lexer4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Parser4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReferenceAnalyzer;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TTCN3ReferenceAnalyzer_V4 extends TTCN3ReferenceAnalyzer {
	
	public TTCN3ReferenceAnalyzer_V4() {
	}
	
	/**
	 * @return the parsed reference or null if the text can not form a reference
	 */
	public Reference parse(IFile file, String code, boolean reportErrors, final int aLine, final int aOffset) {
		Reference reference = null;

		Reader reader = new StringReader( code );
		CharStream charStream = new UnbufferedCharStream( reader );
		TTCN3Lexer4 lexer = new TTCN3Lexer4( charStream );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( code.length() );

		lexer.removeErrorListeners();

		TokenStream tokens = new BufferedTokenStream( lexer );
		TTCN3Parser4 parser = new TTCN3Parser4(tokens);

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
		TTCN3KeywordLessLexer4 lexer = new TTCN3KeywordLessLexer4( charStream );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( code.length() );

		lexer.removeErrorListeners();

		TokenStream tokens = new BufferedTokenStream( lexer );
		TTCN3Parser4 parser = new TTCN3Parser4(tokens);

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
