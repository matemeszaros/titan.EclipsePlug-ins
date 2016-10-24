/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The SelectUnionCase class is helper class for the SelectUnionCase_Statement class.
 * Represents a select union case branch parsed from the source code.
 * 
 * @see SelectUnionCase_Statement
 * @see SelectUnionCases
 * 
 * @author Arpad Lovassy
 */
public final class SelectUnionCase extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String NEVERREACH1 = "Control never reaches this code because of previous effective cases(s)";
	private static final String INVALIDUNIONFIELD = "Union `{0}'' has no field `{1}''";
	private static final String CASEALREADYCOVERED = "Case `{0}'' is already covered";

	private static final String FULLNAMEPART1 = ".identifier";
	private static final String FULLNAMEPART2 = ".block";

	private final Identifier mIdentifier;
	private final StatementBlock mStatementBlock;

	private Location location = NULL_Location.INSTANCE;

	public SelectUnionCase(final Identifier aIdentifier, final StatementBlock aStatementBlock) {
		this.mIdentifier = aIdentifier;
		this.mStatementBlock = aStatementBlock;

		if (mStatementBlock != null) {
			mStatementBlock.setFullNameParent(this);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (mIdentifier == child) {
			return builder.append(FULLNAMEPART1);
		} else if (mStatementBlock == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	public StatementBlock getStatementBlock() {
		return mStatementBlock;
	}

	/**
	 * Sets the scope of the select case branch.
	 * 
	 * @param scope
	 *                the scope to be set.
	 */
	@Override
	public void setMyScope(final Scope scope) {
		//TODO: remove
		/*
		if (mIdentifier != null) {
			mIdentifier.setMyScope(scope);
		}
		*/
		if (mStatementBlock != null) {
			mStatementBlock.setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyAltguards(altGuards);
		}
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	/** @return true if the select case is the else case, false otherwise. */
	public boolean hasElse() {
		return mIdentifier == null;
	}

	/**
	 * Checks whether the select case has a return statement, either
	 * directly or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the select case.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (mStatementBlock != null) {
			return mStatementBlock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	/**
	 * Does the semantic checking of this select case.
	 * 
	 * @param aTimestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param aUnionType
	 *                the referenced union type of the select expression, to check the cases against.
	 *                It can be null. In this case no check needs to be done,
	 *                because type check was done in SelectUnionCase_Statement.check() and it failed
	 * @param aUnreachable
	 *                tells if this case branch is still reachable or not.
	 * @param aFieldNames
	 *                union field names, which are not covered yet.
	 *                If a field name is found, it is removed from the list.
	 *                If case else is found, all the filed names are removed from the list, because all the cases are covered.
	 * 
	 * @return true if this case branch was found to be unreachable, false
	 *         otherwise.
	 * */
	public boolean check( final CompilationTimeStamp aTimestamp, final TTCN3_Choice_Type aUnionType, final boolean aUnreachable,
						  final List<String> aFieldNames ) {
		if ( aUnreachable ) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null), NEVERREACH1);
		}

		boolean unreachable2 = aUnreachable;
		if ( mIdentifier != null ) {
			if ( aUnionType != null ) {
				// name of the union component
				final String name = mIdentifier.getName();
				if ( aUnionType.hasComponentWithName( name ) ) {
					if ( aFieldNames.contains( name ) ) {
						aFieldNames.remove( name );
					} else {
						//this case is already covered
						location.reportSemanticWarning( MessageFormat.format( CASEALREADYCOVERED, name ) );
					}
				} else {
					location.reportSemanticError( MessageFormat.format( INVALIDUNIONFIELD, aUnionType.getFullName(), name ) );
				}
			}
		} else {
			// case else
			unreachable2 = true;
			aFieldNames.clear();
		}

		mStatementBlock.check( aTimestamp );

		return unreachable2;
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		if (mStatementBlock != null) {
			mStatementBlock.checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		if (mStatementBlock != null) {
			mStatementBlock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (mIdentifier != null) {
			//TODO: remove
			//mIdentifier.updateSyntax(reparser, false);
			reparser.updateLocation(mIdentifier.getLocation());
		}

		if (mStatementBlock != null) {
			mStatementBlock.updateSyntax(reparser, false);
			reparser.updateLocation(mStatementBlock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		//TODO: remove
		/*
		if (mIdentifier != null) {
			mIdentifier.findReferences(referenceFinder, foundIdentifiers);
		}
		*/
		if (mStatementBlock != null) {
			mStatementBlock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (mIdentifier != null && !mIdentifier.accept(v)) {
			return false;
		}
		if (mStatementBlock != null && !mStatementBlock.accept(v)) {
			return false;
		}
		return true;
	}
}
