/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The SelectCase_Statement class represents TTCN3 select statements.
 * 
 * @see SelectCases
 * @see SelectCase
 * 
 * @author Kristof Szabados
 * */
public final class SelectCase_Statement extends Statement {
	private static final String UNDETERMINABLETYPE = "Cannot determine the type of the expression";

	private static final String FULLNAMEPART1 = ".expression";
	private static final String FULLNAMEPART2 = ".selectcases";
	private static final String STATEMENT_NAME = "select-case";

	private final Value expression;
	private final SelectCases selectcases;

	public SelectCase_Statement(final Value expression, final SelectCases selectcases) {
		this.expression = expression;
		this.selectcases = selectcases;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		selectcases.setFullNameParent(this);
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_SELECT;
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
		} else if (selectcases == child) {
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
		selectcases.setMyScope(scope);
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		selectcases.setMyStatementBlock(statementBlock, index);
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		selectcases.setMyDefinition(definition);
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		selectcases.setMyAltguards(altGuards);
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		return selectcases.hasReturn(timestamp);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (expression == null) {
			return;
		}

		IValue temp = expression.setLoweridToReference(timestamp);
		final IType governor = temp.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

		if (governor == null) {
			if (!temp.getIsErroneous(timestamp)) {
				expression.getLocation().reportSemanticError(UNDETERMINABLETYPE);
			}
		} else {
			temp = governor.checkThisValueRef(timestamp, expression);
			governor.checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false,
					true, false, false));
		}

		selectcases.check(timestamp, governor);

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		selectcases.checkAllowedInterleave();
	}

	@Override
	public void postCheck() {
		selectcases.postCheck();
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

		if (selectcases != null) {
			selectcases.updateSyntax(reparser, false);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (expression != null) {
			expression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (selectcases != null) {
			selectcases.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null && !expression.accept(v)) {
			return false;
		}
		if (selectcases != null && !selectcases.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getExpression() {
		return expression;
	}

	public SelectCases getSelectCases() {
		return selectcases;
	}
}
