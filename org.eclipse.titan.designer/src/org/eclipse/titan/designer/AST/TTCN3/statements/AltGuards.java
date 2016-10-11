/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard.altguard_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The AltGuards class represents the list of branches in a TTCN3
 * altstep/alt/interleave statement.
 *
 * @see AltGuard
 * @see Alt_Statement
 * @see Interleave_Statement
 * @see Def_Altstep
 * 
 * @author Kristof Szabados
 * */
public final class AltGuards extends ASTNode implements IIncrementallyUpdateable {
	private static final String SHADOWEDBYELSE = "Control never reaches this branch of alternative because of a previous [else] branch";

	private static final String FULLNAMEPART = ".alt_guard_";
	private final ArrayList<AltGuard> altGuards;

	/** stores whether the altguards were freed or not. */
	private boolean freed = false;

	/**
	 * The location of the whole assignment. This location encloses the
	 * assignment fully, as it is used to report errors to.
	 **/
	private Location location;

	public AltGuards() {
		altGuards = new ArrayList<AltGuard>();
		location = NULL_Location.INSTANCE;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = altGuards.size(); i < size; i++) {
			if (altGuards.get(i) == child) {
				return builder.append(FULLNAMEPART).append(String.valueOf(i + 1));
			}
		}

		return builder;
	}

	public void addAltGuard(final AltGuard altGuard) {
		if (altGuard != null) {
			altGuards.add(altGuard);
			altGuard.setFullNameParent(this);
		}
	}

	/**
	 * @return the number of altguards.
	 * */
	public int getNofAltguards() {
		return altGuards.size();
	}

	/**
	 * Returns the altguard at the specified position in this altguard list.
	 *
	 * @param i
	 *                the index of the altguard to return.
	 *
	 * @return the altguard at the index.
	 * */
	public AltGuard getAltguardByIndex(final int i) {
		return altGuards.get(i);
	}

	/**
	 * Sets the scope of the guard statement.
	 *
	 * @param scope
	 *                the scope to be set.
	 * */
	@Override
	public void setMyScope(final Scope scope) {
		altGuards.trimToSize();
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		for (int i = 0, size = this.altGuards.size(); i < size; i++) {
			this.altGuards.get(i).setMyAltguards(altGuards);
		}
	}

	/**
	 * Checks whether there is an else branch among the altguards.
	 *
	 * @return true if there is an else branch, false otherwise.
	 * */
	public boolean hasElse() {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);
			if (guard.getType() == altguard_type.AG_ELSE) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the altguards have a return statement, either directly
	 * or embedded.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the return status of the altguards.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		StatementBlock.ReturnStatus_type result = StatementBlock.ReturnStatus_type.RS_MAYBE;

		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);
			switch (guard.hasReturn(timestamp)) {
			case RS_NO:
				if (result == StatementBlock.ReturnStatus_type.RS_YES) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_NO;
				break;
			case RS_YES:
				if (result == StatementBlock.ReturnStatus_type.RS_NO) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_YES;
				break;
			default:
				return StatementBlock.ReturnStatus_type.RS_MAYBE;
			}

			if (guard instanceof Else_Altguard) {
				break;
			}
		}

		return result;
	}

	/**
	 * Does the semantic checking of the alt guard list.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		boolean unreachableFound = false;
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);
			guard.check(timestamp);

			if (unreachableFound) {
				guard.getLocation().reportConfigurableSemanticProblem(
						Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null),
						SHADOWEDBYELSE);
			}
			if (altguard_type.AG_ELSE.equals(guard.getType())) {
				unreachableFound = true;
			}
		}
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).postCheck();
		}
	}

	/**
	 * Free up the statements stored inside this statement block.
	 * */
	public void free() {
		if (StatementBlock.minimiseMemoryUsage && !freed) {
			freed = true;
			altGuards.clear();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			freed = false;
			boolean enveloped = false;
			int nofDamaged = 0;
			int leftBoundary = location.getOffset() + 1;
			int rightBoundary = location.getEndOffset() - 1;
			final int damageOffset = reparser.getDamageStart();

			for (int i = 0, size = altGuards.size(); i < size && !enveloped; i++) {
				final AltGuard altGuard = altGuards.get(i);
				final Location temporalLocation = altGuard.getLocation();

				if (reparser.envelopsDamage(temporalLocation)) {
					enveloped = true;
					leftBoundary = temporalLocation.getOffset();
					rightBoundary = temporalLocation.getEndOffset();
				} else if (reparser.isDamaged(temporalLocation)) {
					nofDamaged++;
				} else {
					if (temporalLocation.getEndOffset() < damageOffset && temporalLocation.getEndOffset() > leftBoundary) {
						leftBoundary = temporalLocation.getEndOffset() + 1;
					}
					if (temporalLocation.getOffset() >= damageOffset && temporalLocation.getOffset() < rightBoundary) {
						rightBoundary = temporalLocation.getOffset();
					}
				}
			}

			if (nofDamaged != 0) {
				throw new ReParseException();
			}

			for (Iterator<AltGuard> iterator = altGuards.iterator(); iterator.hasNext();) {
				final AltGuard altGuard = iterator.next();
				final Location temporalLocation = altGuard.getLocation();

				if (reparser.isAffectedAppended(temporalLocation)) {
					altGuard.updateSyntax(reparser, enveloped && reparser.envelopsDamage(temporalLocation));
					reparser.updateLocation(altGuard.getLocation());
				}
			}

			return;
		}

		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);

			guard.updateSyntax(reparser, false);
			reparser.updateLocation(guard.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (altGuards == null) {
			return;
		}

		for (AltGuard ag : altGuards) {
			ag.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (altGuards != null) {
			for (AltGuard ag : altGuards) {
				if (!ag.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
