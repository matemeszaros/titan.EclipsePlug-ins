/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

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
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Operation_Altguard extends AltGuard {
	private static final String BOOLEANEXPECTED = "A value or expression of type boolean was expected";

	private static final String FULLNAMEPART1 = ".expression";
	private static final String FULLNAMEPART2 = ".statement";
	private static final String FULLNAMEPART3 = ".block";

	private final Value expression;
	private final Statement statement;

	public Operation_Altguard(final Value expression, final Statement statement, final StatementBlock statementblock) {
		super(altguard_type.AG_OP, statementblock);
		this.expression = expression;
		this.statement = statement;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		if (statement != null) {
			statement.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
			statementblock.setOwnerIsAltguard();
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (expression == child) {
			return builder.append(FULLNAMEPART1);
		} else if (statement == child) {
			return builder.append(FULLNAMEPART2);
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART3);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		if (expression != null) {
			expression.setMyScope(scope);
		}
		if (statement != null) {
			statement.setMyScope(scope);
		}
		if (statementblock != null) {
			statementblock.setMyScope(scope);
		}
	}

	public Statement getGuardStatement() {
		return statement;
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		if (statement != null) {
			statement.setMyStatementBlock(statementBlock, index);
		}
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (statement != null) {
			statement.setMyDefinition(definition);
		}
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

		if (expression != null) {
			final IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);

			final Type_type temporalType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_BOOL.equals(temporalType)) {
				last.getLocation().reportSemanticError(BOOLEANEXPECTED);
				expression.setIsErroneous(true);
			}
		}

		if (statement != null) {
			statement.check(timestamp);
		}

		if (statementblock != null) {
			statementblock.check(timestamp);
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
		if (statement != null) {
			statement.postCheck();
		}

		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			if (statementblock != null && reparser.envelopsDamage(statementblock.getLocation())) {
				statementblock.updateSyntax(reparser, true);

				if (expression != null) {
					expression.updateSyntax(reparser, false);
					reparser.updateLocation(expression.getLocation());
				}

				if (statement != null) {
					statement.updateSyntax(reparser, false);
					reparser.updateLocation(statement.getLocation());
				}

				reparser.updateLocation(statementblock.getLocation());

				return;
			}
		}

		if (expression != null) {
			expression.updateSyntax(reparser, false);
			reparser.updateLocation(expression.getLocation());
		}

		if (statement != null) {
			statement.updateSyntax(reparser, false);
			reparser.updateLocation(statement.getLocation());
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
		if (statement != null) {
			statement.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null) {
			if (!expression.accept(v)) {
				return false;
			}
		}
		if (statement != null) {
			if (!statement.accept(v)) {
				return false;
			}
		}
		if (statementblock != null) {
			if (!statementblock.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
