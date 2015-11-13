/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The SelectCase class is helper class for the SelectCase_Statement class.
 * Represents a select case branch parsed from the source code.
 * 
 * @see SelectCase_Statement
 * @see SelectCases
 * 
 * @author Kristof Szabados
 * */
public final class SelectCase extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String NEVERREACH1 = "Control never reaches this code because of previous effective cases(s)";

	private static final String FULLNAMEPART1 = ".templateinstances";
	private static final String FULLNAMEPART2 = ".block";

	private final TemplateInstances templateInstances;
	private final StatementBlock statementblock;

	private Location location = NULL_Location.INSTANCE;

	public SelectCase(final TemplateInstances templateInstances, final StatementBlock statementblock) {
		this.templateInstances = templateInstances;
		this.statementblock = statementblock;

		if (templateInstances != null) {
			templateInstances.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (templateInstances == child) {
			return builder.append(FULLNAMEPART1);
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}

	/**
	 * Sets the scope of the select case branch.
	 * 
	 * @param scope
	 *                the scope to be set.
	 * */
	@Override
	public void setMyScope(final Scope scope) {
		if (templateInstances != null) {
			templateInstances.setMyScope(scope);
		}
		if (statementblock != null) {
			statementblock.setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
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
		return templateInstances == null;
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
		if (statementblock != null) {
			return statementblock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	/**
	 * Does the semantic checking of this select case.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param governor
	 *                the governor of the select expression, to check the
	 *                cases against.
	 * @param unreachable
	 *                tells if this case branch is still reachable or not.
	 * 
	 * @return true if this case branch was found to be unreachable, false
	 *         otherwise.
	 * */
	public boolean check(final CompilationTimeStamp timestamp, final IType governor, final boolean unreachable) {
		if (unreachable) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null), NEVERREACH1);
		}

		boolean unreachable2 = unreachable;
		if (templateInstances != null) {
			for (int i = 0; i < templateInstances.getNofTis(); i++) {
				templateInstances.getInstanceByIndex(i).check(timestamp, governor);
			}
		} else {
			unreachable2 = true;
		}

		statementblock.check(timestamp);

		return unreachable2;
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (templateInstances != null) {
			templateInstances.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstances.getLocation());
		}

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstances != null) {
			templateInstances.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (templateInstances != null && !templateInstances.accept(v)) {
			return false;
		}
		if (statementblock != null && !statementblock.accept(v)) {
			return false;
		}
		return true;
	}
}
