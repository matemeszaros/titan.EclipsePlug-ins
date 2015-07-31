/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.testexecution;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kristof Szabados
 * */
public final class TestExecutionContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		final List<Object> list = new ArrayList<Object>();
		if (inputElement instanceof LaunchElement) {
			final List<ITreeLeaf> children = ((LaunchElement) inputElement).children();
			for (ITreeLeaf aChildren : children) {
				final BaseExecutor executor = ((MainControllerElement) aChildren).executor();
				if (null != executor) {
					list.addAll(executor.executedTests());
				}
			}
		} else if (inputElement instanceof ArrayList<?>) {
			list.addAll((ArrayList<?>) inputElement);
		}

		return list.toArray();
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}

}
