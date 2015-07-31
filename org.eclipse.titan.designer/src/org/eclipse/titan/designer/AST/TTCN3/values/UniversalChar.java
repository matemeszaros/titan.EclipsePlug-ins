/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

/**
 * Represents a single univerchar character.
 * 
 * @author Kristof Szabados
 */
public final class UniversalChar {
	private final int group, plane, row, cell;

	public UniversalChar(final int group, final int plane, final int row, final int cell) {
		this.group = group;
		this.plane = plane;
		this.row = row;
		this.cell = cell;
	}

	public UniversalChar(final int codePoint) {
		cell  = (codePoint & 0xFF);
		row   = ((codePoint >> 8) & 0xFF);
		plane = ((codePoint >> 16) & 0xFF);
		group = ((codePoint >> 24) & 0xFF);
	}

	public int toCodePoint() {
		return (group << 24) + (plane << 16) + (row << 8) + cell;
	}

	public int group() {
		return group;
	}

	public int plane() {
		return plane;
	}

	public int row() {
		return row;
	}

	public int cell() {
		return cell;
	}

	public boolean isChar() {
		return group == 0 && plane == 0 && row == 0;
	}

	public boolean isValidChar() {
		return isChar() && (cell < 128);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isChar() && (cell >= 32) && (cell <= 127)) {
			// do not print control characters
			sb.append('\"');
			sb.append((char) cell);
			sb.append('\"');
		} else {
			sb.append("char(").append(group).append(',').append(plane).append(',').append(row).append(',').append(cell).append(')');
		}
		return sb.toString();
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
	public int compareWith(final UniversalChar other) {
		if (this == other) {
			return 0;
		}

		if (group > other.group()) {
			return +1;
		} else if (group < other.group()) {
			return -1;
		} else if (plane > other.plane()) {
			return +1;
		} else if (plane < other.plane()) {
			return -1;
		} else if (row > other.row()) {
			return +1;
		} else if (row < other.row()) {
			return -1;
		} else if (cell > other.cell()) {
			return +1;
		} else if (cell < other.cell()) {
			return -1;
		}

		return 0;
	}

	/**
	 * Checks if this universal character equals in meaning with the one provided.
	 *
	 * @param other the one to compare against.
	 *
	 * @return true if they mean the same symbol.
	 * */
	public boolean checkEquality(final UniversalChar other) {
		if (this == other) {
			return true;
		}

		return group == other.group && plane == other.plane && row == other.row && cell == other.cell;
	}

	/**
	 * Checks if this universal character equals in meaning with the one provided.
	 *
	 * @param other the one to compare against.
	 *
	 * @return true if they mean the same symbol.
	 * */
	public boolean checkEquality(final char other) {
		return group == 0 && plane == 0 && row == 0 && cell == other;
	}

	/**
	 * Returns the hashcode for the Universal character.
	 * Useful in case it is stored in a hashmap.
	 *
     * @return  a hash code value for this universal character.
     */
	@Override
	public int hashCode() {
		int h = group;
		h = 31 * h + plane;
		h = 31 * h + row;
		h = 31 * h + cell;
		return h;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof UniversalChar) {
			return checkEquality((UniversalChar) obj);
		}

		return false;
	}
}
