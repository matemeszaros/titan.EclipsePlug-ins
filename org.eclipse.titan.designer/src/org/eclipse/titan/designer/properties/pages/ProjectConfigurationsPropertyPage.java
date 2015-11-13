/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectConfigurationsPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titan.designer.properties.data.ProjectConfigurationsPropertyData.ConfigurationRequirement;
import org.eclipse.titan.designer.wizards.projectFormat.TITANAutomaticProjectExporter;
import org.eclipse.titan.designer.wizards.projectFormat.TITANProjectExporter;
import org.eclipse.ui.dialogs.PropertyPage;
import org.w3c.dom.Document;

/**
 * @author Kristof Szabados
 * */
public class ProjectConfigurationsPropertyPage extends PropertyPage {
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.Project";
	public static final String REFERENCED_CONFIGURATION_INFO = ProductConstants.PRODUCT_ID_DESIGNER + ".referecedConfigurationInfo";
	public static final String CONFIGURATION_REQUIREMENTS_NODE = "ConfigurationRequirements";

	private static final String EDITBUTTON_TEXT = "Edit...";
	private static final String[] hostTableColumnHeaders = { "Project References", "Configuration" };

	private IProject projectResource;
	private Composite pageComposite;
	private ConfigurationManagerControl configurationManager;
	private Label headLabel;
	private TableViewer hostViewer;
	private Composite remoteHostEditComposite;
	private Button editHostButton;

	private Image[] images = { ImageCache.getImageDescriptor("host.gif").createImage(),
			ImageCache.getImageDescriptor("question.gif").createImage() };

	private ColumnLayoutData[] hostTableColumnLayouts = { new ColumnWeightData(40), new ColumnWeightData(60), };

	private static class ConfigurationsContentProvider implements IStructuredContentProvider {
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
		public ConfigurationRequirement[] getElements(final Object inputElement) {
			if (inputElement instanceof ArrayList<?>) {
				ArrayList<?> temp = (ArrayList<?>) inputElement;
				return ((ArrayList<?>) inputElement).toArray(new ConfigurationRequirement[temp.size()]);
			}
			return new ConfigurationRequirement[] {};
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// Do nothing
		}
	}

	private static class ConfigurationsLabelProvider extends LabelProvider implements ITableLabelProvider {
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
			if (element instanceof ConfigurationRequirement) {
				ConfigurationRequirement requirement = (ConfigurationRequirement) element;
				switch (columnIndex) {
				case 0:
					return requirement.getProjectName();
				case 1: {
					String configuration = requirement.getConfiguration();
					if (configuration != null && !"".equals(configuration)) {
						return configuration;
					}
					return "<No requirement>";
				}
				default:
					break;
				}
			}
			return null;
		}

	}

	class ConfigurationSelectionDialog extends Dialog {
		private String projectName;
		private String configuration;

		private Combo combo;

		public ConfigurationSelectionDialog(final Shell shell, final String projectName, final String configuration) {
			super(shell);
			this.projectName = projectName;
			this.configuration = configuration;
		}

		@Override
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Set the configuration required from project `" + projectName + "'");
		}

		public String getConfiguration() {
			return configuration;
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			container.setLayout(new GridLayout(2, false));
			container.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label label = new Label(container, SWT.NONE);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			label.setText("Required configuration: ");
			label.setToolTipText("If a project is required to have a specific configuration, having a different one will be reported as a semantic error.");

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			Document document = ProjectDocumentHandlingUtility.getDocument(project);
			List<String> availableConfigurations = ProjectFileHandler.getConfigurations(document);
			availableConfigurations.add(0, "<No requirement>");

			combo = new Combo(container, SWT.READ_ONLY);
			for (String temp : availableConfigurations) {
				combo.add(temp);
			}
			if (availableConfigurations.contains(configuration)) {
				combo.select(availableConfigurations.indexOf(configuration));
			} else {
				combo.setText("<No requirement>");
			}
			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (combo.getSelectionIndex() == 0) {
						configuration = "";
					} else {
						configuration = combo.getText();
					}

					super.widgetSelected(e);
				}
			});

			return container;
		}
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

		PropertyNotificationManager.firePropertyChange((IProject)getElement());
	}

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
		hostViewer = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
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
		hostViewer.setLabelProvider(new ConfigurationsLabelProvider());
		hostViewer.setContentProvider(new ConfigurationsContentProvider());

		for (int i = 0; i < hostTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(hostTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(true);
			tc.setMoveable(true);
			tc.setText(hostTableColumnHeaders[i]);
			tc.setImage(images[i]);
		}

		hostViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				handleEditButtonSelected();
			}
		});
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

		editHostButton = createPushButton(buttonComposite, EDITBUTTON_TEXT, null);
		editHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleEditButtonSelected();
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

	protected void handleEditButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
		ConfigurationRequirement host = (ConfigurationRequirement) sel.getFirstElement();
		if (host == null) {
			return;
		}

		ConfigurationSelectionDialog dialog = new ConfigurationSelectionDialog(getShell(), host.getProjectName(), host.getConfiguration());

		if (dialog.open() != Window.OK) {
			return;
		}

		host.setConfiguration(dialog.configuration);
		hostViewer.refresh(host, true);
	}

	/**
	 * Loads the properties from the resource into the user interface
	 * elements.
	 * */
	private void loadProperties() {
		hostViewer.setInput(ProjectConfigurationsPropertyData.getConfigurationRequirements(projectResource));
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
		TableItem[] items = hostViewer.getTable().getItems();
		List<ConfigurationRequirement> requirements = new ArrayList<ConfigurationRequirement>();

		for (int i = 0; i < items.length; i++) {
			ConfigurationRequirement requirement = (ConfigurationRequirement) items[i].getData();
			String configuration = requirement.getConfiguration();
			if (configuration != null && !"".equals(configuration)) {
				requirements.add(requirement);
			}
		}
		ProjectConfigurationsPropertyData.setConfigurationRequirements(projectResource, requirements);

		configurationManager.saveActualConfiguration();
		ProjectDocumentHandlingUtility.saveDocument(projectResource);
		TITANAutomaticProjectExporter.saveAllAutomatically(projectResource);

		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		loadProperties();
		return super.performCancel();
	}

	/*
	 * Resets back the values to the default state (empty)
	 */
	@Override
	protected void performDefaults() {
		try {
			projectResource.setPersistentProperty(new QualifiedName(QUALIFIER, CONFIGURATION_REQUIREMENTS_NODE), "");
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		hostViewer.setInput(ProjectConfigurationsPropertyData.getConfigurationRequirements(projectResource));

		configurationManager.saveActualConfiguration();

		super.performDefaults();
	}
}
