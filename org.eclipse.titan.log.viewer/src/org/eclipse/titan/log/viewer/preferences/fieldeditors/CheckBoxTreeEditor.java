/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.fieldeditors;

import java.util.Set;
import java.util.SortedMap;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.utils.ImportExportUtils;

public class CheckBoxTreeEditor extends FieldEditor {
	
	// Variables
	private Tree checkTree;
	private String prefName;
	private int numberOfNodes = 0;
	
	/**
	 * Constructor
	 * 
	 * @param prefName the preference store key name
	 * @param labelText the label text
	 * @param treeContent the tree content (2 levels)
	 * @param parent the parent
	 */
	public CheckBoxTreeEditor(final String prefName, final String labelText, final SortedMap<String, String[]> treeContent, final Composite parent) {
		this.prefName = prefName;
		init(prefName, labelText);
		createControl(parent);
		setTreeContent(treeContent);
	}
	
	@Override
	protected void createControl(final Composite parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = getNumberOfControls();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = HORIZONTAL_GAP;
        parent.setLayout(layout);
        doFillIntoGrid(parent, layout.numColumns);
    }

	@Override
	protected void adjustForNumColumns(final int numColumns) {
        Control control = getLabelControl();
        ((GridData) control.getLayoutData()).horizontalSpan = numColumns;
        ((GridData) this.checkTree.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
        Control control = getLabelControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        control.setLayoutData(gd);

		this.checkTree = new Tree(parent, SWT.CHECK | SWT.BORDER);
		this.checkTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.checkTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.detail != SWT.CHECK
						|| !(e.item instanceof TreeItem)) {
					return;
				}

				TreeItem item = (TreeItem) e.item;
				// Check if item has a parent
				TreeItem parent = item.getParentItem();

				// Item is a child
				if (parent != null) {
					TreeItem[] children = parent.getItems();
					int numberOfChildren = children.length;
					int checkedChildren = countChecked(children);
					updateParent(parent, numberOfChildren, checkedChildren);


					// Item is a parent
				} else {
					item.setGrayed(false);
					boolean checked = item.getChecked();
					TreeItem[] children = item.getItems();
					setChecked(children, checked);
				}
				valueChanged();
			}

			private void updateParent(TreeItem parent, int numberOfChildren, int checkedChildren) {
				// Check if all children checked
				if (checkedChildren == numberOfChildren) {
					parent.setGrayed(false);
					parent.setChecked(true);
				} else if (checkedChildren == 0) {
					// Check if all children unchecked
					parent.setGrayed(false);
					parent.setChecked(false);
				} else {
					// Otherwise...
					parent.setChecked(true);
					parent.setGrayed(true);
				}
			}
		});
        
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = numColumns;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = convertVerticalDLUsToPixels(this.checkTree, 40);
		this.checkTree.setLayoutData(gridData);
	}

	/**
	 * @param treeContent the tree content
	 */
	private void setTreeContent(final SortedMap<String, String[]> treeContent) {
	    // Turn off drawing to avoid flicker
		this.checkTree.setRedraw(false);
		Set<String> parents = treeContent.keySet();
		int index = 0;
		int tmpNumberOfNodes = 0;
		for (String parent : parents) {
			TreeItem parentItem = new TreeItem(this.checkTree, SWT.NONE);
			parentItem.setText(parent);
			this.checkTree.setData(parent, index++);
			tmpNumberOfNodes++;
			String[] strings = treeContent.get(parent);
			for (int i = 0; i < strings.length; i++) {
				String child = strings[i];
				TreeItem childItem = new TreeItem(parentItem, SWT.NONE);
				childItem.setText(child);
				this.checkTree.setData(parent + PreferenceConstants.SILENT_EVENTS_UNDERSCORE + child, i);
				tmpNumberOfNodes++;
			}
		}
		this.numberOfNodes = tmpNumberOfNodes;
		// Turn drawing back on!
	    this.checkTree.setRedraw(true);
	}
	
    private void valueChanged() {
        TreeItem[] newItems = this.checkTree.getItems();
       	fireValueChanged(VALUE, null, newItems);
    }
	
	/**
	 * Deselects all tree items (parents and children)
	 */
	public void deselectAll() {
		TreeItem[] parents = this.checkTree.getItems();
		for (TreeItem parent : parents) {
			parent.setChecked(false);
			if (parent.getGrayed()) {
				parent.setGrayed(false);
			}
			TreeItem[] children = parent.getItems();
			setChecked(children, false);
		}
	}
	
	/**
	 * Selects all tree items (parents and children)
	 */
	public void selectAll() {
		TreeItem[] parents = this.checkTree.getItems();
		for (TreeItem parent : parents) {
			parent.setChecked(true);
			if (parent.getGrayed()) {
				parent.setGrayed(false);
			}
			TreeItem[] children = parent.getItems();
			setChecked(children, true);
		}
	}

	@Override
	protected void doLoadDefault() {
		// Get default values from preference store
		String prefDefValues = getPreferenceStore().getDefaultString(this.prefName);
		if (prefDefValues.length() > 0) {
			updateCheckedState(prefDefValues);
			updateGrayedState();
		}
	}

	@Override
	protected void doLoad() {
		// Stored values from preference store
		String prefValues = getPreferenceStore().getString(this.prefName);
		if (prefValues.length() > 0) {
			updateCheckedState(prefValues);
			updateGrayedState();
		}
	}

	/**
	 * Updates the setChecked state of all tree items (parents and children)
	 * 
	 * @param prefValues the preference store value for which 
	 * tree items that should be checked
	 */
	private void updateCheckedState(final String prefValues) {
		String[] categories = prefValues.split(PreferenceConstants.PREFERENCE_DELIMITER);
		for (String category : categories) {
			String[] currCategory = category.split(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM);

			if (currCategory.length > 1) {
				String currKey = currCategory[0];
				boolean currValue = Boolean.valueOf(currCategory[1]);

				if (currKey.contains(PreferenceConstants.SILENT_EVENTS_UNDERSCORE)) {
					// CAT + SUB CAT
					String cat = currKey.split(PreferenceConstants.SILENT_EVENTS_UNDERSCORE)[0];
					int parentIndex = (Integer) this.checkTree.getData(cat);
					int childIndex = (Integer) this.checkTree.getData(currKey);
					this.checkTree.getItem(parentIndex).getItem(childIndex).setChecked(currValue);
				} else {
					// CAT
					this.checkTree.getItem((Integer) this.checkTree.getData(currKey)).setChecked(currValue);
				}
			}
		}
	}
	
	/**
	 * Updates the setGrayed state of the parents
	 * A parent is grayed if one or more, but not all of the children is checked  
	 */
	private void updateGrayedState() {
		TreeItem[] parents = this.checkTree.getItems();
		for (TreeItem parent : parents) {
			TreeItem[] children = parent.getItems();
			int numOfCheckChildren = countChecked(children);
			if ((numOfCheckChildren > 0) && (numOfCheckChildren < children.length)) {
				parent.setGrayed(true);
			} else {
				parent.setGrayed(false);
			}
		}
	}

	@Override
	protected void doStore() {
		String prefValues = ImportExportUtils.arrayToString(getElements(), PreferenceConstants.PREFERENCE_DELIMITER);
		getPreferenceStore().setValue(this.prefName, prefValues);
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
	
	@Override
	public void setEnabled(final boolean enabled, final Composite parent) {
		this.checkTree.setEnabled(enabled);
		super.setEnabled(enabled, parent);
	}

	/**
	 * Getter for elements 
	 *  
	 * @return the elements as a string array
	 */
	public String[] getElements() {
		TreeItem[] parents = this.checkTree.getItems();
		String[] prefValues = new String[this.numberOfNodes];
		int currPrefIndex = 0;
		for (TreeItem currParent : parents) {
			prefValues[currPrefIndex++] = currParent.getText()
					+ PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM
					+ Boolean.toString(currParent.getChecked());

			// Traverse current parents children
			TreeItem[] children = currParent.getItems();
			for (TreeItem currChild : children) {
				prefValues[currPrefIndex++] = currParent.getText()
						+ PreferenceConstants.SILENT_EVENTS_UNDERSCORE
						+ currChild.getText()
						+ PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM
						+ Boolean.toString(currChild.getChecked());
			}
		}
		return prefValues;
	}

	private static int countChecked(TreeItem[] items) {
		int checked = 0;
		for (TreeItem item : items) {
			if (item.getChecked()) {
				checked++;
			}
		}
		return checked;
	}

	private static void setChecked(TreeItem[] items, boolean checked) {
		for (TreeItem item : items) {
			item.setChecked(checked);
		}
	}
}
