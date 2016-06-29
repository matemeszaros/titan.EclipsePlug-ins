/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
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

/**
 * @author Kristof Szabados
 * */
public class EditPathVariableDialog extends Dialog {
	private String name;
	private URI oldValue;
	private URI actualValue;
	private Text newValue;

	private final ModifyListener modifyListener = new ModifyListener() {

		@Override
		public void modifyText(final ModifyEvent e) {
			validate();
		}
	};

	public EditPathVariableDialog(final Shell shell, final String name, final URI oldValue, final URI actualValue) {
		super(shell);
		setShellStyle(shell.getStyle() | SWT.RESIZE);
		this.name = name;
		this.oldValue = oldValue;
		this.actualValue = actualValue;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Errors during loading path variables.");
	}

	public URI getActualValue() {
		return actualValue;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite nameContainer = new Composite(parent, SWT.NONE);
		nameContainer.setLayout(new GridLayout(2, false));
		nameContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(nameContainer, SWT.NONE);
		label.setText("The name of the path variable: ");
		Text text = new Text(nameContainer, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setText(name);
		text.setEditable(false);

		label = new Label(nameContainer, SWT.NONE);
		label.setText("The old value of the path variable: ");
		text = new Text(nameContainer, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (oldValue != null) {
			text.setText(oldValue.toString());
		}
		text.setEditable(false);

		label = new Label(nameContainer, SWT.NONE);
		label.setText("The new value of the path variable: ");
		newValue = new Text(nameContainer, SWT.SINGLE | SWT.BORDER);
		newValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (actualValue != null) {
			newValue.setText(actualValue.toString());
		}
		newValue.setEditable(true);
		newValue.addModifyListener(modifyListener);

		Dialog.applyDialogFont(container);

		return container;
	}

	private void validate() {
		String temp = newValue.getText();
		Path tempPath = new Path(temp);
		if (tempPath.isValidPath(temp)) {
			actualValue = URIUtil.toURI(tempPath);
			getButton(OK).setEnabled(true);
		} else {
			getButton(OK).setEnabled(false);
		}
	}
}
