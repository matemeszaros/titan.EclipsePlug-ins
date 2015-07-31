/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal representation of a universal charstring.
 * 
 * @author Kristof Szabados
 */
public final class UniversalCharstring implements Comparable<UniversalCharstring> {

	private List<UniversalChar> value;

	public UniversalCharstring() {
		value = new ArrayList<UniversalChar>();
	}

	public UniversalCharstring(final UniversalChar character) {
		value = new ArrayList<UniversalChar>(1);
		value.add(character);
	}

	public UniversalCharstring(final String string) {
		value = new ArrayList<UniversalChar>(string.length());
		for (int i = 0; i < string.length(); i++) {
			value.add(new UniversalChar(0, 0, 0, string.charAt(i)));
		}
	}

	public UniversalCharstring(final UniversalCharstring other) {
		value = new ArrayList<UniversalChar>(other.value.size());
		for (int i = 0; i < other.value.size(); i++) {
			value.add(other.value.get(i));
		}
	}

	private UniversalCharstring(final List<UniversalChar> value) {
		this.value = value;
	}

	public int length() {
		return value.size();
	}

	public UniversalChar get(final int index) {
		return value.get(index);
	}

	public String getString() {
		StringBuilder builder = new StringBuilder(value.size());
		for (int i = 0; i < value.size(); i++) {
			builder.append(value.get(i).cell());
		}

		return builder.toString();
	}

	/**
	 * Creates and returns a string representation if the universal charstring.
	 *
	 * @return the string representation of the universal charstring.
	 * */
	public String getStringRepresentation() {
		StringBuilder builder = new StringBuilder(value.size());
		for (int i = 0; i < value.size(); i++) {
			UniversalChar tempChar = value.get(i);
			if (tempChar.group() == 0 && tempChar.plane() == 0 && tempChar.row() == 0) {
				builder.append((char) tempChar.cell());
			} else {
				builder.append("char(").append(tempChar.group()).append(',').append(tempChar.plane()).append(',')
					.append(tempChar.row()).append(',').append(tempChar.cell()).append(')');
			}
		}

		return builder.toString();
	}

	public UniversalCharstring substring(final int beginIndex) {
		return substring(beginIndex, value.size());
	}

	public UniversalCharstring substring(final int beginIndex, final int endIndex) {
		List<UniversalChar> newList = new ArrayList<UniversalChar>(value.subList(beginIndex, endIndex));
		return new UniversalCharstring(newList);
	}

	public UniversalCharstring append(final String other) {
		for (int i = 0; i < other.length(); i++) {
			value.add(new UniversalChar(0, 0, 0, other.charAt(i)));
		}

		return this;
	}

	public UniversalCharstring append(final UniversalCharstring other) {
		for (int i = 0; i < other.value.size(); i++) {
			value.add(other.value.get(i));
		}

		return this;
	}

	public static boolean isCharstring(final String string) {
		if (string == null) {
			return true;
		}

		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) > 127) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Three way lexicographical comparison of universal character strings.
	 *
	 * @param other the string to be compared.
	 * @return the value 0 if the argument string is equal to this string; a
	 *         value less than 0 if this string is lexicographically less than
	 *         the string argument; and a value greater than 0 if this string is
	 *         lexicographically greater than the string argument.
	 */
	public int compareWith(final UniversalCharstring other) {
		if (this == other) {
			return 0;
		}

		UniversalChar actual;
		UniversalChar otherActual;
		for (int i = 0;; i++) {
			if (i == value.size()) {
				if (i == other.value.size()) {
					return 0;
				}

				return -1;
			} else if (i == other.value.size()) {
				return +1;
			}

			actual = value.get(i);
			otherActual = other.value.get(i);
			if (actual.group() > otherActual.group()) {
				return +1;
			} else if (actual.group() < otherActual.group()) {
				return -1;
			} else if (actual.plane() > otherActual.plane()) {
				return +1;
			} else if (actual.plane() < otherActual.plane()) {
				return -1;
			} else if (actual.row() > otherActual.row()) {
				return +1;
			} else if (actual.row() < otherActual.row()) {
				return -1;
			} else if (actual.cell() > otherActual.cell()) {
				return +1;
			} else if (actual.cell() < otherActual.cell()) {
				return -1;
			}
		}
	}

	@Override
	public int compareTo(final UniversalCharstring other) {
		return compareWith(other);
	}

	/**
	 * Checks if this universal character string equals in meaning with the one provided.
	 *
	 * @param other the one to compare against.
	 *
	 * @return true if they mean the same symbol list.
	 * */
	public boolean checkEquality(final UniversalCharstring other) {
		if (this == other) {
			return true;
		}

		if (value.size() != other.value.size()) {
			return false;
		}

		for (int i = 0; i < value.size(); i++) {
			if (!value.get(i).checkEquality(other.value.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if this universal character string equals in meaning with the one provided.
	 *
	 * @param other the one to compare against.
	 *
	 * @return true if they mean the same symbol list.
	 * */
	public boolean checkEquality(final String other) {
		if (value.size() != other.length()) {
			return false;
		}

		for (int i = 0; i < value.size(); i++) {
			if (!value.get(i).checkEquality(other.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the hashcode for the Universal character string.
	 * Useful in case it is stored in a hashmap.
	 *
     * @return  a hash code value for this universal character  string.
     */
	@Override
	public int hashCode() {
		int h = 0;
		UniversalChar temp;
		for (int i = 0, size = value.size(); i < size; i++) {
			temp = value.get(i);
			h = 31 * h + temp.group();
			h = 31 * h + temp.plane();
			h = 31 * h + temp.row();
			h = 31 * h + temp.cell();
		}

		return h;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof UniversalCharstring) {
			return checkEquality((UniversalCharstring) obj);
		}

		return false;
	}

}
