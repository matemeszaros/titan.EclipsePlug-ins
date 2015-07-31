/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A field editor for a combo box that allows the drop-down selection of one of
 * a list of items.
 * 
 * @author Kristof Szabados
 */
public final class ComboFieldEditor extends FieldEditor {

	/**
	 * The <code>Combo</code> widget.
	 */
	private Combo fCombo;

	/**
	 * The value (not the name) of the currently selected item in the Combo
	 * widget.
	 */
	private String fValue;

	/**
	 * The names (labels) and underlying values to populate the combo
	 * widget. These should be arranged as: { {name1, value1}, {name2,
	 * value2}, ...}
	 */
	private String[][] fEntryNamesAndValues;

	/**
	 * Create the combo box field editor.
	 * 
	 * @param name
	 *                the name of the preference this field editor works on
	 * @param labelText
	 *                the label text of the field editor
	 * @param entryNamesAndValues
	 *                the names (labels) and underlying values to populate
	 *                the combo widget. These should be arranged as: {
	 *                {name1, value1}, {name2, value2}, ...}
	 * @param parent
	 *                the parent composite
	 */
	public ComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		init(name, labelText);
		Assert.isTrue(checkArray(entryNamesAndValues));
		fEntryNamesAndValues = entryNamesAndValues;
		createControl(parent);
	}

	/**
	 * Checks whether given <code>String[][]</code> is of "type"
	 * <code>String[][2]</code>.
	 * 
	 * @param table
	 *                the String array-array to check.
	 * @return <code>true</code> if it is OK, and <code>false</code>
	 *         otherwise
	 */
	private boolean checkArray(final String[][] table) {
		if (table == null) {
			return false;
		}
		for (int i = 0; i < table.length; i++) {
			String[] array = table[i];
			if (array == null || array.length != 2) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		if (numColumns > 1) {
			Control control = getLabelControl();
			int left = numColumns;
			if (control != null) {
				((GridData) control.getLayoutData()).horizontalSpan = 1;
				left = left - 1;
			}
			((GridData) fCombo.getLayoutData()).horizontalSpan = left;
		} else {
			Control control = getLabelControl();
			if (control != null) {
				((GridData) control.getLayoutData()).horizontalSpan = 1;
			}
			((GridData) fCombo.getLayoutData()).horizontalSpan = 1;
		}
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
		int comboC = 1;
		if (numColumns > 1) {
			comboC = numColumns - 1;
		}
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		control.setLayoutData(gd);
		control = getComboBoxControl(parent);
		gd = new GridData();
		gd.horizontalSpan = comboC;
		gd.horizontalAlignment = GridData.END;
		control.setLayoutData(gd);
		control.setFont(parent.getFont());
	}

	@Override
	protected void doLoad() {
		updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	@Override
	protected void doStore() {
		if (fValue == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}
		getPreferenceStore().setValue(getPreferenceName(), fValue);
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/*
	 * Lazily create and return the Combo control.
	 */
	private Combo getComboBoxControl(final Composite parent) {
		if (fCombo == null) {
			fCombo = new Combo(parent, SWT.READ_ONLY);
			fCombo.setFont(parent.getFont());
			for (int i = 0; i < fEntryNamesAndValues.length; i++) {
				fCombo.add(fEntryNamesAndValues[i][0], i);
			}

			fCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent evt) {
					String oldValue = fValue;
					String name = fCombo.getText();
					fValue = getValueForName(name);
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, fValue);
				}
			});
		}
		return fCombo;
	}

	/*
	 * Select the name in the combo widget that matches the specified value.
	 */
	public void setSelectedValue(final String value) {
		for (int i = 0; i < fEntryNamesAndValues.length; i++) {
			if (value.equals(fEntryNamesAndValues[i][1])) {
				fCombo.setText(fEntryNamesAndValues[i][0]);
				fValue = value;
				return;
			}
		}
	}

	/*
	 * Given the name (label) of an entry, return the corresponding value.
	 */
	private String getValueForName(final String name) {
		for (int i = 0; i < fEntryNamesAndValues.length; i++) {
			String[] entry = fEntryNamesAndValues[i];
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return fEntryNamesAndValues[0][0];
	}

	/*
	 * Set the name in the combo widget to match the specified value.
	 */
	private void updateComboForValue(final String value) {
		fValue = value;
		for (int i = 0; i < fEntryNamesAndValues.length; i++) {
			if (value.equals(fEntryNamesAndValues[i][1])) {
				fCombo.setText(fEntryNamesAndValues[i][0]);
				return;
			}
		}
		if (fEntryNamesAndValues.length > 0) {
			fValue = fEntryNamesAndValues[0][1];
			fCombo.setText(fEntryNamesAndValues[0][0]);
		}
	}

	/*
	 * Returns the actual value of the combo control
	 */
	public String getActualValue() {
		return fValue;
	}
}
