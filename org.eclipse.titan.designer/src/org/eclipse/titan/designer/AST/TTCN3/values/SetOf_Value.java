/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class SetOf_Value extends Value {
	private static final String NONNEGATIVEINDEXEXPECTED =
			"A non-negative integer value was expected instead of {0} for indexing a value of `set of'' type `{1}''";
	private static final String INDEXOVERFLOW =
			"Index overflow in a value of `set of'' type `{0}'': the index is {1}, but the value has only {2} elements";
	private static final String NOINDEX = "There is no value assigned to index {0} in the value `{1}''";

	private Values values;

	public SetOf_Value(final Values values) {
		this.values = values;

		if (values != null) {
			values.setFullNameParent(this);
		}
	}

	protected SetOf_Value(final SequenceOf_Value original) {
		copyGeneralProperties(original);
		values = original.getValues();
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.SETOF_VALUE;
	}

	public boolean isIndexed() {
		return values.isIndexed();
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder("{");
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
		if (type.getIsErroneous(timestamp)) {
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

					if (index < 0) {
						arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NONNEGATIVEINDEXEXPECTED, index, type.getTypename()));
						return null;
					}

					if (isIndexed()) {
						for (int i = 0; i < values.getNofIndexedValues(); i++) {
							IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
							indexedValue = indexedValue.getValueRefdLast(timestamp, refChain);

							if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
									&& ((Integer_Value) indexedValue).intValue() == index) {
								return values.getIndexedValueByIndex(i).getValue().getReferencedSubValue(
										timestamp, reference, actualSubReference + 1, refChain);
							}
						}

						arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
					} else if (index >= values.getNofValues()) {
						arrayIndex.getLocation().reportSemanticError(
								MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
					} else {
						return values.getValueByIndex(index).getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
					}

					return null;
				}

				arrayIndex.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
				return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE,
							((FieldSubReference) subreference).getId().getDisplayName(), type.getTypename()));
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
			for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
				IndexedValue temp = values.getIndexedValueByIndex(i);
				IValue tempValue = temp.getValue();
				if (tempValue == null || tempValue.isUnfoldable(timestamp, expectedValue, referenceChain)) {
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

		if (!Value_type.SETOF_VALUE.equals(last.getValuetype())) {
			return false;
		}

		SetOf_Value otherSetof = (SetOf_Value) last;

		if (isIndexed()) {
			if (otherSetof.isIndexed()) {
				if (values.getNofIndexedValues() != otherSetof.values.getNofIndexedValues()) {
					return false;
				}

				List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofIndexedValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = values.getNofIndexedValues() - 1; i >= 0; i--) {
					IndexedValue localTemp = values.getIndexedValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						IndexedValue otherTemp =  otherSetof.values.getIndexedValueByIndex(indicesuncovered.get(j));

						if (localTemp.getValue().checkEquality(timestamp, otherTemp.getValue())) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
						return false;
					}
				}
			} else {
				if (values.getNofIndexedValues() != otherSetof.values.getNofValues()) {
					return false;
				}

				List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofIndexedValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = values.getNofIndexedValues() - 1; i >= 0; i--) {
					IndexedValue localTemp = values.getIndexedValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						IValue otherTemp =  otherSetof.values.getValueByIndex(indicesuncovered.get(j));

						if (localTemp.getValue().checkEquality(timestamp, otherTemp)) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
						return false;
					}
				}
			}
		} else {
			if (otherSetof.isIndexed()) {
				if (values.getNofValues() != otherSetof.values.getNofIndexedValues()) {
					return false;
				}

				List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					IValue localTemp = values.getValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						IndexedValue otherTemp =  otherSetof.values.getIndexedValueByIndex(indicesuncovered.get(j));

						if (localTemp.checkEquality(timestamp, otherTemp.getValue())) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
						return false;
					}
				}
			} else {
				if (values.getNofValues() != otherSetof.values.getNofValues()) {
					return false;
				}

				List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					IValue localTemp = values.getValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						IValue otherTemp =  otherSetof.values.getValueByIndex(indicesuncovered.get(j));

						if (localTemp.checkEquality(timestamp, otherTemp)) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
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
		if (type.getIsErroneous(timestamp)) {
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

					if (index < 0) {
						return false;
					}

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
					} else if (index >= values.getNofValues()) {
						arrayIndex.getLocation().reportSemanticError(MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
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
		if (type.getIsErroneous(timestamp)) {
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

					if (index < 0) {
						return false;
					}

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
					} else if (index >= values.getNofValues()) {
						arrayIndex.getLocation().reportSemanticError(MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
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
	protected boolean memberAccept(ASTVisitor v) {
		if (values!=null && !values.accept(v)) {
			return false;
		}
		return true;
	}
}
