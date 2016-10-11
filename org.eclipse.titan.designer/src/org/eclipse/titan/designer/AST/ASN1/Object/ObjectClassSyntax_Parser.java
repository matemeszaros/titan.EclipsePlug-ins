/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Lexer;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokens;

/**
 * OCS visitor to parse an object definition.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ObjectClassSyntax_Parser extends ObjectClassSyntax_Visitor {

	private final Block mBlock;
	protected final Object_Definition myObject;

	/**
	 * Stores whether the parsing was successful. If it is false, the
	 * parsing cannot be continued.
	 * */
	protected boolean success;
	/** Stores whether the previous parsing was successful. */
	protected boolean previousSuccess;

	/** the actual index till which the tokens are already parsed. */
	protected int internalIndex;

	public ObjectClassSyntax_Parser(final Block aBlock, final Object_Definition myObject) {
		this.myObject = myObject;
		final List<Token> tempTokens = aBlock.getTokenList();
		final List<Token> temp = new ArrayList<Token>(tempTokens.size());
		for (int i = 0; i < tempTokens.size(); i++) {
			temp.add(tempTokens.get(i));
		}
		temp.add(new TokenWithIndexAndSubTokens(Token.EOF));

		this.mBlock = new Block(temp, aBlock.getLocation());
		this.mBlock.setFullNameParent(aBlock);
		success = true;
		internalIndex = 0;
	}

	@Override
	public void visitSetting(final ObjectClassSyntax_setting parameter) {
		FieldSetting fieldSetting = null;
		switch (parameter.getSettingType()) {
		case S_T:
			final ASN1Type type = parseType();
			if (null != type) {
				fieldSetting = new FieldSetting_Type(parameter.getIdentifier().newInstance(), type);
				fieldSetting.setLocation(mBlock.getLocation());
			}
			break;
		case S_V:
			final boolean parseSuccess = parseValue();
			if (parseSuccess) {
				fieldSetting = new FieldSetting_Value(parameter.getIdentifier().newInstance());
				fieldSetting.setLocation(mBlock.getLocation());
			}
			break;
		case S_VS:
			// TODO mark as NOT SUPPORTED
			break;
		case S_O:
			final ASN1Object object = parseObject();
			if (null != object) {
				fieldSetting = new FieldSetting_Object(parameter.getIdentifier().newInstance(), object);
				fieldSetting.setLocation(mBlock.getLocation());
			}
			break;
		case S_OS:
			final ObjectSet objectSet = parseObjectSet();
			if (null != objectSet) {
				fieldSetting = new FieldSetting_ObjectSet(parameter.getIdentifier().newInstance(), objectSet);
				fieldSetting.setLocation(mBlock.getLocation());
			}
			break;
		case S_UNDEF:
			// FATAL ERROR
		default:
			break;
		}

		previousSuccess = null != fieldSetting;
		myObject.addFieldSetting(fieldSetting);
	}

	@Override
	public void visitRoot(final ObjectClassSyntax_root parameter) {
		if (mBlock != null) {
			if (!success || !parameter.getIsBuilded() || (mBlock.getTokenList().isEmpty())) {
				// FATAL ERROR, but now OK
				return;
			}
		}

		previousSuccess = false;
		parameter.getSequence().accept(this);
		if (null != mBlock) {
			if (success && internalIndex < mBlock.getTokenList().size() && mBlock.getTokenList().get(internalIndex).getType() != Token.EOF) {
				success = false;
				final Token token = mBlock.getTokenList().get(internalIndex);
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
		if (null != mBlock) {
			if (mBlock.getTokenList().size() <= internalIndex) {
				return;
			}
		}

		if (null != mBlock) {
			final Token token = mBlock.getTokenList().get(internalIndex);
			if (null == token.getText()) {
				// reached the end of the block
				return;
			}
			if (token.getText().equals(parameter.getLiteral())) {
				if (internalIndex < mBlock.getTokenList().size() - 1) {
					internalIndex++;
				}
				previousSuccess = true;
			}
		}
	}

	@Override
	public void visitSequence(final ObjectClassSyntax_sequence parameter) {

		if (null != mBlock) {
			if (mBlock.getTokenList().size() <= internalIndex) {
				return;
			}
		}

		int i;
		
		if (null != mBlock) {
			Token token = mBlock.getTokenList().get(internalIndex);
			if (parameter.getOptionalFirstComma() && myObject.getNofFieldSettings() > 0) {
				if (token.getType() == Asn1Lexer.COMMA) {
					if (internalIndex < mBlock.getTokenList().size() - 1) {
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
					if (mBlock.getTokenList().size() <= internalIndex) {
						return;
					}
					token = mBlock.getTokenList().get(internalIndex);
					myObject.getLocation().reportSemanticError(
							"Unexpected `" + token.getText() + "', expecting `" + parameter.getNthNode(i).getDisplayName() + "'");
				}
				if (!success) {
					return;
				}
			}
		}
	}

	private ASN1Type parseType() {
		ASN1Type type = null;
		if (mBlock != null) {
			final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock, internalIndex);
			if (parser != null) {
				type = parser.pr_special_Type().type;
				internalIndex += parser.nof_consumed_tokens();
				final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(),
								errors.get(i), IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		return type;
	}

	private boolean parseValue() {
		if (mBlock != null) {
			final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock, internalIndex);
			if (parser != null) {
				parser.pr_special_Value();
				internalIndex += parser.nof_consumed_tokens();
				final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
				return true;
			}
			return false;
		}
		
		return false;
	}

	private ASN1Object parseObject() {
		ASN1Object object = null;
		if (mBlock != null) {
			final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock, internalIndex);
			if (parser != null) {
				object = parser.pr_special_Object().object;
				internalIndex += parser.nof_consumed_tokens();
				final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		return object;
	}

	private ObjectSet parseObjectSet() {
		ObjectSet objectSet = null;
		if (mBlock != null) {
			final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock, internalIndex);
			if (parser != null) {
				objectSet = parser.pr_special_ObjectSet().objectSet;
				internalIndex += parser.nof_consumed_tokens();
				final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
			}
		}
		
		return objectSet;
	}
}
