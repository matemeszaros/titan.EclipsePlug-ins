/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.fieldeditors;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.utils.preferences.PreferenceUtils;

public class MutableComboFieldEditor extends FieldEditor {

	private Combo comboBox;

	private Button addButton;
	private  Button removeButton;

	private IInputValidator inputValidator = null;

	private final Set<IItemListener> itemListeners = new LinkedHashSet<IItemListener>();

	public interface IItemListener {
		void itemAdded(final String item);
		void itemRemoved(final String item);
	}

	/**
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param items
	 * @param parent
	 */
	public MutableComboFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
	}

	/**
	 * Wrapper of {@link Combo#getItems()}
	 */
	public String[] getItems() {
		return comboBox.getItems();
	}

	/**
	 * Wrapper of {@link Combo#getItemCount()}
	 */
	public int getItemCount() {
		return comboBox.getItemCount();
	}

	/**
	 * Wrapper of {@link Combo#getSelectionIndex()}
	 */
	public int getSelectionIndex() {
		return comboBox.getSelectionIndex();
	}

	/**
	 * Wrapper of {@link Combo#getItem(int)}
	 */
	public String getItem(final int index) {
		return comboBox.getItem(index);
	}

	/**
	 * Wrapper of {@link Combo#select(int)}
	 */
	public void select(final int index) {
		comboBox.select(index);
	}

	/**
	 * Adds an item to the combo box and notifies the listeners
	 * @param item The item to add
	 */
	public void addItem(final String item) {
		final String[] oldvalue = new String[comboBox.getItems().length];
		System.arraycopy(comboBox.getItems(), 0, oldvalue, 0, comboBox.getItems().length);
		comboBox.add(item);
		if (!removeButton.getEnabled()) {
			removeButton.setEnabled(true);
		}

		setPresentsDefaultValue(false);
		fireValueChanged(VALUE, oldvalue, comboBox.getItems());
		fireItemAddedListeners(item);
	}

	/**
	 * Removes an item from the combo box and notifies the listeners
	 * @param index The index of the item to remove
	 */
	public void removeItem(final int index) {
		if (index < 0 || index >= comboBox.getItemCount()) {
			return;
		}
		final String[] oldvalue = new String[comboBox.getItems().length];
		System.arraycopy(comboBox.getItems(), 0, oldvalue, 0, comboBox.getItems().length);
		final String removedItem = comboBox.getItem(index);
		comboBox.remove(index);
		if (comboBox.getItemCount() == 0) {
			removeButton.setEnabled(false);
		}

		setPresentsDefaultValue(false);
		fireValueChanged(VALUE, oldvalue, comboBox.getItems());
		fireItemRemovedListeners(removedItem);
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) this.comboBox.getLayoutData()).horizontalSpan = numColumns;
		((GridData) this.addButton.getLayoutData()).horizontalSpan = 1;
		((GridData) this.removeButton.getLayoutData()).horizontalSpan = 1;
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {

		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		control.setLayoutData(gd);

		comboBox = new Combo(parent, SWT.READ_ONLY);
		comboBox.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = numColumns;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		comboBox.setLayoutData(data);

		Composite buttons = new Composite(parent, SWT.NONE);
		GridData buttonsData = new GridData();
		buttonsData.horizontalSpan = numColumns;
		buttonsData.grabExcessHorizontalSpace = true;
		buttonsData.horizontalAlignment = SWT.FILL;
		buttons.setLayoutData(buttonsData);
		GridLayout gridLayout = new GridLayout(2, false);
		buttons.setLayout(gridLayout);


		addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");
		addButton.setLayoutData(new GridData());
		addButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(final Event event) {
				InputDialog dialog = new InputDialog(new Shell(),
						"New Item",
						"",
						"",
						inputValidator);

				dialog.open();

				if (dialog.getReturnCode() == Window.OK) {
					addItem(dialog.getValue());
				}
			}
		});

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setLayoutData(new GridData());
		removeButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				removeItem(comboBox.getSelectionIndex());
			}
		});
	}

	private void fireItemAddedListeners(final String item) {
		for (IItemListener listener : itemListeners) {
			listener.itemAdded(item);
		}
	}

	private void fireItemRemovedListeners(final String item) {
		for (IItemListener listener : itemListeners) {
			listener.itemRemoved(item);
		}
	}

	@Override
	protected void doLoad() {
		comboBox.setItems(PreferenceUtils.deserializeFromString(getPreferenceStore().getString(getPreferenceName())).toArray(new String[0]));
		if (comboBox.getItemCount() > 0) {
			comboBox.select(0);
		}
	}

	@Override
	protected void doLoadDefault() {
		comboBox.setItems(PreferenceUtils.deserializeFromString(getPreferenceStore().getDefaultString(getPreferenceName())).toArray(new String[0]));
		if (comboBox.getItemCount() > 0) {
			comboBox.select(0);
		}
	}

	@Override
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(), PreferenceUtils.serializeToString(Arrays.asList(comboBox.getItems())));
	}

	@Override
	public int getNumberOfControls() {
		return 3;
	}

	public void setInputValidator(final IInputValidator validator) {
		this.inputValidator = validator;
	}

	public void addSelectionListener(final SelectionListener listener) {
		comboBox.addSelectionListener(listener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		comboBox.removeSelectionListener(listener);
	}

	public void addItemListener(final IItemListener listener) {
		itemListeners.add(listener);
	}

	public void removeItemListener(final IItemListener listener) {
		itemListeners.remove(listener);
	}

	public void reload() {
		doLoad();
	}
}
