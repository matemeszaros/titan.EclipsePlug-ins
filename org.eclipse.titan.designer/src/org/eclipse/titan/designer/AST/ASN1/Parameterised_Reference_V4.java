/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignments;
import org.eclipse.titan.designer.AST.ASN1.Ass_pard;
import org.eclipse.titan.designer.AST.ASN1.Ass_pard_V4;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.AST.ASN1.Parameterised_Reference;
import org.eclipse.titan.designer.AST.ASN1.Type_Assignment;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.FormalParameter_Helper_V4;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4;

/**
 * Parameterized reference. This is not the parameterised reference, that
 * represents a function call !
 * <p>
 * Should be implementing Defined Reference, but there seems to be no reason for
 * that.
 * <p>
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Parameterised_Reference_V4 extends Parameterised_Reference {
	
	private final BlockV4 mBlockV4;

	public Parameterised_Reference_V4(final Defined_Reference reference, final BlockV4 aBlockV4) {
		super(reference);
		this.mBlockV4 = aBlockV4;
		if (null != aBlockV4) {
			aBlockV4.setFullNameParent(this);
		}
	}

	@Override
	public Parameterised_Reference newInstance() {
		Parameterised_Reference temp = null;
		temp = new Parameterised_Reference_V4(assignmentReference.newInstance(), mBlockV4);
		temp.setLocation(new Location(location));

		return temp;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (mBlockV4 == child) {
			return builder.append(FULLNAMEPART1);
		} else if (assignments == child) {
			return builder.append(INamedNode.DOT).append(assignmentReference.getFullName()).append(FULLNAMEPART2);
		}

		return builder;
	}
	
	@Override
	protected void addAssignments(Ass_pard aAssPard, CompilationTimeStamp aCompilationTimeStamp) {
		final List<FormalParameter_Helper_V4> formalParameters = ((Ass_pard_V4)aAssPard).getFormalParameters(aCompilationTimeStamp);

		final int nofFormalParameters = formalParameters.size();
		if (null != mBlockV4) {
			final List<List<Token>> actualParameters = new ArrayList<List<Token>>();
			List<Token> temporalBuffer = new ArrayList<Token>();

			/* splitting the list of actual parameters */
			final List<Token> unprocessParameters = mBlockV4.getTokenList();

			for (int i = 0; i < unprocessParameters.size(); i++) {
				Token tempToken = unprocessParameters.get(i);
				if (tempToken.getType() == ASN1Lexer2.COMMA) {
					temporalBuffer.add(new TokenWithIndexAndSubTokensV4(Token.EOF));
					actualParameters.add(temporalBuffer);
					temporalBuffer = new ArrayList<Token>();
				} else {
					temporalBuffer.add(tempToken);
				}
			}

			if (!temporalBuffer.isEmpty()) {
				temporalBuffer.add(new TokenWithIndexAndSubTokensV4(Token.EOF));
				actualParameters.add(temporalBuffer);
			}

			/* checking the number of parameters */
			final int nofActualParameters = actualParameters.size();
			if (nofActualParameters != nofFormalParameters) {
				location.reportSemanticError(MessageFormat.format(DIFFERENTPARAMETERNUMBERS,
						(nofActualParameters < nofFormalParameters) ? "few" : "many", nofFormalParameters,
						nofActualParameters));
			}

			assignments = new ASN1Assignments();

			for (int i = 0; i < nofFormalParameters; i++) {
				final Identifier tempIdentifier = formalParameters.get(i).identifier;
				ASN1Assignment temporalAssignment = null;

				if (i < nofActualParameters) {
					List<Token> temporalTokenBuffer = new ArrayList<Token>();
					temporalTokenBuffer.add(formalParameters.get(i).formalParameterToken);
					Token temporalToken = formalParameters.get(i).governorToken;
					if (null != temporalToken) {
						temporalTokenBuffer.add(temporalToken);
					}

					temporalTokenBuffer.add(new TokenWithIndexAndSubTokensV4(ASN1Lexer2.ASSIGNMENT));
					temporalTokenBuffer.addAll(actualParameters.get(i));

					// parse temp_tokenBuffer as an
					// assignment
					//List<ANTLRException> exceptions = null;
					final ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(new BlockV4(temporalTokenBuffer,
							location));

					if (null != parser) {
						temporalAssignment = parser.pr_special_Assignment().assignment;
						List<SyntacticErrorStorage> errors = parser.getErrorStorage();
						if (null != errors && !errors.isEmpty()) {
							isErroneous = true;
							temporalAssignment = null;
							for (int j = 0; j < errors.size(); j++) {
								ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(j),
										IMarker.SEVERITY_ERROR);
							}
						}
					}
				}

				if (null == temporalAssignment) {
					temporalAssignment = new Type_Assignment(tempIdentifier, null, null);
				}
				temporalAssignment.setLocation(location);
				assignments.addAssignment(temporalAssignment);
			}

			for (List<Token> temporalActualParamater : actualParameters) {
				temporalActualParamater.clear();
			}

			actualParameters.clear();
		}
	}
}
