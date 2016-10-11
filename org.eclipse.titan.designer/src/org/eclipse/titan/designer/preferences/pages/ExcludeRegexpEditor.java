/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;

/**
 * @author Kristof Szabados
 * */
public class ExcludeRegexpEditor extends FieldEditor {

	/**
	 * The list widget; <code>null</code> if none (before creation or after
	 * disposal).
	 */
	private List list;

	/**
	 * The button box containing the Add, Remove, Up, and Down buttons;
	 * <code>null</code> if none (before creation or after disposal).
	 */
	private Composite buttonBox;

	/**
	 * The Add button.
	 */
	private Button addButton;

	/**
	 * The Edit button.
	 */
	private Button editButton;

	/**
	 * The Copy button.
	 */
	private Button copyButton;

	/**
	 * The Remove button.
	 */
	private Button removeButton;

	/**
	 * The selection listener.
	 */
	private SelectionListener selectionListener;


	public ExcludeRegexpEditor(final String name, final String labelText, final Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/**
	 * Notifies that the Add button has been pressed.
	 */
	private void addPressed() {
		setPresentsDefaultValue(false);
		String input = getNewInputObject();

		if (input != null) {
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.add(input, index + 1);
			} else {
				list.add(input);
			}
			selectionChanged();
		}
	}

	/**
	 * Notifies that the Edit button has been pressed.
	 */
	private void editPressed() {
		setPresentsDefaultValue(false);

		int index = list.getSelectionIndex();
		if (index >= 0) {
			String input = list.getItem(index);
			String output = getEditInputObject(input);
			list.setItem(index, output);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the Copy button has been pressed.
	 */
	private void copyPressed() {
		setPresentsDefaultValue(false);

		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.add(list.getItem(index));
		}
		selectionChanged();
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) list.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 * 
	 * @param box
	 *                the box for the buttons
	 */
	private void createButtons(final Composite box) {
		addButton = createPushButton(box, "New...");
		editButton = createPushButton(box, "Edit");
		copyButton = createPushButton(box, "Copy");
		removeButton = createPushButton(box, "Remove");
	}

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent
	 *                the parent control
	 * @param key
	 *                the resource name used to supply the button's label
	 *                text
	 * @return Button
	 */
	private Button createPushButton(final Composite parent, final String name) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(name);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == editButton) {
					editPressed();
				} else if (widget == copyButton) {
					copyPressed();
				} else if (widget == list) {
					selectionChanged();
				}
			}
		};
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		list = getListControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		list.setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		if (list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
	}

	@Override
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
	}

	@Override
	protected void doStore() {
		String newValue = createList(list.getItems());
		if (newValue != null) {
			String oldValue = getPreferenceStore().getString(getPreferenceName());
			getPreferenceStore().setValue(getPreferenceName(), newValue);
			fireValueChanged(VALUE, oldValue, newValue);
		}
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove,
	 * Up, and Down button.
	 * 
	 * @param parent
	 *                the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(final Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent event) {
					addButton = null;
					removeButton = null;
					editButton = null;
					copyButton = null;
					buttonBox = null;
				}
			});

		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *                the parent control
	 * @return the list control
	 */
	public List getListControl(final Composite parent) {
		if (list == null) {
			list = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
			list.setFont(parent.getFont());
			list.addSelectionListener(getSelectionListener());
			list.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent event) {
					list = null;
				}
			});
		} else {
			checkParent(list, parent);
		}
		return list;
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns this field editor's selection listener. The listener is
	 * created if nessessary.
	 * 
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null) {
			createSelectionListener();
		}
		return selectionListener;
	}

	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call
	 * this method.
	 * </p>
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null) {
			return null;
		}
		return addButton.getShell();
	}

	/**
	 * Notifies that the Remove button has been pressed.
	 */
	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.remove(index);
			selectionChanged();
		}
	}

	/**
	 * Invoked when the selection in the list has changed.
	 * 
	 * <p>
	 * The default implementation of this method utilizes the selection
	 * index and the size of the list to toggle the enablement of the up,
	 * down and remove buttons.
	 * </p>
	 * 
	 * <p>
	 * Sublcasses may override.
	 * </p>
	 * 
	 * @since 3.5
	 */
	protected void selectionChanged() {

		int index = list.getSelectionIndex();
		int size = list.getItemCount();

		removeButton.setEnabled(index >= 0);
		editButton.setEnabled(size >= 1 && index >= 0);
		copyButton.setEnabled(size >= 1 && index >= 0 && index < size - 1);
	}

	@Override
	public void setFocus() {
		if (list != null) {
			list.setFocus();
		}
	}

	@Override
	public void setEnabled(final boolean enabled, final Composite parent) {
		super.setEnabled(enabled, parent);
		getListControl(parent).setEnabled(enabled);
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	/**
	 * Combines the given list of items into a single string. This method is
	 * the converse of <code>parseString</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param items
	 *                the list of items
	 * @return the combined string
	 * @see #parseString
	 */
	protected String createList(final String[] items) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < items.length; i++) {
			String item = items[i];

			if (i != 0) {
				builder.append('#');
			}
			builder.append(item.replace("#", "\\#"));
		}

		return builder.toString();
	}

	/**
	 * Creates and returns a new item for the list.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return a new item
	 */
	protected String getNewInputObject() {
		RegexpEntryDialog dialog = new RegexpEntryDialog(getShell());

		if (dialog.open() == Window.OK) {
			return dialog.getEntry();
		}

		return null;
	}

	/**
	 * Creates and returns a new item for the list.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return a new item
	 */
	protected String getEditInputObject(final String original) {
		RegexpEntryDialog dialog = new RegexpEntryDialog(getShell());
		dialog.setEntry(original);

		if (dialog.open() == Window.OK) {
			return dialog.getEntry();
		}

		return original;
	}

	/**
	 * Splits the given string into a list of strings. This method is the
	 * converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param stringList
	 *                the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected String[] parseString(final String stringList) {
		java.util.List<String> splittedList = ResourceExclusionHelper.intelligentSplit(stringList, '#', '\\');
		return splittedList.toArray(new String[splittedList.size()]);

	}
}
