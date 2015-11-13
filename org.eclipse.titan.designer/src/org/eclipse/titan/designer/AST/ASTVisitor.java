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
public class ASTVisitor {
	public static final int V_SKIP = 1;
	public static final int V_ABORT = 2;
	public static final int V_CONTINUE = 3;

	public int visit(IVisitableNode node) {
		return V_CONTINUE;
	}
	public int leave(IVisitableNode node) {
		return V_CONTINUE;
	}
}
