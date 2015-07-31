/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.fieldeditors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Class created for getting the radiobutton state, this 
 * functionallity is not present in base class (RadioGroupFieldEditor)
 */
public class TitanRadioGroupFieldEditor extends FieldEditor {


    /**
     * List of radio button entries of the form [label,value].
     */
    private String[][] labelsAndValues;

    /**
     * Number of columns into which to arrange the radio buttons.
     */
    private int numColumns;

    /**
     * Indent used for the first column of the radion button matrix.
     */
    private int indent = HORIZONTAL_GAP;

    /**
     * The current value, or <code>null</code> if none.
     */
    private String value;

    /**
     * The box of radio buttons, or <code>null</code> if none
     * (before creation and after disposal).
     */
    private Composite radioBox;

    /**
     * The radio buttons, or <code>null</code> if none
     * (before creation and after disposal).
     */
    private Button[] radioButtons;

    /**
     * Whether to use a Group control.
     */
    private boolean useGroup;

    /**
     * Creates a new radio group field editor 
     */
    protected TitanRadioGroupFieldEditor() {
    }

    /**
     * Creates a radio group field editor.  
     * This constructor does not use a <code>Group</code> to contain the radio buttons.
     * It is equivalent to using the following constructor with <code>false</code>
     * for the <code>useGroup</code> argument.
     * <p>
     * Example usage:
     * <pre>
     *		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
     *			"GeneralPage.DoubleClick", resName, 1,
     *			new String[][] {
     *				{"Open Browser", "open"},
     *				{"Expand Tree", "expand"}
     *			},
     *          parent);	
     * </pre>
     * </p>
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param numColumns the number of columns for the radio button presentation
     * @param labelAndValues list of radio button [label, value] entries;
     *  the value is returned when the radio button is selected
     * @param parent the parent of the field editor's control
     */
    public TitanRadioGroupFieldEditor(final String name, final String labelText, final int numColumns,
    		final String[][] labelAndValues, final Composite parent) {
        this(name, labelText, numColumns, labelAndValues, parent, false);
    }

    /**
     * Creates a radio group field editor.
     * <p>
     * Example usage:
     * <pre>
     *		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
     *			"GeneralPage.DoubleClick", resName, 1,
     *			new String[][] {
     *				{"Open Browser", "open"},
     *				{"Expand Tree", "expand"}
     *			},
     *          parent,
     *          true);	
     * </pre>
     * </p>
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param numColumns the number of columns for the radio button presentation
     * @param labelAndValues list of radio button [label, value] entries;
     *  the value is returned when the radio button is selected
     * @param parent the parent of the field editor's control
     * @param useGroup whether to use a Group control to contain the radio buttons
     */
    public TitanRadioGroupFieldEditor(final String name, final String labelText, final int numColumns,
    		final String[][] labelAndValues, final Composite parent, final boolean useGroup) {
        init(name, labelText);
        this.labelsAndValues = labelAndValues;
        this.numColumns = numColumns;
        this.useGroup = useGroup;
        createControl(parent);
    }

    /**
     * Method for retrieving the selected value or ""
     * @param label
     * @return value
     */
    public String getSelectedLabelValue() {

    	for (int i = 0; i < this.radioButtons.length; i++) {
    		if (this.radioButtons[i].getSelection()) {
				return returnLabelValue(this.radioButtons[i].getText());
			}
    	}

    	return ""; //$NON-NLS-1$
    }
    private String returnLabelValue(final String label) {
    	for (int i = 0; i < this.labelsAndValues.length; i++) {		
    		if (this.labelsAndValues[i][0].compareTo(label) == 0) {
    			return this.labelsAndValues[i][1];
    		}		
    	}
    	return ""; //$NON-NLS-1$
    }

    @Override
	protected void adjustForNumColumns(final int numColumns) {
        Control control = getLabelControl();
        if (control != null) {
            ((GridData) control.getLayoutData()).horizontalSpan = numColumns;
        }
        ((GridData) this.radioBox.getLayoutData()).horizontalSpan = numColumns;
    }

    @Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
        if (this.useGroup) {
            Control control = getRadioBoxControl(parent);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            control.setLayoutData(gd);
        } else {
            Control control = getLabelControl(parent);
            GridData gd = new GridData();
            gd.horizontalSpan = numColumns;
            control.setLayoutData(gd);
            control = getRadioBoxControl(parent);
            gd = new GridData();
            gd.horizontalSpan = numColumns;
            gd.horizontalIndent = this.indent;
            control.setLayoutData(gd);
        }

    }

	@Override
	protected void doLoad() {
        updateValue(getPreferenceStore().getString(getPreferenceName()));
    }

	@Override
	protected void doLoadDefault() {
        updateValue(getPreferenceStore().getDefaultString(getPreferenceName()));
    }

	@Override
	protected void doStore() {
        if (this.value == null) {
            getPreferenceStore().setToDefault(getPreferenceName());
            return;
        }

        getPreferenceStore().setValue(getPreferenceName(), this.value);
    }

	@Override
	public int getNumberOfControls() {
        return 1;
    }

    /**
     * Returns this field editor's radio group control.
     * @param parent The parent to create the radioBox in
     * @return the radio group control
     */
    public Composite getRadioBoxControl(final Composite parent) {
        if (this.radioBox == null) {

            Font font = parent.getFont();

            if (this.useGroup) {
                Group group = new Group(parent, SWT.NONE);
                group.setFont(font);
                String text = getLabelText();
                if (text != null) {
					group.setText(text);
				}
                this.radioBox = group;
                GridLayout layout = new GridLayout();
                layout.horizontalSpacing = HORIZONTAL_GAP;
                layout.numColumns = this.numColumns;
                this.radioBox.setLayout(layout);
            } else {
                this.radioBox = new Composite(parent, SWT.NONE);
                GridLayout layout = new GridLayout();
                layout.marginWidth = 0;
                layout.marginHeight = 0;
                layout.horizontalSpacing = HORIZONTAL_GAP;
                layout.numColumns = this.numColumns;
                this.radioBox.setLayout(layout);
                this.radioBox.setFont(font);
            }

            this.radioButtons = new Button[this.labelsAndValues.length];
            for (int i = 0; i < this.labelsAndValues.length; i++) {
                Button radio = new Button(this.radioBox, SWT.RADIO | SWT.LEFT);
                this.radioButtons[i] = radio;
                String[] labelAndValue = this.labelsAndValues[i];
                radio.setText(labelAndValue[0]);
                radio.setData(labelAndValue[1]);
                radio.setFont(font);
                radio.addSelectionListener(new SelectionAdapter() {
                	@Override
					public void widgetSelected(final SelectionEvent event) {
                        String oldValue = TitanRadioGroupFieldEditor.this.value;
                        TitanRadioGroupFieldEditor.this.value = (String) event.widget.getData();
                        setPresentsDefaultValue(false);
                        fireValueChanged(VALUE, oldValue, TitanRadioGroupFieldEditor.this.value);
                    }
                });
            }
            this.radioBox.addDisposeListener(new DisposeListener() {
                @Override
				public void widgetDisposed(final DisposeEvent event) {
                    TitanRadioGroupFieldEditor.this.radioBox = null;
                    TitanRadioGroupFieldEditor.this.radioButtons = null;
                }
            });
        } else {
            checkParent(this.radioBox, parent);
        }
        return this.radioBox;
    }

    /**
     * Sets the indent used for the first column of the radion button matrix.
     *
     * @param indent the indent (in pixels)
     */
    public void setIndent(final int indent) {
        if (indent < 0) {
			this.indent = 0;
		} else {
			this.indent = indent;
		}
    }

    /**
     * Select the radio button that conforms to the given value.
     *
     * @param selectedValue the selected value
     */
    private void updateValue(final String selectedValue) {
        this.value = selectedValue;
        if (this.radioButtons == null) {
			return;
		}

        if (this.value != null) {
            boolean found = false;
            for (int i = 0; i < this.radioButtons.length; i++) {
                Button radio = this.radioButtons[i];
                boolean selection = false;
                if (((String) radio.getData()).equals(this.value)) {
                    selection = true;
                    found = true;
                }
                radio.setSelection(selection);
            }
            if (found) {
				return;
			}
        }

        // We weren't able to find the value. So we select the first
        // radio button as a default.
        if (this.radioButtons.length > 0) {
            this.radioButtons[0].setSelection(true);
            this.value = (String) this.radioButtons[0].getData();
        }
        return;
    }

    @Override
	public void setEnabled(final boolean enabled, final Composite parent) {
        if (!this.useGroup) {
			super.setEnabled(enabled, parent);
		}
        for (int i = 0; i < this.radioButtons.length; i++) {
            this.radioButtons[i].setEnabled(enabled);
        }

    }

	
}
