/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * A single attribute specification as read from the TTCN-3 by the TTCN-3
 * parser.
 * 
 * It needs to be parsed later by an attribute parser, to extract the semantic
 * data from it.
 * 
 * @author Kristof Szabados
 * */
public final class AttributeSpecification implements ILocateableNode, IIncrementallyUpdateable {
	private final String specification;

	/**
	 * The location of the whole specification. This location encloses the
	 * specification fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public AttributeSpecification(final String specification) {
		this.specification = specification;
	}

	/**
	 * @return the specification text of this attribute specification.
	 * */
	public String getSpecification() {
		return specification;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * Handles the incremental parsing of this attribute specification.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}
}
