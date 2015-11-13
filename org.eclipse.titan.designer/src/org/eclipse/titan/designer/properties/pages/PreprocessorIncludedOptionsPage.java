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
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ListConverter;
import org.eclipse.titan.designer.properties.data.PreprocessorIncludedOptionsData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class PreprocessorIncludedOptionsPage implements IOptionsPage {
	private final boolean preprocessor;
	private final IProject project;

	private Composite mainComposite;
	private MyFolderListControl includes;

	public PreprocessorIncludedOptionsPage(final IProject project, final boolean preprocessor) {
		this.project = project;
		this.preprocessor = preprocessor;
	}

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			includes.dispose();
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

		includes = new MyFolderListControl(mainComposite, project.getLocation().toOSString(), "Include paths (-I)", "path");

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		includes.setEnabled(enabled);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String property = preprocessor ? PreprocessorIncludedOptionsData.TTCN3_PREPROCESSOR_INCLUDES_PROPERTY
				: PreprocessorIncludedOptionsData.PREPROCESSOR_INCLUDES_PROPERTY;
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, property));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (temp != null) {
			tempStorage.setValue(property, temp);
		}
	}

	@Override
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String property = preprocessor ? PreprocessorIncludedOptionsData.TTCN3_PREPROCESSOR_INCLUDES_PROPERTY
				: PreprocessorIncludedOptionsData.PREPROCESSOR_INCLUDES_PROPERTY;
		String actualValue = null;
		String copyValue = null;
		try {
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, property));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		copyValue = tempStorage.getString(property);
		return ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));

	}

	@Override
	public void performDefaults() {
		if (mainComposite == null) {
			return;
		}

		includes.setEnabled(true);
		includes.setValues(new String[] {});
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		String property = preprocessor ? PreprocessorIncludedOptionsData.TTCN3_PREPROCESSOR_INCLUDES_PROPERTY
				: PreprocessorIncludedOptionsData.PREPROCESSOR_INCLUDES_PROPERTY;

		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, property));
			includes.setValues(ListConverter.convertToList(temp));
		} catch (CoreException e) {
			includes.setValues(new String[] {});
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		String property = preprocessor ? PreprocessorIncludedOptionsData.TTCN3_PREPROCESSOR_INCLUDES_PROPERTY
				: PreprocessorIncludedOptionsData.PREPROCESSOR_INCLUDES_PROPERTY;

		try {
			QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, property);
			String newValue = ListConverter.convertFromList(includes.getValues());
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
