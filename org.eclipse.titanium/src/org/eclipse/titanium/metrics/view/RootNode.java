/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.MetricGroup;
import org.eclipse.titanium.metrics.ProjectMetric;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;

/**
 * One of the root nodes of the tree, corresponding to a metric type (project,
 * module, function, etc.).
 * 
 * @author poroszd
 * 
 */
class RootNode implements INode {
	private final MetricGroup type;
	private boolean initialized;
	private Object[] children;

	public RootNode(final MetricGroup type) {
		this.type = type;
		initialized = false;
	}

	@Override
	public Object[] getChildren(final MetricData data) {
		if (initialized) {
			return children;
		}

		final List<? super IContentNode> c = new ArrayList<IContentNode>();
		final List<IMetricEnum> subs = type.getMetrics();
		final Iterator<IMetricEnum> it = subs.iterator();
		while (it.hasNext()) {
			final IMetricEnum m = it.next();
			if (!PreferenceManager.isEnabledOnView(m)) {
				it.remove();
			}
		}
		if(MetricGroup.PROJECT == type) {
			for (final IMetricEnum m : subs) {
				c.add(new ProjectNode((ProjectMetric) m));
			}
		} else {
			for (final IMetricEnum m : subs) {
				final IContentNode n = new ProjectStatNode(m);
				if (n.hasChildren(data)) {
					c.add(n);
				}
			}
		}

		children = c.toArray();
		initialized = true;
		return children;
	}

	@Override
	public boolean hasChildren(final MetricData data) {
		if (!initialized) {
			getChildren(data);
		}

		return children.length != 0;
	}

	@Override
	public String getColumnText(final MetricData data, final int i) {
		if (i == 0) {
			return type.getGroupName() + " metrics";
		}

		return null;
	}
}
