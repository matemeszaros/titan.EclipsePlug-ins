/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.metrics.visitors.CCVisitor;
import org.eclipse.titanium.metrics.visitors.Counter;

public class TMCyclomaticComplexity extends BaseTestcaseMetric {
	public TMCyclomaticComplexity() {
		super(TestcaseMetric.CYCLOMATIC_COMPLEXITY);
	}

	@Override
	public Number measure(final MetricData data, final Def_Testcase testcase) {
		final Counter c = new Counter(0);
		testcase.accept(new CCVisitor(c));
		return c.val();
	}
}
