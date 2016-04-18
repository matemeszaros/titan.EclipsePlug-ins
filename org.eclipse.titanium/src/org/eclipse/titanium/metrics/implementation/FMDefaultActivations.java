/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.statements.Activate_Referenced_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Activate_Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ActivateDereferedExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ActivateExpression;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.CounterVisitor;

public class FMDefaultActivations extends BaseFunctionMetric {
	public FMDefaultActivations() {
		super(FunctionMetric.DEFAULT_ACTIVATIONS);
	}

	@Override
	public Number measure(final MetricData data, final Def_Function function) {
		final Counter count = new Counter(0);
		function.accept(new CounterVisitor(count) {
			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof Activate_Statement) {
					count.inc();
				} else if (node instanceof Activate_Referenced_Statement) {
					count.inc();
				} else if (node instanceof ActivateDereferedExpression) {
					count.inc();
				} else if (node instanceof ActivateExpression) {
					count.inc();
				}

				return V_CONTINUE;
			}
		});
		return count.val();
	}
}
