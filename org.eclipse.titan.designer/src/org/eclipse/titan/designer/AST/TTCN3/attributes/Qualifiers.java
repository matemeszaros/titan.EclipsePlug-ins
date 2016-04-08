/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a list of qualifiers.
 * @author Kristof Szabados
 * */
public final class Qualifiers implements IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {
	private final List<Qualifier> qualifiers;

	public Qualifiers() {
		qualifiers = new ArrayList<Qualifier>(1);
	}

	public Qualifiers(final Qualifier qualifier) {
		qualifiers = new ArrayList<Qualifier>(1);
		if (qualifier != null) {
			qualifiers.add(qualifier);
		}
	}

	/**
	 * Adds a qualifier to the list of qualifiers.
	 * 
	 * @param qualifier
	 *                the qualifier to be added.
	 * */
	public void addQualifier(final Qualifier qualifier) {
		if (qualifier != null) {
			qualifiers.add(qualifier);
		}
	}

	/** @return the number of qualifiers handled here */
	public int getNofQualifiers() {
		return qualifiers.size();
	}

	/**
	 * Returns the qualifier at the specified position.
	 * 
	 * @param index
	 *                the index of the element to return.
	 * @return the qualifier at the given index.
	 * */
	public Qualifier getQualifierByIndex(final int index) {
		return qualifiers.get(index);
	}

	/**
	 * Handles the incremental parsing of this list of qualifiers.
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
			Qualifier temp;
			for (int i = 0, size = qualifiers.size(); i < size; i++) {
				temp = qualifiers.get(i);
				temp.updateSyntax(reparser, false);
				reparser.updateLocation(temp.getLocation());
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (qualifiers == null) {
			return;
		}

		for (Qualifier q : qualifiers) {
			q.findReferences(referenceFinder, foundIdentifiers);
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
		if (qualifiers != null) {
			for (Qualifier q : qualifiers) {
				if (!q.accept(v)) {
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
