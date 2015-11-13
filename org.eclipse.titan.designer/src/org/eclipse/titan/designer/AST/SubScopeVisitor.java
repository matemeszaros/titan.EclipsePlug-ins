/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Delic
 * */
public class SubScopeVisitor extends ASTVisitor {
	private Scope root;
	private List<Scope> subScopes = new ArrayList<Scope>();
	public SubScopeVisitor(Scope root) {
		this.root = root;
	}

	@Override
	public int visit(IVisitableNode node) {
		if (node instanceof Scope) {
			Scope scope = (Scope)node;
			if (scope.isChildOf(root)) {
				// this is a sub-scope of the root
				subScopes.add(scope);
			}
		}
		return V_CONTINUE;
	}

	public List<Scope> getSubScopes() {
		return subScopes;
	}
}
