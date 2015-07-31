/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * Provides an interface for nodes in the AST that can have names,
 * or can be part of a naming chain.
 * 
 * @author Kristof Szabados
 * */
public interface INamedNode {

	String MODULENAMEPREFIX = "@";
	String DOT = ".";
	String LESSTHAN = "<";
	String MORETHAN = ">";
	String LEFTPARENTHESES = "(";
	String RIGHTPARENTHESES = ")";
	String SQUAREOPEN = "[";
	String SQUARECLOSE = "]";

	/**
	 * Sets the full name of the node.
	 *
	 * @param nameParent the name to be set
	 * */
	void setFullNameParent(INamedNode nameParent);

	/**
	 * @param child create the first part of the child's name
	 * @return the full name of the node
	 * */
	StringBuilder getFullName(INamedNode child);

	/**
	 * @return the full name of the node
	 * */
	String getFullName();

	/**
	 * @return the naming parent of this node, or null if none
	 * */
	INamedNode getNameParent();
}
