/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.extensionattributeparser;

import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Extension attribute parser analyzer 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExtensionAttributeAnalyzer {

	private List<ExtensionAttribute> attributes;

	public List<ExtensionAttribute> getAttributes() {
		return attributes;
	}

	public void parse(AttributeSpecification specification) {
		ExtensionAttributeLexer lexer;
		Location location = specification.getLocation();

		StringReader reader = new StringReader(specification.getSpecification());
		CharStream charStream = new UnbufferedCharStream(reader);
		lexer = new ExtensionAttributeLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		TitanListener lexerListener = new TitanListener();
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);
		
		// Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		TokenStream tokens = new BufferedTokenStream( lexer );
		ExtensionAttributeParser parser = new ExtensionAttributeParser(tokens);
		parser.setBuildParseTree(false);
		
		TitanListener parserListener = new TitanListener();
		parser.removeErrorListeners();
		parser.addErrorListener(parserListener);
		
		parser.setActualFile((IFile)location.getFile());
		parser.setLine(location.getLine());
		parser.setOffset(location.getOffset() + 1);

		MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, location.getFile(), location.getOffset(),
				location.getEndOffset());

		attributes = null;
		attributes = parser.pr_ExtensionAttributeRoot().list;

		if (!lexerListener.getErrorsStored().isEmpty()) {
			String reportLevel = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, GeneralConstants.WARNING, null);
			int errorLevel;
			if (GeneralConstants.ERROR.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_ERROR;
			} else if (GeneralConstants.WARNING.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_WARNING;
			} else {
				return;
			}
			for (int i = 0; i < lexerListener.getErrorsStored().size(); i++) {
				Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), lexerListener.getErrorsStored().get(i), errorLevel, temp);
			}
		}
		if (!parserListener.getErrorsStored().isEmpty()) {
			String reportLevel = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, GeneralConstants.WARNING, null);
			int errorLevel;
			if (GeneralConstants.ERROR.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_ERROR;
			} else if (GeneralConstants.WARNING.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_WARNING;
			} else {
				return;
			}
			for (int i = 0; i < parserListener.getErrorsStored().size(); i++) {
				Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), parserListener.getErrorsStored().get(i), errorLevel, temp);
			}
		}
	}
}
