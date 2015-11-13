/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.math.BigInteger;

/**
 * @author Adam Delic
 * */
public final class SizeLimit extends LimitType {
	public static final SizeLimit MAXIMUM = new SizeLimit(true);
	public static final SizeLimit MINIMUM = new SizeLimit(BigInteger.ZERO);

	private final boolean infinity;
	private final BigInteger size;

	public SizeLimit(final BigInteger size) {
		infinity = false;
		this.size = size;
	}

	public SizeLimit(final long size) {
		this(BigInteger.valueOf(size));
	}

	public SizeLimit(final boolean infinity) {
		this.infinity = infinity;
		size = BigInteger.ZERO;
	}

	@Override
	public Type getType() {
		return Type.SIZE;
	}

	public BigInteger getSize() {
		return size;
	}

	public boolean getInfinity() {
		return infinity;
	}

	@Override
	public LimitType decrement() {
		if (infinity) {
			return new SizeLimit(true);
		}

		return new SizeLimit(size.subtract(BigInteger.ONE));
	}

	@Override
	public LimitType increment() {
		if (infinity) {
			return new SizeLimit(true);
		}

		return new SizeLimit(size.add(BigInteger.ONE));
	}

	@Override
	public boolean isAdjacent(final LimitType other) {
		final SizeLimit sl = (SizeLimit) other;
		if (infinity || sl.infinity) {
			return false;
		}

		return (size.add(BigInteger.ONE).compareTo(sl.size) == 0);
	}

	@Override
	public int compareTo(final LimitType o) {
		final SizeLimit sl = (SizeLimit) o;
		if (infinity) {
			if (sl.infinity) {
				return 0;
			}

			return 1;
		}

		if (sl.infinity) {
			return -1;
		}

		return size.compareTo(sl.size);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SizeLimit)) {
			return false;
		}

		final SizeLimit other = (SizeLimit) obj;

		return infinity == other.infinity && size == other.size;
	}

	@Override
	public void toString(final StringBuilder sb) {
		if (infinity) {
			sb.append("infinity");
		} else {
			sb.append(size.toString());
		}
	}

	@Override
	public int hashCode() {
		return size.hashCode();
	}
}
