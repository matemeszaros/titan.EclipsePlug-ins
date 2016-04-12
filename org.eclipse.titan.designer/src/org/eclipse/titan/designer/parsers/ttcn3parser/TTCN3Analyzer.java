/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.ISourceAnalyzer;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.PreprocessedTokenStream;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Parser;
import org.eclipse.titan.designer.properties.data.PreprocessorSymbolsOptionsData;

/**
 * TTCN3Analyzer
 * @author Arpad Lovassy
 */
public class TTCN3Analyzer implements ISourceAnalyzer {
	
	private List<TITANMarker> warnings;
	private List<TITANMarker> unsupportedConstructs;
	private Interval rootInterval;
	private TTCN3Module actualTtc3Module;

	/**
	 * The list of markers (ERROR and WARNING) created during parsing
	 * NOTE: used from ANTLR v4
	 */
	private List<SyntacticErrorStorage> mErrorsStored = null;
	
	@Override
	public List<SyntacticErrorStorage> getErrorStorage() {
		return mErrorsStored;
	}
	
	@Override
	public List<TITANMarker> getWarnings() {
		return warnings;
	}

	@Override
	public List<TITANMarker> getUnsupportedConstructs() {
		return unsupportedConstructs;
	}

	@Override
	public TTCN3Module getModule() {
		return actualTtc3Module;
	}

	@Override
	public Interval getRootInterval() {
		return rootInterval;
	}
	
	/**
	 * Parse TTCN-3 file using ANTLR v4
	 * @param aFile TTCN-3 file to parse, It cannot be null
	 * @param aCode TTCN-3 code to parse in string format
	 *              It can be null, in this case code is read from file
	 */
	public void parse( IFile aFile, String aCode ) {
		Reader reader;
		int rootInt;
		if ( aCode != null ) {
			reader = new StringReader( aCode );
			rootInt = aCode.length();
		} else if (aFile != null) {
			try {
				InputStreamReader temp = new InputStreamReader(aFile.getContents());
				if (!aFile.getCharset().equals(temp.getEncoding())) {
					try {
						temp.close();
					} catch (IOException e) {
						ErrorReporter.logWarningExceptionStackTrace(e);
					}
					temp = new InputStreamReader(aFile.getContents(), aFile.getCharset());
				}

				reader = new BufferedReader(temp);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			} catch (UnsupportedEncodingException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			}

			IFileStore store;
			try {
				store = EFS.getStore(aFile.getLocationURI());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			}
			IFileInfo fileInfo = store.fetchInfo();
			rootInt = (int) fileInfo.getLength();
		} else {
			return;
		}
		
		parse( reader, rootInt, aFile );
	}
	
	/**
	 * Parse TTCN-3 file using ANTLR v4
	 * Eclipse independent version
	 * @param aFile TTCN-3 file to parse, It cannot be null
	 */
	public void parse( final File aFile ) {
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader( new FileReader( aFile ) );
		} catch ( FileNotFoundException e ) {
			//TODO: handle error
			return;
		}

		final int fileLength = (int)aFile.length();
		parse( bufferedReader, fileLength, null );
	}
	
	/**
	 * Parse TTCN-3 file using ANTLR v4
	 * @param aReader file to parse (cannot be null, closes aReader)
	 * @param aFileLength file length
	 * @param aEclipseFile Eclipse dependent resource file
	 */
	private void parse( final Reader aReader, final int aFileLength, IFile aEclipseFile ) {
		CharStream charStream = new UnbufferedCharStream( aReader );
		Ttcn3Lexer lexer = new Ttcn3Lexer( charStream );

		lexer.setCommentTodo( true );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( aFileLength );

		TitanListener lexerListener = new TitanListener();
		// remove ConsoleErrorListener
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);

		// Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		TokenStream tokens = new BufferedTokenStream( lexer );
		
		Ttcn3Parser parser = new Ttcn3Parser( tokens );
		parser.setBuildParseTree(false);
		PreprocessedTokenStream preprocessor = null;
		
		if ( aEclipseFile != null && GlobalParser.TTCNPP_EXTENSION.equals( aEclipseFile.getFileExtension() ) ) {
			lexer.setTTCNPP();
			preprocessor = new PreprocessedTokenStream(lexer);
			preprocessor.setActualFile(aEclipseFile);
			if ( aEclipseFile.getProject() != null ) {
				preprocessor.setMacros( PreprocessorSymbolsOptionsData.getTTCN3PreprocessorDefines( aEclipseFile.getProject() ) );
			}
			parser = new Ttcn3Parser( preprocessor );
			preprocessor.setActualLexer(lexer);
			preprocessor.setParser(parser);
		}
		
		if ( aEclipseFile != null ) {
			lexer.setActualFile( aEclipseFile );
			parser.setActualFile( aEclipseFile );
			parser.setProject( aEclipseFile.getProject() );
		}
		
		parser.setLexer( lexer );
		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		TitanListener parserListener = new TitanListener();
		parser.addErrorListener( parserListener );
		
		// This is added because of the following ANTLR 4 bug:
		// Memory Leak in PredictionContextCache #499
		// https://github.com/antlr/antlr4/issues/499
		DFA[] decisionToDFA = parser.getInterpreter().decisionToDFA;
		parser.setInterpreter(new ParserATNSimulator(parser, parser.getATN(), decisionToDFA, new PredictionContextCache()));
		
		//try SLL mode
		try {
			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			parser.pr_TTCN3File();
			warnings = parser.getWarnings();
			mErrorsStored = lexerListener.getErrorsStored();
			mErrorsStored.addAll( parserListener.getErrorsStored() );
		} catch (RecognitionException e) {
			// quit
		}
		
		if (!warnings.isEmpty() || !mErrorsStored.isEmpty()) {
			//SLL mode might have failed, try LL mode
			try {
				CharStream charStream2 = new UnbufferedCharStream( aReader );
				lexer.setInputStream(charStream2);
				//lexer.reset();
				parser.reset();
				parserListener.reset();
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				parser.pr_TTCN3File();
				warnings = parser.getWarnings();
				mErrorsStored = lexerListener.getErrorsStored();
				mErrorsStored.addAll( parserListener.getErrorsStored() );
			} catch(RecognitionException e) {

			}
		}

		unsupportedConstructs = parser.getUnsupportedConstructs();
		rootInterval = lexer.getRootInterval();
		actualTtc3Module = parser.getModule();
		if ( preprocessor != null ) {
			// if the file was preprocessed
			mErrorsStored.addAll(preprocessor.getErrorStorage());
			warnings.addAll( preprocessor.getWarnings() );
			unsupportedConstructs.addAll( preprocessor.getUnsupportedConstructs() );
			if ( actualTtc3Module != null ) {
				actualTtc3Module.setIncludedFiles( preprocessor.getIncludedFiles() );
				actualTtc3Module.setInactiveCodeLocations( preprocessor.getInactiveCodeLocations() );
			}
		}
		//TODO: empty mErrorsStored not to store errors from the previous parse round in case of exception

		try {
			aReader.close();
		} catch (IOException e) {
		}
	}
}
