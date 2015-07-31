/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TTCN3PreprocessorOptionsData;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3PreprocessorOptionsPage implements IOptionsPage {
	private Composite mainComposite;

	private StringFieldEditor tool;

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			tool.dispose();
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

		tool = new StringFieldEditor(TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY, "TTCN-3 preprocessor:", mainComposite);
		tool.setStringValue(TTCN3PreprocessorOptionsData.DEFAULT_PREPROCESSOR);

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		tool.setEnabled(enabled, mainComposite);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (temp != null) {
			tempStorage.setValue(TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY, temp);
		}
	}

	@Override
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String actualValue = null;
		String copyValue = null;
		try {
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		copyValue = tempStorage.getString(TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY);
		return ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));

	}

	@Override
	public void performDefaults() {
		if (mainComposite == null) {
			return;
		}

		tool.setEnabled(true, mainComposite);
		tool.setStringValue("cpp");
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		String temp = tool.getStringValue();

		if (temp == null || "".equals(temp)) {
			page.setErrorMessage("The TTCN-3 preprocessor must be set.");
			return false;
		}

		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY));
			if (temp == null || temp.length() == 0) {
				temp = "cpp";
			}
			tool.setStringValue(temp);
		} catch (CoreException e) {
			tool.setStringValue("cpp");
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		try {
			QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY);
			String newValue = tool.getStringValue();
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
