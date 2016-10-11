/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public final class TITANActionPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	static final String DESCRIPTION = "Preferences of the additional TITAN commands";

	private BooleanFieldEditor processExcludedResources;
	private BooleanFieldEditor defaultOmit;

	public TITANActionPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		processExcludedResources = new BooleanFieldEditor(PreferenceConstants.TITANACTIONS_PROCESSEXCLUDEDRESOURCES,
				"Process build excluded resources too", getFieldEditorParent());
		addField(processExcludedResources);

		defaultOmit = new BooleanFieldEditor(PreferenceConstants.TITANACTIONS_DEFAULT_AS_OMIT, "Default as omit", getFieldEditorParent());
		addField(defaultOmit);
		
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}


	@Override
	public void dispose() {
		processExcludedResources.dispose();
		defaultOmit.dispose();
	}
}
