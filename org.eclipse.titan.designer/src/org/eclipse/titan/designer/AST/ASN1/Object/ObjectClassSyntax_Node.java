/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;

/**
 * ObjectClass Syntax. Class to build, manage, ... ObjectClass Syntax.
 * 
 * @author Kristof Szabados
 */
public abstract class ObjectClassSyntax_Node implements ILocateableNode {

	/**
	 * The location of the whole field specification. This location encloses
	 * the field specification fully, as it is used to report errors to.
	 **/
	private Location location;

	/** Stores whether this syntax was already builded or not. */
	protected boolean isBuilded;

	public ObjectClassSyntax_Node() {
		isBuilded = false;
	}

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	public abstract void accept(ObjectClassSyntax_Visitor visitor);

	public final boolean getIsBuilded() {
		return isBuilded;
	}

	public final void setIsBuilded(final boolean isBuilded) {
		this.isBuilded = isBuilded;
	}

	public abstract String getDisplayName();
}
