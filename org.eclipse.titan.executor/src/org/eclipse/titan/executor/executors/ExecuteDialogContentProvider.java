/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.List;

/**
 * Simple content provider for the execute dialog.
 * 
 * @author Kristof Szabados
 * */
public final class ExecuteDialogContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public ITreeLeaf[] getChildren(final Object parentElement) {
		if (parentElement instanceof ITreeBranch) {
			final List<ITreeLeaf> temp = ((ITreeBranch) parentElement).children();
			return temp.toArray(new ITreeLeaf[temp.size()]);
		}
		return new TreeLeaf[] {};
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
		return element instanceof ITreeBranch && !((ITreeBranch) element).children().isEmpty();
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}

}
