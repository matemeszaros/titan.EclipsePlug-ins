/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.LinkerLibrariesOptionsData;
import org.eclipse.titan.designer.properties.data.ListConverter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class LinkerLibrariesOptionsPage implements IOptionsPage {
	private final IProject project;

	private Composite mainComposite;
	private MyFileListControl objects;
	private MyListControl libraries;
	private MyFolderListControl librarySearchPath;
	private Button disablePredefinedExtrnalDirs;

	public LinkerLibrariesOptionsPage(final IProject project) {
		this.project = project;
	}

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			objects.dispose();
			libraries.dispose();
			librarySearchPath.dispose();
			disablePredefinedExtrnalDirs.dispose();
		}
	}

	@Override
	public Composite createContents(final Composite parent) {
		if (mainComposite != null) {
			return mainComposite;
		}

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		objects = new MyFileListControl(mainComposite, project.getLocation().toOSString(), "additional object files ", "object");
		libraries = new MyListControl(mainComposite, "Libraries (-l)", "library");
		librarySearchPath = new MyFolderListControl(mainComposite, project.getLocation().toOSString(), "Library search path (-L)",
				"search path");

		disablePredefinedExtrnalDirs = new Button(mainComposite, SWT.CHECK);
		disablePredefinedExtrnalDirs.setText("Disable the entries of predefined libraries");
		disablePredefinedExtrnalDirs.setToolTipText("Right now the OPENSSL_DIR and XMLDIR entries.\n"
				+ " Please note that these folders are mandatory for the proper operation of TITAN.");

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		objects.setEnabled(enabled);
		libraries.setEnabled(enabled);
		librarySearchPath.setEnabled(enabled);
		disablePredefinedExtrnalDirs.setEnabled(enabled);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		for (int i = 0; i < LinkerLibrariesOptionsData.PROPERTIES.length; i++) {
			try {
				temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						LinkerLibrariesOptionsData.PROPERTIES[i]));
				if (temp != null) {
					tempStorage.setValue(LinkerLibrariesOptionsData.PROPERTIES[i], temp);
				}
			} catch (CoreException ce) {
				ErrorReporter.logExceptionStackTrace(ce);
			}
		}
	}

	@Override
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String actualValue = null;
		String copyValue = null;
		boolean result = false;
		for (int i = 0; i < LinkerLibrariesOptionsData.PROPERTIES.length; i++) {
			try {
				actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						LinkerLibrariesOptionsData.PROPERTIES[i]));
				copyValue = tempStorage.getString(LinkerLibrariesOptionsData.PROPERTIES[i]);
				result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				result = true;
			}
		}

		return result;
	}

	@Override
	public void performDefaults() {
		if (mainComposite == null) {
			return;
		}

		objects.setEnabled(true);
		objects.setValues(new String[] {});

		libraries.setEnabled(true);
		libraries.setValues(new String[] {});

		librarySearchPath.setEnabled(true);
		librarySearchPath.setValues(new String[] {});

		disablePredefinedExtrnalDirs.setEnabled(true);
		disablePredefinedExtrnalDirs.setSelection(false);
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.ADDITIONAL_OBJECTS_PROPERTY));
			objects.setValues(ListConverter.convertToList(temp));

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.LINKER_LIBRARIES_PROPERTY));
			libraries.setValues(ListConverter.convertToList(temp));

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.LINKER_LIBRARY_SEARCH_PATH_PROPERTY));
			librarySearchPath.setValues(ListConverter.convertToList(temp));

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.DISABLE_EXTERNAL_DIRS_PROPERTY));
			disablePredefinedExtrnalDirs.setSelection("true".equals(temp) ? true : false);
		} catch (CoreException e) {
			objects.setValues(new String[] {});
			libraries.setValues(new String[] {});
			librarySearchPath.setValues(new String[] {});
			disablePredefinedExtrnalDirs.setEnabled(false);
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		try {
			QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.ADDITIONAL_OBJECTS_PROPERTY);
			String newValue = ListConverter.convertFromList(objects.getValues());
			String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}

			qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, LinkerLibrariesOptionsData.LINKER_LIBRARIES_PROPERTY);
			newValue = ListConverter.convertFromList(libraries.getValues());
			oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}

			qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.LINKER_LIBRARY_SEARCH_PATH_PROPERTY);
			newValue = ListConverter.convertFromList(librarySearchPath.getValues());
			oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}

			qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.DISABLE_EXTERNAL_DIRS_PROPERTY);
			newValue = disablePredefinedExtrnalDirs.getSelection() ? "true" : "false";
			oldValue = project.getPersistentProperty(qualifiedName);
			if (!newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		return true;
	}
}
