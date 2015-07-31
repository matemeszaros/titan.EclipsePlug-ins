/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;

/**
 * contains a set of strings, cannot be complemented
 * 
 * @author Adam Delic
 * */
public final class UStringValueConstraint extends SubtypeConstraint {
	private final Set<UniversalCharstring> values;

	/** construct empty set */
	public UStringValueConstraint() {
		values = new TreeSet<UniversalCharstring>();
	}

	/** single value set */
	public UStringValueConstraint(final UniversalCharstring str) {
		values = new TreeSet<UniversalCharstring>();
		values.add(str);
	}

	private UStringValueConstraint(final Set<UniversalCharstring> values) {
		this.values = values;
	}

	@Override
	public SubtypeConstraint complement() {
		return null;
	}

	/** return (first - second) set */
	@Override
	public UStringValueConstraint except(final SubtypeConstraint other) {
		UStringValueConstraint o = (UStringValueConstraint) other;
		Set<UniversalCharstring> returnValue = new TreeSet<UniversalCharstring>();
		for (UniversalCharstring str : values) {
			if (!o.values.contains(str)) {
				returnValue.add(str);
			}
		}
		return new UStringValueConstraint(returnValue);
	}

	/** return if this is a subset of set */
	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return this.except(other).isEmpty();
	}

	public UStringValueConstraint setOperation(final SubtypeConstraint other, final boolean isUnion) {
		UStringValueConstraint o = (UStringValueConstraint) other;
		Set<UniversalCharstring> returnValue = new TreeSet<UniversalCharstring>();
		if (isUnion) {
			returnValue.addAll(values);
			returnValue.addAll(o.values);
		} else {
			returnValue.addAll(values);
			returnValue.retainAll(o.values);
		}
		return new UStringValueConstraint(returnValue);
	}

	@Override
	public UStringValueConstraint intersection(final SubtypeConstraint other) {
		return setOperation(other, false);
	}

	@Override
	public boolean isElement(final Object o) {
		UniversalCharstring str = (UniversalCharstring) o;
		return values.contains(str);
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.fromBool(values.isEmpty());
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		UStringValueConstraint o = (UStringValueConstraint) other;
		return TernaryBool.fromBool(values.equals(o.values));
	}

	@Override
	public TernaryBool isFull() {
		return TernaryBool.TFALSE;
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append('(');
		boolean needComma = false;
		for (UniversalCharstring str : values) {
			if (needComma) {
				sb.append(", ");
			}
			sb.append('\"').append(str.getStringRepresentation()).append('\"');
			needComma = true;
		}
		sb.append(')');
	}

	public UStringValueConstraint remove(final RangeListConstraint range_constraint, final boolean ifElement) {
		switch (range_constraint.getLimitType()) {
		case SIZE: {
			Set<UniversalCharstring> returnValue = new TreeSet<UniversalCharstring>();
			for (UniversalCharstring str : values) {
				if (range_constraint.isElement(new SizeLimit(str.length())) != ifElement) {
					returnValue.add(str);
				}
			}
			return new UStringValueConstraint(returnValue);
		}
		case UCHAR: {
			Set<UniversalCharstring> returnValue = new TreeSet<UniversalCharstring>();
			for (UniversalCharstring str : values) {
				boolean allCharsAreElements = true;
				for (int charIndex = 0; charIndex < str.length(); charIndex++) {
					if (!range_constraint.isElement(new UCharLimit(str.get(charIndex)))) {
						allCharsAreElements = false;
						break;
					}
				}
				if (allCharsAreElements != ifElement) {
					returnValue.add(str);
				}
			}
			return new UStringValueConstraint(returnValue);
		}
		default:
			// illegal range_constraint type, ignore
			return this;
		}
	}

	/** remove/retain all strings that match the supplied pattern */
	public UStringValueConstraint remove(final StringPatternConstraint patternConstraint, final boolean ifElement) {
		return this;
		/*
		 * TODO activate this commented code when
		 * pattern_constraint.isElement() will be implemented
		 * Set<UniversalCharstring> ret_val = new
		 * HashSet<UniversalCharstring>(); for (UniversalCharstring
		 * str:values) { if
		 * (pattern_constraint.isElement(str)!=if_element) {
		 * ret_val.add(str); } } return new
		 * UStringValueConstraint(ret_val);
		 */
	}

	@Override
	public UStringValueConstraint union(final SubtypeConstraint other) {
		return setOperation(other, true);
	}
}
