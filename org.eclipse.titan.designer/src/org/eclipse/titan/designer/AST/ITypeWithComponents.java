/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * This interface represents a type which has components.
 * The component's identifiers can be accessed through their name.
 * 
 * @author Kristof Szabados
 */
public interface ITypeWithComponents {

	/**
	 * Returns the element with the specified name.
	 * 
	 * @param identifier the name of the element to return
	 * @return The element which has the same name as the given Identifier.
	 */
	Identifier getComponentIdentifierByName(final Identifier identifier);
}
