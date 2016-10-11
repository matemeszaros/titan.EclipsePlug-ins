/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * @author Adam Delic
 * */
public final class BooleanListConstraint extends SubtypeConstraint {
	public enum ConstraintValue {
		FALSE(0x01), TRUE(0x02), ALL(0x03);
		private final int value;

		ConstraintValue(final int v) {
			value = v;
		}

		public int value() {
			return value;
		}
	}

	private final int constraint;

	public BooleanListConstraint(final ConstraintValue constraint) {
		this.constraint = constraint.value();
	}

	public BooleanListConstraint(final boolean b) {
		constraint = b ? ConstraintValue.TRUE.value() : ConstraintValue.FALSE.value();
	}

	private BooleanListConstraint(final int constraint) {
		this.constraint = constraint;
	}

	@Override
	public BooleanListConstraint complement() {
		return new BooleanListConstraint(constraint ^ ConstraintValue.ALL.value());
	}

	@Override
	public BooleanListConstraint intersection(final SubtypeConstraint other) {
		BooleanListConstraint o = (BooleanListConstraint) other;
		return new BooleanListConstraint(constraint & o.constraint);
	}

	@Override
	public boolean isElement(final Object o) {
		Boolean b = (Boolean) o;
		return b ? ((constraint & ConstraintValue.TRUE.value()) != 0) : ((constraint & ConstraintValue.FALSE.value()) != 0);
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.fromBool(constraint == 0);
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		BooleanListConstraint o = (BooleanListConstraint) other;
		return TernaryBool.fromBool(constraint == o.constraint);
	}

	@Override
	public TernaryBool isFull() {
		return TernaryBool.fromBool(constraint == ConstraintValue.ALL.value());
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append('(');
		if ((constraint & ConstraintValue.FALSE.value()) != 0) {
			sb.append("false");
		}
		if (constraint == ConstraintValue.ALL.value()) {
			sb.append(", ");
		}
		if ((constraint & ConstraintValue.TRUE.value()) != 0) {
			sb.append("true");
		}
		sb.append(')');
	}

	@Override
	public BooleanListConstraint union(final SubtypeConstraint other) {
		BooleanListConstraint o = (BooleanListConstraint) other;
		return new BooleanListConstraint(constraint | o.constraint);
	}

}
