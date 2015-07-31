/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.StringListEditor;
import org.eclipse.titan.log.viewer.utils.ImportExportUtils;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class FilteredComponentsPrefPage	extends LogViewerPreferenceRootPage {

	private StringListEditor filterComponentsEditor;
	
	/**
	 * Constructor 
	 */
	public FilteredComponentsPrefPage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("FilteredComponentsPrefPage.0")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
		this.filterComponentsEditor = new StringListEditor(PreferenceConstants.PREF_FILTER_COMPONENT_ID,
													  "", //$NON-NLS-1$
													  getFieldEditorParent(),
													  false);
		this.filterComponentsEditor.setPreferenceStore(getPreferenceStore());
		addField(this.filterComponentsEditor);
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_FILTERED_COMP_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences() {
		Map<String, String> currentPrefs = new HashMap<String, String>();
		String signalFilter = ""; //$NON-NLS-1$
		String[] elements = this.filterComponentsEditor.getElements();
		for (String element : elements) {
			signalFilter = signalFilter.concat(element + PreferenceConstants.PREFERENCE_DELIMITER);
		}
		currentPrefs.put(this.filterComponentsEditor.getPreferenceName(), signalFilter);
		return currentPrefs;
	}
	
	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String[]> getCurrentPreferencesSeparated() {
		Map<String, String[]> currentPrefs = new HashMap<String, String[]>();
		currentPrefs.put(this.filterComponentsEditor.getPreferenceName(), this.filterComponentsEditor.getElements());
		return currentPrefs;
	}

	@Override
	protected void exportPreferences() {
		ImportExportUtils.exportSettings(getPageId(), getCurrentPreferencesSeparated(), true);
	}
	
	@Override
	protected void importPreferences() {
		Map<String, String> prop = ImportExportUtils.importSettings(PreferenceConstants.PAGE_ID_FILTERED_COMP_PAGE);
		//if cancel
		if (prop == null) {
			return;
		}
		setOldPreferences(getCurrentPreferences());
		setProperties(prop);
		// Settings changed -> Enable apply button
		getApplyButton().setEnabled(true);
	}

	@Override
	protected void updatePage() {
		this.filterComponentsEditor.loadDefault();
		this.filterComponentsEditor.load();
	}
}

