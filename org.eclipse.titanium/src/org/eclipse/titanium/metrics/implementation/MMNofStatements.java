/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.CounterVisitor;


public class MMNofStatements extends BaseModuleMetric {

	private static class StatementCounterVisitor extends CounterVisitor {

		public StatementCounterVisitor(Counter n) {
			super(n);
		}

		@Override
		public int visit(IVisitableNode node) {
			if (node instanceof StatementBlock) {
				count.increase(((StatementBlock) node).getSize());
			}
			return V_CONTINUE;
		}
	}

	public MMNofStatements() {
		super(ModuleMetric.NOF_STATEMENTS);
	}

	@Override
	public Number measure(MetricData data, Module module) {
		final Counter statements = new Counter(0);
		final StatementCounterVisitor visitor = new StatementCounterVisitor(statements);
		module.accept(visitor);
		return statements.val();
	}
}
