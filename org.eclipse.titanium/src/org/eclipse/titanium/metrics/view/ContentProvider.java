/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.MetricGroup;

/**
 * The content provider for the {@link MetricsView}.
 * <p>
 * The main input element is a {@link ProjectMetricData} object. From that a
 * tree is built up, which has {@link INode} nodes. These nodes store any
 * information that is necessary for displaying, and also to get its children.
 * </p>
 * 
 * @author poroszd
 * 
 */
class ContentProvider implements ITreeContentProvider {
	private MetricData data;

	@Override
	public void dispose() {
		//Do nothing
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		//Do nothing
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (!(inputElement instanceof MetricData)) {
			throw new IllegalArgumentException();
		}

		data = (MetricData) inputElement;

		final List<? super INode> children = new ArrayList<INode>();
		for (final MetricGroup t : MetricGroup.values()) {
			final RootNode n = new RootNode(t);
			if (n.hasChildren(data)) {
				children.add(n);
			}
		}
		return children.toArray();
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		return ((INode) parentElement).getChildren(data);
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return ((INode) element).hasChildren(data);
	}
}
