/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;

/**
 * @author Kristof Szabados
 * */
public final class DeclarationCollector {

	/** the reference to identify. */
	private final Reference refrence;

	private final List<DeclarationCollectionHelper> collected;

	public DeclarationCollector(final Reference reference) {
		this.refrence = reference;

		collected = new ArrayList<DeclarationCollectionHelper>();
	}

	/**
	 * Returns the reference whose assignments we are searching for.
	 * 
	 * @return the Reference to be identified
	 * */
	public Reference getReference() {
		return refrence;
	}

	public int getCollectionSize() {
		return collected.size();
	}

	public List<DeclarationCollectionHelper> getCollected() {
		return collected;
	}

	public void addDeclaration(final String description, final Location location, final Scope scope) {
		collected.add(new DeclarationCollectionHelper(description, location, scope));
	}

	public void addDeclaration(final String description, final Location location, final ASTNode node) {
		collected.add(new DeclarationCollectionHelper(description, location, node));
	}

	public void addDeclaration(final Assignment assignment) {
		if (assignment == null) {
			return;
		}

		if (assignment.getIdentifier() == null) {
			collected.add(new DeclarationCollectionHelper(assignment.getProposalDescription(), assignment.getLocation(), assignment));
		} else {
			collected.add(new DeclarationCollectionHelper(assignment.getProposalDescription(), assignment.getIdentifier().getLocation(),
					assignment));
		}
	}

	public void addDeclaration(final Definition definition) {
		if (definition == null) {
			return;
		}

		StringBuilder description = new StringBuilder();

		// adds proposal description
		description.append(definition.getProposalDescription());
		if (definition.getIdentifier() == null) {
			collected.add(new DeclarationCollectionHelper(description.toString(), definition.getLocation(), definition));
		} else {
			collected.add(new DeclarationCollectionHelper(description.toString(), definition.getIdentifier().getLocation(), definition));
		}
	}

	public void addDeclaration(final Group group) {
		if (group == null) {
			return;
		}

		StringBuilder description = new StringBuilder();

		// adds proposal description
		// description.append(group.getProposalDescription());

		collected.add(new DeclarationCollectionHelper(description.toString(), group.getLocation(), group));
	}

}
