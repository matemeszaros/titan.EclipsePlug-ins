/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kristof Szabados
 * */
public final class TestsetTreeDragSourceListener implements DragSourceListener {
	private final TreeViewer testsetViewer;

	public TestsetTreeDragSourceListener(final TreeViewer testsetViewer) {
		this.testsetViewer = testsetViewer;
	}

	@Override
	public void dragFinished(final DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) testsetViewer.getSelection();
		if (!selection.isEmpty()) {
			testsetViewer.getTree().setRedraw(false);
			if (event.detail == DND.DROP_MOVE) {
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object element = it.next();
					if (element instanceof TestCaseTreeElement) {
						((TestsetTreeElement) ((TestCaseTreeElement) element).parent()).remove((TestCaseTreeElement) element);
						((TestCaseTreeElement) element).dispose();
					}
				}
			}
			testsetViewer.getTree().setRedraw(true);
			testsetViewer.refresh();
		}
	}

	@Override
	public void dragSetData(final DragSourceEvent event) {
		if (TestcaseTransfer.getInstance().isSupportedType(event.dataType)) {
			IStructuredSelection selection = (IStructuredSelection) testsetViewer.getSelection();
			List<TestCaseTreeElement> testcases = new ArrayList<TestCaseTreeElement>();
			if (!selection.isEmpty()) {
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object element = it.next();
					if (element instanceof TestCaseTreeElement) {
						testcases.add((TestCaseTreeElement) element);
					}
				}
				event.data = testcases.toArray(new TestCaseTreeElement[testcases.size()]);
			}
		}
	}

	@Override
	public void dragStart(final DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) testsetViewer.getSelection();
		event.doit = !selection.isEmpty() && !(selection.getFirstElement() instanceof TestsetTreeElement);
	}
}
