/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NameReStarter;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Lexer;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;
import org.eclipse.titan.designer.parsers.asn1parser.FormalParameter_Helper;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokens;

/**
 * Parameterized reference. This is not the parameterised reference, that
 * represents a function call !
 * <p>
 * Should be implementing Defined Reference, but there seems to be no reason for
 * that.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Parameterised_Reference extends Defined_Reference {
	protected static final String FULLNAMEPART1 = ".<block>";
	protected static final String FULLNAMEPART2 = "{}";

	private static final String ASSIGNMENTEXPECTED = "ASN.1 assignment expected";
	private static final String PARAMETERISEDASSIGNMENTEXPECTED = "Parameterized assignment expected";
	protected static final String DIFFERENTPARAMETERNUMBERS = "Too {0} parameters: `{1}'' was expected instead of `{2}''";

	private final Block mBlock;

	protected final Defined_Reference assignmentReference;
	protected boolean isErroneous;

	protected ASN1Assignments assignments;
	private Defined_Reference finalReference;

	private CompilationTimeStamp lastChekTimeStamp;

	private NameReStarter newAssignmentNameStart;

	protected Location location;

	public Parameterised_Reference(final Defined_Reference reference, final Block aBlock) {
		super(null);
		assignmentReference = reference;
		isErroneous = false;
		this.mBlock = aBlock;

		assignmentReference.setFullNameParent(this);
		if (null != aBlock) {
			aBlock.setFullNameParent(this);
		}
	}

	public Parameterised_Reference newInstance() {
		Parameterised_Reference temp = null;
		temp = new Parameterised_Reference(assignmentReference.newInstance(), mBlock);
		temp.setLocation(new Location(location));

		return temp;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		assignmentReference.setMyScope(scope);
	}

	@Override
	public Identifier getId() {
		final Defined_Reference ref = getRefDefdSimple();
		if (null != ref) {
			return ref.getId();
		}
		return null;
	}

	@Override
	public Identifier getModuleIdentifier() {
		final Defined_Reference ref = getRefDefdSimple();
		if (null != ref) {
			return ref.getModuleIdentifier();
		}
		return null;
	}

	@Override
	public String getDisplayName() {
		getRefDefdSimple();
		if (null != finalReference) {
			return finalReference.getDisplayName();
		}

		return assignmentReference.getDisplayName() + "{}";
	}

	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (mBlock == child) {
			return builder.append(FULLNAMEPART1);
		} else if (assignments == child) {
			return builder.append(INamedNode.DOT).append(assignmentReference.getFullName()).append(FULLNAMEPART2);
		}

		return builder;
	}

	/**
	 * Resolve the formal parameters of the referenced assignment with the
	 * help of the actual parameters. Instantiate a new assignment from it
	 * and return a reference to this assignment.
	 *
	 * @return the reference to the newly instantiated assignment.
	 * */
	public Defined_Reference getRefDefdSimple() {
		final Module module = myScope.getModuleScope();

		// This is a little trick, but otherwise we would not have the
		// true compilation timestamp
		final CompilationTimeStamp compilationTimeStamp = module.getLastImportationCheckTimeStamp();
		if (null != lastChekTimeStamp && !lastChekTimeStamp.isLess(compilationTimeStamp)) {
			if (isErroneous) {
				return null;
			}

			return finalReference;
		}

		lastChekTimeStamp = compilationTimeStamp;

		final Assignment parass = assignmentReference.getRefdAssignment(compilationTimeStamp, true);
		if (null == parass) {
			isErroneous = true;
			return null;
		} else if (!(parass instanceof ASN1Assignment)) {
			assignmentReference.getLocation().reportSemanticError(ASSIGNMENTEXPECTED);
			isErroneous = true;
			return null;
		}

		final Ass_pard assPard = ((ASN1Assignment) parass).getAssPard();
		if (null == assPard) {
			assignmentReference.getLocation().reportSemanticError(PARAMETERISEDASSIGNMENTEXPECTED);
			isErroneous = true;
			return assignmentReference;
		}

		addAssignments( assPard, compilationTimeStamp );
		
		// Add the assignments made from the formal and actual
		// parameters to the actual module
		assignments.setRightScope(myScope);
		assignments.setParentScope(parass.getMyScope());
		assignments.setFullNameParent(this);
		assignments.check(compilationTimeStamp);

		// create a copy of the assignment and add it to the actual
		// module
		final ASN1Assignment newAssignment = ((ASN1Assignment) parass).newInstance(module);

		newAssignmentNameStart = new NameReStarter(new StringBuilder(module.getFullName()).append(INamedNode.DOT)
				.append(newAssignment.getIdentifier().getDisplayName()).toString());
		newAssignmentNameStart.setFullNameParent(parass);
		newAssignment.setFullNameParent(newAssignmentNameStart);
		newAssignment.setLocation(location);
		newAssignment.getIdentifier().setLocation(assignmentReference.getLocation());

		((ASN1Assignments) module.getAssignments()).addDynamicAssignment(compilationTimeStamp, newAssignment);
		newAssignment.setMyScope(assignments);
		newAssignment.check(compilationTimeStamp);

		final List<ISubReference> subreferences = new ArrayList<ISubReference>(1);
		subreferences.add(new FieldSubReference(newAssignment.getIdentifier()));

		finalReference = new Defined_Reference(module.getIdentifier(), subreferences);
		finalReference.setFullNameParent(this);
		finalReference.setMyScope(module);
		return finalReference;
	}
	
	/**
	 * Fill the assignments according to the formal parameters
	 * @param aAssPard (in) formal parameters for the conversion
	 * @param aCompilationTimeStamp compilation timestamp
	 */
	private void addAssignments(final Ass_pard aAssPard, final CompilationTimeStamp aCompilationTimeStamp) {
		final List<FormalParameter_Helper> formalParameters = ((Ass_pard)aAssPard).getFormalParameters(aCompilationTimeStamp);

		final int nofFormalParameters = formalParameters.size();
		if (null != mBlock) {
			final List<List<Token>> actualParameters = new ArrayList<List<Token>>();
			List<Token> temporalBuffer = new ArrayList<Token>();

			/* splitting the list of actual parameters */
			final List<Token> unprocessParameters = mBlock.getTokenList();

			for (int i = 0; i < unprocessParameters.size(); i++) {
				Token tempToken = unprocessParameters.get(i);
				if (tempToken.getType() == Asn1Lexer.COMMA) {
					temporalBuffer.add(new TokenWithIndexAndSubTokens(Token.EOF));
					actualParameters.add(temporalBuffer);
					temporalBuffer = new ArrayList<Token>();
				} else {
					temporalBuffer.add(tempToken);
				}
			}

			if (!temporalBuffer.isEmpty()) {
				temporalBuffer.add(new TokenWithIndexAndSubTokens(Token.EOF));
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

					temporalTokenBuffer.add(new TokenWithIndexAndSubTokens(Asn1Lexer.ASSIGNMENT));
					temporalTokenBuffer.addAll(actualParameters.get(i));

					// parse temp_tokenBuffer as an
					// assignment
					//List<ANTLRException> exceptions = null;
					final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(new Block(temporalTokenBuffer,
							location));

					if (null != parser) {
						temporalAssignment = parser.pr_special_Assignment().assignment;
						List<SyntacticErrorStorage> errors = parser.getErrorStorage();
						if (null != errors && !errors.isEmpty()) {
							isErroneous = true;
							temporalAssignment = null;
							for (int j = 0; j < errors.size(); j++) {
								ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(j),
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
