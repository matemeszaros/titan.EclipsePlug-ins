/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class StringConcatenationExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of the `&' operation should be a string, `record of', or a `set of' value";
	private static final String SECONDOPERANDERROR = "The second operand of the `&' operation should be a string, `record of', or a `set of' value";
	private static final String SAMEOPERANDERROR = "The operands of operation `&' should be of the same type";

	private final Value value1;
	private final Value value2;

	public StringConcatenationExpression(final Value value1, final Value value2) {
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
		return Operation_type.CONCATENATION_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append('(').append(value1.createStringRepresentation());
		builder.append(" & ");
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
		IValue last = getValueRefdLast(timestamp, expectedValue, null);

		if (last == null || value1 == null || value2 == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (last.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}

		Type_type tempType;
		if (last == this) {
			// not good, but in case of recursive call this is the
			// only way out.
			value1.setLoweridToReference(timestamp);
			tempType = value1.getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UCHARSTRING.equals(tempType)) {
				value2.setLoweridToReference(timestamp);
				tempType = value2.getExpressionReturntype(timestamp, expectedValue);
			}
		} else {
			last.setLoweridToReference(timestamp);
			tempType = last.getExpressionReturntype(timestamp, expectedValue);
		}

		switch (tempType) {
		case TYPE_BITSTRING:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_SET_OF:
		case TYPE_SEQUENCE_OF:
			return tempType;
		case TYPE_UNDEFINED:
			return tempType;
		default:
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value1 == null || value2 == null || getIsErroneous(timestamp)) {
			return true;
		}

		if (value1.isUnfoldable(timestamp, expectedValue, referenceChain) || value2.isUnfoldable(timestamp, expectedValue, referenceChain)) {
			return true;
		}

		value1.setLoweridToReference(timestamp);
		Type_type tempType = value1.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_BITSTRING:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
			return false;
		default:
			return true;
		}
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
		Type_type tempType1 = null;
		Type_type tempType2 = null;

		if (value1 != null) {
			value1.setLoweridToReference(timestamp);
			tempType1 = value1.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType1) {
			case TYPE_BITSTRING:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(FIRSTOPERANDERROR);
				setIsErroneous(true);
				break;
			}
		}

		if (value2 != null) {
			value2.setLoweridToReference(timestamp);
			tempType2 = value2.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType2) {
			case TYPE_BITSTRING:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				value2.getValueRefdLast(timestamp, expectedValue, referenceChain);
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(SECONDOPERANDERROR);
				setIsErroneous(true);
				break;
			}
		}

		if (value1 != null && value2 != null && !getIsErroneous(timestamp)) {
			if (value1.getIsErroneous(timestamp) || value2.getIsErroneous(timestamp)) {
				setIsErroneous(true);
				return;
			}

			if (!((Type_type.TYPE_CHARSTRING.equals(tempType1) && Type_type.TYPE_UCHARSTRING.equals(tempType2)) || (Type_type.TYPE_CHARSTRING
					.equals(tempType2) && Type_type.TYPE_UCHARSTRING.equals(tempType1))) && tempType1 != tempType2) {
				location.reportSemanticError(SAMEOPERANDERROR);
				setIsErroneous(true);
			}
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
			return lastValue;
		}

		switch (last1.getValuetype()) {
		case BITSTRING_VALUE: {
			String string1 = ((Bitstring_Value) last1).getValue();
			String string2 = ((Bitstring_Value) last2).getValue();
			lastValue = new Bitstring_Value(string1 + string2);
			lastValue.copyGeneralProperties(this);
			break;
		}
		case HEXSTRING_VALUE: {
			String string1 = ((Hexstring_Value) last1).getValue();
			String string2 = ((Hexstring_Value) last2).getValue();
			lastValue = new Hexstring_Value(string1 + string2);
			lastValue.copyGeneralProperties(this);
			break;
		}
		case OCTETSTRING_VALUE: {
			String string1 = ((Octetstring_Value) last1).getValue();
			String string2 = ((Octetstring_Value) last2).getValue();
			lastValue = new Octetstring_Value(string1 + string2);
			lastValue.copyGeneralProperties(this);
			break;
		}
		case CHARSTRING_VALUE: {
			String string1 = ((Charstring_Value) last1).getValue();
			if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(last2.getValuetype())) {
				UniversalCharstring string2 = ((UniversalCharstring_Value) last2).getValue();
				lastValue = new UniversalCharstring_Value(new UniversalCharstring(string1).append(string2));
			} else {
				String string2 = ((Charstring_Value) last2).getValue();
				lastValue = new Charstring_Value(string1 + string2);
			}
			lastValue.copyGeneralProperties(this);
			break;
		}
		case UNIVERSALCHARSTRING_VALUE: {
			UniversalCharstring string1 = ((UniversalCharstring_Value) last1).getValue();
			if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(last2.getValuetype())) {
				UniversalCharstring string2 = ((UniversalCharstring_Value) last2).getValue();
				lastValue = new UniversalCharstring_Value(new UniversalCharstring(string1).append(string2));
			} else {
				String string2 = ((Charstring_Value) last2).getValue();
				lastValue = new UniversalCharstring_Value(new UniversalCharstring(string1).append(string2));
			}
			lastValue.copyGeneralProperties(this);
			break;
		}
		default:
			setIsErroneous(true);
			break;
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
