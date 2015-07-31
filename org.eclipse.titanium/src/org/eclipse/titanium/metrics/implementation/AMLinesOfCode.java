/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.LargeLocation;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.CounterVisitor;

public class AMLinesOfCode extends BaseAltstepMetric {
	public AMLinesOfCode() {
		super(AltstepMetric.LINES_OF_CODE);
	}

	@Override
	public Number measure(final MetricData data, final Def_Altstep altstep) {
		final Counter c = new Counter(0);
		altstep.accept(new CounterVisitor(c) {
			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof Def_Altstep) {
					return V_CONTINUE;
				}
				if (node instanceof StatementBlock) {
					count.set(((LargeLocation) ((StatementBlock) node).getLocation()).getEndLine());
				}
				return V_SKIP;
			}
		});
		return c.val() - altstep.getLocation().getLine() + 1;
	}
}
