/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Adam Delic
 * */
public final class RealLimit extends LimitType {
	public enum ValueType {
		LOWER(-1), EXACT(0), UPPER(1);
		private final int value;

		ValueType(final int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	public static final RealLimit MAXIMUM = new RealLimit(Double.POSITIVE_INFINITY);
	public static final RealLimit MINIMUM = new RealLimit(Double.NEGATIVE_INFINITY);

	private final ValueType value_type;
	private final double value;

	private RealLimit(final ValueType vt, final double d) {
		value_type = vt;
		value = d;
	}

	public RealLimit(final double d) {
		if (Double.isNaN(d)) {
			ErrorReporter.INTERNAL_ERROR("NaN range limit");
		}
		value_type = ValueType.EXACT;
		value = d;
	}

	public double getValue() {
		return value;
	}

	@Override
	public LimitType decrement() {
		switch (value_type) {
		case UPPER:
			return new RealLimit(value);
		default:
			return new RealLimit(ValueType.LOWER, value);
		}
	}

	@Override
	public Type getType() {
		return Type.REAL;
	}

	@Override
	public LimitType increment() {
		switch (value_type) {
		case LOWER:
			return new RealLimit(value);
		default:
			return new RealLimit(ValueType.UPPER, value);
		}
	}

	@Override
	public boolean isAdjacent(final LimitType other) {
		RealLimit rl = (RealLimit) other;
		return ((Double.compare(value, rl.value) == 0) && ((value_type.value() + 1) == rl.value_type.value()));
	}

	@Override
	public void toString(final StringBuilder sb) {
		if (value_type != ValueType.EXACT) {
			sb.append('!');
		}
		sb.append(value);
	}

	@Override
	public int compareTo(final LimitType o) {
		final RealLimit rl = (RealLimit) o;
		// compare the double values with "natural ordering" compare,
		// where -0.0 < 0.0 ... INF < NaN
		final int rv = Double.compare(value, rl.value);
		return (rv != 0) ? rv : (value_type.value() - rl.value_type.value());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof RealLimit)) {
			return false;
		}

		final RealLimit other = (RealLimit) obj;

		return value_type == other.value_type && Double.compare(value, other.value) == 0;
	}

	@Override
	public int hashCode() {
		return (int) value;
	}
}
