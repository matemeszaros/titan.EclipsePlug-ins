/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kristof Szabados
 * */
public class MyFileListControl extends MyListControl {
	private final String basePath;

	public MyFileListControl(final Composite parent, final String basePath, final String title, final String itemDescription) {
		super(parent, title, itemDescription);
		this.basePath = basePath;
	}

	@Override
	protected void addNewItem() {
		FileListItemDialog dialog = new FileListItemDialog(list.getShell(), basePath, "Add new " + itemDescription, itemDescription, "");
		if (dialog.open() != Window.OK) {
			return;
		}

		String newItem = dialog.getItem();

		if (newItem != null && newItem.length() > 0) {
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.add(newItem, index + 1);
				list.setSelection(index + 1);
			} else {
				int size = list.getItemCount();
				list.add(newItem, size);
				list.setSelection(size);
			}
		}

		selectionChanged();
	}

	@Override
	protected void editSelectedItem() {
		int index = list.getSelectionIndex();

		if (index != -1) {
			String item = list.getItem(index);

			FileListItemDialog dialog = new FileListItemDialog(list.getShell(), basePath, "Edit " + itemDescription, itemDescription,
					item);
			if (dialog.open() != Window.OK) {
				return;
			}

			String newItem = dialog.getItem();
			if (newItem != null && newItem.length() > 0 && !newItem.equals(item)) {
				list.setItem(index, newItem);
				selectionChanged();
			}
		}
	}
}
