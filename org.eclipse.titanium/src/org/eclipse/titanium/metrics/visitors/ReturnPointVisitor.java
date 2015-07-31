/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.visitors;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;

/**
 * Helper visitor class, used by the metrics.
 * <p>
 * Counts the return points of function (the visit should start at a
 * Def_Function node).
 * 
 * @author poroszd
 * 
 */
public class ReturnPointVisitor extends CounterVisitor {
	private int depth;

	public ReturnPointVisitor(final Counter n) {
		super(n);
		depth = 0;
		count.inc();
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof StatementBlock) {
			++depth;
		} else if (node instanceof Return_Statement && depth > 1) {
			count.inc();
		} else if (node instanceof Expression_Value) {
			return V_SKIP;
		}

		return V_CONTINUE;
	}

	@Override
	public int leave(final IVisitableNode node) {
		if (node instanceof StatementBlock) {
			--depth;
		}

		return V_CONTINUE;
	}
}
