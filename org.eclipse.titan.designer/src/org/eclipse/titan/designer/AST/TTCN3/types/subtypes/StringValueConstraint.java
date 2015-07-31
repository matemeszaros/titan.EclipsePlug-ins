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

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * contains a set of strings, cannot be complemented
 * 
 * @author Adam Delic
 * */
public final class StringValueConstraint extends SubtypeConstraint {
	private final Set<String> values;

	/** construct empty set */
	public StringValueConstraint() {
		values = new TreeSet<String>();
	}

	/** single value set */
	public StringValueConstraint(final String str) {
		values = new TreeSet<String>();
		values.add(str);
	}

	private StringValueConstraint(final Set<String> values) {
		this.values = values;
	}

	@Override
	public SubtypeConstraint complement() {
		ErrorReporter.INTERNAL_ERROR("invalid set operation");
		return null;
	}

	/** return (first - second) set */
	@Override
	public StringValueConstraint except(final SubtypeConstraint other) {
		StringValueConstraint o = (StringValueConstraint) other;
		Set<String> returnValue = new TreeSet<String>();
		for (String str : values) {
			if (!o.values.contains(str)) {
				returnValue.add(str);
			}
		}
		return new StringValueConstraint(returnValue);
	}

	/** return if this is a subset of set */
	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return this.except(other).isEmpty();
	}

	public StringValueConstraint setOperation(final SubtypeConstraint other, final boolean isUnion) {
		StringValueConstraint o = (StringValueConstraint) other;
		Set<String> returnValue = new TreeSet<String>();
		if (isUnion) {
			returnValue.addAll(values);
			returnValue.addAll(o.values);
		} else {
			returnValue.addAll(values);
			returnValue.retainAll(o.values);
		}
		return new StringValueConstraint(returnValue);
	}

	@Override
	public StringValueConstraint intersection(final SubtypeConstraint other) {
		return setOperation(other, false);
	}

	@Override
	public boolean isElement(final Object o) {
		String str = (String) o;
		return values.contains(str);
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.fromBool(values.isEmpty());
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		StringValueConstraint o = (StringValueConstraint) other;
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
		for (String str : values) {
			if (needComma) {
				sb.append(", ");
			}
			// TODO use get_stringRepresentation()
			// when it will be implemented
			sb.append('"');
			sb.append(str);
			sb.append('"');
			needComma = true;
		}
		sb.append(')');
	}

	public StringValueConstraint remove(final RangeListConstraint rangeConstraint, final boolean ifElement) {
		switch (rangeConstraint.getLimitType()) {
		case SIZE: {
			Set<String> returnValue = new TreeSet<String>();
			for (String str : values) {
				if (rangeConstraint.isElement(new SizeLimit(str.length())) != ifElement) {
					returnValue.add(str);
				}
			}
			return new StringValueConstraint(returnValue);
		}
		case CHAR: {
			Set<String> returnValue = new TreeSet<String>();
			for (String str : values) {
				boolean allCharsAreElements = true;
				for (int charIndex = 0; charIndex < str.length(); charIndex++) {
					if (!rangeConstraint.isElement(new CharLimit(str.charAt(charIndex)))) {
						allCharsAreElements = false;
						break;
					}
				}
				if (allCharsAreElements != ifElement) {
					returnValue.add(str);
				}
			}
			return new StringValueConstraint(returnValue);
		}
		default:
			// illegal range_constraint type, ignore
			return this;
		}
	}

	/** remove/retain all strings that match the supplied pattern */
	public StringValueConstraint remove(final StringPatternConstraint patternConstraint, final boolean ifElement) {
		return this;
		/*
		 * TODO activate this commented code when
		 * pattern_constraint.isElement() will be implemented
		 * Set<String> ret_val = new HashSet<String>(); for (String
		 * str:values) { if
		 * (pattern_constraint.isElement(str)!=if_element) {
		 * ret_val.add(str); } } return new
		 * StringValueConstraint(ret_val);
		 */
	}

	@Override
	public StringValueConstraint union(final SubtypeConstraint other) {
		return setOperation(other, true);
	}
}
