/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.fieldeditors;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.utils.Messages;

public abstract class TitanListEditor extends FieldEditor {
	/**
	 * The list widget; <code>null</code> if none (before creation or after
	 * disposal).
	 */
	private List list;

	/**
	 * Cache for items
	 */
	private String[] oldItems;

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
	 * The Remove button.
	 */
	private Button removeButton;

	/**
	 * The Up button.
	 */
	private Button upButton;

	/**
	 * The Down button.
	 */
	private Button downButton;

	/**
	 * The selection listener.
	 */
	private SelectionListener selectionListener;

	private final Set<SelectionListener> registeredSelectionListeners = new HashSet<SelectionListener>();

	private TitanColorFieldEditor colorEditor;

	private Map<String, RGB> colors;

	/**
	 * Creates a new list field editor
	 */
	protected TitanListEditor() {
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	protected TitanListEditor(final String name, final String labelText,
			final Composite parent) {
		init(name, labelText);
		createControl(parent);

		this.downButton.setEnabled(false);
		this.upButton.setEnabled(false);
		this.removeButton.setEnabled(false);
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	protected TitanListEditor(final String name, final String labelText,
			final Composite parent, final boolean hideUpAndDown) {
		init(name, labelText);
		createControl(parent);

		this.downButton.setVisible(hideUpAndDown);
		this.upButton.setVisible(hideUpAndDown);
		this.removeButton.setEnabled(false);
	}

	protected TitanListEditor(final String name, final String labelText,
			final Composite parent, final boolean hideUpAndDown,
			final boolean hideColorEditor) {
		init(name, labelText);
		createControl(parent);

		this.downButton.setVisible(hideUpAndDown);
		this.upButton.setVisible(hideUpAndDown);
		this.removeButton.setEnabled(false);
		if (!hideColorEditor) {
			this.list.removeAll();
			createColors(this.buttonBox);
			this.colorEditor.setEnabled(false, this.buttonBox);
			this.colors = new HashMap<String, RGB>();
			// move the invisible buttons down
			this.downButton.moveBelow(this.colorEditor.getColorSelector()
					.getButton());
			this.upButton.moveBelow(this.colorEditor.getColorSelector()
					.getButton());
			this.colorEditor.getColorSelector().addListener(
					new IPropertyChangeListener() {

						@Override
						public void propertyChange(
								final PropertyChangeEvent event) {
							RGB selectedColor = TitanListEditor.this.colorEditor
									.getColorSelector().getColorValue();
							String[] selection = TitanListEditor.this.list
									.getSelection();
							for (String string : selection) {
								TitanListEditor.this.colors.put(string,
										selectedColor);
							}
						}

					});
		}

	}

	/**
	 * Notifies that the Add button has been pressed.
	 */
	private void addPressed() {
		setPresentsDefaultValue(false);
		String input = getNewInputObject();

		if (input != null) {

			// make sure that leading and trailing white space is removed
			input = input.trim();

			// check if already added to the list
			if (this.list.indexOf(input) != -1) {
				TitanLogExceptionHandler.handleException(new UserException(input + "  " + Messages.getString("TitanListEditor.2")));
				return;
			}

			int index = this.list.getSelectionIndex();
			if (index >= 0) {
				this.list.add(input, index + 1);
			} else {
				this.list.add(input, 0);
			}
			selectionChanged();
		}
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) this.list.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	private void createButtons(final Composite box) {
		this.addButton = createPushButton(box, "ListEditor.add"); //$NON-NLS-1$
		this.addButton.setText(Messages.getString("TitanListEditor.3")); //$NON-NLS-1$
		this.removeButton = createPushButton(box, "ListEditor.remove"); //$NON-NLS-1$
		this.upButton = createPushButton(box, "ListEditor.up"); //$NON-NLS-1$
		this.downButton = createPushButton(box, "ListEditor.down"); //$NON-NLS-1$   
	}

	/**
	 * Creates a Color field editor, e.g color button in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	private void createColors(final Composite box) {
		// TODO: Check if label text can be removed
		this.colorEditor = new TitanColorFieldEditor(
				"COLOR_PREFERENCE", "", box); //$NON-NLS-1$  //$NON-NLS-2$
	}

	/**
	 * Combines the given list of items into a single string. This method is the
	 * converse of <code>parseString</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param items the list of items
	 * @return the combined string
	 * @see #parseString
	 */
	protected abstract String createList(String[] items);

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent the parent control
	 * @param key the resource name used to supply the button's label text
	 * @return Button
	 */
	private Button createPushButton(final Composite parent, final String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Widget widget = event.widget;
				String[] selection;
				if (TitanListEditor.this.list != null) {
					selection = TitanListEditor.this.list.getSelection();

					if (widget == TitanListEditor.this.addButton) {
						addPressed();
					} else if (widget == TitanListEditor.this.removeButton) {
						if (selection.length > 1) {
							removeMultipleElements(selection);
						} else {
							removePressed();
						}
					} else if (widget == TitanListEditor.this.upButton) {
						upPressed();
					} else if (widget == TitanListEditor.this.downButton) {
						downPressed();
					} else if (widget == TitanListEditor.this.list) {
						selectionChanged();
					}

					valueChanged();
					// if multiple select gray others than remove out
					if (selection.length > 1) {

						if (!containsMtsORSut(selection)) {
							TitanListEditor.this.removeButton.setEnabled(true);
							TitanListEditor.this.upButton.setEnabled(false);
							TitanListEditor.this.downButton.setEnabled(false);
							if (TitanListEditor.this.colorEditor != null) {
								TitanListEditor.this.colorEditor.setEnabled(
										true, TitanListEditor.this.buttonBox);
							}
						} else {
							TitanListEditor.this.removeButton.setEnabled(false);
							TitanListEditor.this.upButton.setEnabled(false);
							TitanListEditor.this.downButton.setEnabled(false);
							if (TitanListEditor.this.colorEditor != null) {
								TitanListEditor.this.colorEditor.setEnabled(
										false, TitanListEditor.this.buttonBox);
							}
						}
					}
				}
			}

			private boolean containsMtsORSut(final String[] element) {
				for (String anElement : element) {
					if ((anElement.equals(PreferenceConstants.SUT_DESCRIPTION))
							|| (anElement.equals(PreferenceConstants.MTC_DESCRIPTION))) {
						return true;
					}
				}
				return false;
			}
		};
	}

	private void removeMultipleElements(final String[] elements) {
		for (String element : elements) {
			this.list.remove(element);
		}
		selectionChanged();
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		this.list = getListControl(parent);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = numColumns;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = convertVerticalDLUsToPixels(this.list, 40);
		this.list.setLayoutData(gridData);

		this.buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		this.buttonBox.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {

		if (this.list != null) {
			list.removeAll();
			String s = getPreferenceStore().getString(getPreferenceName());
			if (this.colorEditor != null) {
				loadColors(s);
			} else {
				String[] array = parseString(s);
				for (String str : array) {
					this.list.add(str);
				}
			}
		}
	}

	@Override
	protected void doLoadDefault() {
		if (this.list == null) {
			return;
		}

		this.list.removeAll();
		String s = getPreferenceStore().getDefaultString(getPreferenceName());
		String[] array = parseString(s);
		for (String str : array) {
			this.list.add(str);
		}
	}

	@Override
	protected void doStore() {
		String s = null;
		String[] listItems = this.list.getItems();
		if (this.colorEditor != null) {
			s = createColorList(listItems);
		} else {
			s = createList(listItems);
		}
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	/**
	 * Notifies that the Down button has been pressed.
	 */
	private void downPressed() {
		swap(false);
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove, Up,
	 * and Down button.
	 * 
	 * @param parent the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(final Composite parent) {
		if (this.buttonBox == null) {
			this.buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			this.buttonBox.setLayout(layout);
			createButtons(this.buttonBox);
			this.buttonBox.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent event) {
					TitanListEditor.this.addButton = null;
					TitanListEditor.this.removeButton = null;
					TitanListEditor.this.upButton = null;
					TitanListEditor.this.downButton = null;
					TitanListEditor.this.colorEditor = null;
					TitanListEditor.this.buttonBox = null;
				}
			});

		} else {
			checkParent(this.buttonBox, parent);
		}

		selectionChanged();
		return this.buttonBox;
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent the parent control
	 * @return the list control
	 */
	public List getListControl(final Composite parent) {
		if (this.list == null) {
			this.list = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
					| SWT.H_SCROLL);
			this.list.setFont(parent.getFont());
			for (SelectionListener listener : registeredSelectionListeners) {
				this.list.addSelectionListener(listener);
			}
			this.list.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent event) {
					TitanListEditor.this.list = null;
				}
			});
		} else {
			checkParent(this.list, parent);
		}
		return this.list;
	}

	/**
	 * Creates and returns a new item for the list.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return a new item
	 */
	protected abstract String getNewInputObject();

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns this field editor's selection listener. The listener is created
	 * if required.
	 * 
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (this.selectionListener == null) {
			createSelectionListener();
		}
		return this.selectionListener;
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
		if (this.addButton == null) {
			return null;
		}
		return this.addButton.getShell();
	}

	/**
	 * Splits the given string into a list of strings. This method is the
	 * converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param stringList the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected abstract String[] parseString(String stringList);

	/**
	 * Notifies that the Remove button has been pressed.
	 */
	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = this.list.getSelectionIndex();

		if (index >= 0) {
			if (this.colorEditor != null) {
				String item = this.list.getItem(index);
				this.colors.remove(item);
			}
			this.list.remove(index);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the list selection has changed.
	 */
	private void selectionChanged() {

		int index = this.list.getSelectionIndex();
		int size = this.list.getItemCount();
		if (index != -1) {
			String item = this.list.getItem(index);

			if (item.equals(Messages.getString("TitanListEditor.0")) || item.equals(Messages.getString("TitanListEditor.1"))) {
				this.removeButton.setEnabled(false);
			} else {
				this.removeButton.setEnabled(index >= 0);
			}
			this.upButton.setEnabled((size > 1) && (index > 0));
			this.downButton.setEnabled((size > 1) && (index >= 0)
					&& (index < size - 1));
			if (this.colorEditor != null) {
				this.colorEditor.setEnabled(index >= 0, this.buttonBox);

				RGB rgb = this.colors.get(item);
				if (rgb != null) {
					this.colorEditor.getColorSelector().setColorValue(rgb);
				} else {
					RGB black = new RGB(0, 0, 0);
					this.colorEditor.getColorSelector().setColorValue(black);
				}
			}
		}

	}

	@Override
	public void setFocus() {
		if (this.list != null) {
			this.list.setFocus();
		}
	}

	/**
	 * Moves the currently selected item up or down.
	 * 
	 * @param up
	 *            <code>true</code> if the item should move up, and
	 *            <code>false</code> if it should move down
	 */
	private void swap(final boolean up) {
		setPresentsDefaultValue(false);
		int index = this.list.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			String[] selection = this.list.getSelection();
			this.list.remove(index);
			this.list.add(selection[0], target);
			this.list.setSelection(target);
		}
		selectionChanged();
	}

	/**
	 * Notifies that the Up button has been pressed.
	 */
	private void upPressed() {
		swap(true);
	}

	@Override
	public void setEnabled(final boolean enabled, final Composite parent) {
		super.setEnabled(enabled, parent);
		getListControl(parent).setEnabled(enabled);
		this.addButton.setEnabled(enabled);
		this.removeButton.setEnabled(enabled);
		this.upButton.setEnabled(enabled);
		this.downButton.setEnabled(enabled);
	}

	private void valueChanged() {

		String[] newItems = this.list.getItems();
		if (!Arrays.equals(newItems, this.oldItems)) {
			fireValueChanged(VALUE, this.oldItems, newItems);
			this.oldItems = newItems;
		}
	}

	/**
	 * Method for clearing thing the list editors widget list
	 */
	protected void doClear() {
		this.list.removeAll();
	}

	public void clear() {
		doClear();
	}

	/**
	 * Adds a value to the list
	 * 
	 * @param value
	 *            the value to add to the list
	 */
	public void addElementToList(final String value) {
		this.list.add(value);
	}

	/**
	 * Returns the elements in the list
	 * 
	 * @return the elements in the list
	 */
	public String[] getElements() {
		return this.list.getItems();
	}

	/*
	 * (non-Javadoc) Method declared on ListEditor. Creates a single string from
	 * the given array by separating each string with the appropriate
	 * OS-specific path separator.
	 */
	private String createColorList(final String[] listItems) {
		StringBuilder colorString = new StringBuilder("");
		for (String item : listItems) {
			RGB rgb = this.colors.get(item);
			if (rgb != null) {
				colorString.append(item);
				colorString.append(PreferenceConstants.KEYWORD_COLOR_SEPARATOR);
				colorString.append(rgb.red);
				colorString.append(PreferenceConstants.RGB_COLOR_SEPARATOR);
				colorString.append(rgb.green);
				colorString.append(PreferenceConstants.RGB_COLOR_SEPARATOR);
				colorString.append(rgb.blue);
				colorString.append(File.pathSeparator);

			} else {
				colorString.append(item);
				colorString.append(PreferenceConstants.KEYWORD_COLOR_SEPARATOR);
				colorString.append(File.pathSeparator);
			}
		}
		return colorString.toString();
	}

	/**
	 * Getter for colors
	 * 
	 * @return the colors
	 */
	public Map<String, RGB> getColors() {
		return this.colors;
	}

	/**
	 * Setter for the colors
	 * 
	 * @param colors the colors
	 */
	public void setColors(final Map<String, RGB> colors) {
		this.colors = colors;
	}

	private void loadColors(final String stringList) {
		StringTokenizer stringColors = new StringTokenizer(stringList, File.pathSeparator);

		while (stringColors.hasMoreElements()) {

			String stringColor = (String) stringColors.nextElement();
			String[] sc = stringColor.split(PreferenceConstants.KEYWORD_COLOR_SEPARATOR);
			if (sc.length > 1) {
				String item = sc[0];
				String colorString = sc[1];

				// Add string to list
				this.list.add(item);

				RGB rgb = null;
				if ((colorString != null) && (colorString.trim().length() > 0)) {
					String[] splitColor = colorString.split(PreferenceConstants.RGB_COLOR_SEPARATOR);
					int red = Integer.parseInt(splitColor[0]);
					int green = Integer.parseInt(splitColor[1]);
					int blue = Integer.parseInt(splitColor[2]);
					rgb = new RGB(red, green, blue);
				}
				if ((item != null) && (rgb != null)) {
					this.colors.put(item, rgb);
				}
			}
		}
	}

	public void addSelectionChangedListener(final SelectionListener listener) {
		list.addSelectionListener(listener);
		registeredSelectionListeners.add(listener);
	}

	public void removeSelectionChangedListener(final SelectionListener listener) {
		list.removeSelectionListener(listener);
		registeredSelectionListeners.remove(listener);
	}

	public String[] getSelection() {
		return list.getSelection();
	}
}
