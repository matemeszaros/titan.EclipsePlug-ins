/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.BuildLocation;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectRemoteBuildPropertyData;
import org.eclipse.titan.designer.wizards.projectFormat.TITANAutomaticProjectExporter;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Kristof Szabados
 * */
//FIXME should remove the limitation coming from the use of properties, by using objects in memory.
public final class ProjectRemoteBuildPropertyPage extends PropertyPage {
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String EXECUTE_TEXT = "Execute the build commands in parallel";
	private static final String ADDBUTTON_TEXT = "New...";
	private static final String EDITBUTTON_TEXT = "Edit...";
	private static final String COPYBUTTON_TEXT = "Copy...";
	private static final String REMOVEBUTTON_TEXT = "Remove...";
	private static final String[] hostTableColumnHeaders = { "Active", "Name", "Command" };

	private IProject projectResource;
	private Composite pageComposite;
	private ConfigurationManagerControl configurationManager;
	private Composite remoteHostEditComposite;
	private Label headLabel;
	private TableViewer hostViewer;
	private Button executeInParallel;
	private Button addHostButton;
	private Button editHostButton;
	private Button copyHostButton;
	private Button removeHostButton;

	private Image[] images = { ImageCache.getImageDescriptor("question.gif").createImage(),
			ImageCache.getImageDescriptor("host.gif").createImage(), ImageCache.getImageDescriptor("command.gif").createImage() };

	private ColumnLayoutData[] hostTableColumnLayouts = { new ColumnWeightData(10), new ColumnWeightData(30), new ColumnWeightData(60), };

	private static class RemoteBuildContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements
		 * (java.lang.Object)
		 */
		@Override
		public Object[] getElements(final Object inputElement) {
			if (inputElement instanceof String[]) {
				String[] tempInput = (String[]) inputElement;
				BuildLocation[] result = new BuildLocation[tempInput.length];
				for (int i = 0; i < tempInput.length; i++) {
					result[i] = new BuildLocation(tempInput[i]);
				}
				return result;
			}
			return new BuildLocation[] {};
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// Do nothing
		}
	}

	private static class RemoteBuildLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText
		 * (java.lang.Object, int)
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			if (element instanceof BuildLocation) {
				BuildLocation location = (BuildLocation) element;
				switch (columnIndex) {
				case 0:
					return location.getActive() ? TRUE : FALSE;
				case 1:
					return location.getName();
				case 2:
					return location.getCommand();
				default:
					break;
				}
			}
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		pageComposite.dispose();
		headLabel.dispose();
		hostViewer = null;
		if (executeInParallel != null) {
			executeInParallel.dispose();
			executeInParallel = null;
		}
		if (remoteHostEditComposite != null) {
			remoteHostEditComposite.dispose();
			remoteHostEditComposite = null;
		}
		if (addHostButton != null) {
			addHostButton.dispose();
			addHostButton = null;
		}
		if (editHostButton != null) {
			editHostButton.dispose();
			editHostButton = null;
		}
		if (copyHostButton != null) {
			copyHostButton.dispose();
			copyHostButton = null;
		}
		if (removeHostButton != null) {
			removeHostButton.dispose();
			removeHostButton = null;
		}
		for (int i = 0; i < images.length; i++) {
			images[i].dispose();
		}
		super.dispose();
	}

	/**
	 * Handles the change of the active configuration. Sets the new
	 * configuration to be the active one, and loads its settings.
	 * 
	 * @param configuration
	 *                the name of the new configuration.
	 * */
	public void changeConfiguration(final String configuration) {
		configurationManager.changeActualConfiguration();

		loadProperties();

		PropertyNotificationManager.firePropertyChange(projectResource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		projectResource = (IProject) getElement();

		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout pageCompositeLayout = new GridLayout();
		pageCompositeLayout.numColumns = 1;
		pageComposite.setLayout(pageCompositeLayout);
		GridData pageCompositeGridData = new GridData();
		pageCompositeGridData.horizontalAlignment = GridData.FILL;
		pageCompositeGridData.verticalAlignment = GridData.FILL;
		pageCompositeGridData.grabExcessHorizontalSpace = true;
		pageCompositeGridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(pageCompositeGridData);

		if (TITANBuilder.isBuilderEnabled((IProject) getElement())) {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(ProjectBuildPropertyPage.BUILDER_IS_ENABLED);
		} else {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(ProjectBuildPropertyPage.BUILDER_IS_NOT_ENABLED);
		}

		configurationManager = new ConfigurationManagerControl(pageComposite, projectResource);
		configurationManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (configurationManager.hasConfigurationChanged()) {
					changeConfiguration(configurationManager.getActualSelection());
				}
			}
		});

		executeInParallel = new Button(pageComposite, SWT.CHECK);
		executeInParallel.setText(EXECUTE_TEXT);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		executeInParallel.setLayoutData(gd);

		remoteHostEditComposite = new Composite(pageComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		remoteHostEditComposite.setLayout(layout);
		remoteHostEditComposite.setLayoutData(gridData);
		remoteHostEditComposite.setFont(parent.getFont());

		createHostControllersTable(remoteHostEditComposite);
		createTableButtons(remoteHostEditComposite);

		loadProperties();

		return pageComposite;
	}

	private void createHostControllersTable(final Composite parent) {
		hostViewer = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = hostViewer.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		hostViewer.getControl().setLayoutData(data);
		hostViewer.setLabelProvider(new RemoteBuildLabelProvider());
		hostViewer.setContentProvider(new RemoteBuildContentProvider());

		for (int i = 0; i < hostTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(hostTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(true);
			tc.setMoveable(true);
			tc.setText(hostTableColumnHeaders[i]);
			tc.setImage(images[i]);
		}
	}

	private void createTableButtons(final Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData();
		data.verticalAlignment = SWT.BEGINNING;
		data.horizontalAlignment = SWT.BEGINNING;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(data);
		buttonComposite.setFont(parent.getFont());

		Label label = new Label(buttonComposite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		label.setFont(buttonComposite.getFont());

		addHostButton = createPushButton(buttonComposite, ADDBUTTON_TEXT, null);
		addHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleNewButtonSelected();
			}
		});
		editHostButton = createPushButton(buttonComposite, EDITBUTTON_TEXT, null);
		editHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleEditButtonSelected();
			}
		});
		copyHostButton = createPushButton(buttonComposite, COPYBUTTON_TEXT, null);
		copyHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleCopySelected();
			}
		});
		removeHostButton = createPushButton(buttonComposite, REMOVEBUTTON_TEXT, null);
		removeHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleRemoveSelected();
			}
		});
	}

	public static Button createPushButton(final Composite parent, final String label, final Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		button.setLayoutData(gd);
		return button;
	}

	/**
	 * Loads the properties from the resource into the user interface
	 * elements.
	 * */
	private void loadProperties() {
		String temp = null;
		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.PARALLEL_COMMAND_EXECUTION));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		executeInParallel.setSelection(TRUE.equals(temp));

		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.REMOTE_BUILD_HOST_INFO));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		List<String> splittedList = ProjectRemoteBuildPropertyData.intelligentSplit(temp, '#', '\\');
		hostViewer.setInput(splittedList.toArray(new String[splittedList.size()]));
	}

	protected void handleNewButtonSelected() {
		RemoteHostDialog dialog = new RemoteHostDialog(getShell());
		dialog.setActive(false);

		if (dialog.open() == Window.OK) {
			hostViewer.add(new BuildLocation(dialog.getActive(), dialog.getName(), dialog.getCommand()));
		}
	}

	protected void handleEditButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		BuildLocation host = (BuildLocation) sel.getFirstElement();
		if (host == null) {
			return;
		}

		RemoteHostDialog dialog = new RemoteHostDialog(getShell());
		dialog.setActive(host.getActive());
		dialog.setName(host.getName());
		dialog.setCommand(host.getCommand());

		if (dialog.open() != Window.OK) {
			return;
		}

		host.configure(dialog.getActive(), dialog.getName(), dialog.getCommand());
		hostViewer.refresh(host, true);
	}

	protected void handleCopySelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		BuildLocation host = (BuildLocation) sel.getFirstElement();
		if (host == null) {
			return;
		}
		hostViewer.add(host.clone());
	}

	protected void handleRemoveSelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		hostViewer.getControl().setRedraw(false);
		for (Iterator<?> i = sel.iterator(); i.hasNext();) {
			hostViewer.remove(i.next());
		}
		hostViewer.getControl().setRedraw(true);
	}

	@Override
	public void setVisible(final boolean visible) {
		if (!visible) {
			return;
		}

		if (configurationManager != null) {
			configurationManager.refresh();
		}

		super.setVisible(visible);
	}

	@Override
	public boolean performOk() {
		try {
			projectResource.setPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.PARALLEL_COMMAND_EXECUTION), executeInParallel.getSelection() ? TRUE : FALSE);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		TableItem[] items = hostViewer.getTable().getItems();
		BuildLocation location;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			location = (BuildLocation) items[i].getData();
			if (i != 0) {
				builder.append('#');
			}
			builder.append(location.getPropertyValueRepresentation().toString().replace("#", "\\#"));
		}
		try {
			projectResource.setPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.REMOTE_BUILD_HOST_INFO), builder.toString());
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		configurationManager.saveActualConfiguration();
		ProjectDocumentHandlingUtility.saveDocument(projectResource);
		TITANAutomaticProjectExporter.saveAllAutomatically(projectResource);

		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		configurationManager.clearActualConfiguration();
		String temp = null;
		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.PARALLEL_COMMAND_EXECUTION));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		executeInParallel.setSelection(TRUE.equals(temp));

		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.REMOTE_BUILD_HOST_INFO));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			temp = null;
		}
		List<String> splittedList = ProjectRemoteBuildPropertyData.intelligentSplit(temp, '#', '\\');
		hostViewer.setInput(splittedList.toArray(new String[splittedList.size()]));
		return super.performCancel();
	}

	/*
	 * Resets back the values to the default state (empty)
	 */
	@Override
	protected void performDefaults() {
		executeInParallel.setSelection(false);
		try {
			projectResource.setPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.PARALLEL_COMMAND_EXECUTION), FALSE);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		hostViewer.setInput(new BuildLocation[] {});
		// set back the property to empty
		try {
			projectResource.setPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
					ProjectRemoteBuildPropertyData.REMOTE_BUILD_HOST_INFO), "");
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		configurationManager.saveActualConfiguration();

		super.performDefaults();
	}
}
