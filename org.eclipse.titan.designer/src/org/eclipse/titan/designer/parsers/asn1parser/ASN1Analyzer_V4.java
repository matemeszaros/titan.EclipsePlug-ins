/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.AST.ASN1.definitions.ASN1Module;
import org.eclipse.titan.designer.parsers.ISourceAnalyzer_V4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Listener;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.ModuleLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4Factory;

/**
 * ANTLR V4 version of ASN1 Analyzer
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class ASN1Analyzer_V4 implements ISourceAnalyzer_V4 {
	
	private List<TITANMarker> warnings;
	private List<TITANMarker> unsupportedConstructs;
	private Interval rootInterval;
	private ASN1Module actualAsn1Module = null;
	private ASN1Listener lexerListener = null;
	private ASN1Listener parserListener = null;

	@Override
	public List<TITANMarker> getWarnings() {
		return warnings;
	}

	@Override
	public List<TITANMarker> getUnsupportedConstructs() {
		return unsupportedConstructs;
	}

	@Override
	public ASN1Module getModule() {
		return actualAsn1Module;
	}

	@Override
	public Interval getRootInterval() {
		return rootInterval;
	}
	
	@Override
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
	public void parse(IFile file, String code) throws FileNotFoundException {
		Reader reader = null;
		ASN1Lexer2 lexer;

		if (code != null) {
			reader = new StringReader(code);
		} else if (file != null) {
			try {
				reader = new BufferedReader(new InputStreamReader(file.getContents(), StandardCharsets.UTF_8));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Could not get the contents of `" + file.getName() + "'", e);
			}
		} else {
			return;
		}
		CharStream charStream = new UnbufferedCharStream(reader);
		lexer = new ASN1Lexer2(charStream);
		lexer.setTokenFactory(new TokenWithIndexAndSubTokensV4Factory(true));
		lexerListener = new ASN1Listener();
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		lexer.addErrorListener(lexerListener);

		ModuleLevelTokenStreamTrackerV4 tracker = new ModuleLevelTokenStreamTrackerV4(lexer);
		tracker.discard(ASN1Lexer2.WS);
		tracker.discard(ASN1Lexer2.MULTILINECOMMENT);
		tracker.discard(ASN1Lexer2.SINGLELINECOMMENT);
		tracker.setActualFile(file);
		ASN1Parser2 parser = new ASN1Parser2(tracker);
		parser.setProject(file.getProject());
		parser.setActualFile(file);
		parser.setBuildParseTree(false);		
		parserListener = new ASN1Listener(parser);
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);
		parser.pr_ASN1ModuleDefinition();
		actualAsn1Module = parser.getModule();
		warnings = parser.getWarnings();
		unsupportedConstructs = parser.getUnsupportedConstructs();
	}
}
