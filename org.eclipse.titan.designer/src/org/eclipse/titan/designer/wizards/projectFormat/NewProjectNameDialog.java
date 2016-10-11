/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
public class NewProjectNameDialog extends Dialog {
	private String name;
	private Text nameText;
	private Label verdict;

	private final ModifyListener modifyListener = new ModifyListener() {

		@Override
		public void modifyText(final ModifyEvent e) {
			validate();
		}
	};

	public NewProjectNameDialog(final Shell shell, final String name) {
		super(shell);
		if (shell != null) {
			setShellStyle(shell.getStyle() | SWT.RESIZE);
		}
		this.name = name;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Existing project found.");
	}

	public String getName() {
		return name;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite nameContainer = new Composite(parent, SWT.NONE);
		nameContainer.setLayout(new GridLayout(2, false));
		nameContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label nameLabel = new Label(nameContainer, SWT.NONE);
		nameLabel.setText("The name of the project to create: ");

		nameText = new Text(nameContainer, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(name);
		nameText.addModifyListener(modifyListener);

		verdict = new Label(container, SWT.NONE);
		verdict.setText("There is already a project with this name in the workspace.");

		Dialog.applyDialogFont(container);

		return container;
	}

	@Override
	protected Control createContents(final Composite parent) {
		Control temp = super.createContents(parent);
		validate();

		return temp;
	}

	private void validate() {
		if (nameText == null) {
			return;
		}

		name = nameText.getText();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (project.exists()) {
			verdict.setVisible(true);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		} else {
			verdict.setVisible(false);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}
}
