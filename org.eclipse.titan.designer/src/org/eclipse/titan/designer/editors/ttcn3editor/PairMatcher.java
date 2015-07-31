/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.titan.designer.editors.GeneralPairMatcher;
import org.eclipse.titan.designer.editors.Pair;
import org.eclipse.titan.designer.editors.ttcnppeditor.PartitionScanner;

/**
 * @author Kristof Szabados
 * */
public final class PairMatcher extends GeneralPairMatcher {
	public PairMatcher(final Pair[] pairs) {
		this.pairs = pairs;
	}

	@Override
	protected String getPertitioning() {
		return PartitionScanner.TTCN3_PARTITIONING;
	}
}
