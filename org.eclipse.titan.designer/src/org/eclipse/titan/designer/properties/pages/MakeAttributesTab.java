/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.titan.common.fieldeditors.TITANResourceLocatorFieldEditor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class MakeAttributesTab {
	private static final String MAKEFLAGS_STRINGFIELDEDITOR_LABEL = "Makefile flags:";

	private TabItem makeAttributesTabItem;
	private static final String MAKEATTRIBUTES_TEXT = "Make attributes";
	private Composite makeAttributesComposite;
	private Composite makeFlagsComposite;
	private Composite makefileScriptComposite;

	public static final String TEMPORAL_MAKEFILE_SCRIPT = ProductConstants.PRODUCT_ID_DESIGNER + ".temporalMakefileUpdateScript";
	private TITANResourceLocatorFieldEditor temporalMakefileScriptFileFieldEditor;

	public static final String TEMPORAL_MAKEFILE_FLAGS = ProductConstants.PRODUCT_ID_DESIGNER + ".temporalMakefileFlags";
	private StringFieldEditor temporalMakefileFlagsStringFieldEditor;

	public static final String TEMPORAL_WORKINGDIRECTORY = ProductConstants.PRODUCT_ID_DESIGNER + ".temporalWorkingDir";
	private Composite workingDirComposite;
	private TITANResourceLocatorFieldEditor workingDirFieldEditor;

	private Label buildLeveltext;
	private Combo buildLevel;

	private Composite buildLevelComposite;

	private final IProject project;
	private final ProjectBuildPropertyPage page;

	public MakeAttributesTab(final IProject project, final ProjectBuildPropertyPage page) {
		this.project = project;
		this.page = page;
	}

	/**
	 * Disposes the SWT resources allocated by this tab page.
	 */
	public void dispose() {
		buildLevel.dispose();
		buildLevelComposite.dispose();
		temporalMakefileFlagsStringFieldEditor.dispose();
		makeFlagsComposite.dispose();
		workingDirFieldEditor.dispose();
		temporalMakefileScriptFileFieldEditor.dispose();
		makefileScriptComposite.dispose();

		makeAttributesComposite.dispose();
		makeAttributesTabItem.dispose();
	}

	/**
	 * Creates and returns the SWT control for the customized body of this
	 * TabItem under the given parent TabFolder.
	 * <p>
	 * 
	 * @param tabFolder
	 *                the parent TabFolder
	 * @return the new TabItem
	 */
	protected TabItem createContents(final TabFolder tabFolder) {
		makeAttributesTabItem = new TabItem(tabFolder, SWT.BORDER);
		makeAttributesTabItem.setText(MAKEATTRIBUTES_TEXT);
		makeAttributesTabItem.setToolTipText("Settings controlling the usage of the makefile.");

		makeAttributesComposite = new Composite(tabFolder, SWT.MULTI);
		makeAttributesComposite.setEnabled(true);
		makeAttributesComposite.setLayout(new GridLayout());

		// We had to create a new Composite for formatting purposes.
		// The FileFieldEditor doesn't have a setLayoutData function
		// so we couldn't set a GridData directly. Instead we
		// encapsulated
		// it into a new Composite that has the missing method.
		makefileScriptComposite = new Composite(makeAttributesComposite, SWT.NONE);
		GridLayout makefileScriptLayout = new GridLayout();
		makefileScriptComposite.setLayout(makefileScriptLayout);
		GridData makefileScriptData = new GridData(GridData.FILL);
		makefileScriptData.grabExcessHorizontalSpace = true;
		makefileScriptData.horizontalAlignment = SWT.FILL;
		makefileScriptData.horizontalSpan = 2;
		makefileScriptComposite.setLayoutData(makefileScriptData);

		temporalMakefileScriptFileFieldEditor = new TITANResourceLocatorFieldEditor(TEMPORAL_MAKEFILE_SCRIPT, "Makefile updater script:",
				makefileScriptComposite, IResource.FILE, project.getLocation().toOSString());
		temporalMakefileScriptFileFieldEditor.setEnabled(true, makefileScriptComposite);
		temporalMakefileScriptFileFieldEditor.getLabelControl(makefileScriptComposite).setToolTipText(
				"The location of an external script,\n" + " used to fine tune newly generated makefiles.\n"
						+ "This field is optional.");

		buildLevelComposite = new Composite(makeAttributesComposite, SWT.NONE);
		GridLayout buildLevelLayout = new GridLayout(2, false);
		buildLevelComposite.setLayout(buildLevelLayout);
		buildLevelComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		buildLeveltext = new Label(buildLevelComposite, SWT.NONE);
		buildLeveltext.setText("Build level: ");
		buildLeveltext.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		buildLeveltext.setToolTipText("The way the build process should behave, from now on.");

		buildLevel = new Combo(buildLevelComposite, SWT.READ_ONLY);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_0);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_1);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_2);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_2_5);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_3);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_4);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_4_5);
		buildLevel.add(MakeAttributesData.BUILD_LEVEL_5);
		buildLevel.setText(MakeAttributesData.BUILD_LEVEL_5);
		buildLevel.setEnabled(true);
		buildLevel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateBuildLevelDependecies();
			}
		});

		makeFlagsComposite = new Composite(makeAttributesComposite, SWT.NONE);
		GridLayout makeFlagsLayout = new GridLayout();
		makeFlagsComposite.setLayout(makeFlagsLayout);
		GridData makeFlagsData = new GridData(GridData.FILL);
		makeFlagsData.grabExcessHorizontalSpace = true;
		makeFlagsData.horizontalAlignment = SWT.FILL;
		makeFlagsData.horizontalSpan = 2;
		makeFlagsComposite.setLayoutData(makeFlagsData);

		temporalMakefileFlagsStringFieldEditor = new StringFieldEditor(TEMPORAL_MAKEFILE_FLAGS, MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " all",
				makeFlagsComposite);
		temporalMakefileFlagsStringFieldEditor.setEnabled(true, makeFlagsComposite);
		temporalMakefileFlagsStringFieldEditor.getLabelControl(makeFlagsComposite).setToolTipText(
				"The flags, the makefile will be called with when building the project.");
		temporalMakefileFlagsStringFieldEditor.setPage(page);

		workingDirComposite = new Composite(makeAttributesComposite, SWT.NONE);
		GridLayout workingDirLayout2 = new GridLayout();
		workingDirComposite.setLayout(workingDirLayout2);
		GridData workingDirData2 = new GridData(GridData.FILL);
		workingDirData2.grabExcessHorizontalSpace = true;
		workingDirData2.horizontalAlignment = SWT.FILL;
		workingDirData2.horizontalSpan = 2;
		workingDirComposite.setLayoutData(workingDirData2);

		workingDirFieldEditor = new TITANResourceLocatorFieldEditor(MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY,
				"working directory:", workingDirComposite, IResource.FOLDER, project.getLocation().toOSString());
		workingDirFieldEditor.getLabelControl(workingDirComposite).setToolTipText(
				"The location of the working directory. Where the build process will take place");
		workingDirFieldEditor.setPage(page);

		// update the tooltip of the build level combo
		updateBuildLevelDependecies();

		makeAttributesTabItem.setControl(makeAttributesComposite);
		return makeAttributesTabItem;
	}

	/**
	 * This method changes the label of
	 * temporalMakefileFlagsStringFieldEditor to reflect the actual build
	 * level. It is done by appending the appropriate makefile target to the
	 * original label.
	 */
	protected void updateBuildLevelDependecies() {
		int level = buildLevel.getSelectionIndex();
		switch (level) {
		case 0: //Level0
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " check");
			buildLevel.setToolTipText("Only syntactic then semantic checks are done.\nNo executable is generated.");
			break;
		case 1: //Level1
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " compile");
			buildLevel.setToolTipText("Full checking then C++ code generated.\nNor objects nor executable are generated.");
			break;
		case 2: //Level2
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " objects");
			buildLevel.setToolTipText(
					"Full checking then C++ code generated\n"
					+"Compilation into object files but without refreshing the dependencies\n"
					+"No executable is generated.");
			break;
		case 3: //Level2.5
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " objects");
			buildLevel.setToolTipText(
					"Full checking then C++ code generated.\n"
					+"Dependencies are heuristically updated.\n"
					+"Compilation into object files.\n"
					+"No executable is generated.");
			break;
		case 4: //Level3
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " objects");
			buildLevel.setToolTipText(
					"Full checking then C++ code generated.\n"
					+"Dependencies are updated.\n"
					+"Compilation into object files.\n"
					+"No executable is generated.");
			break;
		case 5: //Level4
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " all");
			buildLevel.setToolTipText("Full compilation.\nThe executable is generated.");
			break;
		case 6: //Level4.5
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " all");
			buildLevel.setToolTipText("Full compilation with heuristical refreshing of the dependencies.\nThe executable is generated.");
			break;
		case 7: //Level5
			temporalMakefileFlagsStringFieldEditor.setLabelText(MAKEFLAGS_STRINGFIELDEDITOR_LABEL + " all");
			buildLevel.setToolTipText("Full compilation with the refreshing of the dependencies.\nThe executable is generated.");
			break;
		default:
			break;
		}
		makeFlagsComposite.layout(true);
	}

	/**
	 * Handles the enabling/disabling of controls when the automatic
	 * Makefile generation is enabled/disabled.
	 * 
	 * @param value
	 *                the actual state of automatic Makefile generation.
	 * */
	protected void setMakefileGenerationEnabled(final boolean value) {
		temporalMakefileScriptFileFieldEditor.setEnabled(value, makefileScriptComposite);
	}

	/**
	 * Copies the actual values into the provided preference storage.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * */
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.BUILD_LEVEL_PROPERTY));
			temp = MakeAttributesData.getBuildLevel(temp);			
			tempStorage.setValue(MakeAttributesData.BUILD_LEVEL_PROPERTY, temp);
	
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY, temp);
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY, temp);
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY, temp);
			}
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
	}

	/**
	 * Evaluates the properties on the option page, and compares them with
	 * the saved values.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * 
	 * @return true if the values in the real and the temporal storage are
	 *         different (they have changed), false otherwise.
	 * */
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		boolean result = false;

		try {
			String actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.BUILD_LEVEL_PROPERTY));
			actualValue = MakeAttributesData.getBuildLevel(actualValue);
			String copyValue = tempStorage.getString(MakeAttributesData.BUILD_LEVEL_PROPERTY);
			result |= !actualValue.equals(copyValue);

			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY));
			copyValue = tempStorage.getString(MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY);
			result |= actualValue == null || !actualValue.equals(copyValue);

			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY));
			copyValue = tempStorage.getString(MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY);
			result |= actualValue == null || !actualValue.equals(copyValue);

			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY));
			copyValue = tempStorage.getString(MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY);
			result |= actualValue == null || !actualValue.equals(copyValue);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		return result;
	}

	/**
	 * Performs special processing when the ProjectBuildProperty page's
	 * Defaults button has been pressed.
	 */
	protected void performDefaults() {
		buildLevel.setEnabled(true);
		buildLevel.setText(MakeAttributesData.BUILD_LEVEL_5);

		temporalMakefileScriptFileFieldEditor.setEnabled(true, makefileScriptComposite);
		temporalMakefileScriptFileFieldEditor.setStringValue("");
		temporalMakefileFlagsStringFieldEditor.setEnabled(true, makeFlagsComposite);
		temporalMakefileFlagsStringFieldEditor.setStringValue("");
		workingDirFieldEditor.setEnabled(true, workingDirComposite);
		workingDirFieldEditor.setStringValue("bin");
	}

	/**
	 * Checks the properties of this page for errors.
	 * 
	 * @param page
	 *                the property page to report the errors on if found.
	 * @return true if no error was found, false otherwise.
	 * */
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		if (!"".equals(temporalMakefileScriptFileFieldEditor.getStringValue())) {
			if (!temporalMakefileScriptFileFieldEditor.isValid()) {
				page.setErrorMessage(temporalMakefileScriptFileFieldEditor.getErrorMessage());
				return false;
			}

			URI uri = TITANPathUtilities.resolvePathURI(temporalMakefileScriptFileFieldEditor.getStringValue(), project.getLocation()
					.toOSString());

			File file = URIUtil.toPath(uri).toFile();
			if (!file.exists()) {
				page.setErrorMessage("Makefile updater script must exist !");
				return false;
			}
		}

		if ("".equals(workingDirFieldEditor.getStringValue()) || !workingDirFieldEditor.isValid()) {
			String errorMessage = workingDirFieldEditor.getErrorMessage();
			if (errorMessage == null) {
				errorMessage = "The working directory must be set";
			}
			page.setErrorMessage(errorMessage);
			return false;
		}

		URI uri = TITANPathUtilities.resolvePathURI(workingDirFieldEditor.getStringValue(), project.getLocation().toOSString());
		uri = uri.normalize();
		if (project.getLocationURI().equals(uri)) {
			page.setErrorMessage("The working directory of the project and its location can not be the same folder.");
		}

		return true;
	}

	/**
	 * Loads the properties from the property storage, into the user
	 * interface elements.
	 * 
	 * @param project
	 *                the project to load the properties from.
	 * */
	public void loadProperties(final IProject project) {
		String temp;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.BUILD_LEVEL_PROPERTY));
			temp = MakeAttributesData.getBuildLevel(temp);
			buildLevel.setText(temp);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY));
			temporalMakefileScriptFileFieldEditor.setStringValue(temp);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY));
			temporalMakefileFlagsStringFieldEditor.setStringValue(temp);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY));
			if (temp == null) {
				workingDirFieldEditor.setStringValue("bin");
			} else {
				workingDirFieldEditor.setStringValue(temp);
			}

			updateBuildLevelDependecies();
		} catch (CoreException e) {
			buildLevel.setText(MakeAttributesData.BUILD_LEVEL_5);
			temporalMakefileScriptFileFieldEditor.setStringValue("");
			temporalMakefileFlagsStringFieldEditor.setStringValue("");
			workingDirFieldEditor.setStringValue("bin");
		}
	}

	/**
	 * Saves the properties to the property storage, from the user interface
	 * elements.
	 * 
	 * @param project
	 *                the project to save the properties to.
	 * @return true if the save was successful, false otherwise.
	 * */
	public boolean saveProperties(final IProject project) {
		try {
			setProperty(project, MakeAttributesData.BUILD_LEVEL_PROPERTY, buildLevel.getText());

			String temp = temporalMakefileScriptFileFieldEditor.getStringValue();
			URI path = URIUtil.toURI(temp);
			URI resolvedPath = TITANPathUtilities.resolvePathURI(temp, project.getLocation().toOSString());
			if (path.equals(resolvedPath)) {
				temp = PathUtil.getRelativePath(project.getLocation().toOSString(), temp);
			}
			setProperty(project, MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY, temp);
			setProperty(project, MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY,
					temporalMakefileFlagsStringFieldEditor.getStringValue());

			temp = workingDirFieldEditor.getStringValue();
			path = URIUtil.toURI(temp);
			resolvedPath = TITANPathUtilities.resolvePathURI(temp, project.getLocation().toOSString());
			if (path.equals(resolvedPath)) {
				temp = PathUtil.getRelativePath(project.getLocation().toOSString(), temp);
			}

			setProperty(project, MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY, temp);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return false;
		}

		return true;
	}

	/**
	 * Sets the provided value, on the provided project, for the provided
	 * property.
	 * 
	 * @param project
	 *                the project to work on.
	 * @param name
	 *                the name of the property to change.
	 * @param value
	 *                the value to set.
	 * 
	 * @exception CoreException
	 *                    if this method fails. Reasons include:
	 *                    <ul>
	 *                    <li>This project does not exist.</li>
	 *                    <li>This project is not local.</li>
	 *                    <li>This project is a project that is not open.</li>
	 *                    <li>Resource changes are disallowed during certain
	 *                    types of resource change event notification. See
	 *                    <code>IResourceChangeEvent</code> for more
	 *                    details.</li>
	 *                    </ul>
	 * */
	private void setProperty(final IProject project, final String name, final String value) throws CoreException {
		QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, name);
		String oldValue = project.getPersistentProperty(qualifiedName);
		if (value != null && !value.equals(oldValue)) {
			project.setPersistentProperty(qualifiedName, value);
		}
	}
}
