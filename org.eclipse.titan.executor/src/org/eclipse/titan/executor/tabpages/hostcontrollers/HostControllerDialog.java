/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.hostcontrollers;

import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEHOSTEXECUTABLE;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEHOSTNAME;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEHOSTWORKIGNDIRECTORY;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEMCHOST;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEMCPORT;

import java.net.UnknownHostException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.fieldeditors.TITANResourceLocator;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class HostControllerDialog extends Dialog {
	private static final String EMPTYSTRING = "";
	private static final String DEFAULT_HOSTNAME = "Rhea";

	private String hostName;
	private String workingdirectory;
	private String executable;
	private String command;
	private Text hostNameText;
	private TITANResourceLocator workingdirectoryText;
	private TITANResourceLocator executableText;
	private Text commandText;
	private String title;
	private Label exampleCommand;
	private final IProject project;

	private final Image[] images = {ImageCache.getImageDescriptor("host.gif").createImage(),
			ImageCache.getImageDescriptor("folder.gif").createImage(), ImageCache.getImageDescriptor("executable.gif").createImage(),
			ImageCache.getImageDescriptor("command.gif").createImage(), ImageCache.getImageDescriptor("titan.gif").createImage() };

	private final ModifyListener modifyListener = new ModifyListener() {

		@Override
		public void modifyText(final ModifyEvent e) {
			validate();
			updateExampleText();
		}

	};

	public HostControllerDialog(final Shell shell, final IProject project, final String title) {
		super(shell);
		this.project = project;
		this.title = title;
		hostName = EMPTYSTRING;
		workingdirectory = EMPTYSTRING;
		executable = EMPTYSTRING;
		command = EMPTYSTRING;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	public void setHostName(final String value) {
		hostName = value;
	}

	public String getHostName() {
		return hostName;
	}

	public void setWorkingdirectory(final String value) {
		workingdirectory = value;
	}

	public String getWorkingdirectory() {
		return workingdirectory;
	}

	public void setExecutable(final String value) {
		executable = value;
	}

	public String getExecutable() {
		return executable;
	}

	public void setCommand(final String value) {
		command = value;
	}

	public String getCommand() {
		return command;
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		if (null != title) {
			shell.setText(title);
		}
		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				for (Image image : images) {
					image.dispose();
				}
			}
		});
	}

	private void validate() {
		if (null != getButton(IDialogConstants.OK_ID)) {
			getButton(IDialogConstants.OK_ID).setEnabled(0 != commandText.getText().length());
		}

	}

	private void updateExampleText() {
		String example = "example command: " + commandText.getText();
		example = example.replace(REPLACEABLEHOSTNAME, hostNameText.getText());

		IPath workingDirPath;
		if (null == project) {
			workingDirPath = new Path(workingdirectoryText.getStringValue());
		} else {
			workingDirPath = TITANPathUtilities.resolvePath(workingdirectoryText.getStringValue(), project.getLocation().toOSString());
		}
		example = example.replace(REPLACEABLEHOSTWORKIGNDIRECTORY, workingDirPath.toOSString());

		IPath executablePath;
		if (null == project) {
			executablePath = new Path(executableText.getStringValue());
		} else {
			executablePath = TITANPathUtilities.resolvePath(executableText.getStringValue(), project.getLocation().toOSString());
		}
		String path = PathUtil.getRelativePath(workingDirPath.toOSString(), executablePath.toOSString());
		example = example.replace(REPLACEABLEHOSTEXECUTABLE, path);

		String hostName;
		try {
			hostName = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = DEFAULT_HOSTNAME;
		}
		example = example.replace(REPLACEABLEMCHOST, hostName);
		example = example.replace(REPLACEABLEMCPORT, String.valueOf(1234));
		exampleCommand.setText(example);
	}

	@Override
	protected void okPressed() {
		hostName = hostNameText.getText();
		workingdirectory = workingdirectoryText.getStringValue();
		executable = executableText.getStringValue();
		command = commandText.getText();
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		createHostNameArea(container);
		createWorkingdirectoryArea(container);
		createExecutableArea(container);
		createCommandArea(container);

		exampleCommand = new Label(container, SWT.NONE);
		exampleCommand.setText("example command: ");
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.FILL;
		exampleCommand.setLayoutData(data);
		exampleCommand.setToolTipText("An example for the final command.\n" + "This will be prefixed with sh -c");

		Dialog.applyDialogFont(container);
		validate();
		updateExampleText();
		return container;
	}

	protected void createHostNameArea(final Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(panel, SWT.NONE);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setImage(images[0]);

		Label hostNameLabel = new Label(panel, SWT.NONE);
		hostNameLabel.setText("hostname:");
		hostNameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		hostNameLabel.setToolTipText("The value for %Host");

		hostNameText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		hostNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostNameText.setText(hostName);

		hostNameLabel.setSize(hostNameLabel.getSize().x, hostNameText.getSize().y);

		hostNameText.addModifyListener(modifyListener);
	}

	protected void createWorkingdirectoryArea(final Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (null == project) {
			workingdirectoryText = new TITANResourceLocator("working directory:", panel, IResource.FOLDER, "");
		} else {
			workingdirectoryText = new TITANResourceLocator("working directory:", panel, IResource.FOLDER, project.getLocation().toOSString());
		}
		workingdirectoryText.getTextControl(panel).setToolTipText("The value for %Workingdirectory");
		workingdirectoryText.setStringValue(workingdirectory);

		workingdirectoryText.getTextControl(panel).addModifyListener(modifyListener);
	}

	protected void createExecutableArea(final Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (null == project) {
			executableText = new TITANResourceLocator("executable:", panel, IResource.FOLDER, "");
		} else {
			executableText = new TITANResourceLocator("executable:", panel, IResource.FOLDER, project.getLocation().toOSString());
		}

		executableText.getTextControl(panel).setToolTipText("The value for %Executable");
		executableText.setStringValue(executable);

		executableText.getTextControl(panel).addModifyListener(modifyListener);
	}

	protected void createCommandArea(final Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(panel, SWT.NONE);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setImage(images[3]);

		Label commandLabel = new Label(panel, SWT.NONE);
		commandLabel.setText("command:");
		commandLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		commandLabel.setToolTipText("The command to execute.\n" + "Additonally %MCHost and %MCPort is replaced by maincontroller provided data");

		commandText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		commandText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		commandText.setText(command);

		commandLabel.setSize(commandLabel.getSize().x, commandText.getSize().y);

		commandText.addModifyListener(modifyListener);
	}
}
