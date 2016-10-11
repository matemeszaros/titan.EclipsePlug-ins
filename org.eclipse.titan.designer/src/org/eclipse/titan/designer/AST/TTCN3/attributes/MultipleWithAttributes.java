/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class MultipleWithAttributes implements ILocateableNode, IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {
	private List<SingleWithAttribute> attributes;

	/**
	 * The location of the whole attributes. This location encloses the
	 * attributes fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	/**
	 * Adds an attribute to the list.
	 * 
	 * @param attribute
	 *                the attribute to be added.
	 * */
	public void addAttribute(final SingleWithAttribute attribute) {
		if (attribute != null) {
			if (attributes == null) {
				attributes = new ArrayList<SingleWithAttribute>(1);
			}
			attributes.add(attribute);
		}
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
	 * @return the number of attributes stored here
	 * */
	public int getNofElements() {
		if (attributes == null) {
			return 0;
		}

		return attributes.size();
	}

	/**
	 * Returns the attribute at the given index.
	 * 
	 * @param index
	 *                the index of the attribute to be returned.
	 * 
	 * @return the attribute at the given index.
	 * */
	public SingleWithAttribute getAttribute(final int index) {
		if (attributes == null) {
			return null;
		}

		return attributes.get(index);
	}

	/**
	 * Handles the incremental parsing of this attribute set.
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

		if (attributes != null) {
			SingleWithAttribute temp;
			for (int i = 0, size = attributes.size(); i < size; i++) {
				temp = attributes.get(i);
				temp.updateSyntax(reparser, false);
				reparser.updateLocation(temp.getLocation());
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (attributes == null) {
			return;
		}

		for (SingleWithAttribute attr : attributes) {
			attr.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (attributes != null) {
			for (SingleWithAttribute attr : attributes) {
				if (!attr.accept(v)) {
					return false;
				}
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
