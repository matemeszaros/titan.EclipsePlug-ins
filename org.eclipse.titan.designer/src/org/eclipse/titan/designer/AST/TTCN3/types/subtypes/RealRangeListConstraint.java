/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * RangeListConstraint with added NaN value (NaN is unordered so it cannot be a
 * limit value) this is canonical only if two different Real values are never
 * considered to be adjacent which means that in theory for two different Real
 * values there are always infinite number of Real values that are between them
 * 
 * @author Adam Delic
 */
public final class RealRangeListConstraint extends SubtypeConstraint {
	private final boolean has_nan;
	private final RangeListConstraint rlc;

	/** empty set constructor */
	public RealRangeListConstraint() {
		has_nan = false;
		rlc = new RangeListConstraint(LimitType.Type.REAL);
	}

	/** single value set */
	public RealRangeListConstraint(final Double d) {
		if (Double.isNaN(d)) {
			has_nan = true;
			rlc = new RangeListConstraint(LimitType.Type.REAL);
			return;
		}
		has_nan = false;
		rlc = new RangeListConstraint(new RealLimit(d));
	}

	/** value range set */
	public RealRangeListConstraint(final RealLimit rl_begin, final RealLimit rl_end) {
		has_nan = false;
		rlc = new RangeListConstraint(rl_begin, rl_end);
	}

	private RealRangeListConstraint(final boolean has_nan, final RangeListConstraint rlc) {
		this.has_nan = has_nan;
		this.rlc = rlc;
	}

	@Override
	public RealRangeListConstraint complement() {
		return new RealRangeListConstraint(!has_nan, rlc.complement());
	}

	@Override
	public RealRangeListConstraint intersection(final SubtypeConstraint other) {
		RealRangeListConstraint o = (RealRangeListConstraint) other;
		return new RealRangeListConstraint(has_nan && o.has_nan, rlc.intersection(o.rlc));
	}

	@Override
	public boolean isElement(final Object o) {
		Double d = (Double) o;
		if (d.isNaN()) {
			return has_nan;
		}

		return rlc.isElement(new RealLimit(d));
	}

	@Override
	public TernaryBool isEmpty() {
		return rlc.isEmpty().and(TernaryBool.fromBool(!has_nan));
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		RealRangeListConstraint o = (RealRangeListConstraint) other;
		return rlc.isEqual(o.rlc).and(TernaryBool.fromBool(has_nan == o.has_nan));
	}

	@Override
	public TernaryBool isFull() {
		return rlc.isFull().and(TernaryBool.fromBool(has_nan));
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append('(');
		rlc.toString(sb, false);
		if (has_nan) {
			if (rlc.isEmpty() != TernaryBool.TTRUE) {
				sb.append(", ");
			}
			sb.append("NaN");
		}
		sb.append(')');
	}

	@Override
	public RealRangeListConstraint union(final SubtypeConstraint other) {
		RealRangeListConstraint o = (RealRangeListConstraint) other;
		return new RealRangeListConstraint(has_nan || o.has_nan, rlc.union(o.rlc));
	}

}
