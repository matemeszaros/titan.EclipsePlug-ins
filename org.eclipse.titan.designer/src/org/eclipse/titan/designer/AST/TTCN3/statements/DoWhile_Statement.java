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
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The DoWhile_Statement class represents a TTCN3 do{}while() statement.
 * 
 * @author Kristof Szabados
 * */
public final class DoWhile_Statement extends Statement {
	private static final String BOOLEANEXPECTED = "A value or expression of type boolean was expected";
	private static final String UNNECESSARYCONTROL = "This control is unnecessary because the conditional expression evaluates to false";
	private static final String INFINITELOOP = "Inifinite loop detected: the program can not escape from this do-while statement";

	private static final String FULLNAMEPART1 = ".expr";
	private static final String FULLNAMEPART2 = ".block";
	private static final String STATEMENT_NAME = "do-while";

	/** The conditional expression. */
	private final Value expression;

	/**
	 * the statementblock of the do-while statement.
	 * <p>
	 * This can be null
	 * */
	private final StatementBlock statementblock;

	private boolean isInfiniteLoop = false;

	public DoWhile_Statement(final Value expression, final StatementBlock statementblock) {
		this.expression = expression;
		this.statementblock = statementblock;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
			statementblock.setOwnerIsLoop();
		}
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_DOWHILE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
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

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return isInfiniteLoop;
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			return statementblock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isInfiniteLoop = false;

		if (statementblock != null) {
			statementblock.check(timestamp);
		}
		if (expression != null) {
			final IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);

			final Type_type temporalType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp)) {
				if (!Type_type.TYPE_BOOL.equals(temporalType)) {
					last.getLocation().reportSemanticError(BOOLEANEXPECTED);
					expression.setIsErroneous(true);
				} else if (!expression.isUnfoldable(timestamp)) {
					if (!((Boolean_Value) last).getValue()) {
						final String severity = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORTUNNECESSARYCONTROLS,
								GeneralConstants.WARNING, null);
						expression.getLocation().reportConfigurableSemanticProblem(severity, UNNECESSARYCONTROL);
					} else if (ReturnStatus_type.RS_NO.equals(hasReturn(timestamp))) {
						isInfiniteLoop = true;
						final String severity = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORTINFINITELOOPS,
								GeneralConstants.WARNING, null);
						location.reportConfigurableSemanticProblem(severity, INFINITELOOP);
					}
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();

		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;

			if (statementblock != null) {
				if (reparser.envelopsDamage(statementblock.getLocation())) {
					statementblock.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(statementblock.getLocation());
				}
			}

			if (expression != null) {
				if (enveloped) {
					expression.updateSyntax(reparser, false);
					reparser.updateLocation(expression.getLocation());
				} else if (reparser.envelopsDamage(expression.getLocation())) {
					expression.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(expression.getLocation());
				}
			}

			if (!enveloped) {
				throw new ReParseException();
			}

			return;
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
}
