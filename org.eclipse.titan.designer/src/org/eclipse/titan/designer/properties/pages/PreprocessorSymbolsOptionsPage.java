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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.ListConverter;
import org.eclipse.titan.designer.properties.data.PreprocessorSymbolsOptionsData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class PreprocessorSymbolsOptionsPage implements IOptionsPage {
	private final boolean ttcn3symbols;

	private Composite mainComposite;
	private MyListControl definedSymbols;
	private MyListControl undefinedSymbols;

	public PreprocessorSymbolsOptionsPage(final boolean ttcn3symbols) {
		this.ttcn3symbols = ttcn3symbols;
	}

	@Override
	public Composite createContents(final Composite parent) {
		if (mainComposite != null) {
			return mainComposite;
		}

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		definedSymbols = new MyListControl(mainComposite, "Defined Symbols (-D)", "symbol");
		undefinedSymbols = new MyListControl(mainComposite, "Undefined Symbols (-U)", "symbol");

		return mainComposite;
	}

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			definedSymbols.dispose();
			undefinedSymbols.dispose();
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		definedSymbols.setEnabled(enabled);
		undefinedSymbols.setEnabled(enabled);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String definesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_DEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_DEFINES_PROPERTY;
		String undefinesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_UNDEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_UNDEFINES_PROPERTY;
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, definesProperty));
			if (temp != null) {
				tempStorage.setValue(definesProperty, temp);
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, undefinesProperty));
			if (temp != null) {
				tempStorage.setValue(undefinesProperty, temp);
			}
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

	}

	@Override
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String definesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_DEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_DEFINES_PROPERTY;
		String undefinesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_UNDEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_UNDEFINES_PROPERTY;
		boolean result = false;
		String actualValue = null;
		String copyValue = null;
		try {
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, definesProperty));
			copyValue = tempStorage.getString(definesProperty);
			result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));

			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, undefinesProperty));
			copyValue = tempStorage.getString(undefinesProperty);
			result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		if (result
				&& ttcn3symbols
				&& Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
						PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			GlobalParser.getProjectSourceParser(project).clearSemanticInformation();
			GlobalParser.getProjectSourceParser(project).analyzeAll();
		}

		return result;
	}

	@Override
	public void performDefaults() {
		if (mainComposite == null) {
			return;
		}

		definedSymbols.setEnabled(true);
		definedSymbols.setValues(new String[] {});

		undefinedSymbols.setEnabled(true);
		undefinedSymbols.setValues(new String[] {});
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		String definesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_DEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_DEFINES_PROPERTY;
		String undefinesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_UNDEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_UNDEFINES_PROPERTY;

		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, definesProperty));
			definedSymbols.setValues(ListConverter.convertToList(temp));

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, undefinesProperty));
			undefinedSymbols.setValues(ListConverter.convertToList(temp));
		} catch (CoreException e) {
			definedSymbols.setValues(new String[] {});
			undefinedSymbols.setValues(new String[] {});
		}
	}

	@Override
	public boolean saveProperties(final IProject project) {
		String definesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_DEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_DEFINES_PROPERTY;
		String undefinesProperty = ttcn3symbols ? PreprocessorSymbolsOptionsData.TTCN3_PREPROCESSOR_UNDEFINES_PROPERTY
				: PreprocessorSymbolsOptionsData.PREPROCESSOR_UNDEFINES_PROPERTY;

		try {
			QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, definesProperty);
			String newValue = ListConverter.convertFromList(definedSymbols.getValues());
			String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}

			qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, undefinesProperty);
			newValue = ListConverter.convertFromList(undefinedSymbols.getValues());
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
