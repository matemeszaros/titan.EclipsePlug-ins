/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

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
 * Stores a single attribute.
 * 
 * @author Kristof Szabados
 */
public final class SingleWithAttribute implements ILocateableNode, IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {

	public enum Attribute_Type {
		Encode_Attribute,
		Variant_Attribute,
		Display_Attribute,
		Extension_Attribute,
		Optional_Attribute,
		Erroneous_Attribute,
		Invalid_Attribute
	}

	private Attribute_Type attributeType;
	private boolean hasOverride;
	private Qualifiers qualifiers;
	private AttributeSpecification attributeSpecficiation;

	/**
	 * The location of the whole attribute. This location encloses the
	 * attribute fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public SingleWithAttribute(final Attribute_Type attributeType, final boolean hasOverride, final Qualifiers qualifiers,
			final AttributeSpecification attributeSpecficiation) {
		this.attributeType = attributeType;
		this.hasOverride = hasOverride;
		this.qualifiers = qualifiers;
		this.attributeSpecficiation = attributeSpecficiation;
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
	 * @return the type of the attribute
	 * */
	public Attribute_Type getAttributeType() {
		return attributeType;
	}

	/**
	 * @return true if the attribute has the override option set, false
	 *         otherwise.
	 * */
	public boolean hasOverride() {
		return hasOverride;
	}

	/**
	 * @return the qualifiers of this single attribute
	 * */
	public Qualifiers getQualifiers() {
		return qualifiers;
	}

	/**
	 * @return the attribute specification of this attribute
	 * */
	public AttributeSpecification getAttributeSpecification() {
		return attributeSpecficiation;
	}

	/**
	 * Handles the incremental parsing of this attribute.
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

		if (qualifiers != null) {
			qualifiers.updateSyntax(reparser, false);
		}

		if (attributeSpecficiation != null) {
			attributeSpecficiation.updateSyntax(reparser, false);
			reparser.updateLocation(attributeSpecficiation.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (qualifiers != null) {
			qualifiers.findReferences(referenceFinder, foundIdentifiers);
		}
		// attributeSpecficiation is not searched, it contains a string
		// that is parsed into AST objects
		// elsewhere, those are searched there.
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (qualifiers != null) {
			if (!qualifiers.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
