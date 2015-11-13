/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Signature parameter.
 * 
 * @author Kristof Szabados
 */
public final class SignatureFormalParameter extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".<type>";

	public static final int PARAM_IN = 0;
	public static final int PARAM_OUT = 1;
	public static final int PARAM_INOUT = 2;

	private final int parameterDirection;
	private final Identifier identifier;
	private final Type type;

	/**
	 * The location of the whole parameter. This location encloses the parameter
	 * fully, as it is used to report errors to.
	 **/
	private Location location;

	public SignatureFormalParameter(final int parameterDirection, final Type type, final Identifier identifier) {
		this.parameterDirection = parameterDirection;
		this.type = type;
		this.identifier = identifier;

		if (type != null) {
			type.setFullNameParent(this);
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

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		if (type != null) {
			type.setMyScope(scope);
		}
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Type getType() {
		return type;
	}

	public int getDirection() {
		return parameterDirection;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier!=null && !identifier.accept(v)) {
			return false;
		}
		if (type!=null && !type.accept(v)) {
			return false;
		}
		return true;
	}
}
