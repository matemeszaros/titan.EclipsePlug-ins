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
public final class FullStringSet extends StringSubtypeTreeElement {

	public FullStringSet(final StringType string_type) {
		super(string_type);
	}

	@Override
	public ElementType getElementType() {
		return ElementType.ALL;
	}

	@Override
	public EmptyStringSet complement() {
		return new EmptyStringSet(string_type);
	}

	@Override
	public SubtypeConstraint intersection(final SubtypeConstraint other) {
		return other;
	}

	@Override
	public boolean isElement(final Object o) {
		return true;
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.TFALSE;
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		return other.isFull();
	}

	@Override
	public TernaryBool isFull() {
		return TernaryBool.TTRUE;
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append("ALL");
	}

	@Override
	public FullStringSet union(final SubtypeConstraint other) {
		return this;
	}

	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return TernaryBool.TTRUE;
	}

}
