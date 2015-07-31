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
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Hex2BitExpression extends Expression_Value {
	private static final String OPERANDERROR = "The operand of the `hex2bit' operation should be a hexstring value";

	private final Value value;

	public Hex2BitExpression(final Value value) {
		this.value = value;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.HEX2BIT_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append("hex2bit(").append(value.createStringRepresentation()).append(')');
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
		return Type_type.TYPE_BITSTRING;
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
		case TYPE_HEXSTRING:
			value.getValueRefdLast(timestamp, expectedValue, referenceChain);
			return;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			return;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERANDERROR);
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
			setIsErroneous(true);
			return lastValue;
		}

		switch (last.getValuetype()) {
		case HEXSTRING_VALUE:
			String temp = ((Hexstring_Value) last).getValue();
			lastValue = new Bitstring_Value(hex2bit(temp));
			lastValue.copyGeneralProperties(this);
			break;
		default:
			setIsErroneous(true);
			break;
		}

		return lastValue;
	}

	public static String hex2bit(final String hexString) {
		StringBuilder buider = new StringBuilder(hexString.length() * 4);
		byte[] bytes = hexString.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			switch (bytes[i]) {
			case '0':
				buider.append("0000");
				break;
			case '1':
				buider.append("0001");
				break;
			case '2':
				buider.append("0010");
				break;
			case '3':
				buider.append("0011");
				break;
			case '4':
				buider.append("0100");
				break;
			case '5':
				buider.append("0101");
				break;
			case '6':
				buider.append("0110");
				break;
			case '7':
				buider.append("0111");
				break;
			case '8':
				buider.append("1000");
				break;
			case '9':
				buider.append("1001");
				break;
			case 'a':
			case 'A':
				buider.append("1010");
				break;
			case 'b':
			case 'B':
				buider.append("1011");
				break;
			case 'c':
			case 'C':
				buider.append("1100");
				break;
			case 'd':
			case 'D':
				buider.append("1101");
				break;
			case 'e':
			case 'E':
				buider.append("1110");
				break;
			case 'f':
			case 'F':
				buider.append("1111");
				break;
			default:
				break;
			}
		}

		return buider.toString();
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
