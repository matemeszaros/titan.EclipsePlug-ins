/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.hostcontrollers;

import static org.eclipse.titan.executor.GeneralConstants.COMMAND;
import static org.eclipse.titan.executor.GeneralConstants.EXECUTABLE;
import static org.eclipse.titan.executor.GeneralConstants.EXECUTABLEFILEPATH;
import static org.eclipse.titan.executor.GeneralConstants.HOST;
import static org.eclipse.titan.executor.GeneralConstants.HOSTCOMMANDS;
import static org.eclipse.titan.executor.GeneralConstants.HOSTCONTROLLER;
import static org.eclipse.titan.executor.GeneralConstants.HOSTEXECUTABLES;
import static org.eclipse.titan.executor.GeneralConstants.HOSTNAMES;
import static org.eclipse.titan.executor.GeneralConstants.HOSTWORKINGDIRECTORIES;
import static org.eclipse.titan.executor.GeneralConstants.WORKINGDIRECTORY;
import static org.eclipse.titan.executor.GeneralConstants.WORKINGDIRECTORYPATH;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.HostController;
import org.eclipse.titan.executor.TITANDebugConsole;
import org.eclipse.titan.executor.designerconnection.DesignerHelper;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.titan.executor.tabpages.maincontroller.BaseMainControllerTab;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Kristof Szabados
 * */
public final class HostControllersTab extends AbstractLaunchConfigurationTab {
	private TableViewer hostViewer;
	private Composite mainComposite;
	private Composite tableComposite;

	private final ILaunchConfigurationTabGroup tabGroup;

	private static final String[] HOST_TABLE_COLUMN_HEADERS = {HOST, WORKINGDIRECTORY, EXECUTABLE, COMMAND};

	private final ColumnLayoutData[] hostTableColumnLayouts = {new ColumnWeightData(1, 75), new ColumnWeightData(2, 150), new ColumnWeightData(2, 100),
			new ColumnWeightData(2, 100)};

	private final Image[] columnImages = {ImageCache.getImageDescriptor("host.gif").createImage(),
			ImageCache.getImageDescriptor("folder.gif").createImage(), ImageCache.getImageDescriptor("executable.gif").createImage(),
			ImageCache.getImageDescriptor("command.gif").createImage()};

	private Button addHostButton;
	private Button editHostButton;
	private Button copyHostButton;
	private Button removeHostButton;
	private Button initFromProject;

	public HostControllersTab(final ILaunchConfigurationTabGroup tabGroup) {
		this.tabGroup = tabGroup;
	}

	@Override
	public void dispose() {
		for (Image columnImage : columnImages) {
			columnImage.dispose();
		}
		hostViewer = null;
		if (null != mainComposite) {
			mainComposite.dispose();
			mainComposite = null;
		}
		if (null != tableComposite) {
			tableComposite.dispose();
			tableComposite = null;
		}
		if (null != addHostButton) {
			addHostButton.dispose();
			addHostButton = null;
		}
		if (null != editHostButton) {
			editHostButton.dispose();
			editHostButton = null;
		}
		if (null != copyHostButton) {
			copyHostButton.dispose();
			copyHostButton = null;
		}
		if (null != removeHostButton) {
			removeHostButton.dispose();
			removeHostButton = null;
		}
		if (null != initFromProject) {
			initFromProject.dispose();
			initFromProject = null;
		}
		super.dispose();
	}

	@Override
	public void createControl(final Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());

		createHostControllersTable(mainComposite);
		createTableButtons(mainComposite);

		Dialog.applyDialogFont(mainComposite);
	}

	private void createHostControllersTable(final Composite parent) {
		tableComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(data);
		tableComposite.setFont(parent.getFont());
		Label label = new Label(tableComposite, SWT.NONE);
		label.setFont(parent.getFont());
		label.setText("Host Controllers to use:");

		hostViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = hostViewer.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		hostViewer.getControl().setLayoutData(data);
		hostViewer.setContentProvider(new HostControllerContentProvider());
		hostViewer.setLabelProvider(new HostControllerLabelProvider());

		hostViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(final DoubleClickEvent event) {
				handleEditButtonSelected();
			}

		});
		for (int i = 0; i < HOST_TABLE_COLUMN_HEADERS.length; i++) {
			tableLayout.addColumnData(hostTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(true);
			tc.setMoveable(true);
			tc.setText(HOST_TABLE_COLUMN_HEADERS[i]);
			tc.setImage(columnImages[i]);
		}
	}

	private void createTableButtons(final Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(data);
		buttonComposite.setFont(parent.getFont());

		createVerticalSpacer(buttonComposite, 1);

		addHostButton = createPushButton(buttonComposite, "New...", null);
		addHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleNewButtonSelected();
			}
		});
		editHostButton = createPushButton(buttonComposite, "Edit...", null);
		editHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleEditButtonSelected();
			}
		});
		copyHostButton = createPushButton(buttonComposite, "Copy...", null);
		copyHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleCopySelected();
			}
		});
		removeHostButton = createPushButton(buttonComposite, "Remove...", null);
		removeHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleRemoveSelected();
			}
		});

		initFromProject = createPushButton(buttonComposite, "Init...", null);
		initFromProject.setToolTipText(
				"Clears the list and tries to add a single host controller with properties set for the project set on the Main Controller page");
		initFromProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleInitialization();
			}
		});
	}

	private void handleNewButtonSelected() {
		HostControllerDialog dialog = new HostControllerDialog(getShell(), getProject(), HOSTCONTROLLER);
		dialog.setHostName("");
		dialog.setWorkingdirectory("");
		dialog.setExecutable("");
		dialog.setCommand("rsh %Host cd %Workingdirectory; %Executable %MCHost %MCPort");

		if (dialog.open() != Window.OK) {
			return;
		}

		hostViewer.add(new HostController(dialog.getHostName(), dialog.getWorkingdirectory(), dialog.getExecutable(), dialog.getCommand()));
		updateLaunchConfigurationDialog();
	}

	private void handleEditButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		HostController controller = (HostController) sel.getFirstElement();
		if (null == controller) {
			return;
		}
		String host = controller.host();
		String workingDirectory = controller.workingdirectory();
		String executable = controller.executable();
		String command = controller.command();

		HostControllerDialog dialog = new HostControllerDialog(getShell(), getProject(), HOSTCONTROLLER);
		dialog.setHostName(host);
		dialog.setWorkingdirectory(workingDirectory);
		dialog.setExecutable(executable);
		dialog.setCommand(command);

		if (dialog.open() != Window.OK) {
			return;
		}

		controller.configure(dialog.getHostName(), dialog.getWorkingdirectory(), dialog.getExecutable(), dialog.getCommand());
		hostViewer.refresh(controller, true);

		updateLaunchConfigurationDialog();
	}

	private void handleCopySelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		HostController controller = (HostController) sel.getFirstElement();
		if (null == controller) {
			return;
		}
		hostViewer.add(controller.clone());
		updateLaunchConfigurationDialog();
	}

	private void handleRemoveSelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		hostViewer.getControl().setRedraw(false);
		for (Iterator<?> i = sel.iterator(); i.hasNext();) {
			hostViewer.remove(i.next());
		}
		hostViewer.getControl().setRedraw(true);
		updateLaunchConfigurationDialog();
	}

	/**
	 * @return the project set on the main controller tab, or null if none.
	 * */
	private IProject getProject() {
		ILaunchConfigurationTab[] tabs = tabGroup.getTabs();

		BaseMainControllerTab maincontrollerTab = null;
		for (ILaunchConfigurationTab tab : tabs) {
			if (tab instanceof BaseMainControllerTab) {
				maincontrollerTab = (BaseMainControllerTab) tab;
			}
		}

		if (null == maincontrollerTab) {
			return null;
		}

		return maincontrollerTab.getProject();
	}

	private void handleInitialization() {
		MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
		stream.println("initializing a host controller");


		IProject project = getProject();
		if (null == project) {
			return;
		}

		stream.println(project.getName());

		try {
			String workingdirectory = project.getPersistentProperty(new QualifiedName(DesignerHelper.PROJECT_BUILD_PROPERTYPAGE_QUALIFIER,
					DesignerHelper.WORKINGDIR_PROPERTY));

			String executable = project.getPersistentProperty(new QualifiedName(DesignerHelper.PROJECT_BUILD_PROPERTYPAGE_QUALIFIER,
					DesignerHelper.EXECUTABLE_PROPERTY));
			if (null == executable || 0 == executable.length()) {
				executable = workingdirectory + "/" + project.getName();
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					executable += ".exe";
				}
			}
			stream.println(workingdirectory);
			stream.println(executable);

			TableItem[] items = hostViewer.getTable().getItems();
			for (TableItem item : items) {
				hostViewer.remove(item.getData());
			}
			hostViewer.add(new HostController("localhost", workingdirectory, executable, "cd %Workingdirectory; %Executable %MCHost %MCPort"));

			updateLaunchConfigurationDialog();
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public String getName() {
		return "HostControllerTab";
	}

	@Override
	public Image getImage() {
		return ImageCache.getImageDescriptor("host.gif").createImage();
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		hostViewer.setInput(configuration);
	}

	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		TableItem[] items = hostViewer.getTable().getItems();
		List<String> hostNames = new ArrayList<String>();
		List<String> hostWorkingDirectories = new ArrayList<String>();
		List<String> hostExecutables = new ArrayList<String>();
		List<String> hostLoginCommands = new ArrayList<String>();
		HostController controller;
		for (TableItem item : items) {
			controller = (HostController) item.getData();
			hostNames.add(controller.host());
			hostWorkingDirectories.add(controller.workingdirectory());
			hostExecutables.add(controller.executable());
			hostLoginCommands.add(controller.command());
		}
		if (!hostNames.isEmpty()) {
			configuration.setAttribute(HOSTNAMES, hostNames);
			configuration.setAttribute(HOSTWORKINGDIRECTORIES, hostWorkingDirectories);
			configuration.setAttribute(HOSTEXECUTABLES, hostExecutables);
			configuration.setAttribute(HOSTCOMMANDS, hostLoginCommands);
		} else {
			configuration.setAttribute(HOSTNAMES, (ArrayList<String>) null);
			configuration.setAttribute(HOSTWORKINGDIRECTORIES, (ArrayList<String>) null);
			configuration.setAttribute(HOSTEXECUTABLES, (ArrayList<String>) null);
			configuration.setAttribute(HOSTCOMMANDS, (ArrayList<String>) null);
		}
	}

	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(HOSTNAMES, (ArrayList<String>) null);
		configuration.setAttribute(HOSTWORKINGDIRECTORIES, (ArrayList<String>) null);
		configuration.setAttribute(HOSTEXECUTABLES, (ArrayList<String>) null);
		configuration.setAttribute(HOSTCOMMANDS, (ArrayList<String>) null);
	}

	/**
	 * Initializes a default host controller for the provided launch configuration.
	 *
	 * @param configuration the launch configuration to use.
	 * */
	public static boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration) {
		List<String> hostNames = new ArrayList<String>();
		List<String> hostWorkingDirectories = new ArrayList<String>();
		List<String> hostExecutables = new ArrayList<String>();
		List<String> hostLoginCommands = new ArrayList<String>();
		hostNames.add("localhost");
		try {
			String workingDirectoryPath = configuration.getAttribute(WORKINGDIRECTORYPATH, (String) null);
			hostWorkingDirectories.add(workingDirectoryPath);

			String executablePath = configuration.getAttribute(EXECUTABLEFILEPATH, (String) null);
			hostExecutables.add(executablePath);
		} catch (CoreException e) {
			return false;
		}

		hostLoginCommands.add("cd %Workingdirectory; %Executable %MCHost %MCPort");
		configuration.setAttribute(HOSTNAMES, hostNames);
		configuration.setAttribute(HOSTWORKINGDIRECTORIES, hostWorkingDirectories);
		configuration.setAttribute(HOSTEXECUTABLES, hostExecutables);
		configuration.setAttribute(HOSTCOMMANDS, hostLoginCommands);

		return true;
	}
}
