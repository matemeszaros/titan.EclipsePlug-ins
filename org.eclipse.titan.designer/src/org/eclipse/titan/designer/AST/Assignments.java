/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.Iterator;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to store assignments.
 * 
 * @author Kristof Szabados
 * */
public abstract class Assignments extends Scope implements IOutlineElement, Iterable<Assignment>{
	public static final String DUPLICATEDEFINITIONFIRST = "Duplicate definition with name `{0}'' was first declared here";
	public static final String DUPLICATEDEFINITIONREPEATED = "Duplicate definition with name `{0}'' was declared here again";

	/**
	 * Returns whether an assignment with an id exists.
	 * Unlike has_assignment_withId() this function does not look into the parent scope.
	 *
	 * @param timestamp the timestamp of the actual check cycle
	 * @param identifier the identifier to look up
	 *
	 * @return true if the assignment was found locally, false otherwise
	 **/
	public abstract boolean hasLocalAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier);

	/**
	 * Returns the locally defined assignment with the given id, or null if it does not exist.
	 *
	 * @param timestamp the timestamp of the actual check cycle
	 * @param identifier the identifier to look up
	 * @return the found assignment or null if none.
	 * */
	public abstract Assignment getLocalAssignmentByID(final CompilationTimeStamp timestamp, final Identifier identifier);

	/**
	 * @return the number of assignments. */
	public abstract int getNofAssignments();

	/**
	 * Returns the assignment with the given index.
	 *
	 * @param i the index of the assignment.
	 * @return the assignment at the provided index.
	 * */
	public abstract Assignment getAssignmentByIndex(int i);

	@Override
	public Identifier getIdentifier() {
		return getModuleScope().getIdentifier();
	}

	@Override
	public Assignments getAssignmentsScope() {
		return this;
	}

	/**
	 * Does the semantic checking of the contained assignments.
	 * <p>
	 * <ul>
	 * <li>The assignments are checked one by one
	 * </ul>
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	/** Checks properties, that can only be checked after the semantic check was completely run. */
	public abstract void postCheck();

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		return getAssBySRef(timestamp, reference, null);
	}
	
	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference, IReferenceChain refChain) {
			if (null != reference.getModuleIdentifier() || null == reference.getId()) {
			return getModuleScope().getAssBySRef(timestamp, reference);
		}

		if (hasLocalAssignmentWithID(timestamp, reference.getId())) {
			return getLocalAssignmentByID(timestamp, reference.getId());
		}
		return getParentScope().getAssBySRef(timestamp, reference);
	}

	@Override
	public boolean hasAssignmentWithId(final CompilationTimeStamp timestamp, final Identifier identifier) {

		if (hasLocalAssignmentWithID(timestamp, identifier)) {
			return true;
		}

		return super.hasAssignmentWithId(timestamp, identifier);
	}

	public String getOutlineText() {
		return "";
	}

	public int category() {
		return 0;
	}


	@Override
	/**Remove is not supported */
	public abstract Iterator<Assignment> iterator();
}
