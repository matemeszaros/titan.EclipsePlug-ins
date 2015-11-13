/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_setting.SyntaxSetting_types;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Lexer;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.SyntaxLevelTokenStreamTracker;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokens;

/**
 * OCS visitor to build the OCS. :) It's clear and simple, isn't it?
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ObjectClassSyntax_Builder extends ObjectClassSyntax_Visitor {

	protected Block mBlock;
	protected final FieldSpecifications fieldSpecifications;

	public ObjectClassSyntax_Builder(final Block aBlock, final FieldSpecifications aFieldSpecifications) {
		this.mBlock = aBlock;
		this.fieldSpecifications = aFieldSpecifications;
		if ( aBlock != null ) {
			final List<Token> internalTokens = new ArrayList<Token>(aBlock.getTokenList().size());
			Token token;

			for (int i = 0; i < aBlock.getTokenList().size(); i++) {
				token = aBlock.getTokenList().get(i);

				if (token.getType() == Asn1Lexer.LEFTVERSIONBRACKETS) {
					org.antlr.v4.runtime.CommonToken token2 = ((TokenWithIndexAndSubTokens) token).copy();
					token2.setType(Asn1Lexer.SQUAREOPEN);
					internalTokens.add(token2);
					internalTokens.add(token2);
				} else if (token.getType() == Asn1Lexer.RIGHTVERSIONBRACKETS) {
					org.antlr.v4.runtime.CommonToken token2 = ((TokenWithIndexAndSubTokens) token).copy();
					token2.setType(Asn1Lexer.SQUARECLOSE);
					internalTokens.add(token2);
					internalTokens.add(token2);
				} else {
					internalTokens.add(token);
				}
			}
			internalTokens.add(new TokenWithIndexAndSubTokens(Token.EOF));

			this.mBlock = new Block(internalTokens, aBlock.getLocation());
		}
	}

	@Override
	public void visitRoot(final ObjectClassSyntax_root parameter) {
		if (parameter.getIsBuilded()) {
			return;
		}

		if (null == mBlock) {
			final ObjectClassSyntax_sequence sequence = parameter.getSequence();
			FieldSpecification fieldSpecification;
			for (int i = 0; i < fieldSpecifications.getNofFieldSpecifications(); i++) {
				fieldSpecification = fieldSpecifications.getFieldSpecificationByIndex(i).getLast();
				ObjectClassSyntax_sequence temporalSequence = new ObjectClassSyntax_sequence(fieldSpecification.getIsOptional()
						|| fieldSpecification.hasDefault(), true);

				ObjectClassSyntax_literal literal = new ObjectClassSyntax_literal(fieldSpecification.getIdentifier().newInstance());
				literal.setLocation(fieldSpecification.getLocation());

				ObjectClassSyntax_setting setting = null;
				switch (fieldSpecification.getFieldSpecificationType()) {
				case FS_T:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_T, newIdentifier);
					break;
				}
				case FS_V_FT:
				case FS_V_VT:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_V, newIdentifier);
					break;
				}
				case FS_VS_FT:
				case FS_VS_VT:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_VS, newIdentifier);
					break;
				}
				case FS_O:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_O, newIdentifier);
					break;
				}
				case FS_OS:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_OS, newIdentifier);
					break;
				}
				case FS_ERROR:
				default:
					break;
				}

				if (null != setting) {
					setting.setLocation(fieldSpecification.getLocation());

					temporalSequence.addNode(literal);
					temporalSequence.addNode(setting);
					temporalSequence.trimToSize();

					sequence.addNode(temporalSequence);
				}
			}

			sequence.trimToSize();
		} else {
			parameter.getSequence().accept(this);
		}

		parameter.setIsBuilded(true);
	}

	public void visitSequence(final ObjectClassSyntax_sequence parameter) {
		if (parameter.getIsBuilded()) {
			return;
		}
		
		final Asn1Parser parser = SyntaxLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
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
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		} else if (parameter.getIsOptional() && 0 == parameter.getNofNodes()) {
			parameter.getLocation().reportSemanticError("Empty optional group is not allowed");
		}
		
		parameter.setIsBuilded(true);
		parameter.trimToSize();
	}

	@Override
	public void visitLiteral(final ObjectClassSyntax_literal parameter) {
		// FATAL ERROR
	}

	@Override
	public void visitSetting(final ObjectClassSyntax_setting parameter) {
		// FATAL ERROR
	}
}
