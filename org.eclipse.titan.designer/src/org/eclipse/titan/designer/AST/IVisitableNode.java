/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Adam Delic
 * */
public interface IVisitableNode {
	/**
	 * Accept a visitor on this node
	 * @param v the visitor
	 * @return false to abort visiting the tree, true otherwise
	 */
	boolean accept(ASTVisitor v);
}
