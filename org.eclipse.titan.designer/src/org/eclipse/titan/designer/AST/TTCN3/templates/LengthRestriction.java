/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a range restriction.
 * 
 * @author Kristof Szabados
 * */
public abstract class LengthRestriction extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	/**
	 * The location of the whole restriction. This location encloses the
	 * restriction fully, as it is used to report errors to.
	 **/
	private Location location;

	public LengthRestriction() {
		location = NULL_Location.INSTANCE;
	}

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	/**
	 * Creates and returns a string representation if the length
	 * restriction.
	 * 
	 * @return the string representation of the length restriction.
	 * */
	public abstract String createStringRepresentation();

	/**
	 * Check that the length restriction is a correct value, and at is
	 * allowed at a given location.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expected_value
	 *                the value kind expected.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp, final Expected_Value_type expected_value);

	/**
	 * Checks if the length restriction is valid for the array type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param dimension
	 *                the dimension of the array type.
	 * */
	public abstract void checkArraySize(final CompilationTimeStamp timestamp, final ArrayDimension dimension);

	/**
	 * Checks if the provided amount of elements can be valid or not
	 * according to this length restriction.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param nof_elements
	 *                the number of elements the checked template has.
	 * @param lessAllowed
	 *                wheter less elements should be accepted (subset
	 *                template)
	 * @param more_allowed
	 *                wheter more elements should be accepted (the template
	 *                has anyornone elements).
	 * @param has_anyornone
	 *                whether the template has anyornone elements.
	 * @param locatable
	 *                the location errors should be reported to if found.
	 * */
	public abstract void checkNofElements(final CompilationTimeStamp timestamp, final int nof_elements, boolean lessAllowed,
			final boolean more_allowed, final boolean has_anyornone, final ILocateableNode locatable);

	/**
	 * Handles the incremental parsing of this length restriction.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException;
}
