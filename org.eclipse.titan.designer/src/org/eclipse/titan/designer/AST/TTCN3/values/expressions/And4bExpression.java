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
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class And4bExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of the `and4b' operation should be a binary string value";
	private static final String SECONDOPERANDERROR = "The second operand of the `and4b' operation should be a binary string value";
	private static final String SAMEOPERANDERROR = "The operands of operation `and4b' should be of the same type";
	private static final String SAMEOPERANDLENGTHERROR = "The operands must have the same length";

	private final Value value1;
	private final Value value2;

	public And4bExpression(final Value value1, final Value value2) {
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
		return Operation_type.AND4B_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append('(').append(value1.createStringRepresentation());
		builder.append(" and4b ");
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

		if (last.getIsErroneous(timestamp) || value1 == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (last.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}

		value1.setLoweridToReference(timestamp);
		Type_type tempType = value1.getExpressionReturntype(timestamp, expectedValue);
		switch (tempType) {
		case TYPE_BITSTRING:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
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
		Type_type tempType1 = null;
		Type_type tempType2 = null;

		if (value1 != null) {
			value1.setLoweridToReference(timestamp);
			tempType1 = value1.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType1) {
			case TYPE_BITSTRING:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
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

			// the operands must be of the same kind
			if (tempType1 != tempType2) {
				location.reportSemanticError(SAMEOPERANDERROR);
				setIsErroneous(true);
				return;
			}

			if (value1.isUnfoldable(timestamp) || value2.isUnfoldable(timestamp)) {
				return;
			}

			// the operands must have the same length
			IValue last1 = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
			IValue last2 = value2.getValueRefdLast(timestamp, expectedValue, referenceChain);
			switch (last1.getValuetype()) {
			case BITSTRING_VALUE:
				if (((Bitstring_Value) last1).getValueLength() != ((Bitstring_Value) last2).getValueLength()) {
					location.reportSemanticError(SAMEOPERANDLENGTHERROR);
					setIsErroneous(true);
				}
				break;
			case HEXSTRING_VALUE:
				if (((Hexstring_Value) last1).getValueLength() != ((Hexstring_Value) last2).getValueLength()) {
					location.reportSemanticError(SAMEOPERANDLENGTHERROR);
					setIsErroneous(true);
				}
				break;
			case OCTETSTRING_VALUE:
				if (((Octetstring_Value) last1).getValueLength() != ((Octetstring_Value) last2).getValueLength()) {
					location.reportSemanticError(SAMEOPERANDLENGTHERROR);
					setIsErroneous(true);
				}
				break;
			default:
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
			setIsErroneous(true);
			return lastValue;
		}

		IValue last1 = value1.getValueRefdLast(timestamp, referenceChain);
		IValue last2 = value2.getValueRefdLast(timestamp, referenceChain);

		String str1;
		String str2;

		switch (last1.getValuetype()) {
		case BITSTRING_VALUE:
			str1 = ((Bitstring_Value) last1).getValue();
			str2 = ((Bitstring_Value) last2).getValue();
			lastValue = new Bitstring_Value(and4b(str1, str2));
			lastValue.copyGeneralProperties(this);
			break;
		case HEXSTRING_VALUE:
			str1 = ((Hexstring_Value) last1).getValue();
			str2 = ((Hexstring_Value) last2).getValue();
			lastValue = new Hexstring_Value(and4b(str1, str2));
			lastValue.copyGeneralProperties(this);
			break;
		case OCTETSTRING_VALUE:
			str1 = ((Octetstring_Value) last1).getValue();
			str2 = ((Octetstring_Value) last2).getValue();
			lastValue = new Octetstring_Value(and4b(str1, str2));
			lastValue.copyGeneralProperties(this);
			break;
		default:
			setIsErroneous(true);
			break;
		}

		return lastValue;
	}

	/**
	 * Calculates the binary and -ing of two strings.
	 * 
	 * @param str1
	 *                the first original string.
	 * @param str2
	 *                the second original string.
	 * 
	 * @return the and -ed value.
	 * */
	public static String and4b(final String str1, final String str2) {
		byte[] b1 = str1.getBytes();
		byte[] b2 = str2.getBytes();
		byte[] result = new byte[b1.length];

		for (int i = 0; i < b1.length; i++) {
			result[i] = BitstringUtilities.hexdigitToChar(BitstringUtilities.charToHexdigit(b1[i])
					& BitstringUtilities.charToHexdigit(b2[i]));
		}

		return new String(result);
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
