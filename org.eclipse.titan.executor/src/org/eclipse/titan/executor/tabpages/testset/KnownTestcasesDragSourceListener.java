/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

import java.util.Iterator;

/**
 * @author Kristof Szabados
 * */
public final class KnownTestcasesDragSourceListener extends DragSourceAdapter {
	private final TableViewer viewer;

	public KnownTestcasesDragSourceListener(final TableViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragSetData(final DragSourceEvent event) {
		if (TestcaseTransfer.getInstance().isSupportedType(event.dataType)) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

			if (!selection.isEmpty()) {
				final TestCaseTreeElement[] testcases = new TestCaseTreeElement[selection.size()];
				int i = 0;
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					testcases[i] = (TestCaseTreeElement) it.next();
					i++;
				}
				event.data = testcases;
			}
		}
	}

	@Override
	public void dragStart(final DragSourceEvent event) {
		event.doit = !viewer.getSelection().isEmpty();
	}
}
