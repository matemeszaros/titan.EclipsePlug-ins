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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;

/**
 * @author Kristof Szabados
 * */
public final class TITANFlagsOptionsPage implements IOptionsPage {
	private Composite mainComposite;

	private Button disableBER;
	private Button disableRAW;
	private Button disableTEXT;
	private Button disableXER;
	private Button disableJSON;
	private Button forceXER;
	private Button disableSubtypeChecking;
	private Button defaultAsOmit;
	//private Button enumHack;
	private Button forceOldFuncOutPar;
	private Button gccMessageFormat;
	private Button lineNumbersOnlyInMessages;
	private Button includeSourceInfo;
	private Button addSourceLineInfo;
	private Button suppressWarnings;
	private Button quietly;

	//private Composite namingRuleComposite;
	//private ComboFieldEditor namingRules;

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			disableBER.dispose();
			disableRAW.dispose();
			disableTEXT.dispose();
			disableXER.dispose();
			disableJSON.dispose();
			forceXER.dispose();
			disableSubtypeChecking.dispose();
			defaultAsOmit.dispose();
			forceOldFuncOutPar.dispose();
			gccMessageFormat.dispose();
			lineNumbersOnlyInMessages.dispose();
			includeSourceInfo.dispose();
			addSourceLineInfo.dispose();
			suppressWarnings.dispose();
			quietly.dispose();

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

		disableBER = new Button(mainComposite, SWT.CHECK);
		disableBER.setText("Disable BER encoding (-b)");

		disableRAW = new Button(mainComposite, SWT.CHECK);
		disableRAW.setText("Disable RAW encoding (-r)");

		disableTEXT = new Button(mainComposite, SWT.CHECK);
		disableTEXT.setText("Disable TEXT encoding (-x)");

		disableXER = new Button(mainComposite, SWT.CHECK);
		disableXER.setText("Disable XER encoding (-X)");
		
		disableJSON = new Button(mainComposite, SWT.CHECK);
		disableJSON.setText("Disable JSON encoder (-j)");

		forceXER = new Button(mainComposite, SWT.CHECK);
		forceXER.setText("Force XER in ASN.1 files (-a)");

		disableSubtypeChecking = new Button(mainComposite, SWT.CHECK);
		disableSubtypeChecking.setText("Disable subtype checking (-y)");

		defaultAsOmit = new Button(mainComposite, SWT.CHECK);
		defaultAsOmit.setText("Treat default fields as omit (-d)");

		forceOldFuncOutPar = new Button(mainComposite, SWT.CHECK);
		forceOldFuncOutPar.setText("Force old function out par handling (-Y)");

		gccMessageFormat = new Button(mainComposite, SWT.CHECK);
		gccMessageFormat.setText("Emulate gcc error/warning message format (-g)");

		lineNumbersOnlyInMessages = new Button(mainComposite, SWT.CHECK);
		lineNumbersOnlyInMessages.setText("Use only line numbers in error/warning messages (-i)");

		includeSourceInfo = new Button(mainComposite, SWT.CHECK);
		includeSourceInfo.setText("Include source line info in C++ code (-l)");

		addSourceLineInfo = new Button(mainComposite, SWT.CHECK);
		addSourceLineInfo.setText("Add source line info for logging (-L)");

		suppressWarnings = new Button(mainComposite, SWT.CHECK);
		suppressWarnings.setText("Suppress warnings (-w)");

		quietly = new Button(mainComposite, SWT.CHECK);
		quietly.setText("Suppress all messages (quiet mode) (-q)");

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		disableBER.setEnabled(enabled);
		disableRAW.setEnabled(enabled);
		disableTEXT.setEnabled(enabled);
		disableXER.setEnabled(enabled);
		disableJSON.setEnabled(enabled);
		forceXER.setEnabled(enabled);
		disableSubtypeChecking.setEnabled(enabled);
		defaultAsOmit.setEnabled(enabled);
		forceOldFuncOutPar.setEnabled(enabled);
		gccMessageFormat.setEnabled(enabled);
		lineNumbersOnlyInMessages.setEnabled(enabled);
		includeSourceInfo.setEnabled(enabled);
		addSourceLineInfo.setEnabled(enabled);
		suppressWarnings.setEnabled(enabled);
		quietly.setEnabled(enabled);

	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		for (int i = 0; i < TITANFlagsOptionsData.PROPERTIES.length; i++) {
			try {
				temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						TITANFlagsOptionsData.PROPERTIES[i]));
				if (temp != null) {
					tempStorage.setValue(TITANFlagsOptionsData.PROPERTIES[i], temp);
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
		for (int i = 0; i < TITANFlagsOptionsData.PROPERTIES.length; i++) {
			try {
				actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						TITANFlagsOptionsData.PROPERTIES[i]));
				copyValue = tempStorage.getString(TITANFlagsOptionsData.PROPERTIES[i]);
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

		disableBER.setSelection(false);
		disableRAW.setSelection(false);
		disableTEXT.setSelection(false);
		disableXER.setSelection(false);
		disableJSON.setSelection(false);
		forceXER.setSelection(false);
		disableSubtypeChecking.setSelection(false);
		defaultAsOmit.setSelection(false);
		forceOldFuncOutPar.setSelection(false);
		gccMessageFormat.setSelection(false);
		lineNumbersOnlyInMessages.setSelection(false);
		includeSourceInfo.setSelection(true);
		addSourceLineInfo.setSelection(true);
		suppressWarnings.setSelection(false);
		quietly.setSelection(false);

	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		boolean xerDisabled = disableXER.getSelection();
		boolean xerOnASN1Forced = forceXER.getSelection();

		if (xerDisabled && xerOnASN1Forced) {
			page.setErrorMessage("Forcing XER in ASN.1 files and disabling XER are incompatible options.");
			return false;
		}

		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		String temp;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DISABLE_BER_PROPERTY));
			disableBER.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DISABLE_RAW_PROPERTY));
			disableRAW.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DISABLE_TEXT_PROPERTY));
			disableTEXT.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DISABLE_XER_PROPERTY));
			disableXER.setSelection("true".equals(temp) ? true : false);
			
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DISABLE_JSON_PROPERTY));
			disableJSON.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.FORCE_XER_IN_ASN1_PROPERTY));
			forceXER.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DISABLE_SUBTYPE_CHECKING_PROPERTY));
			disableSubtypeChecking.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.DEFAULT_AS_OMIT_PROPERTY));
			defaultAsOmit.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.FORCE_OLD_FUNC_OUT_PAR_PROPERTY));
			forceOldFuncOutPar.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.GCC_MESSAGE_FORMAT_PROPERTY));
			gccMessageFormat.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.LINE_NUMBERS_ONLY_IN_MESSAGES_PROPERTY));
			lineNumbersOnlyInMessages.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.INCLUDE_SOURCEINFO_PROPERTY));
			includeSourceInfo.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.ADD_SOURCELINEINFO_PROPERTY));
			if (temp == null) {
				addSourceLineInfo.setSelection(false);
			} else {
				addSourceLineInfo.setSelection("true".equals(temp) ? true : false);
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.SUPPRESS_WARNINGS_PROPERTY));
			suppressWarnings.setSelection("true".equals(temp) ? true : false);

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.QUIETLY_PROPERTY));
			quietly.setSelection("true".equals(temp) ? true : false);

		} catch (CoreException e) {
			disableBER.setSelection(false);
			disableRAW.setSelection(false);
			disableTEXT.setSelection(false);
			disableXER.setSelection(false);
			disableJSON.setSelection(false);
			forceXER.setSelection(false);
			disableSubtypeChecking.setSelection(false);
			defaultAsOmit.setSelection(false);
			forceOldFuncOutPar.setSelection(false);
			gccMessageFormat.setSelection(false);
			lineNumbersOnlyInMessages.setSelection(false);
			includeSourceInfo.setSelection(false);
			addSourceLineInfo.setSelection(false);
			suppressWarnings.setSelection(false);
			quietly.setSelection(false);

		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		try {
			setProperty(project, TITANFlagsOptionsData.DISABLE_BER_PROPERTY, disableBER.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.DISABLE_RAW_PROPERTY, disableRAW.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.DISABLE_TEXT_PROPERTY, disableTEXT.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.DISABLE_XER_PROPERTY, disableXER.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.DISABLE_JSON_PROPERTY, disableJSON.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.FORCE_XER_IN_ASN1_PROPERTY, forceXER.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.DISABLE_SUBTYPE_CHECKING_PROPERTY, disableSubtypeChecking.getSelection() ? "true"
					: "false");
			setProperty(project, TITANFlagsOptionsData.DEFAULT_AS_OMIT_PROPERTY, defaultAsOmit.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.FORCE_OLD_FUNC_OUT_PAR_PROPERTY, forceOldFuncOutPar.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.GCC_MESSAGE_FORMAT_PROPERTY, gccMessageFormat.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.LINE_NUMBERS_ONLY_IN_MESSAGES_PROPERTY,
					lineNumbersOnlyInMessages.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.INCLUDE_SOURCEINFO_PROPERTY, includeSourceInfo.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.ADD_SOURCELINEINFO_PROPERTY, addSourceLineInfo.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.SUPPRESS_WARNINGS_PROPERTY, suppressWarnings.getSelection() ? "true" : "false");
			setProperty(project, TITANFlagsOptionsData.QUIETLY_PROPERTY, quietly.getSelection() ? "true" : "false");
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
