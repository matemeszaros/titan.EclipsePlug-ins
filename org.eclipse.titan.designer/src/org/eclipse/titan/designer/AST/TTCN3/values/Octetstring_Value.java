/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.TTCN3.types.OctetString_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Bit2OctExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Hex2OctExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Octetstring_Value extends Value {

	private final String value;

	public Octetstring_Value(final String value) {
		this.value = value;
	}

	Octetstring_Value(final Bitstring_Value original) {
		this.value = Bit2OctExpression.bit2oct(original.getValue());
		copyGeneralProperties(original);
	}

	Octetstring_Value(final Hexstring_Value original) {
		this.value = Hex2OctExpression.hex2oct(original.getValue());
		copyGeneralProperties(original);
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.OCTETSTRING_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append('\'').append(value).append("\'O");

		return builder.toString();
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_OCTETSTRING;
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		return new OctetString_Type();
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final IReferenceChain refChain) {
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

		return value.length() / 2;
	}

	public Octetstring_Value getStringElement(final int index, final Location location) {
		if (value == null) {
			return null;
		}

		if (index < 0) {
			location.reportSemanticError(MessageFormat.format(Bitstring_Value.NEGATIVEINDEX, index));
			return null;
		} else if (index >= value.length() / 2) {
			location.reportSemanticError(MessageFormat.format(Bitstring_Value.INDEXOWERFLOW, index, value.length() / 2));
			return null;
		}

		return new Octetstring_Value(value.substring(2 * index, 2 * index + 2));
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.OCTETSTRING_VALUE.equals(last.getValuetype()) && value.equals(((Octetstring_Value) last).getValue());
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
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}
}
