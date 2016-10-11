/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * http://en.wikipedia.org/wiki/Ternary_logic
 * 
 * @author Adam Delic
 */
public enum TernaryBool {
	TFALSE(false), TUNKNOWN(), TTRUE(true);

	private static final TernaryBool[][] AND_TRUTH_TABLE = { { TFALSE, TFALSE, TFALSE }, { TFALSE, TUNKNOWN, TUNKNOWN },
		{ TFALSE, TUNKNOWN, TTRUE } };
	private static final TernaryBool[][] OR_TRUTH_TABLE = { { TFALSE, TUNKNOWN, TTRUE }, { TUNKNOWN, TUNKNOWN, TTRUE }, { TTRUE, TTRUE, TTRUE } };
	private static final TernaryBool[] NOT_TRUTH_TABLE = { TTRUE, TUNKNOWN, TFALSE };


	private final int value;

	TernaryBool() {
		value = 1;
	}

	TernaryBool(final boolean b) {
		value = b ? 2 : 0;
	}

	public static TernaryBool fromBool(final boolean b) {
		return b ? TTRUE : TFALSE;
	}

	public TernaryBool and(final TernaryBool other) {
		return AND_TRUTH_TABLE[value][other.value];
	}

	public TernaryBool or(final TernaryBool other) {
		return OR_TRUTH_TABLE[value][other.value];
	}

	public TernaryBool not() {
		return NOT_TRUTH_TABLE[value];
	}
}
