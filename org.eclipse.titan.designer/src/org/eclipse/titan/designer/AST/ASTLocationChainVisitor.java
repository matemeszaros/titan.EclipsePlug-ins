/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * @author Adam Delic
 * */
public class ASTLocationChainVisitor extends ASTVisitor {
	private List<IVisitableNode> chain = new ArrayList<IVisitableNode>();
	private int offset;

	public ASTLocationChainVisitor(int offset) {
		this.offset = offset;
	}

	public List<IVisitableNode> getChain() {
		return chain;
	}

	@Override
	public int visit(IVisitableNode node) {
		if (node instanceof ILocateableNode) {
			Location loc = ((ILocateableNode)node).getLocation();
			if (loc != null && loc.containsOffset(offset)) {
				chain.add(node);
			} else {
				// skip the children, the offset is not inside this node
				return V_SKIP;
			}
		}
		return V_CONTINUE;
	}

	public void printChain() {
		StringBuilder sb = new StringBuilder();
		sb.append("Node chain for offset ").append(offset).append(" : ");
		boolean first = true;
		for (IVisitableNode node : chain) {
			if (!first) {
				sb.append(" -> ");
			} else {
				first = false;
			}
			sb.append(node.getClass().getName());
		}
		TITANDebugConsole.println(sb.toString());
	}

}
