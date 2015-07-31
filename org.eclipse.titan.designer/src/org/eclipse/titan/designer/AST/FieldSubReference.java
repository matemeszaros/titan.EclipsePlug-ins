/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The FieldSubReference class represents a part of a TTCN3 or ASN.1 reference, which was given in fieldreference notation ('.field1').
 * 
 * @author Kristof Szabados
 * */
public final class FieldSubReference implements ISubReference, ILocateableNode {
	public static final String INVALIDSUBREFERENCE = "Invalid field reference `{0}'': type `{1}'' does not have fields.";
	public static final String NONEXISTENTSUBREFERENCE = "Reference to a non-existent field `{0}'' in type `{1}''";
//	public static final String INVALIDREFERENCE1 = "Type `{0}'' does not have fields.";

	private final Identifier fieldId;

	public FieldSubReference(final Identifier fieldId) {
		this.fieldId = fieldId;
	}

	@Override
	public Subreference_type getReferenceType() {
		return Subreference_type.fieldSubReference;
	}

	@Override
	public Identifier getId() {
		return fieldId;
	}

	@Override
	public void setMyScope(final Scope scope) {
	}

	@Override
	public String getFullName() {
		return "";
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		return new StringBuilder();
	}

	@Override
	public INamedNode getNameParent() {
		return null;
	}

	@Override
	public void setFullNameParent(final INamedNode nameParent) {
	}

	@Override
	public void setLocation(final Location location) {
		if (null != fieldId) {
			fieldId.setLocation(location);
		}
	}

	// Location is optimized not to store an object as it is not needed
	@Override
	public Location getLocation() {
		return new Location(fieldId.getLocation());
	}

	@Override
	public String toString() {
		return "fieldSubReference: " + fieldId.getDisplayName();
	}

	@Override
	public void appendDisplayName(final StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append('.');
		}
		builder.append(fieldId.getDisplayName());
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(fieldId.getLocation());
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT: return false;
		case ASTVisitor.V_SKIP: return true;
		}
		if (fieldId != null) {
			if (!fieldId.accept(v)) {
				return false;
			}
		}
		if (v.leave(this)==ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
