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
public final class EmptyStringSet extends StringSubtypeTreeElement {

	public EmptyStringSet(final StringType stringType) {
		super(stringType);
	}

	@Override
	public ElementType getElementType() {
		return ElementType.NONE;
	}

	@Override
	public SubtypeConstraint complement() {
		return new FullStringSet(stringType);
	}

	@Override
	public EmptyStringSet intersection(final SubtypeConstraint other) {
		return this;
	}

	@Override
	public boolean isElement(final Object o) {
		return false;
	}

	@Override
	public TernaryBool isEmpty() {
		return TernaryBool.TTRUE;
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		return other.isEmpty();
	}

	@Override
	public TernaryBool isFull() {
		return TernaryBool.TFALSE;
	}

	@Override
	public void toString(final StringBuilder sb) {
		// nothing to write
	}

	@Override
	public SubtypeConstraint union(final SubtypeConstraint other) {
		return other;
	}

	@Override
	public EmptyStringSet except(final SubtypeConstraint other) {
		return this;
	}

	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return other.isEmpty();
	}

}
