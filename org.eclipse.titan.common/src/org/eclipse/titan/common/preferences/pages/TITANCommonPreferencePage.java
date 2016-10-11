/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.preferences.pages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.common.Activator;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TITANCommonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = "TITAN Common Preferences";

	private Group mergeGroup;
	private RadioGroupFieldEditor mergeOptions;

	public TITANCommonPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
		super.initialize();
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		mergeGroup = new Group(parent, SWT.SHADOW_ETCHED_OUT);
		mergeGroup.setText("Log file merge");
		mergeGroup.setLayout(new GridLayout(1, false));
		mergeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final String[][] labelAndValues = {
				{ "Overwrite file", org.eclipse.titan.common.preferences.PreferenceConstants.LOG_MERGE_OPTIONS_OVERWRITE },
				{ "Create a new file", org.eclipse.titan.common.preferences.PreferenceConstants.LOG_MERGE_OPTIONS_CREATE },
				{ "Ask", org.eclipse.titan.common.preferences.PreferenceConstants.LOG_MERGE_OPTIONS_ASK } };

		mergeOptions = new RadioGroupFieldEditor(org.eclipse.titan.common.preferences.PreferenceConstants.LOG_MERGE_OPTIONS,
				"If the merged file already exists:", 1, labelAndValues, mergeGroup, true);
		addField(mergeOptions);
	}

	@Override
	public boolean performOk() {
		final boolean result = super.performOk();

		final IEclipsePreferences preferences = Platform.getPreferencesService().getRootNode();
		final IEclipsePreferences node = (IEclipsePreferences) preferences.node(InstanceScope.SCOPE).node(ProductConstants.PRODUCT_ID_COMMON);
		if (node != null) {
			try {
				node.flush();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace("Error while saving the preferences", e);
			}
		}
		return result;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		mergeOptions.dispose();
		mergeGroup.dispose();
		super.dispose();
	}
}
