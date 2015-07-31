/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a parameter assignment in a parameter redirection.
 * 
 * @author Kristof Szabados
 * */
public final class Parameter_Assignment extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private final Reference reference;
	private final Identifier identifier;

	private Location location = NULL_Location.INSTANCE;

	public Parameter_Assignment(final Reference reference, final Identifier identifier) {
		this.reference = reference;
		this.identifier = identifier;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Reference getReference() {
		return reference;
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
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reference.updateSyntax(reparser, isDamaged);
		reparser.updateLocation(reference.getLocation());

		reparser.updateLocation(identifier.getLocation());
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (identifier != null) {
			// TODO
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		return true;
	}
}
