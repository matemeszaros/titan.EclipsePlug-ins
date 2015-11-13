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
class ScopeTreeNode {
	Scope scope;
	List<ScopeTreeNode> children = new ArrayList<ScopeTreeNode>();
	List<Assignment> assignments = new ArrayList<Assignment>();

	public ScopeTreeNode(Scope scope) {
		this.scope = scope;
	}

	public void add(Scope s, Assignment a) {
		List<Scope> scopePath = new ArrayList<Scope>();
		while (s != null) {
			scopePath.add(s);
			s = s.getParentScope();
		}
		addPath(scopePath, a);
	}

	void addPath(List<Scope> scopePath, Assignment a) {
		if (scopePath.isEmpty()) {
			if (a != null) {
				assignments.add(a);
			}
			return;
		}
		Scope lastParent = scopePath.remove(scopePath.size() - 1);
		for (ScopeTreeNode stn : children) {
			if (stn.scope == lastParent) {
				stn.addPath(scopePath, a);
				lastParent = null;
				break;
			}
		}
		if (lastParent != null) {
			ScopeTreeNode newChild = new ScopeTreeNode(lastParent);
			children.add(newChild);
			newChild.addPath(scopePath, a);
		}
	}

	public void printTreeAsHTML(StringBuilder sb) {
		if (scope != null) {
			sb.append("<li><b>").append(scope.getClass().getSimpleName()).append("</b> <i>").append(scope.getFullName()).append("</i>");
		}
		if (!assignments.isEmpty()) {
			sb.append("<ul>");
			for (Assignment a : assignments) {
				sb.append("<li><font color='blue'>").append(a.getClass().getSimpleName()).append(" : <u>").append(a.getIdentifier())
						.append("</u></font></li>");
			}
			sb.append("</ul>");
		}
		if (!children.isEmpty()) {
			sb.append("<ul>");
			for (ScopeTreeNode stn : children) {
				stn.printTreeAsHTML(sb);
			}
			sb.append("</ul>");
		}
	}
}

/**
 * @author Adam Delic
 * */
public class ScopeHierarchyVisitor extends ASTVisitor {
	ScopeTreeNode scopeTree = new ScopeTreeNode(null);

	@Override
	public int visit(IVisitableNode node) {
		if (node instanceof Scope) {
			Scope scope = (Scope) node;
			scopeTree.add(scope, null);
		} else if (node instanceof Assignment) {
			Assignment ass = (Assignment) node;
			scopeTree.add(ass.getMyScope(), ass);
		}
		return V_CONTINUE;
	}

	public String getScopeTreeAsHTMLPage() {
		StringBuilder sb = new StringBuilder();
		sb.append("<HTML><HEAD><TITLE>Scope Tree</TITLE></HEAD><BODY><ul>");
		scopeTree.printTreeAsHTML(sb);
		sb.append("</ul></BODY></HTML>");
		return sb.toString();
	}
}
