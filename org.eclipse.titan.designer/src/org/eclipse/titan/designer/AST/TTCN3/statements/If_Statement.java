/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Lexer4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The If_Statement class represents TTCN3 if statements.
 * 
 * @see If_Clauses
 * @see If_Clause
 * 
 * @author Kristof Szabados
 * */
public final class If_Statement extends Statement {
	private static final String NEVERREACH = "Control never reaches this code because of previous effective condition(s)";
	private static final String IFWITHOUTELSE = "Conditional operation without else clause";

	private static final String FULLNAMEPART1 = ".ifclauses";
	private static final String FULLNAMEPART2 = ".elseblock";
	private static final String STATEMENT_NAME = "if";

	private final If_Clauses if_clauses;

	/**
	 * this statementblock represents the else clause.
	 * <p>
	 * Null is a valid value.
	 * */
	private final StatementBlock statementblock;

	/** whether to report the problem of not having an else branch */
	private static String reportIfWithoutElse;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			reportIfWithoutElse = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_IF_WITHOUT_ELSE, GeneralConstants.IGNORE, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORT_IF_WITHOUT_ELSE.equals(property)) {
							reportIfWithoutElse = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORT_IF_WITHOUT_ELSE,
									GeneralConstants.IGNORE, null);
						}
					}
				});
			}
		}
	}

	public If_Statement(final If_Clauses if_clauses, final StatementBlock statementblock) {
		this.if_clauses = if_clauses;
		this.statementblock = statementblock;

		if (if_clauses != null) {
			if_clauses.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_IF;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (if_clauses == child) {
			return builder.append(FULLNAMEPART1);
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	public If_Clauses getIfClauses() {
		return if_clauses;
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (if_clauses != null) {
			if_clauses.setMyScope(scope);
		}
		if (statementblock != null) {
			statementblock.setMyScope(scope);
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		if (if_clauses != null) {
			if_clauses.setMyStatementBlock(statementBlock, index);
		}
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (if_clauses != null) {
			if_clauses.setMyDefinition(definition);
		}
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		if (if_clauses != null) {
			if_clauses.setMyAltguards(altGuards);
		}
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (if_clauses != null) {
			return if_clauses.hasReturn(timestamp, statementblock);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		boolean unreachable = false;
		if (if_clauses != null) {
			unreachable = if_clauses.check(timestamp, unreachable);
		}

		if (statementblock != null) {
			if (unreachable) {
				statementblock.getLocation().reportConfigurableSemanticProblem(
						Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null),
						NEVERREACH);
			}
			statementblock.check(timestamp);
		} else {
			getLocation().reportConfigurableSemanticProblem(reportIfWithoutElse, IFWITHOUTELSE);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		if (if_clauses != null) {
			if_clauses.checkAllowedInterleave();
		}
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	@Override
	public void postCheck() {
		if (if_clauses != null) {
			if_clauses.postCheck();
		}
		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (statementblock != null) {
			return null;
		}

		List<Integer> result = new ArrayList<Integer>();
		result.add(TTCN3Lexer4.ELSE);

		return result;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (if_clauses != null) {
			if_clauses.updateSyntax(reparser, false);
		}

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (if_clauses != null) {
			if_clauses.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (if_clauses != null && !if_clauses.accept(v)) {
			return false;
		}
		if (statementblock != null && !statementblock.accept(v)) {
			return false;
		}
		return true;
	}
}
