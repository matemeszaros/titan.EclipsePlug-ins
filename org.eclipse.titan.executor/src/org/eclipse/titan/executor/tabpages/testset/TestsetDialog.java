/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

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
public final class TestsetDialog extends Dialog {
	private final String title;
	private String testsetName;
	private Label testsetNameLabel;
	private Text testsetNameText;

	public TestsetDialog(final Shell shell, final String title) {
		super(shell);
		this.title = title;
		testsetName = "";
	}

	public void setTestsetName(final String name) {
		testsetName = name;
	}

	public String getTestsetName() {
		return testsetName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		if (null != title) {
			shell.setText(title);
		}
	}

	private void validate() {
		if (null == getButton(IDialogConstants.OK_ID)) {
			return;
		}
		getButton(IDialogConstants.OK_ID).setEnabled(0 != testsetNameText.getText().length());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		testsetName = testsetNameText.getText();
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		testsetNameLabel = new Label(container, SWT.NONE);
		testsetNameLabel.setText("name:");
		testsetNameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		testsetNameLabel.setToolTipText("The name of the testset");

		testsetNameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		testsetNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		testsetNameText.setText(testsetName);

		testsetNameLabel.setSize(testsetNameLabel.getSize().x, testsetNameText.getSize().y);

		testsetNameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				validate();
			}

		});

		Dialog.applyDialogFont(container);
		validate();
		return container;
	}
}
