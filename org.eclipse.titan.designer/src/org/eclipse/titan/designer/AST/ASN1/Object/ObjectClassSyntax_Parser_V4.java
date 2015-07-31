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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_Parser;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_literal;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_root;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_sequence;
import org.eclipse.titan.designer.AST.ASN1.Object.Object_Definition;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;

/**
 * OCS visitor to parse an object definition.
 * ANTLR 4 specific
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ObjectClassSyntax_Parser_V4 extends ObjectClassSyntax_Parser {

	private final BlockV4 mBlockV4;

	public ObjectClassSyntax_Parser_V4(final BlockV4 blockV4, final Object_Definition myObject) {
		super( myObject );
		final List<org.antlr.v4.runtime.Token> tempTokens = blockV4.getTokenList();
		final List<org.antlr.v4.runtime.Token> temp = new ArrayList<org.antlr.v4.runtime.Token>(tempTokens.size());
		for (int i = 0; i < tempTokens.size(); i++) {
			temp.add(tempTokens.get(i));
		}
		temp.add(new TokenWithIndexAndSubTokensV4(org.antlr.v4.runtime.Token.EOF));

		this.mBlockV4 = new BlockV4(temp, blockV4.getLocation());
		this.mBlockV4.setFullNameParent(blockV4);
		success = true;
		internalIndex = 0;
	}

	@Override
	public void visitRoot(final ObjectClassSyntax_root parameter) {
		if (mBlockV4 != null) {
			if (!success || !parameter.getIsBuilded() || (mBlockV4.getTokenList().isEmpty())) {
				// FATAL ERROR, but now OK
				return;
			}
		}

		previousSuccess = false;
		parameter.getSequence().accept(this);
		if (null != mBlockV4) {
			if (success && internalIndex < mBlockV4.getTokenList().size() && mBlockV4.getTokenList().get(internalIndex).getType() != org.antlr.v4.runtime.Token.EOF) {
				success = false;
				final org.antlr.v4.runtime.Token token = mBlockV4.getTokenList().get(internalIndex);
				myObject.getLocation().reportSemanticError("Unexpected `" + token.getText() + "', it is a superfluous part");
			}
		} 

		if (!success) {
			myObject.getLocation().reportSemanticError("Check the syntax of objectclass");
			myObject.setIsErroneous(true);
		}
	}

	@Override
	public void visitLiteral(final ObjectClassSyntax_literal parameter) {
		previousSuccess = false;
		if (null != mBlockV4) {
			if (mBlockV4.getTokenList().size() <= internalIndex) {
				return;
			}
		}

		if (null != mBlockV4) {
			final org.antlr.v4.runtime.Token token = mBlockV4.getTokenList().get(internalIndex);
			if (null == token.getText()) {
				// reached the end of the block
				return;
			}
			if (token.getText().equals(parameter.getLiteral())) {
				if (internalIndex < mBlockV4.getTokenList().size() - 1) {
					internalIndex++;
				}
				previousSuccess = true;
			}
		}
	}

	@Override
	public void visitSequence(final ObjectClassSyntax_sequence parameter) {

		if (null != mBlockV4) {
			if (mBlockV4.getTokenList().size() <= internalIndex) {
				return;
			}
		}

		int i;
		
		if (null != mBlockV4) {
			org.antlr.v4.runtime.Token token = mBlockV4.getTokenList().get(internalIndex);
			if (parameter.getOptionalFirstComma() && myObject.getNofFieldSettings() > 0) {
				if (token.getType() == ASN1Lexer2.COMMA) {
					if (internalIndex < mBlockV4.getTokenList().size() - 1) {
						internalIndex++;
					}
				} else {
					if (parameter.getIsOptional()) {
						previousSuccess = true;
					} else {
						success = false;
						myObject.getLocation().reportSemanticError("Unexpected `" + token.getText() + "', expecting `,'");
					}
					return;
				}
				i = 0;
			} else {
				if (0 == parameter.getNofNodes()) {
					return;
				}
				parameter.getNthNode(0).accept(this);
				if (!success) {
					return;
				}
				if (!previousSuccess) {
					if (parameter.getIsOptional()) {
						previousSuccess = true;
					} else {
						success = false;
						myObject.getLocation().reportSemanticError(
								"Unexpected `" + token.getText() + "', expecting `"
										+ parameter.getNthNode(0).getDisplayName() + "'");
					}
					return;
				}
				i = 1;
			}
	
			for (; i < parameter.getNofNodes(); i++) {
				parameter.getNthNode(i).accept(this);
				if (!previousSuccess) {
					if (parameter.getIsOptional()) {
						previousSuccess = true;
						internalIndex--;
						return;
					}
					success = false;
					if (mBlockV4.getTokenList().size() <= internalIndex) {
						return;
					}
					token = mBlockV4.getTokenList().get(internalIndex);
					myObject.getLocation().reportSemanticError(
							"Unexpected `" + token.getText() + "', expecting `" + parameter.getNthNode(i).getDisplayName() + "'");
				}
				if (!success) {
					return;
				}
			}
		}
	}

	@Override
	protected ASN1Type parseType() {
		ASN1Type type = null;
		if (mBlockV4 != null) {
			ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4, internalIndex);
			if (parser != null) {
				type = parser.pr_special_Type().type;
				internalIndex += parser.nof_consumed_tokens();
				List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(),
								errors.get(i), IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		return type;
	}

	@Override
	protected boolean parseValue() {
		if (mBlockV4 != null) {
			ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4, internalIndex);
			if (parser != null) {
				parser.pr_special_Value();
				internalIndex += parser.nof_consumed_tokens();
				List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
				return true;
			}
			return false;
		}
		
		return false;
	}

	@Override
	protected ASN1Object parseObject() {
		ASN1Object object = null;
		if (mBlockV4 != null) {
			ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4, internalIndex);
			if (parser != null) {
				object = parser.pr_special_Object().object;
				internalIndex += parser.nof_consumed_tokens();
				List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		return object;
	}

	@Override
	protected ObjectSet parseObjectSet() {
		ObjectSet objectSet = null;
		if (mBlockV4 != null) {
			ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4, internalIndex);
			if (parser != null) {
				objectSet = parser.pr_special_ObjectSet().objectSet;
				internalIndex += parser.nof_consumed_tokens();
				List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		return objectSet;
	}
}
