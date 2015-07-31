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
 * The ArraySubReference class represents a part of a TTCN3 or ASN.1 reference, which was given in array notation ('[index]').
 * <p>
 * This is the only sub-reference type which does not have an identifier associated to it.
 * 
 * @author Kristof Szabados
 * */
public final class ArraySubReference extends ASTNode implements ISubReference, ILocateableNode {
	public static final String INVALIDSUBREFERENCE = "Type `{0}'' can not be indexed";
	public static final String INVALIDVALUESUBREFERENCE = "Invalid array element reference: type `{0}'' can not be indexed";
	public static final String INVALIDSTRINGELEMENTINDEX = "A string element cannot be indexed";
	public static final String INTEGERINDEXEXPECTED = "An integer value was expected as index";
	public static final String NATIVEINTEGEREXPECTED = "Using a large integer value ({0}) as index is not supported";

	private static final String ARRAYSUBFULLNAME = ".<array_index>";
	private static final Identifier ID = new Identifier(Identifier.Identifier_type.ID_NAME, "");

	private final Value value;

	private Location location;

	public ArraySubReference(final Value value) {
		this.value = value;
		if (null != value) {
			value.setFullNameParent(this);
		}
	}

	public Value getValue() {
		return value;
	}

	@Override
	public Subreference_type getReferenceType() {
		return Subreference_type.arraySubReference;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (child == value) {
			return builder.append(ARRAYSUBFULLNAME);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != value) {
			value.setMyScope(scope);
		}
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "arraySubReference";
	}

	@Override
	public void appendDisplayName(final StringBuilder builder) {
		builder.append('[');
		if (null != value) {
			builder.append(value.createStringRepresentation());
		}
		builder.append(']');
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (null != value) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null) {
			if (!value.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
