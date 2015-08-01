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
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;

/**
 * This class directs the incremental parsing. Stores all information about the nature and size of the damage done to the system, helps in reparsing
 * only the needed part of the file. And also takes care of cleaning and reporting errors inside the damaged area.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TTCN3ReparseUpdater_V4 extends TTCN3ReparseUpdater {
	
	/** Errors from the parser (indicating syntax errors). */
	private List<SyntacticErrorStorage> mErrors;

	public TTCN3ReparseUpdater_V4(IFile file, String code, int firstLine, int lineShift, int startOffset, int endOffset, int shift) {
		super(file, code, firstLine, lineShift, startOffset, endOffset, shift);
	}

	public int parse(ITTCN3ReparseBase_V4 userDefined) {
		if (modificationStartOffset == modificationEndOffset + shift) {
			return 0;
		}
		// double wideparsing = System.nanoTime();
		mErrors = null;
		warnings = null;
		Iterator<TITANMarker> iterator = unsupportedConstructs.iterator();
		while (iterator.hasNext()) {
			TITANMarker marker = iterator.next();
			if ((marker.getOffset() > modificationStartOffset && marker.getOffset() <= modificationEndOffset)
					|| (marker.getEndOffset() > modificationStartOffset && marker.getEndOffset() <= modificationEndOffset)) {
				iterator.remove();
			}
		}

		MarkerHandler.markAllOnTheFlyMarkersForRemoval(file, modificationStartOffset, modificationEndOffset + shift);

		if (code == null) {
			return Integer.MAX_VALUE;
		}

		int line = getLineOfOfset(code, modificationStartOffset);
		String substring;
		if (code.length() <= modificationEndOffset + shift) {
			substring = code.substring(modificationStartOffset);
		} else {
			substring = code.substring(modificationStartOffset, modificationEndOffset + shift);
		}
		Reader reader = new StringReader(substring);
		CharStream charStream = new UnbufferedCharStream(reader);
		TTCN3Lexer4 lexer = new TTCN3Lexer4(charStream);
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval(modificationEndOffset - modificationStartOffset + 1);
		
		// lexer and parser listener
		TitanListener parserListener = new TitanListener();
		// remove ConsoleErrorListener
		lexer.removeErrorListeners();
		lexer.addErrorListener(parserListener);

		// Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		TokenStream tokens = new BufferedTokenStream( lexer );
		
		TTCN3Reparser4 parser = new TTCN3Reparser4( tokens );

		lexer.setActualFile(file);
		parser.setActualFile(file);
		parser.setProject(file.getProject());
		parser.setLexer(lexer);
		parser.setOffset( modificationStartOffset );
		parser.setLine( line + 1 );

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		parser.addErrorListener( parserListener );
		
		//double narrowparsing = System.nanoTime();

		userDefined.reparse(parser);
		mErrors = parserListener.getErrorsStored();
		warnings = parser.getWarnings();
		unsupportedConstructs.addAll(parser.getUnsupportedConstructs());
		
		//TITANDebugConsole.getConsole().newMessageStream().println("  parsing " + file.getName() + " took " + (System.nanoTime() - narrowparsing) * (1e-9) + " secs");

		int result = 0;
		if (mErrors != null && !mErrors.isEmpty()) {
			result = measureIntervallDamage();
		}

		//TITANDebugConsole.getConsole().newMessageStream().println("  (wider)parsing " + file.getName() + " took " + (System.nanoTime() - wideparsing) * (1e-9) + " secs");

		return result;
	}

	@Override
	public boolean startsWithFollow(List<Integer> followSet) {
		if (followSet == null || followSet.isEmpty()) {
			return false;
		}

		if (code == null) {
			return false;
		}

		int line = getLineOfOfset(code, modificationStartOffset);
		int column = getPositionInLine(code, modificationStartOffset);
		String substring;
		if (code.length() <= modificationEndOffset + shift) {
			substring = code.substring(modificationStartOffset);
		} else {
			substring = code.substring(modificationStartOffset, modificationEndOffset + shift);
		}
		Reader reader = new StringReader(substring);
		CharStream charStream = new UnbufferedCharStream(reader);
		TTCN3Lexer4 lexer = new TTCN3Lexer4(charStream);
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.setLine( line + 1 );
		lexer.setCharPositionInLine(column);
		lexer.initRootInterval(modificationEndOffset - modificationStartOffset + 1);

		Token token = lexer.nextToken();
		if (token == null) {
			return false;
		}

		return followSet.contains( token.getType() );
	}

	@Override
	public boolean endsWithToken(List<Integer> followSet) {
		if (followSet == null || followSet.isEmpty()) {
			return false;
		}

		if (code == null) {
			return false;
		}

		int line = getLineOfOfset(code, modificationStartOffset);
		int column = getPositionInLine(code, modificationStartOffset);
		String substring;
		if (code.length() <= modificationEndOffset + shift) {
			substring = code.substring(modificationStartOffset);
		} else {
			substring = code.substring(modificationStartOffset, modificationEndOffset + shift);
		}
		Reader reader = new StringReader(substring);
		CharStream charStream = new UnbufferedCharStream(reader);
		TTCN3Lexer4 lexer = new TTCN3Lexer4(charStream);
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.setLine( line + 1 );
		lexer.setCharPositionInLine(column);
		lexer.initRootInterval(modificationEndOffset - modificationStartOffset + 1);

		Token token = lexer.nextToken();
		if (token == null) {
			return false;
		}

		return followSet.contains( token.getType() );
	}

	@Override
	protected void reportSpecificSyntaxErrors() {
		if (mErrors != null) {
			Location temp = new Location(file, firstLine, modificationStartOffset, modificationEndOffset + shift);
			for (int i = 0; i < mErrors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlySyntacticMarker(file, mErrors.get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}
	}
}
