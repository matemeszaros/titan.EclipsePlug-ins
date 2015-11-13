/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.include;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;

/**
 * @author Kristof Szabados
 * */
public final class IncludeSectionDropTargetListener implements DropTargetListener {

	private TableViewer viewer;
	private ConfigEditor editor;

	public IncludeSectionDropTargetListener(final TableViewer viewer, final ConfigEditor editor) {
		this.viewer = viewer;
		this.editor = editor;
	}

	@Override
	public void dragEnter(final DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT) {
			if ((event.operations & (DND.DROP_MOVE | DND.DROP_COPY)) == 0) {
				event.detail = DND.DROP_NONE;
			}
		}
	}

	@Override
	public void dragLeave(final DropTargetEvent event) {
	}

	@Override
	public void dragOperationChanged(final DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT) {
			if ((event.operations & (DND.DROP_MOVE | DND.DROP_COPY)) == 0) {
				event.detail = DND.DROP_NONE;
			}
		}
	}

	@Override
	public void dragOver(final DropTargetEvent event) {
		if (event.item == null) {
			event.feedback = DND.FEEDBACK_SCROLL;
			event.detail = DND.DROP_NONE;
		} else {
			if (event.item.getData() instanceof LocationAST) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
			} else {
				event.feedback = DND.FEEDBACK_INSERT_BEFORE | DND.FEEDBACK_SCROLL;
			}
			if (event.detail == DND.DROP_NONE) {
				if ((event.operations & DND.DROP_MOVE) != 0) {
					event.detail = DND.DROP_MOVE;
				} else if ((event.operations & DND.DROP_COPY) != 0) {
					event.detail = DND.DROP_COPY;
				}
			}
		}
	}

	@Override
	public void drop(final DropTargetEvent event) {
		if (IncludeItemTransfer.getInstance().isSupportedType(event.currentDataType)) {
			if (event.item != null && viewer.getInput() != null) {
				IncludeSectionHandler includeSectionHandler = (IncludeSectionHandler) viewer.getInput();
				LocationAST element = (LocationAST) event.item.getData();
				LocationAST[] items = (LocationAST[]) event.data;

				int baseindex = includeSectionHandler.getFiles().indexOf(element);

				LocationAST oldSibling = element.getNextSibling();
				if (items.length > 0) {
					element.setNextSibling(items[0]);
					for (int i = 0; i < items.length - 1; i++) {
						items[i].setNextSibling(items[i + 1]);
						includeSectionHandler.getFiles().add(++baseindex, items[i]);
					}
					items[items.length - 1].setNextSibling(oldSibling);
					includeSectionHandler.getFiles().add(++baseindex, items[items.length - 1]);
				}

				viewer.refresh(true);
				editor.setDirty();
			}
		}
	}

	@Override
	public void dropAccept(final DropTargetEvent event) {
	}

}
