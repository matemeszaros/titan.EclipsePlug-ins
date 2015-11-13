/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;

/**
 * ComponentType (abstract class).
 * <p>
 * Originally CT in TITAN
 * 
 * @author Kristof Szabados
 */
public abstract class ComponentType extends ExtensionAddition implements ILocateableNode {
	/**
	 * The location of the whole componentType. This location encloses it
	 * fully, as it is used to report errors to.
	 **/
	protected Location location;

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}
}
