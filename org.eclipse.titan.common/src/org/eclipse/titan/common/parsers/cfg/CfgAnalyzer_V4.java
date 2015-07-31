/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.common.utils.StandardCharsets;

/**
 * ANTLR 4 version
 * @author eptedim
 * @author Arpad Lovassy
 */
public final class CfgAnalyzer_V4 extends CfgAnalyzer {

	private TitanListener lexerListener = null;
	private TitanListener parserListener = null;
	
	public List<SyntacticErrorStorage> getErrorStorage() {
		if (!lexerListener.getErrorsStored().isEmpty() && parserListener.getErrorsStored().isEmpty()) {
			return lexerListener.getErrorsStored();
		}
		else if (lexerListener.getErrorsStored().isEmpty() && !parserListener.getErrorsStored().isEmpty()) {
			return parserListener.getErrorsStored();
		}
		else if (!lexerListener.getErrorsStored().isEmpty() && !parserListener.getErrorsStored().isEmpty()) {
			if (lexerListener.addAll(parserListener.getErrorsStored())) {
				return lexerListener.getErrorsStored();
			}
		}
		return new ArrayList<SyntacticErrorStorage>();
	}
	
	@Override
	public void parse(IFile file, String code) {
		String fileName = "<unknown file>";
		if(file != null){
			fileName = file.getFullPath().toOSString();
		}
		directParse(file, fileName, code);
	}
	
	@Override
	public void directParse(final IFile file, final String fileName, final String code) {
		CFGLexer2 lexer;
		Reader reader = null;
		if (null != code) {
			reader = new StringReader(code);
		}
		else if (null != file) {
			try {
				reader = new BufferedReader(new InputStreamReader(file.getContents(), StandardCharsets.UTF8));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Could not get the contents of `" + fileName + "'", e);
				return;
			}
		}
		CharStream charStream = new UnbufferedCharStream(reader);
		lexer = new CFGLexer2(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexerListener = new TitanListener();
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		lexer.addErrorListener(lexerListener);
		
		// Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		TokenStream tokens = new BufferedTokenStream( lexer );
		CFGParser2 parser = new CFGParser2(tokens);
		parser.setBuildParseTree(false);		
		parserListener = new TitanListener();
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);
		parser.pr_ConfigFile();
		warnings = parser.getWarnings();
		//TODO: fill rootInterval if needed
		
		definitions = parser.getDefinitions();
		includeFiles = parser.getIncludeFiles();
		
		//TODO: fill handlers if needed
		/*
		moduleParametersHandler = parser.moduleParametersHandler;
		testportParametersHandler = parser.testportParametersHandler;
		componentSectionHandler = parser.componentSectionHandler;
		groupSectionHandler = parser.groupSectionHandler;
		mcSectionHandler = parser.mcSectionHandler;
		externalCommandsSectionHandler = parser.externalCommandsSectionHandler;
		executeSectionHandler = parser.executeSectionHandler;
		includeSectionHandler = parser.includeSectionHandler;
		orderedIncludeSectionHandler = parser.includeSectionHandler;
		defineSectionHandler = parser.defineSectionHandler;
		loggingSectionHandler = parser.loggingSectionHandler;
		*/
		logFileNameDefined = parser.isLogFileDefined();
	}
}
