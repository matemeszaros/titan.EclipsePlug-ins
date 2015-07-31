/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kristof Szabados
 * */
public class RegexpEntryDialog extends Dialog {
	private static final String TITLE = "Regular expression";

	private String title;
	private String entry;
	private Label entryLabel;
	private Text entryText;
	private Label errorLabel;

	public RegexpEntryDialog(final Shell shell) {
		super(shell);
		this.title = TITLE;
		entry = "";
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(final String entry) {
		this.entry = entry;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		if (title != null) {
			newShell.setText(title);
		}
	}

	private void validate(final String newText) {
		try {
			Pattern.compile(newText);
			errorLabel.setVisible(false);

			Button button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(true);
			}
		} catch (PatternSyntaxException e) {
			errorLabel.setVisible(true);
			Button button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(false);
			}
		}
	}

	@Override
	protected void okPressed() {
		entry = entryText.getText();

		super.okPressed();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite panel = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createNameArea(panel);

		Dialog.applyDialogFont(container);
		validate("");
		return container;
	}

	protected void createNameArea(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setToolTipText("tooltip");

		entryLabel = new Label(parent, SWT.NONE);
		entryLabel.setText("text");
		entryLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		entryLabel.setToolTipText("tooltip");

		entryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		entryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		entryText.setText(entry);
		entryText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				validate(entryText.getText());
			}
		});

		entryLabel.setSize(entryLabel.getSize().x, entryText.getSize().y);

		errorLabel = new Label(parent, SWT.NONE);
		errorLabel.setText("error");
		errorLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		errorLabel.setToolTipText("error tooltip");
		errorLabel.setVisible(false);
	}
}
