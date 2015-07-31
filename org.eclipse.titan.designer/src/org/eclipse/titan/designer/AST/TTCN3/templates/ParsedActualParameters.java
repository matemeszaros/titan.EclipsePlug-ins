/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the actual parameters from the parser in "raw form".
 * 
 * Contains both positional and named parameters. There is not enough
 * information during parsing to construct a "full" ActualParameters object
 * (information about formal parameters is needed). This object holds the
 * available information until the ActualParameters object can be constructed
 * during semantic analysis.
 * 
 * @author Kristof Szabados
 */
public final class ParsedActualParameters extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	/** the unnamed parameters. */
	private TemplateInstances unnamedPart;

	/** the named parameters. */
	private NamedParameters namedPart;

	/** the formal parameter list that this was checked against */
	private FormalParameterList formalParList = null;

	private Location location;

	public ParsedActualParameters() {
		super();
		location = NULL_Location.INSTANCE;
	}

	public void setFormalParList(final FormalParameterList formalParList) {
		this.formalParList = formalParList;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (unnamedPart != null) {
			unnamedPart.setMyScope(scope);
		}
		if (namedPart != null) {
			namedPart.setMyScope(scope);
		}
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
		if (unnamedPart != null) {
			unnamedPart.setLocation(location);
		}
		if (namedPart != null) {
			namedPart.setLocation(location);
		}
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public void addUnnamedParameter(final TemplateInstance parameter) {
		if (unnamedPart == null) {
			unnamedPart = new TemplateInstances();
			unnamedPart.setFullNameParent(this);
		}

		unnamedPart.addTemplateInstance(parameter);
	}

	public TemplateInstances getInstances() {
		if (unnamedPart == null) {
			return new TemplateInstances();
		}

		return unnamedPart;
	}

	public void addNamedParameter(final NamedParameter parameter) {
		if (namedPart == null) {
			namedPart = new NamedParameters();
			namedPart.setFullNameParent(this);
		}

		namedPart.addParameter(parameter);
	}

	public NamedParameters getNamedParameters() {
		if (namedPart == null) {
			return new NamedParameters();
		}

		return namedPart;
	}

	/**
	 * Handles the incremental parsing of this list of parsed actual
	 * parameters.
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

		// The parts have the same location object, so it must not be
		// updated here
		if (unnamedPart != null) {
			unnamedPart.updateSyntax(reparser, false);
		}

		if (namedPart != null) {
			namedPart.updateSyntax(reparser, false);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (unnamedPart != null) {
			unnamedPart.findReferences(referenceFinder, foundIdentifiers);
		}
		if (namedPart != null) {
			if (formalParList != null) {
				for (int i = 0; i < namedPart.getNofParams(); i++) {
					Identifier parName = namedPart.getParamByIndex(i).getName();
					FormalParameter fp = formalParList.getParameterById(parName);
					if (fp == referenceFinder.assignment) {
						foundIdentifiers.add(new Hit(parName));
					}
				}
			}
			namedPart.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (unnamedPart != null && !unnamedPart.accept(v)) {
			return false;
		}
		if (namedPart != null && !namedPart.accept(v)) {
			return false;
		}
		return true;
	}

	public String createStringRepresentation() {
		StringBuilder sb = new StringBuilder();
		if (unnamedPart != null) {
			sb.append(unnamedPart.createStringRepresentation());
		}

		if (namedPart != null) {
			if (namedPart.getNofParams() != 0) {
				sb.append(", ");
			}
			sb.append(namedPart.createStringRepresentation());
		}
		return sb.toString();
	}
}
