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
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Array_Value extends Value {
	private static final String NOINDEX = "There is no value assigned to index {0} in the value `{1}''";

	private final Values values;

	protected Array_Value(final SequenceOf_Value original) {
		copyGeneralProperties(original);
		values = original.getValues();
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.ARRAY_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("{");
		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				if (i > 0) {
					builder.append(", ");
				}
				IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
				builder.append(indexedValue.createStringRepresentation());
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				if (i > 0) {
					builder.append(", ");
				}
				IValue indexedValue = values.getValueByIndex(i);
				builder.append(indexedValue.createStringRepresentation());
			}
		}
		builder.append('}');

		return builder.toString();
	}

	public boolean isIndexed() {
		return values.isIndexed();
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp) || !Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
			return null;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			Value arrayIndex = ((ArraySubReference) subreference).getValue();
			IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, refChain);
			if (valueIndex.isUnfoldable(timestamp)) {
				return null;
			}

			if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
				int index = ((Integer_Value) valueIndex).intValue();

				ArrayDimension dimension = ((Array_Type) type).getDimension();
				dimension.checkIndex(timestamp, valueIndex, Expected_Value_type.EXPECTED_CONSTANT);
				if (dimension.getIsErroneous(timestamp)) {
					return null;
				}

				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						indexedValue = indexedValue.getValueRefdLast(timestamp, refChain);

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).intValue() == index) {
							return values.getIndexedValueByIndex(i).getValue().getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
						}
					}

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index < 0 || index >= values.getNofValues()) {
					//the error was already reported
				} else {
					return values.getValueByIndex(index).getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
				}

				return null;
			}

			arrayIndex.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
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
		if (values.isIndexed()) {
			for (int i = 0, size = values.getNofIndexedValues(); i < size; ++i) {
				if (values.getIndexedValueByIndex(i).isUnfoldable(timestamp, expectedValue, referenceChain)) {
					return true;
				}
			}
		} else {
			for (int i = 0, size = values.getNofValues(); i < size; i++) {
				if (values.getValueByIndex(i).isUnfoldable(timestamp, expectedValue, referenceChain)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getNofComponents() {
		if (values.isIndexed()) {
			return values.getNofIndexedValues();
		}

		return values.getNofValues();
	}

	public IValue getValueByIndex(final int index) {
		if (values.isIndexed()) {
			return values.getIndexedValueByIndex(index).getValue();
		}

		return values.getValueByIndex(index);
	}

	public Value getIndexByIndex(final int index) {
		if (values.isIndexed()) {
			return values.getIndexedValueByIndex(index).getIndex().getValue();
		}

		return null;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (values != null) {
			values.setMyScope(scope);
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (values.isIndexed()) {
				for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
					referenceChain.markState();
					values.getIndexedValueByIndex(i).getValue().checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			} else {
				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					referenceChain.markState();
					values.getValueByIndex(i).checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (!Value_type.ARRAY_VALUE.equals(last.getValuetype())) {
			return false;
		}

		Array_Value otherArray = (Array_Value) last;
		if (values.isIndexed()) {
			if (otherArray.isIndexed()) {
				if (values.getNofIndexedValues() != otherArray.values.getNofIndexedValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
					IndexedValue localTemp = values.getIndexedValueByIndex(i);
					IValue indexValue = localTemp.getIndex().getValue();
					if (Value_type.INTEGER_VALUE.equals(indexValue.getValuetype())) {
						Integer_Value integerValue = (Integer_Value) indexValue;
						IValue otherValue = otherArray.values.getIndexedValueByRealIndex(integerValue.intValue());
						if (otherValue == null || !localTemp.getValue().checkEquality(timestamp, otherValue)) {
							return false;
						}
					} else {
						return false;
					}
				}
			} else {
				if (values.getNofIndexedValues() != otherArray.values.getNofValues()) {
					return false;
				}

				for (int i = 0, size = otherArray.values.getNofValues(); i < size; i++) {
					IValue value = values.getIndexedValueByRealIndex(i);
					if (value == null || !otherArray.values.getValueByIndex(i).checkEquality(timestamp, value)) {
						return false;
					}
				}
			}
		} else {
			if (otherArray.isIndexed()) {
				if (values.getNofValues() != otherArray.values.getNofIndexedValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					IValue otherValue = otherArray.values.getIndexedValueByRealIndex(i);
					if (otherValue == null || !values.getValueByIndex(i).checkEquality(timestamp, otherValue)) {
						return false;
					}
				}
			} else {
				if (values.getNofValues() != otherArray.values.getNofValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					if (!values.getValueByIndex(i).checkEquality(timestamp, otherArray.values.getValueByIndex(i))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (values != null) {
			values.updateSyntax(reparser, false);
		}
	}

	@Override
	public boolean evaluateIsvalue(final boolean fromSequence) {
		if (values == null) {
			return true;
		}

		for (int i = 0, size = values.getNofValues(); i < size; i++) {
			if (!values.getValueByIndex(i).evaluateIsvalue(false)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean evaluateIsbound(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return true;
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp) || !Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
			return false;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			Value arrayIndex = ((ArraySubReference) subreference).getValue();
			IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, referenceChain);
			referenceChain.release();
			if (valueIndex.isUnfoldable(timestamp)) {
				return false;
			}

			if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
				int index = ((Integer_Value) valueIndex).intValue();

				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						indexedValue = indexedValue.getValueRefdLast(timestamp, referenceChain);
						referenceChain.release();

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).intValue() == index) {
							return values.getIndexedValueByIndex(i).getValue().evaluateIsbound(timestamp, reference, actualSubReference + 1);
						}
					}

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index < 0 || index >= values.getNofValues()) {
					//the error was already reported
				} else {
					return values.getValueByIndex(index).evaluateIsbound(timestamp, reference, actualSubReference + 1);
				}

				return false;
			}

			return false;
		case fieldSubReference:
			return false;
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean evaluateIspresent(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return true;
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp) || !Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
			return false;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			Value arrayIndex = ((ArraySubReference) subreference).getValue();
			IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, referenceChain);
			referenceChain.release();
			if (valueIndex.isUnfoldable(timestamp)) {
				return false;
			}

			if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
				int index = ((Integer_Value) valueIndex).intValue();

				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						indexedValue = indexedValue.getValueRefdLast(timestamp, referenceChain);
						referenceChain.release();

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).intValue() == index) {
							return values.getIndexedValueByIndex(i).getValue().evaluateIspresent(timestamp, reference, actualSubReference + 1);
						}
					}

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index < 0 || index >= values.getNofValues()) {
					//the error was already reported
				} else {
					return values.getValueByIndex(index).evaluateIspresent(timestamp, reference, actualSubReference + 1);
				}

				return false;
			}

			return false;
		case fieldSubReference:
			return false;
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (values == null) {
			return;
		}

		values.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (values!=null && !values.accept(v)) {
			return false;
		}
		return true;
	}
}
