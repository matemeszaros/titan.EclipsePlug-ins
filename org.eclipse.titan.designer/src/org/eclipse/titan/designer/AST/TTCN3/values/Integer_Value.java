/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represent an arbitrary precision integer value. Most of the functions are
 * wrappers for BigInteger.
 * 
 * @author Kristof Szabados
 **/
public final class Integer_Value extends Value implements Comparable<Integer_Value> {
	private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);

	private BigInteger value;

	public Integer_Value(final long value) {
		this.value = BigInteger.valueOf(value);
	}

	public Integer_Value(final String value) {
		this.value = new BigInteger(value);
	}

	public Integer_Value(final BigInteger value) {
		this.value = value;
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.INTEGER_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		return value.toString();
	}

	@Override
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		if (Value_type.REAL_VALUE.equals(newType)) {
			return new Real_Value(value.floatValue());
		}

		return super.setValuetype(timestamp, newType);
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(
					FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), type.getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(ParameterisedSubReference.INVALIDVALUESUBREFERENCE);
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return false;
	}

	public long getValue() {
		return value.longValue();
	}

	public int intValue() {
		return value.intValue();
	}

	public long longValue() {
		return value.longValue();
	}

	public float floatValue() {
		return value.floatValue();
	}

	public BigInteger getValueValue() {
		return value;
	}

	public void changeSign() {
		value = value.negate();
	}

	public boolean isNative() {
		return value.compareTo(MAX_INTEGER) <= 0;
	}

	public Integer_Value negate() {
		return new Integer_Value(value.negate());
	}

	public int signum() {
		return value.signum();
	}

	public Integer_Value and(final Integer_Value val) {
		return new Integer_Value(value.and(val.getValueValue()));
	}

	public Integer_Value shiftRight(final int n) {
		return new Integer_Value(value.shiftRight(n));
	}

	public Integer_Value shiftLeft(final int n) {
		return new Integer_Value(value.shiftLeft(n));
	}

	public Integer_Value abs() {
		return new Integer_Value(value.abs());
	}

	public Integer_Value add(final Integer_Value val) {
		return new Integer_Value(value.add(val.getValueValue()));
	}

	public Integer_Value subtract(final Integer_Value val) {
		return new Integer_Value(value.subtract(val.getValueValue()));
	}

	public Integer_Value multiply(final Integer_Value val) {
		return new Integer_Value(value.multiply(val.getValueValue()));
	}

	public Integer_Value divide(final Integer_Value val) {
		return new Integer_Value(value.divide(val.getValueValue()));
	}

	public Integer_Value remainder(final Integer_Value val) {
		return new Integer_Value(value.remainder(val.getValueValue()));
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_INTEGER;
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		return new Integer_Type();
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.INTEGER_VALUE.equals(last.getValuetype()) && value.equals(((Integer_Value) last).getValueValue());
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public boolean equals(final Object x) {
		if (this == x) {
			return true;
		}

		if (!(x instanceof Integer_Value)) {
			return false;
		}

		return value.equals(((Integer_Value) x).getValueValue());
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public int compareTo(final Integer_Value val) {
		return value.compareTo(val.getValueValue());
	}

	@Override
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// no members
		return true;
	}
}
