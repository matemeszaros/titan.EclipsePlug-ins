/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.titan.executor.executors.TreeLeaf;

/**
 * @author Kristof Szabados
 * */
public final class TestsetTreeDropTargetListener extends DropTargetAdapter {
	private final TreeViewer testsetViewer;
	private final TestSetTab testsetTab;

	public TestsetTreeDropTargetListener(final TreeViewer testsetViewer, final TestSetTab testsetTab) {
		this.testsetViewer = testsetViewer;
		this.testsetTab = testsetTab;
	}

	@Override
	public void dragEnter(final DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT
				&& 0 == (event.operations & (DND.DROP_MOVE | DND.DROP_COPY))) {
			event.detail = DND.DROP_NONE;
		}
	}

	@Override
	public void dragOperationChanged(final DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT
				&& 0 == (event.operations & (DND.DROP_MOVE | DND.DROP_COPY))) {
			event.detail = DND.DROP_NONE;
		}
	}

	@Override
	public void dragOver(final DropTargetEvent event) {
		if (null == event.item) {
			event.feedback = DND.FEEDBACK_SCROLL;
			event.detail = DND.DROP_NONE;
		} else {
			TreeLeaf element = (TreeLeaf) event.item.getData();
			if (element instanceof TestsetTreeElement) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
			} else {
				event.feedback = DND.FEEDBACK_INSERT_BEFORE | DND.FEEDBACK_SCROLL;
			}
			if (event.detail == DND.DROP_NONE) {
				if (0 != (event.operations & DND.DROP_MOVE)) {
					event.detail = DND.DROP_MOVE;
				} else if (0 != (event.operations & DND.DROP_COPY)) {
					event.detail = DND.DROP_COPY;
				}
			}
		}
	}

	@Override
	public void drop(final DropTargetEvent event) {
		if (!TestcaseTransfer.getInstance().isSupportedType(event.currentDataType)
				|| null == event.item) {
			return;
		}

		final TreeLeaf element = (TreeLeaf) event.item.getData();
		final TestCaseTreeElement[] treeElements = (TestCaseTreeElement[]) event.data;
		if (element instanceof TestsetTreeElement) {
			for (TestCaseTreeElement treeElement : treeElements) {
				((TestsetTreeElement) element).addChildToEnd(treeElement);
			}
			testsetViewer.refresh(element);
		} else {
			for (TestCaseTreeElement treeElement : treeElements) {
				((TestsetTreeElement) element.parent()).addChildBefore(treeElement, element);
			}
			testsetViewer.refresh(element.parent());
		}
		testsetTab.update();
	}
}
