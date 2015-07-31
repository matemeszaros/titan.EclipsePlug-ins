/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Kristof Szabados
 * */
public final class FolderBuildPropertyPage extends PropertyPage {

	private Composite pageComposite = null;
	private Label headLabel = null;
	private ConfigurationManagerControl configurationManager;
	private String firstConfiguration;

	private Button centralStorageButton = null;
	private static final String CENTRAL_STORAGE_DISPLAY_TEXT = "Folder is in central storage.";

	private Button excludeFromBuildButton = null;
	private static final String EXCLUDE_DISPLAY_TEXT = "Excluded from build.";

	private IFolder folderResource;
	private final PreferenceStore tempStore;

	public FolderBuildPropertyPage() {
		super();
		tempStore = new PreferenceStore();
	}

	@Override
	public void dispose() {
		pageComposite.dispose();
		headLabel.dispose();

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

		PropertyNotificationManager.firePropertyChange(folderResource.getProject());
	}

	@Override
	protected Control createContents(final Composite parent) {
		folderResource = (IFolder) getElement();
		try {
			String temp = folderResource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY));
			tempStore.setValue(FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY,
					FolderBuildPropertyData.TRUE_STRING.equals(temp) ? FolderBuildPropertyData.TRUE_STRING
							: FolderBuildPropertyData.FALSE_STRING);
			temp = folderResource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY));
			tempStore.setValue(FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY,
					FolderBuildPropertyData.TRUE_STRING.equals(temp) ? FolderBuildPropertyData.TRUE_STRING
							: FolderBuildPropertyData.FALSE_STRING);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);
		if (TITANBuilder.isBuilderEnabled(folderResource.getProject())) {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(ProjectBuildPropertyPage.BUILDER_IS_ENABLED);
		} else {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(ProjectBuildPropertyPage.BUILDER_IS_NOT_ENABLED);
		}

		configurationManager = new ConfigurationManagerControl(pageComposite, folderResource.getProject());
		configurationManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (configurationManager.hasConfigurationChanged()) {
					changeConfiguration(configurationManager.getActualSelection());
				}
			}
		});
		firstConfiguration = configurationManager.getActualSelection();

		centralStorageButton = new Button(pageComposite, SWT.CHECK);
		centralStorageButton.setText(CENTRAL_STORAGE_DISPLAY_TEXT);
		centralStorageButton.setEnabled(true);
		try {
			String mode = folderResource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY));
			centralStorageButton.setSelection(FolderBuildPropertyData.TRUE_STRING.equals(mode) ? true : false);
			if (mode == null) {
				folderResource.setPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
						FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY), FolderBuildPropertyData.FALSE_STRING);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		excludeFromBuildButton = new Button(pageComposite, SWT.CHECK);
		excludeFromBuildButton.setText(EXCLUDE_DISPLAY_TEXT);
		excludeFromBuildButton.setEnabled(true);
		try {
			String mode = folderResource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY));
			excludeFromBuildButton.setSelection(FolderBuildPropertyData.TRUE_STRING.equals(mode));
			if (mode == null) {
				folderResource.setPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
						FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY), FolderBuildPropertyData.FALSE_STRING);
			}
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		return null;
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
	protected void performDefaults() {
		centralStorageButton.setSelection(false);
		excludeFromBuildButton.setSelection(false);

		configurationManager.saveActualConfiguration();
	}

	@Override
	public boolean performCancel() {
		configurationManager.clearActualConfiguration();

		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		final IProject project = folderResource.getProject();

		try {
			final String tempCentralStorage = centralStorageButton.getSelection() ? FolderBuildPropertyData.TRUE_STRING
					: FolderBuildPropertyData.FALSE_STRING;
			final String tempExcluded = excludeFromBuildButton.getSelection() ? FolderBuildPropertyData.TRUE_STRING
					: FolderBuildPropertyData.FALSE_STRING;
			folderResource.setPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY), tempCentralStorage);
			folderResource.setPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY), tempExcluded);
			final boolean configurationChanged = !firstConfiguration.equals(configurationManager.getActualSelection());
			if (configurationChanged || !tempCentralStorage.equals(tempStore.getString(FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY))
					|| !tempExcluded.equals(tempStore.getString(FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY))) {
				tempStore.setValue(FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY, FolderBuildPropertyData.TRUE_STRING
						.equals(tempCentralStorage) ? FolderBuildPropertyData.TRUE_STRING
						: FolderBuildPropertyData.FALSE_STRING);
				tempStore.setValue(FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY, FolderBuildPropertyData.TRUE_STRING
						.equals(tempExcluded) ? FolderBuildPropertyData.TRUE_STRING : FolderBuildPropertyData.FALSE_STRING);

				configurationManager.saveActualConfiguration();
				ProjectDocumentHandlingUtility.saveDocument(project);

				MarkerHandler.markAllMarkersForRemoval(folderResource);

				PropertyNotificationManager.firePropertyChange(folderResource);
			}
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return false;
		}


		return true;
	}

	/**
	 * Loads the properties from the resource into the user interface
	 * elements.
	 * */
	private void loadProperties() {
		try {
			String mode = folderResource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY));
			centralStorageButton.setSelection(FolderBuildPropertyData.TRUE_STRING.equals(mode) ? true : false);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		try {
			String mode = folderResource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
					FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY));
			excludeFromBuildButton.setSelection(FolderBuildPropertyData.TRUE_STRING.equals(mode));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
	}
}
