/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Unknown_Start_Statement extends Statement {
	private static final String UNEXPECEDSTARTREFERENCE = "Port, timer or component reference was expected"
			+ " as the operand of start operation instead of {0}";
	private static final String FUNCTIONARGUMENTEXPECTED = "The argument of start operation is not a function,"
			+ " although it cannot be a start timer or start port operation";
	private static final String MISSINGARGUMENT = "The argument of start operation is missing,"
			+ " although it cannot be a start timer or start port operation";
	private static final String NOARGUMENTEXPECTED = "Start port operation cannot have argument";

	private static final String FULLNAMEPART1 = ".reference";
	private static final String FULLNAMEPART2 = ".value";
	private static final String STATEMENT_NAME = "start";

	private final Reference reference;
	private final IValue value;

	private Statement realStatement;

	/** The index of this statement in its parent statement block. */
	private int statementIndex;

	public Unknown_Start_Statement(final Reference reference, final IValue value) {
		this.reference = reference;
		this.value = value;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_START_UNKNOWN;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (value == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		this.statementIndex = index;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_PORT:
		case A_PAR_PORT:
			if (value != null) {
				value.getLocation().reportSemanticError(NOARGUMENTEXPECTED);
			}
			if (realStatement == null || !Statement_type.S_START_PORT.equals(realStatement.getType())) {
				realStatement = new Start_Port_Statement(reference);
				realStatement.setMyScope(getMyScope());
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);
			break;
		case A_TIMER:
		case A_PAR_TIMER:
			if (realStatement == null || !Statement_type.S_START_TIMER.equals(realStatement.getType())) {
				realStatement = new Start_Timer_Statement(reference, value);
				realStatement.setMyScope(getMyScope());
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
			if (value == null) {
				location.reportSemanticError(MISSINGARGUMENT);
				return;
			}

			if (!Value_type.REFERENCED_VALUE.equals(value.getValuetype())) {
				value.getLocation().reportSemanticError(FUNCTIONARGUMENTEXPECTED);
				return;
			}

			if (realStatement == null || !Statement_type.S_START_COMPONENT.equals(realStatement.getType())) {
				realStatement = new Start_Component_Statement(new Referenced_Value(reference),
						((Referenced_Value) value).getReference());
				realStatement.setMyScope(getMyScope());
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(UNEXPECEDSTARTREFERENCE, assignment.getDescription()));
			break;
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (value != null) {
			return null;
		}

		List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.LPAREN);

		return result;
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

		if (value instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) value).updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		} else if (value != null) {
			throw new ReParseException();
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
			if (value != null) {
				value.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (realStatement != null) {
			return realStatement.accept(v);
		} else {
			if (reference != null && !reference.accept(v)) {
				return false;
			}
			if (value != null && !value.accept(v)) {
				return false;
			}
			return true;
		}
	}
}
