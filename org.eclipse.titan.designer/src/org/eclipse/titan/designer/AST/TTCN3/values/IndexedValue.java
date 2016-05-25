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
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an IndexedValue.
 * 
 * @author Kristof Szabados
 */
public final class IndexedValue extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private final ArraySubReference index;
	private final IValue value;

	/**
	 * The location of the whole item.
	 * This location encloses the item fully,
	 * as it is used to report errors to.
	 **/
	private Location location;

	public IndexedValue(final ArraySubReference index, final IValue value) {
		this.index = index;
		this.value = value;
		location = NULL_Location.INSTANCE;

		if (index != null) {
			index.setFullNameParent(this);
		}
		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value == null || index == null) {
			return true;
		}

		return value.isUnfoldable(timestamp, expectedValue, referenceChain)
				|| index.getValue().isUnfoldable(timestamp, expectedValue, referenceChain);
	}

	public ArraySubReference getIndex() {
		return index;
	}

	public IValue getValue() {
		return value;
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
		if (index != null) {
			index.setMyScope(scope);
		}
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	/**
	 * Handles the incremental parsing of this indexed value.
	 *
	 * @param reparser the parser doing the incremental parsing.
	 * @param isDamaged true if the location contains the damaged area, false if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (index != null) {
			index.updateSyntax(reparser, false);
			reparser.updateLocation(index.getLocation());
		}
		if (value != null) {
			((Value)value).updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (index != null) {
			index.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (index!=null && !index.accept(v)) {
			return false;
		}
		if (value!=null && !value.accept(v)) {
			return false;
		}
		return true;
	}
}
