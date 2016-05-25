/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
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
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ExecuteDereferedExpression extends Expression_Value {
	private static final String OPERANDERROR = "The guard timer operand of the `execute' operation should be a float value";
	private static final String NEGATIVEDURATION = "The testcase guard timer has negative value: `{0}''";
	private static final String FLOATEXPECTED = "{0} can not be used as the testcase quard timer duration";

	private static final String FULLNAMEPART = ".<reference>";
	private static final String FULLNAMEPART2 = ".<parameters>";
	private static final String OPERATIONNAME = "execute()";

	private final Value value;
	private final ParsedActualParameters actualParameterList;
	private final Value timerValue;

	public ExecuteDereferedExpression(final Value value, final ParsedActualParameters actualParameterList, final Value timerValue) {
		this.value = value;
		this.actualParameterList = actualParameterList;
		this.timerValue = timerValue;

		if (value != null) {
			value.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
		if (timerValue != null) {
			timerValue.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.EXECUTE_REFERENCED_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("execute(derefers(");
		if (value != null) {
			builder.append(value.createStringRepresentation());
		}
		builder.append(")(");
		// TODO implement more precise create_stringRepresentation
		builder.append("...");
		builder.append(')');
		if (timerValue != null) {
			builder.append(", ");
			builder.append(timerValue.createStringRepresentation());
		}
		builder.append(')');

		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
		if (actualParameterList != null) {
			actualParameterList.setMyScope(scope);
		}
		if (timerValue != null) {
			timerValue.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(FULLNAMEPART);
		} else if (actualParameterList == child) {
			return builder.append(FULLNAMEPART2);
		} else if (timerValue == child) {
			return builder.append(OPERAND3);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_VERDICT;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value != null) {
			IType type;
			value.setLoweridToReference(timestamp);
			IValue last = value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_TEMPLATE, referenceChain);
			if (last.getIsErroneous(timestamp)) {
				type = null;
			} else {
				type = last.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
				if (type != null) {
					type = type.getTypeRefdLast(timestamp);
				}
			}

			if (type == null || type.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			} else if (Type_type.TYPE_TESTCASE.equals(type.getTypetype())) {
				FormalParameterList formalParameters = ((Testcase_Type) type).getFormalParameters();
				ActualParameterList tempParameterList = new ActualParameterList();
				boolean isErroneous = formalParameters.checkActualParameterList(timestamp, actualParameterList, tempParameterList);
				if (isErroneous) {
					setIsErroneous(true);
				}
			} else {
				value.getLocation()
						.reportSemanticError(
								MessageFormat.format(
										"Reference to a value of type testcase was expected in the argument of `derefers()'' instead of `{0}''",
										type.getTypename()));
				setIsErroneous(true);
			}
		}

		if (timerValue != null) {
			timerValue.setLoweridToReference(timestamp);
			Type_type tempType = timerValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (tempType) {
			case TYPE_REAL:
				IValue last = timerValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
				if (!last.isUnfoldable(timestamp)) {
					Real_Value real = (Real_Value) last;
					double i = real.getValue();
					if (i < 0.0) {
						timerValue.getLocation().reportSemanticError(
								MessageFormat.format(NEGATIVEDURATION, real.createStringRepresentation()));
					} else if (real.isPositiveInfinity()) {
						timerValue.getLocation().reportSemanticError(
								MessageFormat.format(FLOATEXPECTED, real.createStringRepresentation()));
					}
				}
				return;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				return;
			default:
				if (!isErroneous) {
					timerValue.getLocation().reportSemanticError(OPERANDERROR);
					setIsErroneous(true);
				}
				return;
			}
		}

		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, true, false, false);
	}

	@Override
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (value == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		return lastValue;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
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
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (actualParameterList != null) {
			actualParameterList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (timerValue != null) {
			timerValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (value != null && !value.accept(v)) {
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
