/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumItem;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Enumerated_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class LessThanExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of the `<' operation should be an integer, float or enumerated value";
	private static final String SECONDOPERANDERROR = "The second operand of the `<' operation should be an integer, float or enumerated value";

	private final Value value1;
	private final Value value2;

	public LessThanExpression(final Value value1, final Value value2) {
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
		return Operation_type.LESSTHAN_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append('(').append(value1.createStringRepresentation());
		builder.append(" < ");
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
		final StringBuilder builder = super.getFullName(child);

		if (value1 == child) {
			return builder.append(OPERAND1);
		} else if (value2 == child) {
			return builder.append(OPERAND2);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_BOOL;
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

		ExpressionUtilities.checkExpressionOperatorCompatibility(timestamp, this, referenceChain, expectedValue, value1, value2);

		IValue tempValue1 = value1.setLoweridToReference(timestamp);
		Type_type tempType1 = tempValue1.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType1) {
		case TYPE_INTEGER:
		case TYPE_REAL:
		case TYPE_TTCN3_ENUMERATED:
			tempValue1.getValueRefdLast(timestamp, expectedValue, referenceChain);
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			tempValue1.getLocation().reportSemanticError(FIRSTOPERANDERROR);
			setIsErroneous(true);
			break;
		}

		IValue tempValue2 = value2.setLoweridToReference(timestamp);
		Type_type tempType2 = tempValue2.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType2) {
		case TYPE_INTEGER:
		case TYPE_REAL:
		case TYPE_TTCN3_ENUMERATED:
			tempValue2.getValueRefdLast(timestamp, expectedValue, referenceChain);
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			tempValue2.getLocation().reportSemanticError(SECONDOPERANDERROR);
			setIsErroneous(true);
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

		if (getIsErroneous(timestamp) || isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		IValue last1 = value1.getValueRefdLast(timestamp, referenceChain);
		IValue last2 = value2.getValueRefdLast(timestamp, referenceChain);

		if (last1.getIsErroneous(timestamp) || last2.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return lastValue;
		}

		switch (last1.getValuetype()) {
		case INTEGER_VALUE: {
			lastValue = new Boolean_Value(((Integer_Value) last1).compareTo((Integer_Value) last2) < 0);
			lastValue.copyGeneralProperties(this);
			break;
		}
		case REAL_VALUE: {
			double float1 = ((Real_Value) last1).getValue();
			double float2 = ((Real_Value) last2).getValue();
			lastValue = new Boolean_Value(Double.compare(float1, float2) < 0);
			lastValue.copyGeneralProperties(this);
			break;
		}
		case ENUMERATED_VALUE: {
			IType governor1 = last1.getExpressionGovernor(timestamp, expectedValue);
			governor1 = governor1.getTypeRefdLast(timestamp);
			IType governor2 = last2.getExpressionGovernor(timestamp, expectedValue);
			governor2 = governor2.getTypeRefdLast(timestamp);

			if (governor1 instanceof TTCN3_Enumerated_Type) {
				EnumItem item1 = ((TTCN3_Enumerated_Type) governor1).getEnumItemWithName(((Enumerated_Value) last1).getValue());
				EnumItem item2 = ((TTCN3_Enumerated_Type) governor2).getEnumItemWithName(((Enumerated_Value) last2).getValue());
				lastValue = new Boolean_Value(
						((Integer_Value) item1.getValue()).intValue() < ((Integer_Value) item2.getValue()).intValue());
				lastValue.copyGeneralProperties(this);
			} else {
				EnumItem item1 = ((ASN1_Enumerated_Type) governor1).getEnumItemWithName(((Enumerated_Value) last1).getValue());
				EnumItem item2 = ((ASN1_Enumerated_Type) governor2).getEnumItemWithName(((Enumerated_Value) last2).getValue());
				lastValue = new Boolean_Value(
						((Integer_Value) item1.getValue()).intValue() < ((Integer_Value) item2.getValue()).intValue());
				lastValue.copyGeneralProperties(this);
			}
			break;
		}
		default:
			setIsErroneous(true);
		}

		return lastValue;
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
	protected boolean memberAccept(final ASTVisitor v) {
		if (value1 != null && !value1.accept(v)) {
			return false;
		}
		if (value2 != null && !value2.accept(v)) {
			return false;
		}
		return true;
	}
}
