/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Control interface for managing project build configurations.
 * 
 * @author Kristof Szabados
 * */
public class ConfigurationManagerControl {
	private final IProject project;
	private String actualConfiguration;

	private Combo configurations;
	private Button manageButton;

	class ConfigurationListDialog extends Dialog {
		private TableViewer hostViewer;
		private final String[] hostTableColumnHeaders = { "Configuration", "Status" };
		private ColumnLayoutData[] hostTableColumnLayouts = { new ColumnWeightData(60), new ColumnWeightData(40), };
		private Text errorText;

		class ConfigurationContentProvider implements IStructuredContentProvider {
			@Override
			public void dispose() {
				// Do nothing
			}

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
				// Do nothing
			}

			@Override
			public Object[] getElements(final Object inputElement) {
				if (inputElement instanceof String[]) {
					return (String[]) inputElement;
				}

				return new String[] {};
			}
		}

		class ConfigurationLabelProvider extends LabelProvider implements ITableLabelProvider {
			private final String activeConfigurationName;

			ConfigurationLabelProvider(final String activeConfigurationName) {
				this.activeConfigurationName = activeConfigurationName;
			}

			@Override
			public Image getColumnImage(final Object element, final int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(final Object element, final int columnIndex) {
				if (element instanceof String) {
					String temp = (String) element;
					switch (columnIndex) {
					case 0:
						return temp;
					case 1:
						return activeConfigurationName.equals(temp) ? "Active" : "";
					default:
						break;
					}
				}

				return null;
			}
		}

		public ConfigurationListDialog(final Shell shell) {
			super(shell);
		}

		@Override
		protected int getShellStyle() {
			return super.getShellStyle() | SWT.RESIZE;
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Manage Configurations");
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);

			hostViewer = new TableViewer(container, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
			Table table = hostViewer.getTable();
			TableLayout tableLayout = new TableLayout();
			table.setLayout(tableLayout);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			table.setFont(container.getFont());
			GridData data = new GridData();
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			hostViewer.getControl().setLayoutData(data);
			hostViewer.setLabelProvider(new ConfigurationLabelProvider(ProjectFileHandler.getActiveConfigurationName(project)));
			hostViewer.setContentProvider(new ConfigurationContentProvider());

			for (int i = 0; i < hostTableColumnHeaders.length; i++) {
				tableLayout.addColumnData(hostTableColumnLayouts[i]);
				TableColumn tc = new TableColumn(table, SWT.NONE, i);
				tc.setResizable(true);
				tc.setMoveable(true);
				tc.setText(hostTableColumnHeaders[i]);
			}

			List<String> tempConfigurations = ProjectFileHandler.getConfigurations(ProjectDocumentHandlingUtility.getDocument(project));
			hostViewer.setInput(tempConfigurations.toArray(new String[tempConfigurations.size()]));

			Composite composite = new Composite(container, SWT.NONE);
			GridLayout layout = new GridLayout(3, true);
			composite.setLayout(layout);
			data = new GridData();
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);

			Button addButton = new Button(composite, SWT.PUSH);
			addButton.setText("New...");
			addButton.setFont(parent.getFont());
			addButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent evt) {
					ConfigurationDialog dialog = new ConfigurationDialog(getShell(), "", "Create new configuration");
					if (dialog.open() == Window.OK) {
						String newName = dialog.getName();
						Document document = ProjectDocumentHandlingUtility.getDocument(project);
						List<String> configurations = ProjectFileHandler.getConfigurations(document);
						if (configurations.contains(newName)) {
							errorText.setText("It is not possible to create a new configuration with name `" + newName
									+ "' as there is already one with that name.");
							errorText.setEnabled(true);
							return;
						}

						errorText.setEnabled(false);

						Node configurationsRoot = ProjectFileHandler.getNodebyName(document.getDocumentElement()
								.getChildNodes(), "Configurations");
						if (configurationsRoot == null) {
							configurationsRoot = document.createElement("Configurations");
							document.getDocumentElement().appendChild(configurationsRoot);
						}

						Node configurationRoot = ProjectFileHandler.findConfigurationNode(document.getDocumentElement(),
								newName);
						if (configurationRoot == null) {
							Element newConfiguration = document.createElement("Configuration");

							newConfiguration.setAttribute("name", newName);
							configurationsRoot.appendChild(newConfiguration);
							configurationRoot = newConfiguration;
						}

						ProjectFileHandler.saveProjectInfoToNode(project, configurationRoot, document);

						configurations = ProjectFileHandler.getConfigurations(ProjectDocumentHandlingUtility
								.getDocument(project));
						hostViewer.setInput(configurations.toArray(new String[configurations.size()]));
					}
				}
			});

			Button deleteButton = new Button(composite, SWT.PUSH);
			deleteButton.setText("Delete");
			deleteButton.setFont(parent.getFont());
			deleteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent evt) {
					IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
					String configuration = (String) sel.getFirstElement();
					if ("Default".equals(configuration)) {
						errorText.setText("The `Default' configuration can not be deleted.");
						errorText.setEnabled(true);
						return;
					}

					errorText.setEnabled(false);

					Document document = ProjectDocumentHandlingUtility.getDocument(project);
					ProjectFileHandler fileHandler = new ProjectFileHandler(project);
					fileHandler.removeConfigurationNode(document.getDocumentElement(), configuration);

					List<String> tempConfigurations = ProjectFileHandler.getConfigurations(ProjectDocumentHandlingUtility
							.getDocument(project));
					hostViewer.setInput(tempConfigurations.toArray(new String[tempConfigurations.size()]));

					String tempActualConfiguration = getActualSelection();
					if (configuration.equals(tempActualConfiguration)) {
						ConfigurationManagerControl.this.configurations.setText("Default");
						changeActualConfiguration();
					}
				}
			});

			Button renameButton = new Button(composite, SWT.PUSH);
			renameButton.setText("Rename");
			renameButton.setFont(parent.getFont());
			renameButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent evt) {
					IStructuredSelection sel = (IStructuredSelection) hostViewer.getSelection();
					String configuration = (String) sel.getFirstElement();
					if ("Default".equals(configuration)) {
						errorText.setText("The `Default' configuration can not be renamed.");
						errorText.setEnabled(true);
						return;
					}

					ConfigurationDialog dialog = new ConfigurationDialog(getShell(), configuration, "Rename configuration");
					if (dialog.open() == Window.OK) {

						String newName = dialog.getName();
						List<String> newConfigurations = ProjectFileHandler.getConfigurations(ProjectDocumentHandlingUtility
								.getDocument(project));
						if (newConfigurations.contains(newName)) {
							errorText.setText("It is not possible to create a new configuration with name `" + newName
									+ "' as there is already one with that name.");
							errorText.setEnabled(true);
							return;
						}

						Document document = ProjectDocumentHandlingUtility.getDocument(project);
						Node configurationsRoot = ProjectFileHandler.getNodebyName(document.getDocumentElement()
								.getChildNodes(), "Configurations");
						if (configurationsRoot == null) {
							configurationsRoot = document.createElement("Configurations");
							document.getDocumentElement().appendChild(configurationsRoot);
						}
						Node configurationRoot = ProjectFileHandler.findConfigurationNode(document.getDocumentElement(),
								configuration);
						((Element) configurationRoot).setAttribute("name", newName);

						newConfigurations = ProjectFileHandler.getConfigurations(ProjectDocumentHandlingUtility
								.getDocument(project));
						hostViewer.setInput(newConfigurations.toArray(new String[newConfigurations.size()]));
						ConfigurationManagerControl.this.configurations.setText(newName);
						saveActualConfiguration();
					}

					errorText.setEnabled(false);
				}
			});

			errorText = new Text(container, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
			errorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			errorText.setEditable(false);
			errorText.setEnabled(false);

			return container;
		}
	}

	static class ConfigurationDialog extends Dialog {
		private String name;
		private Text nameText;

		private String windowHeader;

		public ConfigurationDialog(final Shell shell, final String name, final String windowHeader) {
			super(shell);
			this.name = name;
			this.windowHeader = windowHeader;

			int style = this.getShellStyle();
			setShellStyle(style | SWT.RESIZE);
		}

		public String getName() {
			return name;
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(windowHeader);
		}

		@Override
		protected void createButtonsForButtonBar(final Composite parent) {
			super.createButtonsForButtonBar(parent);

			Button ok = getButton(IDialogConstants.OK_ID);
			if (ok != null) {
				ok.setEnabled(name.length() > 0);
			}
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			composite.setLayout(layout);
			GridData data = new GridData();
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.FILL;
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			composite.setLayoutData(data);

			Label label = new Label(composite, SWT.NONE);
			label.setText("The name of the configuration: ");
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

			nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setText(name);
			nameText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(final ModifyEvent e) {
					String tempName = nameText.getText();
					Button ok = getButton(IDialogConstants.OK_ID);
					ok.setEnabled(tempName.length() > 0);
				}
			});

			Control result = super.createDialogArea(parent);

			return result;
		}

		@Override
		protected void okPressed() {
			name = nameText.getText();

			super.okPressed();
		}
	}

	public ConfigurationManagerControl(final Composite parent, final IProject project) {
		this.project = project;

		Group group = new Group(parent, SWT.NONE);
		group.setText("Build Configurations");
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		group.setLayoutData(gridData);

		Label label = new Label(group, SWT.NONE);
		label.setText("The actual build configuration:");

		configurations = new Combo(group, SWT.READ_ONLY);
		configurations.setEnabled(true);

		manageButton = new Button(group, SWT.PUSH);
		manageButton.setText("Manage Configurations");
		manageButton.setFont(parent.getFont());
		manageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent evt) {
				ConfigurationListDialog dialog = new ConfigurationListDialog(null);
				if (dialog.open() == Window.OK) {
					loadConfigurations();
				}
			}
		});

		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		manageButton.setLayoutData(gd);

		loadConfigurations();
	}

	public boolean hasConfigurationChanged() {
		return actualConfiguration != null && configurations != null && !configurations.isDisposed()
				&& !actualConfiguration.equals(configurations.getText());
	}

	private void loadConfigurations() {
		configurations.removeAll();

		Document document = ProjectDocumentHandlingUtility.getDocument(project);
		if (document == null) {
			ErrorReporter.logWarning("No property file was found for project `" + project.getName() + "'");
			document = ProjectDocumentHandlingUtility.createDocument(project);
		}

		List<String> configurationNames = ProjectFileHandler.getConfigurations(document);
		for (String name : configurationNames) {
			configurations.add(name);
		}

		if (actualConfiguration == null) {
			actualConfiguration = ProjectFileHandler.getActiveConfigurationName(project);
		}
		configurations.setText(actualConfiguration);
	}

	public String getActualSelection() {
		return actualConfiguration;
	}

	/**
	 * Changes the actually used configuration to the one being selected in
	 * the configuration selector. Also loads all of the settings of the new
	 * configuration into the project.
	 * */
	public void changeActualConfiguration() {
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule rule = ruleFactory.createRule(project);
		try {
			Job.getJobManager().beginRule(rule, new NullProgressMonitor());
			actualConfiguration = configurations.getText();

			final HashSet<IResource> changedResources = new HashSet<IResource>();
			final Document document = ProjectDocumentHandlingUtility.getDocument(project);
			Node configurationNode = ProjectFileHandler.findConfigurationNode(document.getDocumentElement(), actualConfiguration);
			if (actualConfiguration == null) {
				ErrorReporter.logError("The configuration `" + actualConfiguration + "' for project `" + project.getName()
						+ "' does not exist.");
			} else {
				ProjectFileHandler fileHandler = new ProjectFileHandler(project);
				fileHandler.loadProjectInfoFromNode(configurationNode, changedResources);
			}
		} finally {
			Job.getJobManager().endRule(rule);
		}
	}

	/**
	 * Saves all of the information related to the actually selected
	 * configuration into the temporal document.
	 * */
	public void saveActualConfiguration() {
		final String configuration = configurations.getText();

		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.ACTIVECONFIGURATION), configuration);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		Document document = ProjectDocumentHandlingUtility.getDocument(project);
		if (document == null) {
			return;
		}

		Node configurationNode = ProjectFileHandler.findConfigurationNode(document.getDocumentElement(), getActualSelection());
		if (configurationNode == null) {
			configurationNode = ProjectFileHandler.createConfigurationNode(document, getActualSelection());
		}
		ProjectFileHandler.saveProjectInfoToNode(project, configurationNode, document);
		ProjectFileHandler.saveActualConfigurationInfoToNode(project, document);
	}

	/**
	 * Clears the actual information stored.
	 * */
	public void clearActualConfiguration() {
		actualConfiguration = null;
		ProjectDocumentHandlingUtility.clearDocument(project);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the user changes the receiver's selection, by sending it one of
	 * the messages defined in the <code>SelectionListener</code> interface.
	 * */
	public void addSelectionListener(final SelectionListener listener) {
		configurations.addSelectionListener(listener);
	}

	public void refresh() {
		if (configurations == null) {
			return;
		}

		final String temp = configurations.getText();

		if (!actualConfiguration.equals(temp)) {
			configurations.setText(actualConfiguration);
		}
	}
}
