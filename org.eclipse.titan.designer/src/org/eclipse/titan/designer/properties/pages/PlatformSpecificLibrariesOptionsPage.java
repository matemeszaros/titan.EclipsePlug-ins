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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.designer.properties.data.ListConverter;
import org.eclipse.titan.designer.properties.data.PlatformSpecificLibrariesOptionsData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class PlatformSpecificLibrariesOptionsPage implements IOptionsPage {
	private Composite mainComposite;
	private MyListControl libraries;

	private final String platform;

	public PlatformSpecificLibrariesOptionsPage(final String platform) {
		this.platform = platform;
	}

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			libraries.dispose();
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

		libraries = new MyListControl(mainComposite, "Additional libraries (-l)", "library");

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		libraries.setEnabled(enabled);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = ResourceUtils.getPersistentProperty(project, ProjectBuildPropertyData.QUALIFIER, platform
					+ PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY);
		if (temp != null) {
			tempStorage.setValue(platform + PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY, temp);
		}
	}

	@Override
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String actualValue = ResourceUtils.getPersistentProperty(project, ProjectBuildPropertyData.QUALIFIER, platform
				+ PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY);
		String copyValue = tempStorage.getString(platform + PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY);
		return (actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null);

	}

	@Override
	public void performDefaults() {
		if (mainComposite == null) {
			return;
		}

		libraries.setEnabled(true);
		libraries.setValues(new String[] {});
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, platform
					+ PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY));
			libraries.setValues(ListConverter.convertToList(temp));
		} catch (CoreException e) {
			libraries.setValues(new String[] {});
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		try {
			QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, platform
					+ PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY);
			String newValue = ListConverter.convertFromList(libraries.getValues());
			String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		return true;
	}
}
