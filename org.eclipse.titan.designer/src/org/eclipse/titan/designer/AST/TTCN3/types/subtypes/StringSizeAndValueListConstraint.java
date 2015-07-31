/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * size and value list constraint for bitstring, hexstring and octetstring in
 * the compiler octetstring is a special hexstring where 1 octet = 2 hex.chars
 * not_values is needed because the operation complement/except not_values must
 * always be inside size_constraint set, has_values must be outside of
 * size_constraint set canonical form can be obtained by simplifying value list
 * constraints into size constraints and by converting not_values information
 * into the other two sets if number of not values is >= [number of all values
 * for L] / 2 for length(L) there must be exactly N^L number of values that have
 * length=L, where an element can have N different values where N = 2^BITCNT,
 * BITCNT is the number of bits needed to store one element, works for
 * BITCNT=1,4,8 for octetstrings one octet element is 2 chars long, for others
 * one element is one char, real size of string = elem.size()/ELEMSIZE
 * 
 * @author Adam Delic
 */
public final class StringSizeAndValueListConstraint extends SubtypeConstraint {
	public enum Type {
		BITSTRING(1, 1, 'B'), HEXSTRING(4, 1, 'H'), OCTETSTRING(8, 2, 'O');

		private final int bitcount;
		private final int elemsize;
		private final char suffix;

		Type(final int bitcount, final int elemsize, final char suffix) {
			this.bitcount = bitcount;
			this.elemsize = elemsize;
			this.suffix = suffix;
		}

		public int bitCount() {
			return bitcount;
		}

		public int elemSize() {
			return elemsize;
		}

		public char suffix() {
			return suffix;
		}
	}

	private final Type type;
	private RangeListConstraint sizeConstraint;
	private Set<String> hasValues;
	private Set<String> notValues;

	private void canonicalize(final Set<String> values, final Set<String> otherValues, final boolean ifValues) {
		// length -> number of values
		Map<Integer, Integer> valuesLengths = new HashMap<Integer, Integer>();
		for (String s : values) {
			int valueSize = s.length() / type.elemSize();
			if (valuesLengths.containsKey(valueSize)) {
				valuesLengths.put(valueSize, valuesLengths.get(valueSize) + 1);
			} else {
				valuesLengths.put(valueSize, 1);
			}
		}
		//
		for (Entry<Integer, Integer> entry : valuesLengths.entrySet()) {
			int size = entry.getKey();
			int count = entry.getValue();
			 // regarded as infinity in practice
			int allValuesCount = Integer.MAX_VALUE;
			if (type.bitCount() * size < 32) {
				allValuesCount = (1 << (type.bitCount() * size));
			}
			if (count == allValuesCount) {
				for (Iterator<String> i = values.iterator(); i.hasNext();) {
					if (i.next().length() / type.elemSize() == size) {
						i.remove();
					}
				}
				if (ifValues) {
					sizeConstraint = sizeConstraint.union(new RangeListConstraint(new SizeLimit(size)));
				} else {
					sizeConstraint = (RangeListConstraint) sizeConstraint.except(new RangeListConstraint(new SizeLimit(size)));
				}
			} else if ((!ifValues && (count >= allValuesCount / 2)) || (ifValues && (count > allValuesCount / 2))) {
				for (int actualValue = 0; actualValue < allValuesCount; actualValue++) {
					StringBuilder sb = new StringBuilder();
					for (int elemIndex = 0; elemIndex < size; elemIndex++) {
						int ei = (actualValue >> (elemIndex * type.bitCount())) & ((1 << type.bitCount()) - 1);
						if (type == Type.BITSTRING) {
							sb.append((char) ('0' + ei));
						} else if (type == Type.HEXSTRING) {
							sb.append((ei < 10) ? (char) ('0' + ei) : (char) ('A' + (ei - 10)));
						} else if (type == Type.OCTETSTRING) {
							int c = (char) (ei & 0x0F);
							sb.append((c < 10) ? (char) ('0' + c) : (char) ('A' + (c - 10)));
							c = (ei >> (type.bitCount() / type.elemSize()) & 0x0F);
							sb.append((c < 10) ? (char) ('0' + c) : (char) ('A' + (c - 10)));
						}
					}
					String str = sb.toString();
					if (!values.contains(str)) {
						otherValues.add(str);
					}
				}
				for (Iterator<String> i = values.iterator(); i.hasNext();) {
					if (i.next().length() / type.elemSize() == size) {
						i.remove();
					}
				}
				if (ifValues) {
					sizeConstraint = sizeConstraint.union(new RangeListConstraint(new SizeLimit(size)));
				} else {
					sizeConstraint = (RangeListConstraint) sizeConstraint.except(new RangeListConstraint(new SizeLimit(size)));
				}
			}
		}
	}

	private void canonicalize() {
		canonicalize(hasValues, notValues, true);
		canonicalize(notValues, hasValues, false);
	}

	/** empty set */
	public StringSizeAndValueListConstraint(final Type type) {
		this.type = type;
		sizeConstraint = new RangeListConstraint(LimitType.Type.SIZE);
		hasValues = new TreeSet<String>();
		notValues = new TreeSet<String>();
	}

	/** single value set */
	public StringSizeAndValueListConstraint(final Type type, final String str) {
		this(type);
		hasValues.add(str);
	}

	/** single size set */
	public StringSizeAndValueListConstraint(final Type type, final LimitType sl) {
		this.type = type;
		sizeConstraint = new RangeListConstraint(sl);
		hasValues = new TreeSet<String>();
		notValues = new TreeSet<String>();
	}

	/** size range set */
	public StringSizeAndValueListConstraint(final Type type, final LimitType slBegin, final LimitType slEnd) {
		this.type = type;
		sizeConstraint = new RangeListConstraint(slBegin, slEnd);
		hasValues = new TreeSet<String>();
		notValues = new TreeSet<String>();
	}

	@Override
	public StringSizeAndValueListConstraint complement() {
		StringSizeAndValueListConstraint returnValue = new StringSizeAndValueListConstraint(type);
		returnValue.sizeConstraint = sizeConstraint.complement();
		returnValue.notValues = new TreeSet<String>(hasValues);
		returnValue.hasValues = new TreeSet<String>(notValues);
		returnValue.canonicalize();
		return returnValue;
	}

	private StringSizeAndValueListConstraint setOperation(final SubtypeConstraint other, final boolean isUnion) {
		StringSizeAndValueListConstraint o = (StringSizeAndValueListConstraint) other;
		StringSizeAndValueListConstraint returnValue = new StringSizeAndValueListConstraint(type);
		returnValue.sizeConstraint = sizeConstraint.setOperation(o.sizeConstraint, isUnion);
		if (isUnion) {
			// V1+V2 (union of has_values)
			returnValue.hasValues.addAll(hasValues);
			returnValue.hasValues.addAll(o.hasValues);
			// N1*N2 (intersection of not_values)
			returnValue.notValues.addAll(notValues);
			returnValue.notValues.retainAll(o.notValues);
			// ~S1*N2
			for (String str : o.notValues) {
				if (!sizeConstraint.isElement(new SizeLimit(str.length() / type.elemSize()))) {
					returnValue.notValues.add(str);
				}
			}
			// N1*~S2
			for (String str : notValues) {
				if (!o.sizeConstraint.isElement(new SizeLimit(str.length() / type.elemSize()))) {
					returnValue.notValues.add(str);
				}
			}
		} else { // intersection
				// V1*V2 (intersection of has_values)
			returnValue.hasValues.addAll(hasValues);
			returnValue.hasValues.retainAll(o.hasValues);
			// S2*V1-N2
			for (String str : hasValues) {
				if (o.sizeConstraint.isElement(new SizeLimit(str.length() / type.elemSize())) && !o.notValues.contains(str)) {
					returnValue.hasValues.add(str);
				}
			}
			// S1*V2-N1
			for (String str : o.hasValues) {
				if (sizeConstraint.isElement(new SizeLimit(str.length() / type.elemSize())) && !notValues.contains(str)) {
					returnValue.hasValues.add(str);
				}
			}
			// N1+N2 (union of not_values)
			returnValue.notValues.addAll(notValues);
			returnValue.notValues.addAll(o.notValues);
		}
		// drop ret_val.has_values that are elements of
		// ret_val.not_values too, drop from ret_val.not_values too
		for (Iterator<String> i = returnValue.notValues.iterator(); i.hasNext();) {
			String str = i.next();
			if (returnValue.hasValues.contains(str)) {
				returnValue.hasValues.remove(str);
				i.remove();
			}
		}
		// drop ret_val.has_values elements that are elements of the
		// ret_val.size_constraint set
		for (Iterator<String> i = returnValue.hasValues.iterator(); i.hasNext();) {
			String str = i.next();
			if (returnValue.sizeConstraint.isElement(new SizeLimit(str.length() / type.elemSize()))) {
				i.remove();
			}
		}
		// drop ret_val.not_values elements that are not elements of the
		// ret_val.size_constraint set
		for (Iterator<String> i = returnValue.notValues.iterator(); i.hasNext();) {
			String str = i.next();
			if (!returnValue.sizeConstraint.isElement(new SizeLimit(str.length() / type.elemSize()))) {
				i.remove();
			}
		}
		// make returned value canonical
		returnValue.canonicalize();
		return returnValue;
	}

	@Override
	public StringSizeAndValueListConstraint intersection(final SubtypeConstraint other) {
		return setOperation(other, false);
	}

	@Override
	public boolean isElement(final Object o) {
		String s = (String) o;
		return (sizeConstraint.isElement(new SizeLimit(s.length() / type.elemSize())) && !notValues.contains(s)) || hasValues.contains(s);
	}

	@Override
	public TernaryBool isEmpty() {
		return sizeConstraint.isEmpty().and(TernaryBool.fromBool(hasValues.isEmpty()));
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		StringSizeAndValueListConstraint o = (StringSizeAndValueListConstraint) other;
		return sizeConstraint.isEqual(o.sizeConstraint).and(
				TernaryBool.fromBool(hasValues.equals(o.hasValues)).and(TernaryBool.fromBool(notValues.equals(o.notValues))));
	}

	@Override
	public TernaryBool isFull() {
		return sizeConstraint.isFull().and(TernaryBool.fromBool(notValues.isEmpty()));
	}

	private void valuesToString(final StringBuilder sb, final Set<String> values) {
		sb.append('(');
		boolean needComma = false;
		for (String s : values) {
			if (needComma) {
				sb.append(", ");
			}
			sb.append('\'');
			sb.append(s);
			sb.append('\'');
			sb.append(type.suffix());
			needComma = true;
		}
		sb.append(')');
	}

	@Override
	public void toString(final StringBuilder sb) {
		if (!hasValues.isEmpty()) {
			valuesToString(sb, hasValues);
		}
		if (sizeConstraint.isEmpty() != TernaryBool.TTRUE) {
			if (!hasValues.isEmpty()) {
				sb.append(" union ");
			}
			sb.append("length");
			sizeConstraint.toString(sb);
		}
		if (!notValues.isEmpty()) {
			sb.append(" except ");
			valuesToString(sb, notValues);
		}
	}

	@Override
	public SubtypeConstraint union(final SubtypeConstraint other) {
		return setOperation(other, true);
	}

}
