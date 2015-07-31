/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.SyntaxLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4;

/**
 * OCS visitor to build the OCS. :) It's clear and simple, isn't it?
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ObjectClassSyntax_Builder_V4 extends ObjectClassSyntax_Builder {

	public ObjectClassSyntax_Builder_V4(final BlockV4 aBlockV4, final FieldSpecifications aFieldSpecifications) {
		super( aBlockV4, aFieldSpecifications );
		if ( aBlockV4 != null ) {
			final List<Token> internalTokens = new ArrayList<org.antlr.v4.runtime.Token>(aBlockV4.getTokenList().size());
			Token token;

			for (int i = 0; i < aBlockV4.getTokenList().size(); i++) {
				token = aBlockV4.getTokenList().get(i);

				if (token.getType() == ASN1Lexer2.LEFTVERSIONBRACKETS) {
					org.antlr.v4.runtime.CommonToken token2 = ((TokenWithIndexAndSubTokensV4) token).copy();
					token2.setType(ASN1Lexer2.SQUAREOPEN);
					internalTokens.add(token2);
					internalTokens.add(token2);
				} else if (token.getType() == ASN1Lexer2.RIGHTVERSIONBRACKETS) {
					org.antlr.v4.runtime.CommonToken token2 = ((TokenWithIndexAndSubTokensV4) token).copy();
					token2.setType(ASN1Lexer2.SQUARECLOSE);
					internalTokens.add(token2);
					internalTokens.add(token2);
				} else {
					internalTokens.add(token);
				}
			}
			internalTokens.add(new TokenWithIndexAndSubTokensV4(Token.EOF));

			this.mBlock = new BlockV4(internalTokens, aBlockV4.getLocation());
		}
	}

	
	@Override
	public void visitSequence(final ObjectClassSyntax_sequence parameter) {
		if (parameter.getIsBuilded()) {
			return;
		}
		
		final ASN1Parser2 parser = SyntaxLevelTokenStreamTrackerV4.getASN1ParserForBlock((BlockV4)mBlock);
		if (null == parser) {
			return;
		}
		final List<ObjectClassSyntax_Node> nodes = parser.pr_special_ObjectClassSyntax_Builder(fieldSpecifications).nodes;
		if (null != nodes) {
			for (ObjectClassSyntax_Node node : nodes) {
				parameter.addNode(node);
			}
		}
		List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		} else if (parameter.getIsOptional() && 0 == parameter.getNofNodes()) {
			parameter.getLocation().reportSemanticError("Empty optional group is not allowed");
		}
		
		parameter.setIsBuilded(true);
		parameter.trimToSize();
	}
}
