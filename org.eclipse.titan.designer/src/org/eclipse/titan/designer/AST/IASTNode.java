/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public interface IASTNode extends INamedNode {

	/**
	 * Sets the actual scope of this node.
	 *
	 * @param scope the scope to be set
	 * */
	void setMyScope(final Scope scope);

	/**
	 * @return the scope of the actual node
	 * */
	Scope getMyScope();
}
