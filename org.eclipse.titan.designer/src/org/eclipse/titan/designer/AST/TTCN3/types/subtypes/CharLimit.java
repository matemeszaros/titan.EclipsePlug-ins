/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * @author Adam Delic
 * */
public final class CharLimit extends LimitType {
	public static final CharLimit MAXIMUM = new CharLimit((char) 127);
	public static final CharLimit MINIMUM = new CharLimit((char) 0);

	private final char value;

	CharLimit(final char v) {
		value = v;
	}

	@Override
	public LimitType decrement() {
		return new CharLimit((char) (value - 1));
	}

	@Override
	public Type getType() {
		return Type.CHAR;
	}

	@Override
	public LimitType increment() {
		return new CharLimit((char) (value + 1));
	}

	@Override
	public boolean isAdjacent(final LimitType other) {
		final CharLimit cl = (CharLimit) other;
		return ((value + 1) == cl.value);
	}

	@Override
	public void toString(final StringBuilder sb) {
		// FIXME: need Charstring.get_stringRepresentation()
		sb.append(value);
	}

	@Override
	public int compareTo(final LimitType o) {
		final CharLimit cl = (CharLimit) o;
		if (value < cl.value) {
			return -1;
		}
		if (value == cl.value) {
			return 0;
		}
		return 1;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof CharLimit)) {
			return false;
		}

		final CharLimit other = (CharLimit) obj;

		return value == other.value;
	}

	@Override
	public int hashCode() {
		return value;
	}
}
