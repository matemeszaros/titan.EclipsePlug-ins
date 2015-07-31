/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;

/**
 * @author Adam Delic
 * */
public final class VerdicttypeListConstraint extends SubtypeConstraint {

	static final String[] VERDICT_NAMES = { "none", "pass", "inconc", "fail", "error" };

	public enum ConstraintValue {
		NONE(0x01), PASS(0x02), INCONC(0x04), FAIL(0x08), ERROR(0x10), ALL(0x1F); // all
												// values,
												// full
												// set
		private final int value;

		ConstraintValue(final int v) {
			value = v;
		}

		public int value() {
			return value;
		}
	}

	private final int constraint;

	public VerdicttypeListConstraint(final ConstraintValue constraint) {
		this.constraint = constraint.value();
	}

	public VerdicttypeListConstraint(final Verdict_Value.Verdict_type verdictType) {
		switch (verdictType) {
		case NONE:
			constraint = ConstraintValue.NONE.value();
			break;
		case PASS:
			constraint = ConstraintValue.PASS.value();
			break;
		case INCONC:
			constraint = ConstraintValue.INCONC.value();
			break;
		case FAIL:
			constraint = ConstraintValue.FAIL.value();
			break;
		case ERROR:
			constraint = ConstraintValue.ERROR.value();
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
			constraint = 0;
		}
	}

	private VerdicttypeListConstraint(final int constraint) {
		this.constraint = constraint;
	}

	@Override
	public VerdicttypeListConstraint complement() {
		return new VerdicttypeListConstraint(constraint ^ ConstraintValue.ALL.value());
	}

	@Override
	public VerdicttypeListConstraint intersection(final SubtypeConstraint other) {
		VerdicttypeListConstraint o = (VerdicttypeListConstraint) other;
		return new VerdicttypeListConstraint(constraint & o.constraint);
	}

	@Override
	public boolean isElement(final Object o) {
		Verdict_Value.Verdict_type vt = (Verdict_Value.Verdict_type) o;
		switch (vt) {
		case ERROR:
			return (constraint & ConstraintValue.ERROR.value()) != 0;
		case FAIL:
			return (constraint & ConstraintValue.FAIL.value()) != 0;
		case INCONC:
			return (constraint & ConstraintValue.INCONC.value()) != 0;
		case NONE:
			return (constraint & ConstraintValue.NONE.value()) != 0;
		case PASS:
			return (constraint & ConstraintValue.PASS.value()) != 0;
		default:
			return false;
		}
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.fromBool(constraint == 0);
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		VerdicttypeListConstraint o = (VerdicttypeListConstraint) other;
		return TernaryBool.fromBool(constraint == o.constraint);
	}

	@Override
	public TernaryBool isFull() {
		return TernaryBool.fromBool(constraint == ConstraintValue.ALL.value());
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append('(');
		boolean hasValue = false;
		for (int i = ConstraintValue.NONE.value(), idx = 0; (i < ConstraintValue.ALL.value()) && (idx < VERDICT_NAMES.length); i <<= 1, idx++) {
			if ((constraint & i) != 0) {
				if (hasValue) {
					sb.append(", ");
				}
				sb.append(VERDICT_NAMES[idx]);
				hasValue = true;
			}
		}
		sb.append(')');
	}

	@Override
	public VerdicttypeListConstraint union(final SubtypeConstraint other) {
		VerdicttypeListConstraint o = (VerdicttypeListConstraint) other;
		return new VerdicttypeListConstraint(constraint | o.constraint);
	}

}
