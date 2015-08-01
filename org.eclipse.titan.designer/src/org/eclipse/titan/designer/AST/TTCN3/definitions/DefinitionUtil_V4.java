/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

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
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3CharstringLexer4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Lexer4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * Utility class for Definition class, which represents general TTCN3 definitions.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class DefinitionUtil_V4 {

	public static ErroneousAttributeSpecification parseErrAttrSpecString(final AttributeSpecification aAttrSpec) {
		ErroneousAttributeSpecification returnValue = null;
		Location location = aAttrSpec.getLocation();
		String code = aAttrSpec.getSpecification();
		if (code == null) {
			return null;
		}
		// code must be transformed, according to
		// compiler2/ttcn3/charstring_la.l
		code = TTCN3CharstringLexer4.parseCharstringValue(code, location); // TODO
		Reader reader = new StringReader(code);
		CharStream charStream = new UnbufferedCharStream(reader);
		TTCN3Lexer4 lexer = new TTCN3Lexer4(charStream);
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		// needs to be shifted by one because of the \" of the string
		lexer.setLine( location.getLine() );
		lexer.setCharPositionInLine( 0 );

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
		IFile file = (IFile) location.getFile();
		parser.setActualFile(file);
		parser.setOffset( location.getOffset() + 1 );
		parser.setLine( location.getLine() );

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		parser.addErrorListener( parserListener );
		
		MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, location.getFile(), location.getOffset(),
				location.getEndOffset());

		List<SyntacticErrorStorage> errors = new ArrayList<SyntacticErrorStorage>();
		List<TITANMarker> warnings = new ArrayList<TITANMarker>();
		List<TITANMarker> unsupportedConstructs = new ArrayList<TITANMarker>();
		returnValue = parser.pr_ErroneousAttributeSpec().errAttrSpec;
		errors = parser.getErrors();
		warnings = parser.getWarnings();
		unsupportedConstructs = parser.getUnsupportedConstructs();

		// add markers
		if (errors != null) {
			for (int i = 0; i < errors.size(); i++) {
				Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport_V4.createOnTheFlySyntacticMarker(file, errors.get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}
		if (warnings != null) {
			for (TITANMarker marker : warnings) {
				if (file.isAccessible()) {
					Location loc = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					loc.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}
		if (unsupportedConstructs != null) {
			for (TITANMarker marker : unsupportedConstructs) {
				if (file.isAccessible()) {
					Location loc = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					loc.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}
		return returnValue;
	}
}
