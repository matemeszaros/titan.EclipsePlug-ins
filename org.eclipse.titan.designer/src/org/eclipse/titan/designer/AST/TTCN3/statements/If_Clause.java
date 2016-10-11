/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The If_Clause class represents a single clause (branch) of a TTCN3 if
 * statement.
 * 
 * @see If_Clauses
 * @see If_Statement
 * 
 * @author Kristof Szabados
 * */
public final class If_Clause extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String BOOLEANEXPECTED = "A value or expression of type boolean was expected";
	private static final String UNNECESSARYCONTROL1 = "This control is unnecessary because the conditional expression evaluates to true";
	private static final String UNNECESSARYCONTROL2 = "This control is unnecessary because the conditional expression evaluates to false";
	private static final String NEVERREACH1 = "Control never reaches this code because of previous effective condition(s)";
	private static final String NEVERREACH2 = "Control never reaches this code because the conditional expression evaluates to false";

	private static final String FULLNAMEPART1 = ".expr";
	private static final String FULLNAMEPART2 = ".block";

	/** The conditional expression. */
	private final Value expression;

	/**
	 * the statementblock of the branch.
	 * <p>
	 * This can be null
	 * */
	private final StatementBlock statementblock;

	private Location location = NULL_Location.INSTANCE;

	public If_Clause(final Value expression, final StatementBlock statementblock) {
		this.expression = expression;
		this.statementblock = statementblock;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (expression == child) {
			return builder.append(FULLNAMEPART1);
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (expression != null) {
			expression.setMyScope(scope);
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

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
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

	/**
	 * Checks whether the if clause has a return statement, either directly
	 * or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the if clause.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			return statementblock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	/**
	 * Does the semantic checking of this branch.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param unreachable
	 *                boolean parameter telling if this if statement was
	 *                already found unreachable by previous clauses or not
	 * 
	 * @return true if following clauses are unreachable
	 * */
	public boolean check(final CompilationTimeStamp timestamp, final boolean unreachable) {
		if (unreachable) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null), NEVERREACH1);
		}

		boolean unreachable2 = unreachable;
		if (expression != null) {
			final IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);

			final Type_type temporalType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_UNDEFINED.equals(temporalType)) {
				if (!Type_type.TYPE_BOOL.equals(temporalType)) {
					last.getLocation().reportSemanticError(BOOLEANEXPECTED);
					expression.setIsErroneous(true);
				} else if (!expression.isUnfoldable(timestamp)) {
					if (((Boolean_Value) last).getValue()) {
						expression.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTUNNECESSARYCONTROLS,
										GeneralConstants.WARNING, null), UNNECESSARYCONTROL1);
						unreachable2 = true;
					} else {
						expression.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTUNNECESSARYCONTROLS,
										GeneralConstants.WARNING, null), UNNECESSARYCONTROL2);
						statementblock.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTUNNECESSARYCONTROLS,
										GeneralConstants.WARNING, null), NEVERREACH2);
					}
				}
			}
		}
		if (statementblock != null) {
			statementblock.check(timestamp);
		}

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

		if (expression != null) {
			expression.updateSyntax(reparser, false);
			reparser.updateLocation(expression.getLocation());
		}

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (expression != null) {
			expression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	/**
	 * Checks whether the condition is a negated expression.
	 * 
	 * @return true if it is negated
	 */
	public boolean isNegatedCondition() {
		return expression != null && Value_type.EXPRESSION_VALUE.equals(expression.getValuetype())
				&& Operation_type.NOT_OPERATION.equals(((Expression_Value) expression).getOperationType());
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null && !expression.accept(v)) {
			return false;
		}
		if (statementblock != null && !statementblock.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getExpression() {
		return expression;
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}
}
