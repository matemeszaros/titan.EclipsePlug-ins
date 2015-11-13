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
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.pages.ComboFieldEditor;
import org.eclipse.titan.designer.properties.data.COptimalizationOptionsData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class COptimalizationOptionsPage implements IOptionsPage {
	private Composite mainComposite;

	private ComboFieldEditor optimalizationLevel;
	private StringFieldEditor otherFlags;

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			optimalizationLevel.dispose();
			otherFlags.dispose();
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

		optimalizationLevel = new ComboFieldEditor(COptimalizationOptionsData.OPTIMIZATION_LEVEL_PROPERTY, "Optimization level",
				new String[][] { { "Unspecified", "Unspecified" }, { "None (-O0)", "None" },
						{ "Minor optimizations (-O1)", "Minor optimizations" },
						{ "Common optimizations (-O2)", "Common optimizations" },
						{ "Optimize for speed (-O3)", "Optimize for speed" },
						{ "Optimize for size (-Os)", "Optimize for size" } }, mainComposite);

		otherFlags = new StringFieldEditor(COptimalizationOptionsData.OTHER_OPTIMIZATION_FLAGS_PROPERTY, "Other optimization flags:",
				mainComposite);

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		optimalizationLevel.setEnabled(enabled, mainComposite);
		otherFlags.setEnabled(enabled, mainComposite);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		for (int i = 0; i < COptimalizationOptionsData.PROPERTIES.length; i++) {
			try {
				temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						COptimalizationOptionsData.PROPERTIES[i]));
				if (temp != null) {
					tempStorage.setValue(COptimalizationOptionsData.PROPERTIES[i], temp);
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
		for (int i = 0; i < COptimalizationOptionsData.PROPERTIES.length; i++) {
			try {
				actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						COptimalizationOptionsData.PROPERTIES[i]));
				copyValue = tempStorage.getString(COptimalizationOptionsData.PROPERTIES[i]);
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

		optimalizationLevel.setEnabled(true, mainComposite);
		optimalizationLevel.setSelectedValue("Common optimizations");

		otherFlags.setEnabled(true, mainComposite);
		otherFlags.setStringValue("");
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					COptimalizationOptionsData.OPTIMIZATION_LEVEL_PROPERTY));
			if (temp != null && temp.length() != 0) {
				optimalizationLevel.setSelectedValue(temp);
			} else {
				optimalizationLevel.setSelectedValue("Common optimizations");
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					COptimalizationOptionsData.OTHER_OPTIMIZATION_FLAGS_PROPERTY));
			otherFlags.setStringValue(temp);
		} catch (CoreException e) {
			optimalizationLevel.setSelectedValue("Common optimizations");
			otherFlags.setStringValue("");
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		try {
			QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					COptimalizationOptionsData.OPTIMIZATION_LEVEL_PROPERTY);
			String newValue = optimalizationLevel.getActualValue();
			String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}

			qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					COptimalizationOptionsData.OTHER_OPTIMIZATION_FLAGS_PROPERTY);
			newValue = otherFlags.getStringValue();
			oldValue = project.getPersistentProperty(qualifiedName);
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
