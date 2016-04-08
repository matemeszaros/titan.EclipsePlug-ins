/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.designer.AST.IOutlineElement;

/**
 * @author Kristof Szabados
 * */
public final class OutlineContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IOutlineElement) {
			return ((IOutlineElement) parentElement).getOutlineChildren();
		} else if (parentElement instanceof List<?>) {
			return ((List<?>) parentElement).toArray();
		}

		return new Object[] {};
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof IOutlineElement) {
			Object[] children = ((IOutlineElement) element).getOutlineChildren();
			return children != null && children.length > 0;
		} else if (element instanceof List<?>) {
			return true;
		}

		return false;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		//Do nothing
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		//Do nothing
	}
}
