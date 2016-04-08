/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.designer.AST.TTCN3.values.UniversalChar;

/**
 * @author Adam Delic
 * */
public final class UCharLimit extends LimitType {
	public static final UCharLimit MAXIMUM = new UCharLimit(0x7FFFFFFF);
	public static final UCharLimit MINIMUM = new UCharLimit(0);

	private final int value;

	public UCharLimit(final UniversalChar uc) {
		value = uc.toCodePoint();
	}

	public UCharLimit(final int value) {
		this.value = value;
	}

	@Override
	public LimitType decrement() {
		return new UCharLimit(value - 1);
	}

	@Override
	public Type getType() {
		return Type.UCHAR;
	}

	@Override
	public LimitType increment() {
		return new UCharLimit(value + 1);
	}

	@Override
	public boolean isAdjacent(final LimitType other) {
		final UCharLimit ucl = (UCharLimit) other;
		return ((value + 1) == ucl.value);
	}

	@Override
	public void toString(final StringBuilder sb) {
		final UniversalChar uc = new UniversalChar(value);
		sb.append(uc.toString());
	}

	@Override
	public int compareTo(final LimitType o) {
		final UCharLimit ucl = (UCharLimit) o;
		if (value < ucl.value) {
			return -1;
		}
		if (value == ucl.value) {
			return 0;
		}

		return 1;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof UCharLimit)) {
			return false;
		}

		final UCharLimit other = (UCharLimit) obj;

		return value == other.value;
	}

	@Override
	public int hashCode() {
		return value;
	}
}
