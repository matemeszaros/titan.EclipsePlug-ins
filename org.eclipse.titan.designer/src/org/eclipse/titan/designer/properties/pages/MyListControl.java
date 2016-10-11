/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public class MyListControl {
	private ToolBar toolBar;
	private ToolItem addItem, deleteItem, editItem, moveUpItem, moveDownItem;
	private Label title;
	protected List list;
	protected String itemDescription;

	// images
	private final Image imgAdd = ImageCache.getImage("list-add.gif");
	private final Image imgAddDisabled = ImageCache.getImage("list-add_d.gif");
	private final Image imgDelete = ImageCache.getImage("list-delete.gif");
	private final Image imgDeleteDisabled = ImageCache.getImage("list-delete_d.gif");
	private final Image imgEdit = ImageCache.getImage("list-edit.gif");
	private final Image imgEditDisabled = ImageCache.getImage("list-edit_d.gif");
	private final Image imgMoveUp = ImageCache.getImage("list-moveup.gif");
	private final Image imgMoveUpDisabled = ImageCache.getImage("list-moveup_d.gif");
	private final Image imgMoveDown = ImageCache.getImage("list-movedown.gif");
	private final Image imgMoveDownDisabled = ImageCache.getImage("list-movedown_d.gif");

	/**
	 * The constructor of the class.
	 * 
	 * @param parent
	 *                the parent composite.
	 * @param title
	 *                the title of this list control.
	 * @param itemDescription
	 *                the description of an item this list can contain, as
	 *                it should be displayed in the add/edit dialog.
	 * */
	public MyListControl(final Composite parent, final String title, final String itemDescription) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout form1 = new GridLayout();
		form1.numColumns = 1;
		form1.horizontalSpacing = 0;
		form1.verticalSpacing = 0;
		form1.marginHeight = 0;
		form1.marginWidth = 0;
		mainComposite.setLayout(form1);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// title composite
		Composite titleComposite = new Composite(mainComposite, SWT.BORDER);
		GridLayout titleform = new GridLayout(2, false);
		titleform.horizontalSpacing = 0;
		titleform.verticalSpacing = 0;
		titleform.marginHeight = 0;
		titleform.marginWidth = 0;
		titleComposite.setLayout(titleform);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = IDialogConstants.BUTTON_BAR_HEIGHT;
		titleComposite.setLayoutData(data);

		this.title = new Label(titleComposite, SWT.NONE | SWT.BOLD);
		this.title.setText(title);
		GridData titleGrid = new GridData(GridData.FILL_HORIZONTAL);
		this.title.setLayoutData(titleGrid);

		// button panel
		Composite buttonPanel = new Composite(titleComposite, SWT.NONE);
		GridLayout form2 = new GridLayout();
		form2.numColumns = 5;
		form2.horizontalSpacing = 0;
		form2.verticalSpacing = 0;
		form2.marginWidth = 0;
		form2.marginHeight = 0;
		buttonPanel.setLayout(form2);

		GridData buttonGrid = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		buttonPanel.setLayoutData(buttonGrid);

		// toolbar
		toolBar = new ToolBar(buttonPanel, SWT.HORIZONTAL | SWT.RIGHT | SWT.FLAT);
		// add toolbar item
		addItem = new ToolItem(toolBar, SWT.PUSH);
		addItem.setImage(imgAdd);
		addItem.setDisabledImage(imgAddDisabled);
		addItem.setToolTipText("Add");
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addNewItem();
			}
		});
		// delete toolbar item
		deleteItem = new ToolItem(toolBar, SWT.PUSH);
		deleteItem.setImage(imgDelete);
		deleteItem.setDisabledImage(imgDeleteDisabled);
		deleteItem.setToolTipText("Delete");
		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeSelectedItem();
			}
		});
		// edit toolbar item
		editItem = new ToolItem(toolBar, SWT.PUSH);
		editItem.setImage(imgEdit);
		editItem.setDisabledImage(imgEditDisabled);
		editItem.setToolTipText("Edit");
		editItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				editSelectedItem();
			}
		});
		// moveup toolbar item
		moveUpItem = new ToolItem(toolBar, SWT.PUSH);
		moveUpItem.setImage(imgMoveUp);
		moveUpItem.setDisabledImage(imgMoveUpDisabled);
		moveUpItem.setToolTipText("Move up");
		moveUpItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				moveUpSelectedItem();
			}
		});
		// movedown toolbar item
		moveDownItem = new ToolItem(toolBar, SWT.PUSH);
		moveDownItem.setImage(imgMoveDown);
		moveDownItem.setDisabledImage(imgMoveDownDisabled);
		moveDownItem.setToolTipText("Move down");
		moveDownItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				moveDownSelectedItem();
			}
		});

		// list control
		list = new List(mainComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData listGrid = new GridData(GridData.FILL_BOTH);
		// force the list to be no wider than the title bar
		Point preferredSize = titleComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		listGrid.widthHint = preferredSize.x;
		listGrid.heightHint = preferredSize.y * 3;
		listGrid.horizontalSpan = 2;
		list.setLayoutData(listGrid);
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				selectionChanged();
			}
		});

		// Add a double-click event handler
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				editSelectedItem();
			}
		});
		// Add a delete event handler
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeSelectedItem();
				} else {
					super.keyPressed(e);
				}
			}
		});

		this.itemDescription = itemDescription;
		selectionChanged();
	}

	/**
	 * Disposes the SWT resources allocated by this control.
	 */
	public void dispose() {
		if (toolBar == null) {
			return;
		}

		toolBar.dispose();
		addItem.dispose();
		deleteItem.dispose();
		editItem.dispose();
		moveUpItem.dispose();
		moveDownItem.dispose();
		title.dispose();
		list.dispose();
	}

	/** @return the contents of the list */
	public final String[] getValues() {
		if (list == null) {
			return new String[] {};
		}

		return list.getItems();
	}

	/**
	 * Sets the contents of the list.
	 * 
	 * @param values
	 *                the new contents to be set.
	 * */
	public final void setValues(final String[] values) {
		if (values == null || list == null) {
			return;
		}

		String[] items = list.getItems();
		boolean same = items.length == values.length;
		if (same) {
			for (int i = 0; same && i < items.length; i++) {
				same &= items[i].equals(values[i]);
			}
		}

		if (same) {
			return;
		}

		list.removeAll();
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null && values[i].length() > 0) {
				list.add(values[i]);
			}
		}

		selectionChanged();
	}

	/**
	 * Handle the change in the selection of the list. Used to
	 * enable/disable the toolbar items.
	 * */
	protected final void selectionChanged() {
		int size = list.getItemCount();
		int index = list.getSelectionIndex();
		addItem.setEnabled(true);
		editItem.setEnabled(size > 0 && index != -1);
		deleteItem.setEnabled(size > 0 && index != -1);
		moveUpItem.setEnabled(size > 1 && index > 0);
		moveDownItem.setEnabled(size > 1 && index < size - 1 && index != -1);
	}

	/**
	 * Add a new item to the list.
	 * */
	protected void addNewItem() {
		ListItemDialog dialog = new ListItemDialog(list.getShell(), "Add new " + itemDescription, itemDescription, "");
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

	/**
	 * Remove the item selected in the list.
	 * */
	private void removeSelectedItem() {
		int index = list.getSelectionIndex();

		if (index != -1) {
			list.remove(index);
		}

		selectionChanged();
	}

	/**
	 * Edit the item selected in the list.
	 * */
	protected void editSelectedItem() {
		int index = list.getSelectionIndex();

		if (index != -1) {
			String item = list.getItem(index);

			ListItemDialog dialog = new ListItemDialog(list.getShell(), "Edit " + itemDescription, itemDescription, item);
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

	/**
	 * Move up one place in the list the item that is selected.
	 * */
	private final void moveUpSelectedItem() {
		int index = list.getSelectionIndex();
		String item = list.getItem(index);
		String previous = list.getItem(index - 1);
		list.setItem(index - 1, item);
		list.setItem(index, previous);
		list.setSelection(index - 1);

		selectionChanged();
	}

	/**
	 * Move down one place in the list the item that is selected.
	 * */
	private final void moveDownSelectedItem() {
		int index = list.getSelectionIndex();
		String item = list.getItem(index);
		String previous = list.getItem(index + 1);
		list.setItem(index + 1, item);
		list.setItem(index, previous);
		list.setSelection(index + 1);

		selectionChanged();
	}

	/**
	 * Enables the receiver if the argument is <code>true</code>, and
	 * disables it otherwise. A disabled control is typically not selectable
	 * from the user interface and draws with an inactive or "grayed" look.
	 * 
	 * @see Control#setEnabled(boolean)
	 * 
	 * @param enabled
	 *                the new enabled state
	 * */
	public final void setEnabled(final boolean enabled) {
		toolBar.setEnabled(enabled);
		title.setEnabled(enabled);
		list.setEnabled(enabled);

		if (enabled) {
			selectionChanged();
		} else {
			addItem.setEnabled(enabled);
			deleteItem.setEnabled(enabled);
			editItem.setEnabled(enabled);
			moveUpItem.setEnabled(enabled);
			moveDownItem.setEnabled(enabled);
		}
	}
}
