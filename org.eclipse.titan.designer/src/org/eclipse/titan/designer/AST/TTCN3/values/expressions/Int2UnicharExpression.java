/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalChar;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Int2UnicharExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "The operand of the `int2unichar' operation should be an integer value";
	private static final String OPERANDERROR2 = "The operand of the `int2unichar' operation should be in range 0..2147483647";

	private final Value value;

	public Int2UnicharExpression(final Value value) {
		this.value = value;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.INT2STR_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append("int2unichar(").append(value.createStringRepresentation()).append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UCHARSTRING;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value == null) {
			return true;
		}

		return value.isUnfoldable(timestamp, expectedValue, referenceChain);
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
		if (value == null) {
			return;
		}

		value.setLoweridToReference(timestamp);
		Type_type tempType = value.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_INTEGER:
			IValue last = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp) && Value.Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
				Integer_Value i = (Integer_Value) last;
				if (i.signum() < 0 || i.compareTo(new Integer_Value(Integer.MAX_VALUE)) > 0) {
					value.getLocation().reportSemanticError(OPERANDERROR2);
					setIsErroneous(true);
				}
			}
			return;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			return;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			}
			return;
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

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		IValue last = value.getValueRefdLast(timestamp, referenceChain);
		if (last.getIsErroneous(timestamp)) {
			return lastValue;
		}

		switch (last.getValuetype()) {
		case INTEGER_VALUE: {
			long i = ((Integer_Value) last).getValue();

			long group = (i >> 24) & 0xFF;
			long plane = (i >> 16) & 0xFF;
			long row = (i >> 8) & 0xFF;
			long cell = i & 0xFF;

			lastValue = new UniversalCharstring_Value(new UniversalCharstring(new UniversalChar((int) group, (int) plane, (int) row,
					(int) cell)));
			break;
		}
		default:
			return this;
		}

		lastValue.copyGeneralProperties(this);
		return lastValue;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && value != null) {
			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
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
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}
}
