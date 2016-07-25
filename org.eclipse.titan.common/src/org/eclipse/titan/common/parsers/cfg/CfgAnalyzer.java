/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExternalCommandSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.MCSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.titan.common.utils.StandardCharsets;

/**
 * @author eptedim
 * @author Arpad Lovassy
 */
public final class CfgAnalyzer {
	private CfgInterval rootInterval;
	private TitanListener lexerListener = null;
	private TitanListener parserListener = null;
	
	private ModuleParameterSectionHandler moduleParametersHandler = null;
	private TestportParameterSectionHandler testportParametersHandler = null;
	private ComponentSectionHandler componentSectionHandler = null;
	private GroupSectionHandler groupSectionHandler = null;
	private MCSectionHandler mcSectionHandler = null;
	private ExternalCommandSectionHandler externalCommandsSectionHandler = null;
	private ExecuteSectionHandler executeSectionHandler = null;
	private IncludeSectionHandler includeSectionHandler = null;
	private IncludeSectionHandler orderedIncludeSectionHandler = null;
	private DefineSectionHandler defineSectionHandler = null;
	private LoggingSectionHandler loggingSectionHandler = null;

	/** result of the last parsing */
	private CfgParseResult mCfgParseResult;
	
	public CfgParseResult getCfgParseResult() {
		return mCfgParseResult;
	}
	
	public CfgInterval getRootInterval(){
		return rootInterval;
	}

	public ModuleParameterSectionHandler getModuleParametersHandler() {
		return moduleParametersHandler;
	}

	public TestportParameterSectionHandler getTestportParametersHandler() {
		return testportParametersHandler;
	}

	public ComponentSectionHandler getComponentSectionHandler() {
		return componentSectionHandler;
	}

	public GroupSectionHandler getGroupSectionHandler() {
		return groupSectionHandler;
	}

	public MCSectionHandler getMcSectionHandler() {
		return mcSectionHandler;
	}

	public ExternalCommandSectionHandler getExternalCommandsSectionHandler() {
		return externalCommandsSectionHandler;
	}

	public ExecuteSectionHandler getExecuteSectionHandler() {
		return executeSectionHandler;
	}

	public IncludeSectionHandler getIncludeSectionHandler() {
		return includeSectionHandler;
	}

	public IncludeSectionHandler getOrderedIncludeSectionHandler() {
		return orderedIncludeSectionHandler;
	}

	public DefineSectionHandler getDefineSectionHandler() {
		return defineSectionHandler;
	}

	public LoggingSectionHandler getLoggingSectionHandler() {
		return loggingSectionHandler;
	}
	
	public List<SyntacticErrorStorage> getErrorStorage() {
		if (lexerListener != null && parserListener != null) {
			lexerListener.addAll(parserListener.getErrorsStored());
			return lexerListener.getErrorsStored();
		} else {
			return new ArrayList<SyntacticErrorStorage>();
		}
	}
	
    /**
     * Parses the provided elements.
     * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
     * 
     * @param file the file to parse, and report the errors to
     * @param code the contents of an editor, or null.
     */
	public void parse(final IFile file, final String code) {
		String fileName = "<unknown file>";
		if(file != null){
			fileName = file.getFullPath().toOSString();
		}
		directParse(file, fileName, code);
	}
	
    /**
     * Parses the provided elements.
     * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
     * 
     * @param file the file to parse
     * @param fileName the name of the file, to refer to.
     * @param code the contents of an editor, or null.
     */
	public void directParse(final IFile file, final String fileName, final String code) {
		final Reader reader;
		final int fileLength;
		if (null != code) {
			reader = new StringReader(code);
			fileLength = code.length();
		} else if (null != file) {
			try {
				reader = new BufferedReader(new InputStreamReader(file.getContents(), StandardCharsets.UTF8));
				IFileStore store;
				try {
					store = EFS.getStore( file.getLocationURI() );
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace( e );
					return;
				}
				IFileInfo fileInfo = store.fetchInfo();
				fileLength = (int) fileInfo.getLength();
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Could not get the contents of `" + fileName + "'", e);
				return;
			}
		} else {
			ErrorReporter.INTERNAL_ERROR("CfgAnalyzer.directParse(): nothing to parse");
			return;
		}

		final CharStream charStream = new UnbufferedCharStream(reader);
		CfgLexer lexer = new CfgLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.initRootInterval( fileLength );
		lexerListener = new TitanListener();
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		lexer.addErrorListener(lexerListener);
		
		// 1. Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		// 2. Changed from BufferedTokenStream to CommonTokenStream, otherwise tokens with "-> channel(HIDDEN)" are not filtered out in lexer.
		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		final CfgParser parser = new CfgParser( tokenStream );
		//parser tree is built by default
		parserListener = new TitanListener();
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);
		final ParserRuleContext parseTreeRoot = parser.pr_ConfigFile();
		parser.checkMacroErrors();
		
		mCfgParseResult = parser.getCfgParseResult();
		// manually add the result parse tree, and its corresponding token stream,
		// because they logically belong to here
		mCfgParseResult.setParseTreeRoot( parseTreeRoot );
		mCfgParseResult.setTokens( tokenStream.getTokens() );
		
		// fill handlers
		moduleParametersHandler = parser.getModuleParametersHandler();
		testportParametersHandler = parser.getTestportParametersHandler();
		componentSectionHandler = parser.getComponentSectionHandler();
		groupSectionHandler = parser.getGroupSectionHandler();
		mcSectionHandler = parser.getMcSectionHandler();
		externalCommandsSectionHandler = parser.getExternalCommandsSectionHandler();
		executeSectionHandler = parser.getExecuteSectionHandler();
		includeSectionHandler = parser.getIncludeSectionHandler();
		orderedIncludeSectionHandler = parser.getOrderedIncludeSectionHandler();
		defineSectionHandler = parser.getDefineSectionHandler();
		loggingSectionHandler = parser.getLoggingSectionHandler();
		
		rootInterval = lexer.getRootInterval();
	}
}
