/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Testcase_Instance_Statement extends Statement {
	private static final String FLOATEXPECTED = "float value expected";
	private static final String FLOATEXPECTED2 = "{0} can not be used as the testcase quard timer duration";
	private static final String DEFINITIONWITHOUTRUNSONEXPECTED = "A definition that has `runs on' clause cannot execute testcases";
	private static final String NEGATIVEDURATION = "The testcase quard timer has negative duration: `{0}''";
	private static final String TESTCASEEXPECTED = "Reference to a testcase was expected in the argument instead of {0}";

	private static final String FULLNAMEPART1 = ".testcasereference";
	private static final String FULLNAMEPART2 = ".timerValue";
	private static final String STATEMENT_NAME = "execute";

	private final Reference testcaseReference;
	private final Value timerValue;

	public Testcase_Instance_Statement(final Reference testcaseReference, final Value timerValue) {
		this.testcaseReference = testcaseReference;
		this.timerValue = timerValue;

		if (testcaseReference != null) {
			testcaseReference.setFullNameParent(this);
		}
		if (timerValue != null) {
			timerValue.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_TESTCASE_INSTANCE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (testcaseReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (timerValue == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (testcaseReference != null) {
			testcaseReference.setMyScope(scope);
		}
		if (timerValue != null) {
			timerValue.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (testcaseReference == null) {
			return;
		}

		Assignment assignment = testcaseReference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		if (!Assignment_type.A_TESTCASE.equals(assignment.getAssignmentType())) {
			testcaseReference.getLocation().reportSemanticError(MessageFormat.format(TESTCASEEXPECTED, assignment.getFullName()));
			return;
		}

		if (myStatementBlock.getScopeRunsOn() != null) {
			testcaseReference.getLocation().reportSemanticError(DEFINITIONWITHOUTRUNSONEXPECTED);
			return;
		}

		if (timerValue != null) {
			timerValue.setLoweridToReference(timestamp);
			Type_type temporalType = timerValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (temporalType) {
			case TYPE_REAL:
				IValue last = timerValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				if (!last.isUnfoldable(timestamp)) {
					Real_Value real = (Real_Value) last;
					double i = real.getValue();
					if (i < 0.0) {
						timerValue.getLocation().reportSemanticError(
								MessageFormat.format(NEGATIVEDURATION, real.createStringRepresentation()));
					} else if (real.isPositiveInfinity()) {
						timerValue.getLocation().reportSemanticError(
								MessageFormat.format(FLOATEXPECTED2, real.createStringRepresentation()));
					}
				}
				break;
			default:
				timerValue.getLocation().reportSemanticError(FLOATEXPECTED);
				break;
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (testcaseReference != null) {
			testcaseReference.updateSyntax(reparser, false);
			reparser.updateLocation(testcaseReference.getLocation());
		}

		if (timerValue != null) {
			timerValue.updateSyntax(reparser, false);
			reparser.updateLocation(timerValue.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (testcaseReference != null) {
			testcaseReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (timerValue != null) {
			timerValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (testcaseReference != null && !testcaseReference.accept(v)) {
			return false;
		}
		if (timerValue != null && !timerValue.accept(v)) {
			return false;
		}
		return true;
	}
}
