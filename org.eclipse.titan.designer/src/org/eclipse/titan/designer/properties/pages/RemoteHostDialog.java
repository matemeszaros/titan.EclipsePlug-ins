/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class RemoteHostDialog extends Dialog {
	private static final String TITLE = "Remote build host";
	private static final String ACTIVE_TEXT = "Active";
	private static final String ACTIVE_TOOLTIP = "The host only takes part in the build if it is active";
	private static final String NAME_TEXT = "Name:";
	private static final String DEFAULT_NAME = "temporary name";
	private static final String NAME_TOOLTIP = "The name of the host (used to identify the running process)";
	private static final String COMMAND_TEXT = "Command:";
	private static final String DEFAULT_COMMAND = "rsh <[user@]hostname> -n 'cd <working directory>; make dep; make'";
	private static final String COMMAND_TOOLTIP = "The command to execute" + System.getProperty("line.separator")
			+ "For example: rsh <[user@]hostname> -n 'cd <working directory>; make dep; make'";

	private String name;
	private String command;
	private Label nameLabel;
	private Label commandLabel;
	private Text nameText;
	private Text commandText;
	private Button activeButton;
	private boolean active;

	private String title;

	private Image[] images = { ImageCache.getImageDescriptor("question.gif").createImage(),
			ImageCache.getImageDescriptor("host.gif").createImage(), ImageCache.getImageDescriptor("command.gif").createImage() };

	public RemoteHostDialog(final Shell shell) {
		super(shell);
		this.title = TITLE;
		this.active = false;
		this.name = DEFAULT_NAME;
		this.command = DEFAULT_COMMAND;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	public void setActive(final boolean value) {
		active = value;
	}

	public boolean getActive() {
		return active;
	}

	public void setName(final String value) {
		name = value;
	}

	public String getName() {
		return name;
	}

	public void setCommand(final String value) {
		command = value;
	}

	public String getCommand() {
		return command;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		if (title != null) {
			newShell.setText(title);
		}
		newShell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				for (int i = 0; i < images.length; i++) {
					images[i].dispose();
				}
			}
		});
	}

	private void validate() {
		if (null != getButton(IDialogConstants.OK_ID)) {
			getButton(IDialogConstants.OK_ID).setEnabled(commandText.getText().length() != 0);
		}
	}

	@Override
	protected void okPressed() {
		active = activeButton.getSelection();
		name = nameText.getText();
		command = commandText.getText();
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		createActiveArea(container);

		Composite panel = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createNameArea(panel);
		createCommandArea(panel);

		Dialog.applyDialogFont(container);
		validate();
		return container;
	}

	protected void createActiveArea(final Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(panel, SWT.NONE);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setImage(images[0]);
		label.setToolTipText(ACTIVE_TOOLTIP);

		activeButton = new Button(panel, SWT.CHECK);
		activeButton.setText(ACTIVE_TEXT);
		activeButton.setToolTipText(ACTIVE_TOOLTIP);
		activeButton.setSelection(active);
	}

	protected void createNameArea(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setImage(images[1]);
		label.setToolTipText(NAME_TOOLTIP);

		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText(NAME_TEXT);
		nameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		nameLabel.setToolTipText(NAME_TOOLTIP);

		nameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(name);

		nameLabel.setSize(nameLabel.getSize().x, nameText.getSize().y);
	}

	protected void createCommandArea(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setImage(images[2]);
		label.setToolTipText(COMMAND_TOOLTIP);

		commandLabel = new Label(parent, SWT.NONE);
		commandLabel.setText(COMMAND_TEXT);
		commandLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		commandLabel.setToolTipText(COMMAND_TOOLTIP);

		commandText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		commandText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		commandText.setText(command);

		commandLabel.setSize(commandLabel.getSize().x, commandText.getSize().y);
	}
}
