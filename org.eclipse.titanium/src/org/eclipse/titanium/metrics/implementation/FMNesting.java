/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.DepthVisitor;

public class FMNesting extends BaseFunctionMetric {
	public FMNesting() {
		super(FunctionMetric.NESTING);
	}

	@Override
	public Number measure(final MetricData data, final Def_Function function) {
		final Counter count = new Counter(0);
		function.accept(new DepthVisitor(count));
		return count.val();
	}
}
