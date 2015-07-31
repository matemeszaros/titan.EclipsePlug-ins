/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.visitors;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;

/**
 * Helper visitor class, used by the metrics.
 * <p>
 * Counts the depth of nesting of the code.
 * 
 * @author poroszd
 * 
 */
public class DepthVisitor extends CounterVisitor {
	private int currentDepth;

	public DepthVisitor(final Counter n) {
		super(n);
		currentDepth = n.val();
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof StatementBlock) {
			++currentDepth;
			if (count.val() < currentDepth) {
				count.inc();
			}
		}
		return V_CONTINUE;
	}

	@Override
	public int leave(final IVisitableNode node) {
		if (node instanceof StatementBlock) {
			--currentDepth;
		}

		return V_CONTINUE;
	}
}
