/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;

/**
 * AST objects that can contain Identifier instances and these Identifiers can be found
 * 
 * @author Kristof Szabados
 */
public interface IIdentifierContainer {

	/**
	 * Recursively find all references to a given language element (definition,field,etc.).
	 * 
	 * @param referenceFinder contains all the required data needed for the search
	 * @param foundIdentifiers contains the found references
	 */
	void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers);

}
