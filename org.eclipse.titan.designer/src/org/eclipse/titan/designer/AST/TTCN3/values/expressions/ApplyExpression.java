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
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ApplyExpression extends Expression_Value {
	private static final String NORETURNTYPE = "The type `{0}' has no return type";
	private static final String VALUEXPECTED1 = "A value of type function was expected";
	private static final String VALUEXPECTED2 = "Reference to a value was expected, but functions of type `{0}'' return a template of type `{1}''";
	private static final String VALUEXPECTED3 = "A value of type function was expected in the argument of `{0}''";
	private static final String EVALUATABLEEXPECTED = "An evaluatable value was expected instead of operation `apply()''";
	private static final String STATICEXPECTED = "A static value was expected instead of operation `apply()''";

	private static final String FULLNAMEPART1 = ".<parameters>";

	private final Value value;
	private final ParsedActualParameters actualParameterList;

	public ApplyExpression(final Value value, final ParsedActualParameters actualParameterList) {
		this.value = value;
		this.actualParameterList = actualParameterList;

		if (value != null) {
			value.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.APPLY_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append(value.createStringRepresentation());
		builder.append(".apply(");
		// TODO implement more precise create_stringRepresentation
		builder.append("...");
		builder.append(')');

		return builder.toString();
	}

	public Value getValue() {
		return value;
	}

	public ParsedActualParameters getParameters() {
		return actualParameterList;
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
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(OPERAND);
		} else if (actualParameterList == child) {
			return builder.append(FULLNAMEPART1);
		}

		return builder;
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		if (value == null) {
			return null;
		}

		IType type = value.getExpressionGovernor(timestamp, expectedValue);
		if (type == null) {
			if (!value.getIsErroneous(timestamp)) {
				value.getLocation().reportSemanticError(VALUEXPECTED1);
			}
			setIsErroneous(true);
			return null;
		}

		type = type.getTypeRefdLast(timestamp);
		switch (type.getTypetype()) {
		case TYPE_FUNCTION:
			Type result = ((Function_Type) type).getReturnType();
			if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) && ((Function_Type) type).returnsTemplate()) {
				location.reportSemanticError(MessageFormat.format(VALUEXPECTED2, type.getTypename(), result.getTypename()));
			}
			return result;
		default:
			setIsErroneous(true);
			return null;
		}
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (value == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (value.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}

		IType type = value.getExpressionGovernor(timestamp, expectedValue);
		if (type == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		type = type.getTypeRefdLast(timestamp);
		switch (type.getTypetype()) {
		case TYPE_FUNCTION:
			IType returnType = ((Function_Type) type).getReturnType();
			if (returnType == null) {
				value.getLocation().reportSemanticError(MessageFormat.format(NORETURNTYPE, type.getTypename()));
				setIsErroneous(true);
				return Type_type.TYPE_UNDEFINED;
			}

			return returnType.getTypeRefdLast(timestamp).getTypetype();
		case TYPE_TESTCASE:
			return Type_type.TYPE_VERDICT;
		default:
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}
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
		IType type = null;

		if (value != null) {
			value.setLoweridToReference(timestamp);
			type = value.getExpressionGovernor(timestamp, expectedValue);
		}

		if (type == null || type.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return;
		}

		type = type.getTypeRefdLast(timestamp);
		if (!Type_type.TYPE_FUNCTION.equals(type.getTypetype())) {
			value.getLocation().reportSemanticError(MessageFormat.format(VALUEXPECTED3, type.getTypename()));
			setIsErroneous(true);
			return;
		}

		if (myScope != null) {
			myScope.checkRunsOnScope(timestamp, type, this, "call");
		}

		ActualParameterList tempActualParameters = new ActualParameterList();
		FormalParameterList formalParameterList = ((Function_Type) type).getFormalParameters();
		if (!formalParameterList.checkActualParameterList(timestamp, actualParameterList, tempActualParameters)) {
			tempActualParameters.setFullNameParent(this);
			tempActualParameters.setMyScope(getMyScope());
		}

		switch (expectedValue) {
		case EXPECTED_CONSTANT:
			getLocation().reportSemanticError(EVALUATABLEEXPECTED);
			setIsErroneous(true);
			break;
		case EXPECTED_STATIC_VALUE:
			getLocation().reportSemanticError(STATICEXPECTED);
			setIsErroneous(true);
			break;
		default:
			break;
		}
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
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (actualParameterList != null) {
			actualParameterList.findReferences(referenceFinder, foundIdentifiers);
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
		return true;
	}
}
