/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Testcase_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Referenced_Testcase_Instance_Statement extends Statement {
	private static final String FLOATEXPECTED = "float value expected";
	private static final String FLOATEXPECTED2 = "{0} can not be used as the testcase quard timer duration";
	private static final String DEFINITIONWITHOUTRUNSONEXPECTED = "A definition that has `runs on' clause cannot execute testcases";
	private static final String NEGATIVEDURATION = "The testcase quard timer has negative duration: `{0}''";

	private static final String FULLNAMEPART1 = ".testcasereference";
	private static final String FULLNAMEPART2 = ".<parameters>";
	private static final String FULLNAMEPART3 = ".timerValue";
	private static final String STATEMENT_NAME = "execute";

	private final Value dereferedValue;
	private final ParsedActualParameters actualParameterList;
	private final Value timerValue;

	public Referenced_Testcase_Instance_Statement(final Value dereferedValue, final ParsedActualParameters actualParameterList,
			final Value timerValue) {
		this.dereferedValue = dereferedValue;
		this.actualParameterList = actualParameterList;
		this.timerValue = timerValue;

		if (dereferedValue != null) {
			dereferedValue.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
		if (timerValue != null) {
			timerValue.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_REFERENCED_TESTCASE_INSTANCE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (dereferedValue == child) {
			return builder.append(FULLNAMEPART1);
		} else if (actualParameterList == child) {
			return builder.append(FULLNAMEPART2);
		} else if (timerValue == child) {
			return builder.append(FULLNAMEPART3);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (dereferedValue != null) {
			dereferedValue.setMyScope(scope);
		}
		if (actualParameterList != null) {
			actualParameterList.setMyScope(scope);
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

		if (dereferedValue == null) {
			return;
		}

		IValue temporalValue = dereferedValue.setLoweridToReference(timestamp);
		IType type = temporalValue.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		if (type != null) {
			type = type.getTypeRefdLast(timestamp);
		}
		if (type == null) {
			return;
		}

		if (!Type_type.TYPE_TESTCASE.equals(type.getTypetype())) {
			dereferedValue.getLocation().reportSemanticError(
					MessageFormat.format(
							"A value of type testcase was expected in the argument of `derefers()'' instead of `{0}''",
							type.getTypename()));
			return;
		}

		if (myStatementBlock.getScopeRunsOn() != null) {
			dereferedValue.getLocation().reportSemanticError(DEFINITIONWITHOUTRUNSONEXPECTED);
			return;
		}

		ActualParameterList tempActualParameters = new ActualParameterList();
		FormalParameterList formalParameterList = ((Testcase_Type) type).getFormalParameters();
		formalParameterList.checkActualParameterList(timestamp, actualParameterList, tempActualParameters);

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

		if (dereferedValue != null) {
			dereferedValue.updateSyntax(reparser, false);
			reparser.updateLocation(dereferedValue.getLocation());
		}

		if (actualParameterList != null) {
			actualParameterList.updateSyntax(reparser, false);
			reparser.updateLocation(actualParameterList.getLocation());
		}

		if (timerValue != null) {
			timerValue.updateSyntax(reparser, false);
			reparser.updateLocation(timerValue.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (dereferedValue != null) {
			dereferedValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (actualParameterList != null) {
			actualParameterList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (timerValue != null) {
			timerValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (dereferedValue != null && !dereferedValue.accept(v)) {
			return false;
		}
		if (actualParameterList != null && !actualParameterList.accept(v)) {
			return false;
		}
		if (timerValue != null && !timerValue.accept(v)) {
			return false;
		}
		return true;
	}
}
