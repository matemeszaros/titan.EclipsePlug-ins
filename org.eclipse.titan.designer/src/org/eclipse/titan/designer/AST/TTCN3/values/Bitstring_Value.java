/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Hex2BitExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Bitstring_Value extends Value {
	public static final String NEGATIVEINDEX = "A non-negative integer value was expected instead of {0} for indexing a string element";
	public static final String INDEXOWERFLOW =
			"Index overflow when accessing a string element: the index is {0}, but the string has only {1} elements";

	private final String value;

	public Bitstring_Value(final String value) {
		this.value = value;
	}

	Bitstring_Value(final Hexstring_Value original) {
		copyGeneralProperties(original);
		this.value = Hex2BitExpression.hex2bit(original.getValue());
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.BITSTRING_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append('\'').append(value).append("\'B");

		return builder.toString();
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_BITSTRING;
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		return new BitString_Type();
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
			Value arrayIndex = ((ArraySubReference) subreference).getValue();
			IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, refChain);
			if (!valueIndex.isUnfoldable(timestamp)) {
				if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
					int index = ((Integer_Value) valueIndex).intValue();
					return getStringElement(index, arrayIndex.getLocation());
				}

				arrayIndex.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
				return null;
			}
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

	public String getValue() {
		return value;
	}

	public int getValueLength() {
		if (value == null || isErroneous) {
			return 0;
		}

		return value.length();
	}

	public IValue getStringElement(final int index, final Location location) {
		if (value == null) {
			return null;
		}

		if (index < 0) {
			location.reportSemanticError(MessageFormat.format(NEGATIVEINDEX, index));
			return null;
		} else if (index >= value.length()) {
			location.reportSemanticError(MessageFormat.format(INDEXOWERFLOW, index, value.length()));
			return null;
		}

		return new Bitstring_Value(value.substring(index, index + 1));
	}

	@Override
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		if (Value_type.OCTETSTRING_VALUE.equals(newType)) {
			return new Octetstring_Value(this);
		}

		return super.setValuetype(timestamp, newType);
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.BITSTRING_VALUE.equals(last.getValuetype()) && value.equals(((Bitstring_Value) last).getValue());
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
