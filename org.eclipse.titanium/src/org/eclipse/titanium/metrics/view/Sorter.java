/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.titanium.metrics.MetricData;

/**
 * A comparator for the {@link MetricsView}.
 * 
 * @author poroszd
 * 
 */
class Sorter extends ViewerComparator {
	private final MetricData data;

	public Sorter(final MetricData d) {
		data = d;
	}

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if (!(e1 instanceof IContentNode && e2 instanceof IContentNode)) {
			return 0;
		}

		return fineCompare((IContentNode) e1, (IContentNode) e2);
	}

	public int fineCompare(final IContentNode e1, final IContentNode e2) {
		final double d1 = e1.risk(data);
		final double d2 = e2.risk(data);
		return d1 < d2 ? 1 : ((d1 > d2) ? -1 : 0);
	}
}
