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

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
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
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class RotateLeftExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of the `<@' operation should be a string,"
			+ " `record of', `set of' or an array value";
	private static final String SECONDOPERANDERROR = "The second operand of the `<@' operation should be an integer value";
	private static final String EFFECTLESSROTATION = "Rotating will not change the value";
	private static final String NEGATIVEROTATEPROBLEM = "Rotating to the right should be used instead of rotating to the left"
			+ " with a negative value";
	private static final String ZEROROTATEPROBLEM = "Rotating to the left with 0 will not change the original value";
	private static final String TOOBIGROTATEPROBLEM = "Rotating a {0} long value to the left with {1}"
			+ " will have the same effect as rotating by {2}";
	private static final String LARGEINTEGERSECONDOPERANDERROR = "Using a large integer value ({0})"
			+ " as the second operand of the `<@'' operation is not supported";

	private final Value value1;
	private final Value value2;

	public RotateLeftExpression(final Value value1, final Value value2) {
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
		return Operation_type.ROTATELEFT_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append('(').append(value1.createStringRepresentation());
		builder.append(" <@ ");
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

		if (last == null || value1 == null) {
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
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_SET_OF:
		case TYPE_SEQUENCE_OF:
		case TYPE_ARRAY:
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
		long valueSize = 0;
		long rotationSize = 0;
		IValue tempValue;

		if (value1 != null) {
			value1.setLoweridToReference(timestamp);
			tempType1 = value1.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType1) {
			case TYPE_BITSTRING:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.BITSTRING_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((Bitstring_Value) tempValue).getValueLength();
				}
				break;
			case TYPE_HEXSTRING:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.HEXSTRING_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((Hexstring_Value) tempValue).getValueLength();
				}
				break;
			case TYPE_OCTETSTRING:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.OCTETSTRING_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((Octetstring_Value) tempValue).getValueLength();
				}
				break;
			case TYPE_CHARSTRING:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.CHARSTRING_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((Charstring_Value) tempValue).getValueLength();
				}
				break;
			case TYPE_UCHARSTRING:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((UniversalCharstring_Value) tempValue).getValueLength();
				}
				break;
			case TYPE_SET_OF:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.SEQUENCEOF_VALUE.equals(tempValue.getValuetype())) {
					tempValue = tempValue.setValuetype(timestamp, Value_type.SETOF_VALUE);
				}
				if (Value_type.SETOF_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((SetOf_Value) tempValue).getNofComponents();
				}
				break;
			case TYPE_SEQUENCE_OF:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.SEQUENCEOF_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((SequenceOf_Value) tempValue).getNofComponents();
				} else if (Value_type.SETOF_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((SetOf_Value) tempValue).getNofComponents();
				}
				break;
			case TYPE_ARRAY:
				tempValue = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.SEQUENCEOF_VALUE.equals(tempValue.getValuetype())) {
					tempValue = tempValue.setValuetype(timestamp, Value_type.ARRAY_VALUE);
				}
				if (Value_type.ARRAY_VALUE.equals(tempValue.getValuetype())) {
					valueSize = ((Array_Value) tempValue).getNofComponents();
				}
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
			case TYPE_INTEGER:
				tempValue = value2.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (Value_type.INTEGER_VALUE.equals(tempValue.getValuetype()) && !getIsErroneous(timestamp)) {
					if (!((Integer_Value) tempValue).isNative()) {
						value2.getLocation().reportSemanticError(
								MessageFormat.format(LARGEINTEGERSECONDOPERANDERROR, tempValue));
						setIsErroneous(true);
						break;
					}
					rotationSize = ((Integer_Value) tempValue).getValue();
					if (value1 != null && !value1.isUnfoldable(timestamp)) {
						final String severtiy = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTINCORRECTSHIFTROTATESIZE, GeneralConstants.WARNING, null);
						if (valueSize == 0 || valueSize == 1) {
							location.reportConfigurableSemanticProblem(severtiy, EFFECTLESSROTATION);
						} else if (rotationSize < 0) {
							location.reportConfigurableSemanticProblem(severtiy, NEGATIVEROTATEPROBLEM);
						} else if (rotationSize == 0) {
							location.reportConfigurableSemanticProblem(severtiy, ZEROROTATEPROBLEM);
						} else if (rotationSize > valueSize) {
							location.reportConfigurableSemanticProblem(severtiy, MessageFormat.format(
									TOOBIGROTATEPROBLEM, valueSize, rotationSize, rotationSize % valueSize));
						}
					}
				}
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

		String string;
		int shiftSize;

		switch (last1.getValuetype()) {
		case BITSTRING_VALUE:
			string = ((Bitstring_Value) last1).getValue();
			shiftSize = ((Integer_Value) last2).intValue();
			lastValue = new Bitstring_Value(rotateLeft(string, shiftSize));
			lastValue.copyGeneralProperties(this);
			break;
		case HEXSTRING_VALUE:
			string = ((Hexstring_Value) last1).getValue();
			shiftSize = ((Integer_Value) last2).intValue();
			lastValue = new Hexstring_Value(rotateLeft(string, shiftSize));
			lastValue.copyGeneralProperties(this);
			break;
		case OCTETSTRING_VALUE:
			string = ((Octetstring_Value) last1).getValue();
			shiftSize = ((Integer_Value) last2).intValue() * 2;
			lastValue = new Octetstring_Value(rotateLeft(string, shiftSize));
			lastValue.copyGeneralProperties(this);
			break;
		case CHARSTRING_VALUE:
			string = ((Charstring_Value) last1).getValue();
			shiftSize = ((Integer_Value) last2).intValue();
			lastValue = new Charstring_Value(rotateLeft(string, shiftSize));
			lastValue.copyGeneralProperties(this);
			break;
		case UNIVERSALCHARSTRING_VALUE:
			UniversalCharstring string2 = ((UniversalCharstring_Value) last1).getValue();
			shiftSize = ((Integer_Value) last2).intValue();
			lastValue = new UniversalCharstring_Value(rotateLeft(string2, shiftSize));
			lastValue.copyGeneralProperties(this);
			break;
		default:
			setIsErroneous(true);
			break;
		}

		return lastValue;
	}

	/**
	 * Rotates the contents of the string left by the provided amount.
	 * 
	 * @param string
	 *                the string to be rotated
	 * @param rotateSize
	 *                the amount with which the rotation should be done
	 * 
	 * @return the resulting rotated value.
	 * */
	public static String rotateLeft(final String string, final int rotateSize) {
		if (string.length() == 0) {
			return "";
		}

		if (rotateSize < 0) {
			return RotateRightExpression.rotateRight(string, -rotateSize);
		}

		int realAmmount = rotateSize % string.length();
		if (realAmmount == 0) {
			return string;
		}

		return string.substring(realAmmount) + string.substring(0, realAmmount);
	}

	/**
	 * Rotates the contents of the string left by the provided amount.
	 * 
	 * @param string
	 *                the string to be rotated
	 * @param rotateSize
	 *                the amount with which the rotation should be done
	 * 
	 * @return the resulting rotated value.
	 * */
	public static UniversalCharstring rotateLeft(final UniversalCharstring string, final int rotateSize) {
		if (string.length() == 0) {
			return new UniversalCharstring();
		}

		if (rotateSize < 0) {
			return RotateRightExpression.rotateRight(string, -rotateSize);
		}

		int realAmmount = rotateSize % string.length();
		if (realAmmount == 0) {
			return new UniversalCharstring(string);
		}

		return string.substring(realAmmount).append(string.substring(0, realAmmount));
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

	public Value getValue1() {
		return value1;
	}

	public Value getValue2() {
		return value2;
	}
}
