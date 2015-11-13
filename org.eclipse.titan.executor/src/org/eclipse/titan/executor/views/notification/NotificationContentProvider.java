/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.notification;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;

import java.util.List;

/**
 * @author Kristof Szabados
 * */
public final class NotificationContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof LaunchElement) {
			List<ITreeLeaf> children = ((LaunchElement) inputElement).children();
			for (ITreeLeaf aChildren : children) {
				BaseExecutor executor = ((MainControllerElement) aChildren).executor();
				if (null != executor && null != executor.notifications()) {
					return executor.notifications().toArray();
				}
			}
		}
		return new String[] {};
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}
}
