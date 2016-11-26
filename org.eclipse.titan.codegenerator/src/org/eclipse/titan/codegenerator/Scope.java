/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import org.eclipse.titan.designer.AST.IVisitableNode;

/**
 * Represents a scope that is used by the visitor to process the Abstract Syntax Tree.
 */
public interface Scope {
	/**
	 * Visit and process a new node, then return with the next scope
	 * that should be used by the visitor.
	 * @param node the node to process
	 * @return the next scope
	 */
	Scope process(IVisitableNode node);

	/**
	 * Finish and leave a previously processed node, then return with
	 * the next scope that should be used by the visitor.
	 * @param node the node to finish
	 */
	Scope finish(IVisitableNode node);
}
