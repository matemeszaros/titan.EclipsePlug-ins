/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.wizards.projectFormat.TITANAutomaticProjectExporter;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Kristof Szabados
 * */
public final class ProjectBuildPropertyPage extends PropertyPage {
	public static final String BUILDER_IS_ENABLED = "This TITAN project has the TITAN builder enabled.";
	public static final String BUILDER_IS_NOT_ENABLED = "This TITAN project has the TITAN builder disabled.";

	private Composite pageComposite;
	private Label headLabel;

	private TabFolder makefileOperationsTabFolder;

	private Button generateMakefileButton;
	private static final String GENERATEMAKEFILE_TEXT = "Automatic makefile management";
	private Button generateInternalMakefileButton;
	private Button withoutSymbolicLinksButton;

	private MakefileCreationTab makefileCreationTab;
	private InternalMakefileCreationTab internalMakefileCreationTab;
	private MakeAttributesTab makeAttributesTab;

	private PreferenceStore tempStorage;
	private IProject projectResource;

	private ConfigurationManagerControl configurationManager;
	private String firstConfiguration;

	public ProjectBuildPropertyPage() {
		super();
		tempStorage = new PreferenceStore();
	}

	@Override
	public void dispose() {
		headLabel.dispose();
		generateMakefileButton.dispose();
		generateInternalMakefileButton.dispose();
		withoutSymbolicLinksButton.dispose();
		makefileCreationTab.dispose();
		internalMakefileCreationTab.dispose();
		makeAttributesTab.dispose();

		makefileOperationsTabFolder.dispose();
		pageComposite.dispose();
		super.dispose();
	}

	protected void copyPropertyStore() {
		if (generateMakefileButton == null) {
			return;
		}

		copyProjectPersistentProperty(ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY);
		copyProjectPersistentProperty(ProjectBuildPropertyData.GENERATE_INTERNAL_MAKEFILE_PROPERTY);
		copyProjectPersistentProperty(ProjectBuildPropertyData.SYMLINKLESS_BUILD_PROPERTY);
		makefileCreationTab.copyPropertyStore(projectResource, tempStorage);
		makeAttributesTab.copyPropertyStore(projectResource, tempStorage);
		internalMakefileCreationTab.copyPropertyStore(projectResource, tempStorage);
	}

	protected void copyProjectPersistentProperty(final String propertyName) {
		String temp = null;
		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (temp != null) {
			tempStorage.setValue(propertyName, temp);
		}
	}

	/**
	 * Returns true if changes happened to the property given by its name.
	 * 
	 * @param propertyName
	 *                the name of the property to be evaluated.
	 * @return true if the value of the property has changed.
	 */
	protected boolean evaluatePersistentProperty(final String propertyName) {
		String actualValue = null;
		String copyValue = null;
		try {
			actualValue = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		copyValue = tempStorage.getString(propertyName);
		return ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
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

	/**
	 * Evaluating PropertyStore changes. If there is a difference between
	 * the temporary storage and the persistent storage the new values will
	 * be saved into the project property file (XML).
	 */
	protected void evaluatePropertyStore() {
		boolean removeExecutable = false;
		boolean removeMakefile = false;
		boolean saveXMLStore = false;

		saveXMLStore |= evaluatePersistentProperty(ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY);
		saveXMLStore |= evaluatePersistentProperty(ProjectBuildPropertyData.GENERATE_INTERNAL_MAKEFILE_PROPERTY);
		saveXMLStore |= evaluatePersistentProperty(ProjectBuildPropertyData.SYMLINKLESS_BUILD_PROPERTY);
		saveXMLStore |= makeAttributesTab.evaluatePropertyStore(projectResource, tempStorage);

		if (internalMakefileCreationTab.evaluatePropertyStore(projectResource, tempStorage)
				|| makefileCreationTab.evaluatePropertyStore(projectResource, tempStorage)) {
			removeMakefile = true;
			removeExecutable = true;
			saveXMLStore = true;
		}

		if (saveXMLStore) {
			removeMakefile = true;
			copyPropertyStore();
		}

		final boolean configurationChanged = !firstConfiguration.equals(configurationManager.getActualSelection());
		if (configurationChanged || removeMakefile || removeExecutable) {
			PropertyNotificationManager.firePropertyChange(projectResource);
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

		copyPropertyStore();
		loadProperties();
		updateContents();
		checkProperties();

		PropertyNotificationManager.firePropertyChange(projectResource);
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

		if (TITANBuilder.isBuilderEnabled(projectResource)) {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(BUILDER_IS_ENABLED);
		} else {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(BUILDER_IS_NOT_ENABLED);
		}

		try {
			String loadLocation = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.LOAD_LOCATION));
			if (loadLocation == null) {
				headLabel.setText(headLabel.getText() + "\nWas not yet saved ");
			} else {
				headLabel.setText(headLabel.getText() + "\nWas loaded from " + loadLocation);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
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
		firstConfiguration = configurationManager.getActualSelection();

		generateMakefileButton = new Button(pageComposite, SWT.CHECK);
		generateMakefileButton.setText(GENERATEMAKEFILE_TEXT);
		generateMakefileButton.setEnabled(true);
		generateMakefileButton.setToolTipText("If this option is set, the build process will refresh the makefile when needed,\n"
				+ "otherwise this is the user's responsibility.");
		generateMakefileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateContents();
			}
		});

		generateInternalMakefileButton = new Button(pageComposite, SWT.CHECK);
		generateInternalMakefileButton.setText("Generate the makefile using the Eclipse internal Makefile generator");
		generateInternalMakefileButton.setEnabled(true);
		generateInternalMakefileButton
				.setToolTipText("If this option is set, the build process will refresh the Makefile using the Eclipse internal Makefile generator");
		generateInternalMakefileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateContents();
				updateSymlinkLessbuild(generateInternalMakefileButton.getSelection());
			}
		});

		withoutSymbolicLinksButton = new Button(pageComposite, SWT.CHECK);
		withoutSymbolicLinksButton.setText("Don't use symbolic links in the build process (internal only)");
		withoutSymbolicLinksButton
				.setToolTipText("If this option is set, the build process will be run without generating symbolic links for the files");
		withoutSymbolicLinksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateContents();
			}
		});
		updateSymlinkLessbuild(generateInternalMakefileButton.getSelection());

		makefileOperationsTabFolder = new TabFolder(pageComposite, SWT.BORDER);

		GridData makefileOperationsTabFolderGridData = new GridData();
		makefileOperationsTabFolderGridData.horizontalAlignment = GridData.FILL;
		makefileOperationsTabFolderGridData.verticalAlignment = GridData.FILL;
		makefileOperationsTabFolderGridData.grabExcessHorizontalSpace = true;
		makefileOperationsTabFolderGridData.grabExcessVerticalSpace = true;
		makefileOperationsTabFolder.setLayoutData(makefileOperationsTabFolderGridData);

		// Creating the composite that will be set as the control of
		// creationAttributesTabItem.
		// The parent of the control composite must be set to the
		// TabFolder not to the TabItem.
		makefileCreationTab = new MakefileCreationTab(projectResource, this);
		makefileCreationTab.createContents(makefileOperationsTabFolder);

		internalMakefileCreationTab = new InternalMakefileCreationTab(projectResource);
		internalMakefileCreationTab.createContents(makefileOperationsTabFolder);
		internalMakefileCreationTab.setMakefileGenerationEnabled(generateInternalMakefileButton.getSelection());

		makeAttributesTab = new MakeAttributesTab(projectResource, this);
		makeAttributesTab.createContents(makefileOperationsTabFolder);

		copyPropertyStore();
		loadProperties();
		updateContents();
		checkProperties();

		return pageComposite;
	}

	private void updateSymlinkLessbuild(final boolean enabled) {
		withoutSymbolicLinksButton.setEnabled(enabled);
		if (enabled) {
			withoutSymbolicLinksButton.setText("Don't use symbolic links in the build process");
		} else {
			withoutSymbolicLinksButton.setText("Don't use symbolic links in the build process (internal only)");
		}
	}

	protected void updateContents() {
		if (generateMakefileButton == null || generateInternalMakefileButton == null || withoutSymbolicLinksButton == null) {
			return;
		}

		if (generateMakefileButton.getSelection()) {
			makefileCreationTab.setMakefileGenerationEnabled(true);
			internalMakefileCreationTab.setMakefileGenerationEnabled(generateInternalMakefileButton.getSelection());
			makeAttributesTab.setMakefileGenerationEnabled(true);
			generateInternalMakefileButton.setEnabled(true);
			updateSymlinkLessbuild(generateInternalMakefileButton.getSelection());
		} else {
			makefileCreationTab.setMakefileGenerationEnabled(false);
			internalMakefileCreationTab.setMakefileGenerationEnabled(false);
			makeAttributesTab.setMakefileGenerationEnabled(false);
			generateInternalMakefileButton.setEnabled(false);
			updateSymlinkLessbuild(true);
		}

	}

	@Override
	protected void performDefaults() {
		if (generateMakefileButton == null) {
			return;
		}

		makefileCreationTab.performDefaults();
		internalMakefileCreationTab.performDefaults();
		makeAttributesTab.performDefaults();

		generateMakefileButton.setSelection(true);
		generateInternalMakefileButton.setEnabled(true);
		generateInternalMakefileButton.setSelection(true);
		updateSymlinkLessbuild(true);
		withoutSymbolicLinksButton.setSelection(true);

		configurationManager.saveActualConfiguration();
	}

	@Override
	public boolean performOk() {
		if (!checkProperties()) {
			return false;
		}

		if (!saveProperties()) {
			return false;
		}

		final IPreferenceStore pluginPreferenceStore = Activator.getDefault().getPreferenceStore();
		// setting temporal variables to default for sure
		if (!pluginPreferenceStore.isDefault(MakefileCreationTab.TEMPORAL_TARGET_EXECUTABLE)) {
			pluginPreferenceStore.setToDefault(MakefileCreationTab.TEMPORAL_TARGET_EXECUTABLE);
		}
		if (!pluginPreferenceStore.isDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_SCRIPT)) {
			pluginPreferenceStore.setToDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_SCRIPT);
		}
		if (!pluginPreferenceStore.isDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_FLAGS)) {
			pluginPreferenceStore.setToDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_FLAGS);
		}
		if (!pluginPreferenceStore.isDefault(MakeAttributesTab.TEMPORAL_WORKINGDIRECTORY)) {
			pluginPreferenceStore.setToDefault(MakeAttributesTab.TEMPORAL_WORKINGDIRECTORY);
		}

		configurationManager.saveActualConfiguration();
		ProjectDocumentHandlingUtility.saveDocument(projectResource);
		
		TITANAutomaticProjectExporter.saveAllAutomatically(projectResource);
		
		evaluatePropertyStore();
		return true;
	}

	@Override
	public boolean performCancel() {
		loadProperties();
		evaluatePropertyStore();
		configurationManager.clearActualConfiguration();
		return true;
	}

	// Loading persistent property into a GUI element
	public void loadProperty(final String propertyName, final Button button) {
		String temp = "";
		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName));
		} catch (CoreException ce) {
			generateMakefileButton.setSelection(false);
			generateInternalMakefileButton.setSelection(false);
			withoutSymbolicLinksButton.setSelection(false);
			withoutSymbolicLinksButton.setText("Don't use symbolic links in the build process (internal only)");
		}

		button.setSelection(ProjectBuildPropertyData.TRUE_STRING.equals(temp) ? true : false);
	}

	public void loadProperties() {
		if (generateMakefileButton == null) {
			return;
		}

		loadProperty(ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY, generateMakefileButton);
		loadProperty(ProjectBuildPropertyData.GENERATE_INTERNAL_MAKEFILE_PROPERTY, generateInternalMakefileButton);
		loadProperty(ProjectBuildPropertyData.SYMLINKLESS_BUILD_PROPERTY, withoutSymbolicLinksButton);

		makefileCreationTab.loadProperties(projectResource);
		internalMakefileCreationTab.loadProperties(projectResource);
		makeAttributesTab.loadProperties(projectResource);
	}

	/**
	 * Saving state of GUI element into persistent property.
	 * 
	 * @param propertyName
	 *                the name of the property
	 * @param button
	 *                the button to extract the value from
	 * @return whether the operation was successful or not
	 */
	public boolean saveProperty(final String propertyName, final Button button) {
		String temp = button.getSelection() ? ProjectBuildPropertyData.TRUE_STRING : ProjectBuildPropertyData.FALSE_STRING;
		try {
			projectResource.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName), temp);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return false;
		}
		return true;
	}

	/**
	 * Does a general check on the contents of the property pages
	 * 
	 * @return true if no problem was found, false otherwise.
	 * */
	public boolean checkProperties() {
		if (generateMakefileButton == null || generateMakefileButton.isDisposed()) {
			return false;
		}

		boolean result = true;
		result &= makefileCreationTab.checkProperties(this);
		result &= internalMakefileCreationTab.checkProperties(this);
		result &= makeAttributesTab.checkProperties(this);

		try {
			if (!generateInternalMakefileButton.getSelection()) {
				IProject[] projects = projectResource.getReferencedProjects();
				for (IProject referencedProject : projects) {
					if (!referencedProject.isAccessible()) {
						setErrorMessage("The referenced project `" + referencedProject.getName() + "' is not accessible");
						result = false;
						continue;
					}
					if ( ! ProjectBuildPropertyData.useSymbolicLinks(referencedProject) ) {
						setErrorMessage("Will not be able to generate a makefile to project `" + projectResource.getName()
								+ "' with the external makefile generator as project `" + referencedProject.getName()
								+ "' is set to build without generating symbolic links");
						result = false;
					}
				}
			}
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		return result;
	}

	public boolean saveProperties() {
		if (generateMakefileButton == null) {
			return false;
		}
		boolean success = true;
		// saving properties if checking was successful
		success &= saveProperty(ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY, generateMakefileButton);
		success &= saveProperty(ProjectBuildPropertyData.GENERATE_INTERNAL_MAKEFILE_PROPERTY, generateInternalMakefileButton);
		success &= saveProperty(ProjectBuildPropertyData.SYMLINKLESS_BUILD_PROPERTY, withoutSymbolicLinksButton);

		success &= makefileCreationTab.saveProperties(projectResource);
		success &= internalMakefileCreationTab.saveProperties(projectResource);
		success &= makeAttributesTab.saveProperties(projectResource);
		setErrorMessage(null);
		return success;
	}
}
