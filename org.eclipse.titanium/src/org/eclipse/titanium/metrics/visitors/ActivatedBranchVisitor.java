/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.visitors;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ActivateExpression;

/**
 * Helper visitor class, used by the metrics.
 * <p>
 * This counts the number of default branches, that might be activated from a
 * start node (think of function definitions, testcase definitions, etc).
 * 
 * @author poroszd
 * 
 */
public class ActivatedBranchVisitor extends CounterVisitor {
	public ActivatedBranchVisitor(final Counter n) {
		super(n);
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof ActivateExpression) {
			node.accept(new ActVisitor(count));
		} else if (node instanceof Expression_Value) {
			return V_SKIP;
		}

		return V_CONTINUE;
	}
}

class ActVisitor extends CounterVisitor {
	public ActVisitor(final Counter n) {
		super(n);
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof ActivateExpression) {
			return V_CONTINUE;
		} else if (node instanceof Reference) {
			final Reference ref = (Reference) node;
			count.increase(((Def_Altstep) ref.getAssOld()).nofBranches());
		}
		return V_SKIP;
	}
}
