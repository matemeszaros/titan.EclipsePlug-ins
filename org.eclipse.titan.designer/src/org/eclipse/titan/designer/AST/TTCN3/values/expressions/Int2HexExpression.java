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
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Int2HexExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "The first operand of the `int2hex' operation should be an integer value";
	private static final String OPERANDERROR2 = "The first operand of the `int2hex' operation should not be negative";
	private static final String OPERANDERROR3 = "The second operand of the `int2hex' operation should be an integer value";
	private static final String OPERANDERROR4 = "The second operand of the `int2hex' operation should not be negative";
	private static final String OPERANDERROR5 = "Value {0} does not fit in length {1}";
	private static final String OPERANDERROR6 = "Using a large integer value ({0}) as the second operand of `int2hex'' operation is not supported";

	private final Value value1;
	private final Value value2;

	public Int2HexExpression(final Value value1, final Value value2) {
		this.value1 = value1;
		this.value2 = value2;

		if (value1 != null) {
			value1.setFullNameParent(this);
		}
		if (value2 != null) {
			value2.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.INT2HEX_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder("int2hex");
		builder.append('(').append(value1.createStringRepresentation());
		builder.append(", ");
		builder.append(value2.createStringRepresentation()).append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value1 != null) {
			value1.setMyScope(scope);
		}
		if (value2 != null) {
			value2.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (value1 == child) {
			return builder.append(OPERAND1);
		} else if (value2 == child) {
			return builder.append(OPERAND2);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_HEXSTRING;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value1 == null || value2 == null) {
			return true;
		}

		return value1.isUnfoldable(timestamp, expectedValue, referenceChain) || value2.isUnfoldable(timestamp, expectedValue, referenceChain);
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
		if (value1 == null || value2 == null) {
			return;
		}

		IValue last1 = null;
		IValue last2 = null;

		value1.setLoweridToReference(timestamp);
		Type_type tempType1 = value1.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType1) {
		case TYPE_INTEGER:
			last1 = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last1.isUnfoldable(timestamp) && Value.Value_type.INTEGER_VALUE.equals(last1.getValuetype())) {
				if (((Integer_Value) last1).signum() < 0) {
					value1.getLocation().reportSemanticError(OPERANDERROR2);
					setIsErroneous(true);
				}
			}
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			}
			break;
		}

		value2.setLoweridToReference(timestamp);
		Type_type tempType2 = value2.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType2) {
		case TYPE_INTEGER:
			last2 = value2.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last2.isUnfoldable(timestamp) && Value.Value_type.INTEGER_VALUE.equals(last2.getValuetype())) {
				if (!((Integer_Value) last2).isNative()) {
					value2.getLocation().reportSemanticError(MessageFormat.format(OPERANDERROR6, last2));
					setIsErroneous(true);
				} else {
					long i2 = ((Integer_Value) last2).getValue();
					if (i2 < 0) {
						value2.getLocation().reportSemanticError(OPERANDERROR4);
						setIsErroneous(true);
					} else if (last1 != null && !last1.isUnfoldable(timestamp)
							&& Value.Value_type.INTEGER_VALUE.equals(last1.getValuetype())) {
						if ((((Integer_Value) last1).shiftRight((int) i2 * 4)).signum() > 0) {
							location.reportSemanticError(MessageFormat.format(OPERANDERROR5,
									((Integer_Value) last1).toString(), i2));
							setIsErroneous(true);
						}
					}
				}
			}
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERANDERROR3);
				setIsErroneous(true);
			}
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

		if (value1 == null || value2 == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		IValue last1 = value1.getValueRefdLast(timestamp, referenceChain);
		IValue last2 = value2.getValueRefdLast(timestamp, referenceChain);
		if (last1.getIsErroneous(timestamp) || last2.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return lastValue;
		}

		Integer_Value i1 = (Integer_Value) last1;
		long i2 = ((Integer_Value) last2).getValue();
		lastValue = new Hexstring_Value(int2hex(i1, (int) i2));

		lastValue.copyGeneralProperties(this);
		return lastValue;
	}

	public static String int2hex(final Integer_Value value, final int length) {
		StringBuilder builder = new StringBuilder(length);

		final Integer_Value zero = new Integer_Value(Long.valueOf(0x0f));
		Integer_Value temp = value;
		for (int i = 1; i <= length; i++) {
			builder.insert(0, (char) BitstringUtilities.DIGITS[(temp.and(zero)).intValue()]);
			temp = temp.shiftRight(4);
		}

		return builder.toString();
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (value1 != null) {
				referenceChain.markState();
				value1.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value2 != null) {
				referenceChain.markState();
				value2.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value1 != null) {
			value1.updateSyntax(reparser, false);
			reparser.updateLocation(value1.getLocation());
		}

		if (value2 != null) {
			value2.updateSyntax(reparser, false);
			reparser.updateLocation(value2.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value1 != null) {
			value1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value2 != null) {
			value2.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value1 != null && !value1.accept(v)) {
			return false;
		}
		if (value2 != null && !value2.accept(v)) {
			return false;
		}
		return true;
	}
}
