/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.titan.designer.AST.IOutlineElement;

/**
 * @author Kristof Szabados
 * */
public final class OutlineViewSorter extends ViewerComparator {

	private boolean categorize = false;
	private boolean sortByName = false;

	@Override
	public int category(final Object element) {
		if (categorize && element instanceof IOutlineElement) {
			return ((IOutlineElement) element).category();
		}

		return super.category(element);
	}

	public void setCategorizing(final boolean enabled) {
		this.categorize = enabled;
	}

	public void setSortByName(final boolean sortByName) {
		this.sortByName = sortByName;
	}

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if (sortByName) {
			return super.compare(viewer, e1, e2);
		}

		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		if (!(e1 instanceof IOutlineElement) || !(e2 instanceof IOutlineElement)) {
			return 0;
		}

		IOutlineElement o1 = (IOutlineElement) e1;
		IOutlineElement o2 = (IOutlineElement) e2;

		return o1.getIdentifier().getLocation().getOffset() - o2.getIdentifier().getLocation().getOffset();
	}
}
