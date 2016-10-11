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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Unknown_Stop_Statement extends Statement {
	private static final String UNEXPECTEDREFERENCE = "Port, timer or component reference was expected"
			+ " as the operand of stop operation instead of `{0}''";

	private static final String FULLNAMEPART = ".reference";
	private static final String STATEMENT_NAME = "stop";

	private final Reference reference;

	private Statement realStatement;
	/** The index of this statement in its parent statement block. */
	private int statementIndex;

	public Unknown_Stop_Statement(final Reference reference) {
		this.reference = reference;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_STOP_UNKNOWN;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		this.statementIndex = index;
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		if (realStatement != null) {
			return realStatement.isTerminating(timestamp);
		}

		return false;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_PORT:
		case A_PAR_PORT:
			if (realStatement == null || !Statement_type.S_STOP_PORT.equals(realStatement.getType())) {
				realStatement = new Stop_Port_Statement(reference);
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);
			break;
		case A_TIMER:
		case A_PAR_TIMER:
			if (realStatement == null || !Statement_type.S_STOP_TIMER.equals(realStatement.getType())) {
				realStatement = new Stop_Timer_Statement(reference);
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);
			break;
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			if (realStatement == null || !Statement_type.S_STOP_COMPONENT.equals(realStatement.getType())) {
				realStatement = new Stop_Component_Statement(new Referenced_Value(reference));
				realStatement.setMyScope(reference.getMyScope());
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(UNEXPECTEDREFERENCE, assignment.getDescription()));
			break;
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (realStatement != null) {
			realStatement.findReferences(referenceFinder, foundIdentifiers);
		} else {
			if (reference != null) {
				reference.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (realStatement != null) {
			return realStatement.accept(v);
		} else {
			if (reference != null && !reference.accept(v)) {
				return false;
			}
			return true;
		}
	}
}
