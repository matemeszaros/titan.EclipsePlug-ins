/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferencingElement;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a named value.
 * 
 * @author Kristof Szabados
 * */
public final class NamedValue extends ASTNode implements ILocateableNode, IIncrementallyUpdateable, IReferencingElement {

	private final Identifier name;
	private final IValue value;

	/**
	 * The location of the whole item. This location encloses the item fully, as
	 * it is used to report errors to.
	 **/
	private Location location;

	/**
	 * Tells if this named value was parsed or created while doing the semantic
	 * check.
	 * */
	private final boolean parsed;

	public NamedValue(final Identifier name, final IValue value) {
		super();
		location = NULL_Location.INSTANCE;
		this.name = name;
		this.value = value;
		parsed = true;

		if (value != null && value.getNameParent() == null) {
			value.setFullNameParent(this);
		}
	}

	public NamedValue(final Identifier name, final IValue value, final boolean parsed) {
		super();
		location = NULL_Location.INSTANCE;
		this.name = name;
		this.value = value;
		this.parsed = parsed;

		if (value != null && value.getFullName() == null) {
			value.setFullNameParent(this);
		}
	}

	public Identifier getName() {
		return name;
	}

	public IValue getValue() {
		return value;
	}

	public boolean isParsed() {
		return parsed;
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
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	/**
	 * Handles the incremental parsing of this named value.
	 *
	 * @param reparser the parser doing the incremental parsing.
	 * @param isDamaged true if the location contains the damaged area,
	 *         false if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(name.getLocation());

		if (value instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) value).updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		} else if (value != null) {
			throw new ReParseException();
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (name!=null && !name.accept(v)) {
			return false;
		}
		if (value!=null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public Declaration getDeclaration() {

		INamedNode inamedNode = getNameParent();

		while (!(inamedNode instanceof IValue)) {
			if( inamedNode == null) {
				return null; //FIXME: this is just a temp solution! find the reason!
			}
			inamedNode = inamedNode.getNameParent();
		}

		IValue iValue = (IValue) inamedNode;

		IType type = iValue.getMyGovernor();
		if (type == null) {
			return null;
		}
		type = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		

		if (type instanceof ITypeWithComponents) {
			final Identifier id = ((ITypeWithComponents) type).getComponentIdentifierByName(getName());
			return Declaration.createInstance(type.getDefiningAssignment(), id);
		}

		return null;
	}
}
