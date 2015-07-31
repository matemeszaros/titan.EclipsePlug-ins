/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.fieldeditors;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.titan.common.utils.preferences.PreferenceUtils;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;

public class StringListEditor extends TitanListEditor {

	private IInputValidator inputValidator;

	/**
	 * Creates a new path field editor 
	 */
	protected StringListEditor() {
	}

	
	/**
	 * Creates a path field editor.
	 * 
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param showUpAndDown true if the up and down should be shown, otherwise false
	 */
	public StringListEditor(final String name, final String labelText, final Composite parent, final boolean showUpAndDown) {
		super(name, labelText, parent, showUpAndDown);
	}
	
	public StringListEditor(final String name, final String labelText, final Composite parent, final boolean showUpAndDown, final boolean hideColor) {
		super(name, labelText, parent, showUpAndDown, hideColor);
	}
	
	@Override
	protected void init(final String name, final String text) {
		super.init(name, text);

		inputValidator = new IInputValidator() {
			@Override
			public String isValid(final String newText) {
				final String tempText = newText.trim();
				if (Constants.MTC.equals(tempText)) {
					return Messages.getString("StringListEditor.5"); //$NON-NLS-1$
				}
				if (Constants.SUT.equals(tempText)) {
					return Messages.getString("StringListEditor.5"); //$NON-NLS-1$
				}
				if (tempText.length() > Constants.MAX_COMP_NAME) { 
					return Messages.getString("StringListEditor.3"); //$NON-NLS-1$
				}
				if ("".equals(tempText)) { //$NON-NLS-1$
					return ""; //$NON-NLS-1$
				}
				String[] elements = getElements();
				for (String element : elements) {
					if (element.contentEquals(tempText)) {
						return Messages.getString("StringListEditor.6"); //$NON-NLS-1$
					}
				}
				return null;
			}	
		};
	}

	public void setInputValidator(final IInputValidator inputValidator) {
		this.inputValidator = inputValidator;
	}

	@Override
	protected String createList(final String[] items) {
		return PreferenceUtils.serializeToString(Arrays.asList(items));
	}

	@Override
	protected String getNewInputObject() {

		InputDialog dialog = new InputDialog(getShell(),
				Messages.getString("StringListEditor.0"), //$NON-NLS-1$
				Messages.getString("StringListEditor.1"), //$NON-NLS-1$
				Messages.getString("StringListEditor.2"), //$NON-NLS-1$
				inputValidator);
		dialog.open();

		return dialog.getValue();
	}

	@Override
	protected String[] parseString(final String stringList) {
		final List<String> tmp = PreferenceUtils.deserializeFromString(stringList);
		return tmp.toArray(new String[tmp.size()]);
	}

}
