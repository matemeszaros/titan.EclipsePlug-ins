/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Adam Delic
 * */
public final class ValueListConstraint extends SubtypeConstraint {
	private final List<IValue> values;

	/** empty set */
	public ValueListConstraint() {
		values = new ArrayList<IValue>();
	}

	/** single value */
	public ValueListConstraint(final IValue value) {
		values = new ArrayList<IValue>();
		values.add(value);
	}

	private ValueListConstraint(final List<IValue> values) {
		this.values = values;
	}

	@Override
	public ValueListConstraint complement() {
		// invalid operation for this set type
		ErrorReporter.INTERNAL_ERROR("invalid set operation");
		return null;
	}

	@Override
	public ValueListConstraint except(final SubtypeConstraint other) {
		ValueListConstraint o = (ValueListConstraint) other;
		List<IValue> returnValue = new ArrayList<IValue>();
		for (IValue v : values) {
			if (!o.isElement(v)) {
				returnValue.add(v);
			}
		}
		return new ValueListConstraint(returnValue);
	}

	@Override
	public SubtypeConstraint intersection(final SubtypeConstraint other) {
		ValueListConstraint o = (ValueListConstraint) other;
		List<IValue> returnValue = new ArrayList<IValue>();
		for (IValue v : values) {
			if (o.isElement(v)) {
				returnValue.add(v);
			}
		}
		return new ValueListConstraint(returnValue);
	}

	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return except(other).isEmpty();
	}

	@Override
	public boolean isElement(final Object o) {
		Value val = (Value) o;
		for (IValue v : values) {
			if (v.checkEquality(CompilationTimeStamp.getBaseTimestamp(), val)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.fromBool(values.isEmpty());
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		ValueListConstraint o = (ValueListConstraint) other;
		if (values.size() != o.values.size()) {
			return TernaryBool.TFALSE;
		}

		boolean[] found = new boolean[values.size()];
		for (int i = 0; i < found.length; i++) {
			found[i] = false;
		}
		for (int i = 0, size = values.size(); i < size; i++) {
			IValue tempValueI = values.get(i);
			boolean foundI = false;
			for (int j = 0; j < size; j++) {
				if (found[j]) {
					continue;
				}

				IValue tempValueJ = o.values.get(j);
				if (tempValueI.checkEquality(tempValueJ.getLastTimeChecked(), tempValueJ)) {
					found[j] = true;
					foundI = true;
					break;
				}
			}
			if (!foundI) {
				return TernaryBool.TFALSE;
			}
		}
		return TernaryBool.TTRUE;
	}

	@Override
	public TernaryBool isFull() {
		// it's unknown how many possible values we have
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append('(');
		boolean needComma = false;
		for (IValue v : values) {
			if (needComma) {
				sb.append(", ");
			}
			sb.append(v.createStringRepresentation());
			needComma = true;
		}
		sb.append(')');
	}

	@Override
	public ValueListConstraint union(final SubtypeConstraint other) {
		ValueListConstraint o = (ValueListConstraint) other;
		ArrayList<IValue> returnValue = new ArrayList<IValue>();
		returnValue.addAll(values);
		for (IValue v : o.values) {
			if (!isElement(v)) {
				returnValue.add(v);
			}
		}
		return new ValueListConstraint(returnValue);
	}

	/**
	 * remove all elements whose size/length is inside/outside of
	 * size_constraint
	 */
	public ValueListConstraint remove(final RangeListConstraint size_constraint, final boolean if_element) {
		ArrayList<IValue> returnValue = new ArrayList<IValue>();
		for (IValue v : values) {
			switch (v.getValuetype()) {
			case ARRAY_VALUE:
				if (size_constraint.isElement(new SizeLimit(((Array_Value) v).getNofComponents())) != if_element) {
					returnValue.add(v);
				}
				break;
			case SEQUENCEOF_VALUE:
				if (size_constraint.isElement(new SizeLimit(((SequenceOf_Value) v).getNofComponents())) != if_element) {
					returnValue.add(v);
				}
				break;
			case SETOF_VALUE:
				if (size_constraint.isElement(new SizeLimit(((SetOf_Value) v).getNofComponents())) != if_element) {
					returnValue.add(v);
				}
				break;
			default:
				ErrorReporter.INTERNAL_ERROR();
			}
		}
		return new ValueListConstraint(returnValue);
	}
}
