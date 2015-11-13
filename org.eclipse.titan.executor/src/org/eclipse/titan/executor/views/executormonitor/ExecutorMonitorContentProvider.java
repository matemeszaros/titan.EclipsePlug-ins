/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeBranch;
import org.eclipse.titan.executor.executors.ITreeLeaf;

import java.util.List;

/**
 * @author Kristof Szabados
 * */
public final class ExecutorMonitorContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public ITreeLeaf[] getChildren(final Object parentElement) {
		if (parentElement instanceof MainControllerElement) {
			final BaseExecutor executor = ((MainControllerElement) parentElement).executor();
			final List<ITreeLeaf> children = executor.mainControllerRoot().children();
			return children.toArray(new ITreeLeaf[children.size()]);
		} else if (parentElement instanceof ITreeBranch) {
			final List<ITreeLeaf> children = ((ITreeBranch) parentElement).children();
			return children.toArray(new ITreeLeaf[children.size()]);
		}
		return new ITreeBranch[] {};
	}

	@Override
	public ITreeLeaf[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public ITreeBranch getParent(final Object element) {
		if (element instanceof ITreeLeaf) {
			return ((ITreeLeaf) element).parent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof MainControllerElement) {
			final BaseExecutor executor = ((MainControllerElement) element).executor();
			return !executor.mainControllerRoot().children().isEmpty();
		} else if (element instanceof ITreeBranch) {
			return !((ITreeBranch) element).children().isEmpty();
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}
}
