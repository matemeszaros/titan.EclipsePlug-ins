/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class ExecutorMonitorLabelProvider extends LabelProvider {

	@Override
	public Image getImage(final Object element) {
		if (element instanceof LaunchElement) {
			return ImageCache.getImage("titan.gif");
		} else if (element instanceof InformationElement) {
			return ImageCache.getImage("information.gif");
		} else if (element instanceof HostControllerElement
				|| element instanceof MainControllerElement
				|| element instanceof ComponentElement) {
			return ImageCache.getImage("host.gif");
		}
		return super.getImage(element);
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof MainControllerElement) {
			if (((MainControllerElement) element).getTerminated()) {
				return "<terminated> " + ((MainControllerElement) element).name();
			}

			return ((MainControllerElement) element).name();
		} else if (element instanceof LaunchElement) {
			if (((LaunchElement) element).getTerminated()) {
				return "<terminated> " + ((LaunchElement) element).name();
			}

			return ((LaunchElement) element).name();
		} else if (element instanceof ITreeLeaf) {
			return ((ITreeLeaf) element).name();
		}

		return "Unknown: " + super.getText(element);
	}

}
