/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.actions;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Scope;

/**
 * @author Kristof Szabados
 * */
public final class DeclarationCollectionHelper {
	public String description;
	public Location location;
	public Scope scope;

	public ASTNode node;

	public DeclarationCollectionHelper(final String description, final Location location, final Scope scope) {
		this.description = description;
		this.location = location;
		this.scope = scope;
	}

	public DeclarationCollectionHelper(final String description, final Location location, final ASTNode node) {
		this.description = description;
		this.location = location;
		this.node = node;
	}

	/**
	 * Equality is defined by the equality of locations, it is fine to
	 * filter duplications.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DeclarationCollectionHelper)) {
			return false;
		}
		if (location.equals(((DeclarationCollectionHelper) obj).location)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return location.hashCode();
	}
}
