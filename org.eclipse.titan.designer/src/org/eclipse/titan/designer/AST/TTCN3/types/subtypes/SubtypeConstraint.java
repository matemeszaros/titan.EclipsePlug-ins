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
public abstract class SubtypeConstraint {

	/** return if this is an empty set */
	public abstract TernaryBool isEmpty();

	/** return if this is a full set (contains every possible element) */
	public abstract TernaryBool isFull();

	/** return if this set is equal to the other */
	public abstract TernaryBool isEqual(SubtypeConstraint other);

	// FIXME should be much more specific, using the object type indicates
	// design problems
	/** return if o is an element of this set */
	public abstract boolean isElement(Object o);

	/** return union of two sets */
	public abstract SubtypeConstraint union(SubtypeConstraint other);

	/** return intersection of two sets */
	public abstract SubtypeConstraint intersection(SubtypeConstraint other);

	/** return complement of this set */
	public abstract SubtypeConstraint complement();

	/** return (first - second) set */
	public SubtypeConstraint except(final SubtypeConstraint other) {
		return this.intersection(other.complement());
	}

	/** return if this is a subset of set */
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return this.intersection(other.complement()).isEmpty();
	}

	/** append content to sb */
	public abstract void toString(StringBuilder sb);

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
