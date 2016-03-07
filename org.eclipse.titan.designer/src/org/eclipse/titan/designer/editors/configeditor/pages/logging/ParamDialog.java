/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.PluginSpecificParam;

/**
 * @author Balazs Andor Zalanyi
 * */
public class ParamDialog extends TitleAreaDialog {

	static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][0-9A-Za-z_]*");
	static final Pattern VALUE_PATTERN = Pattern.compile("^\"[^\"]*\"$");

	private boolean isAddition;

	private Text name;
	private Text value;
	private String nameField;
	private String valueField;

	private LogParamEntry logentry;
	private PluginSpecificParam param;

	public ParamDialog(final Shell parentShell, final LogParamEntry logentry, final PluginSpecificParam param) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.PRIMARY_MODAL);
		isAddition = param == null;
		this.logentry = logentry;
		this.param = param;
	}

	public String getName() {
		return nameField;
	}

	public String getValue() {
		return valueField;
	}

	@Override
	protected Control createContents(final Composite parent) {
		Control contents = super.createContents(parent);
		if (isAddition) {
			setTitle("Add a new parameter");
			getButton(OK).setEnabled(false);
		} else {
			setTitle("Edit a parameter");
		}
		if (!isAddition) {
			if (param.getParamName() != null) {
				name.setText(param.getParamName());
			}
			if (param.getValue().getText() != null) {
				value.setText(param.getValue().getText());
			}
		}
		setMessage("Please enter the parameter name and the value", IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		area.setLayout(layout);
		area.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		Label label1 = new Label(area, SWT.NONE);
		label1.setText("Name");
		name = new Text(area, SWT.SINGLE | SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				nameField = name.getText();
				validate();
			}
		});
		Label label2 = new Label(area, SWT.NONE);
		label2.setText("Value");
		value = new Text(area, SWT.SINGLE | SWT.BORDER);
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		value.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				valueField = value.getText();
				if (valueField == null) {
					valueField = "";
				}
				validate();
			}
		});
		return area;
	}

	/** Validate the contents of the dialog */
	private void validate() {
		if (nameField == null || !NAME_PATTERN.matcher(nameField).matches()) {
			getButton(OK).setEnabled(false);
			setErrorMessage("The name of the parameter must be a valid identifier.");
			return;
		}
		if (valueField == null || !VALUE_PATTERN.matcher(valueField).matches()) {
			getButton(OK).setEnabled(false);
			setErrorMessage("The value of the parameter must be a valid string.");
			return;
		}
		if (nameField.length() > 0) {
			if (isAddition) {
				List<PluginSpecificParam> params = logentry.getPluginSpecificParam();
				if (params != null) {
					Iterator<PluginSpecificParam> i = params.iterator();
					while (i.hasNext()) {
						PluginSpecificParam p = i.next();
						if (nameField.equals(p.getParamName())) {
							getButton(OK).setEnabled(false);
							setErrorMessage("A parameter with this name already exists!");
							return;
						}
					}
				}
			}
		}

		getButton(OK).setEnabled((nameField.length() > 0) && (valueField != null && valueField.length() > 0));
		setErrorMessage(null);
	}
}
