/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;

/**
 * @author Adam Delic
 * */
public final class ValueListAndSizeConstraint extends SubtypeConstraint {
	private RangeListConstraint sizeConstraint;
	private ValueListConstraint hasValues, notValues;

	/** empty set */
	public ValueListAndSizeConstraint() {
		sizeConstraint = new RangeListConstraint(LimitType.Type.SIZE);
		hasValues = new ValueListConstraint();
		notValues = new ValueListConstraint();
	}

	/** single value */
	public ValueListAndSizeConstraint(final IValue value) {
		sizeConstraint = new RangeListConstraint(LimitType.Type.SIZE);
		hasValues = new ValueListConstraint(value);
		notValues = new ValueListConstraint();
	}

	/** single size */
	public ValueListAndSizeConstraint(final SizeLimit l) {
		sizeConstraint = new RangeListConstraint(l);
		hasValues = new ValueListConstraint();
		notValues = new ValueListConstraint();
	}

	/** size range */
	public ValueListAndSizeConstraint(final SizeLimit lBegin, final SizeLimit lEnd) {
		sizeConstraint = new RangeListConstraint(lBegin, lEnd);
		hasValues = new ValueListConstraint();
		notValues = new ValueListConstraint();
	}

	@Override
	public ValueListAndSizeConstraint complement() {
		ValueListAndSizeConstraint returnValue = new ValueListAndSizeConstraint();
		returnValue.sizeConstraint = sizeConstraint.complement();
		returnValue.hasValues = notValues;
		returnValue.notValues = hasValues;
		return returnValue;
	}

	public ValueListAndSizeConstraint setOperation(final SubtypeConstraint other, final boolean isUnion) {
		ValueListAndSizeConstraint o = (ValueListAndSizeConstraint) other;
		ValueListAndSizeConstraint returnValue = new ValueListAndSizeConstraint();
		returnValue.sizeConstraint = sizeConstraint.setOperation(o.sizeConstraint, isUnion);
		if (isUnion) {
			// V1+V2
			returnValue.hasValues = hasValues.union(o.hasValues);
			// ~S1*N2
			ValueListConstraint vlc1 = o.notValues.remove(sizeConstraint, true);
			// N1*~S2
			ValueListConstraint vlc2 = notValues.remove(o.sizeConstraint, true);
			// ((~S1*N2)+(N1*~S2)+(N1*N2))
			returnValue.notValues = vlc1.union(vlc2).union(notValues.intersection(o.notValues));
		} else {
			// intersection
			// S2*V1-N2
			ValueListConstraint vlc1 = hasValues.remove(o.sizeConstraint, false).except(o.notValues);
			// S1*V2-N1
			ValueListConstraint vlc2 = o.hasValues.remove(sizeConstraint, false).except(notValues);
			// (S1*V2-N1)+(S2*V1-N2)+(V1*V2)
			returnValue.hasValues = (ValueListConstraint) hasValues.intersection(o.hasValues).union(vlc1).union(vlc2);
			// union of not_values
			returnValue.notValues = notValues.union(o.notValues);
		}
		// drop the intersection, holes and points cancel each other
		ValueListConstraint vlc = (ValueListConstraint) returnValue.hasValues.intersection(returnValue.notValues);
		returnValue.hasValues = returnValue.hasValues.except(vlc);
		returnValue.notValues = returnValue.notValues.except(vlc);
		// drop ret_val.has_values elements that are elements of the
		// ret_val.sizeConstraint set
		returnValue.hasValues = returnValue.hasValues.remove(returnValue.sizeConstraint, true);
		// drop ret_val.not_values elements that are not elements of the
		// ret_val.sizeConstraint set
		returnValue.notValues = returnValue.notValues.remove(returnValue.sizeConstraint, false);
		return returnValue;
	}

	@Override
	public ValueListAndSizeConstraint intersection(final SubtypeConstraint other) {
		return setOperation(other, false);
	}

	@Override
	public boolean isElement(final Object o) {
		IValue v = (IValue) o;
		SizeLimit sl;
		switch (v.getValuetype()) {
		case ARRAY_VALUE:
			sl = new SizeLimit(((Array_Value) v).getNofComponents());
			break;
		case SEQUENCEOF_VALUE:
			sl = new SizeLimit(((SequenceOf_Value) v).getNofComponents());
			break;
		case SETOF_VALUE:
			sl = new SizeLimit(((SetOf_Value) v).getNofComponents());
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
			return true;
		}
		if (sizeConstraint.isElement(sl)) {
			return !notValues.isElement(v);
		}
		return hasValues.isElement(v);
	}

	@Override
	public TernaryBool isEmpty() {
		if ((sizeConstraint.isEmpty() == TernaryBool.TTRUE) && (hasValues.isEmpty() == TernaryBool.TTRUE)) {
			return TernaryBool.TTRUE;
		}
		if (hasValues.isEmpty() == TernaryBool.TFALSE) {
			return TernaryBool.TFALSE;
		}
		if (notValues.isEmpty() == TernaryBool.TTRUE) {
			return TernaryBool.TFALSE;
		}
		// the set of not_values may possibly cancel the size constraint set
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		ValueListAndSizeConstraint o = (ValueListAndSizeConstraint) other;
		if ((sizeConstraint.isEqual(o.sizeConstraint) == TernaryBool.TTRUE) && (hasValues.isEqual(o.hasValues) == TernaryBool.TTRUE)
				&& (notValues.isEqual(o.notValues) == TernaryBool.TTRUE)) {
			return TernaryBool.TTRUE;
		}

		// unknown because there's no canonical form
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public TernaryBool isFull() {
		if ((sizeConstraint.isFull() == TernaryBool.TTRUE) && (notValues.isEmpty() == TernaryBool.TTRUE)) {
			return TernaryBool.TTRUE;
		}
		if (notValues.isEmpty() == TernaryBool.TFALSE) {
			return TernaryBool.TFALSE;
		}
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public void toString(final StringBuilder sb) {
		boolean hv = (hasValues.isEmpty() != TernaryBool.TTRUE);
		if (hv) {
			hasValues.toString(sb);
		}
		if (sizeConstraint.isEmpty() != TernaryBool.TTRUE) {
			if (hv) {
				sb.append(" UNION ");
			}
			sb.append("length");
			sizeConstraint.toString(sb);
		}
		if (notValues.isEmpty() != TernaryBool.TTRUE) {
			sb.append(" EXCEPT ");
			notValues.toString(sb);
		}
	}

	@Override
	public ValueListAndSizeConstraint union(final SubtypeConstraint other) {
		return setOperation(other, true);
	}

}
