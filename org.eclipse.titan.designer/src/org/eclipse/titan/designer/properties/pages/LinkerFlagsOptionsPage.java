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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.LinkerFlagsOptionsData;

/**
 * @author Jeno Balasko
 * */
public final class LinkerFlagsOptionsPage implements IOptionsPage {
	private Composite mainComposite;
	private Composite linkerFreeTextComposite;

	private Button useGoldLinker;
	private StringFieldEditor freeTextLinkerOptions;

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;
			
			linkerFreeTextComposite.dispose();
			linkerFreeTextComposite = null;
			
			useGoldLinker.dispose();
			freeTextLinkerOptions.dispose();
		}
	}

	@Override
	public Composite createContents(final Composite parent) {
		if (mainComposite != null) {
			return mainComposite;
		}

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		useGoldLinker = new Button(mainComposite, SWT.CHECK);
		useGoldLinker.setText("Use Gold Linker (-fuse-ld=gold)");
		
		linkerFreeTextComposite = new Composite(mainComposite, SWT.NONE);
		linkerFreeTextComposite.setLayout(new GridLayout());
		linkerFreeTextComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		freeTextLinkerOptions = 
				new StringFieldEditor(LinkerFlagsOptionsData.FREE_TEXT_OPTIONS_PROPERTY,"Other options (free text):", linkerFreeTextComposite );

		freeTextLinkerOptions.setStringValue("");
		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		useGoldLinker.setEnabled(enabled);
		freeTextLinkerOptions.setEnabled(true, linkerFreeTextComposite);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		for (int i = 0; i < LinkerFlagsOptionsData.PROPERTIES.length; i++) {
			try {
				temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						LinkerFlagsOptionsData.PROPERTIES[i]));
				if (temp != null) {
					tempStorage.setValue(LinkerFlagsOptionsData.PROPERTIES[i], temp);
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
		for (int i = 0; i < LinkerFlagsOptionsData.PROPERTIES.length; i++) {
			try {
				actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						LinkerFlagsOptionsData.PROPERTIES[i]));
				copyValue = tempStorage.getString(LinkerFlagsOptionsData.PROPERTIES[i]);
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

		setEnabled(true);

		useGoldLinker.setSelection(false);
		freeTextLinkerOptions.setStringValue("");
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		//TODO: useGoldLinker requires LINUX and gnu compiler
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		String temp;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerFlagsOptionsData.USE_GOLD_LINKER_PROPERTY));
			useGoldLinker.setSelection("true".equals(temp) ? true : false);
			
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerFlagsOptionsData.FREE_TEXT_OPTIONS_PROPERTY));
			freeTextLinkerOptions.setStringValue(temp);

		} catch (CoreException e) {
			useGoldLinker.setSelection(false);
			freeTextLinkerOptions.setStringValue("");
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		try {
			setProperty(project, LinkerFlagsOptionsData.USE_GOLD_LINKER_PROPERTY, useGoldLinker.getSelection() ? "true" : "false");
			setProperty(project, LinkerFlagsOptionsData.FREE_TEXT_OPTIONS_PROPERTY, freeTextLinkerOptions.getStringValue());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
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
