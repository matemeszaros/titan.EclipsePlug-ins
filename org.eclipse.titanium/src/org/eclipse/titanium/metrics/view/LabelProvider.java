/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titanium.metrics.MetricData;

/**
 * A label provider for the {@link MetricsView}.
 * 
 * @author poroszd
 * 
 */
class LabelProvider implements ITableLabelProvider, IColorProvider {
	private final MetricData data;

	public LabelProvider(final MetricData d) {
		data = d;
	}

	@Override
	public void addListener(final ILabelProviderListener listener) {
		//Do nothing
	}

	@Override
	public void dispose() {
		//Do nothing
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {
		//Do nothing
	}

	@Override
	public Color getForeground(final Object element) {
		return null;
	}

	@Override
	public Color getBackground(final Object element) {
		Color c = null;
		if (element instanceof IContentNode) {
			switch (((IContentNode) element).getRiskLevel(data)) {
			case HIGH:
				c = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
				break;
			case LOW:
				c = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
				break;
			default:
				break;
			}
		}
		return c;
	}

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		return ((INode) element).getColumnText(data, columnIndex);
	}
}
