/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;

/**
 * @author Adam Delic
 * */
public final class StringPatternConstraint extends SubtypeConstraint {
	private final PatternString pattern;

	public StringPatternConstraint(final PatternString pattern) {
		this.pattern = pattern;
	}

	@Override
	public StringPatternConstraint complement() {
		return null;
	}

	@Override
	public StringPatternConstraint intersection(final SubtypeConstraint other) {
		return null;
	}

	@Override
	public boolean isElement(final Object o) {
		// TODO
		return true;
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		if (this == other) {
			return TernaryBool.TTRUE;
		}

		return TernaryBool.TUNKNOWN;
	}

	@Override
	public TernaryBool isFull() {
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public void toString(final StringBuilder sb) {
		// TODO
		sb.append("<patterns not implemented yet>");
	}

	@Override
	public StringPatternConstraint union(final SubtypeConstraint other) {
		return null;
	}

	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return TernaryBool.TUNKNOWN;
	}

}
