/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
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
public class FilteredPortsPreferencePage extends LogViewerPreferenceRootPage {

	private BooleanFieldEditor connectingPortsEditor;
	private BooleanFieldEditor mappingPortsEditor;
	
	/**
	 * Constructor 
	 */
	public FilteredPortsPreferencePage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("FilteredPortsPreferencePage.0")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
		
		this.connectingPortsEditor = new BooleanFieldEditor(
				PreferenceConstants.PREF_CONNECTING_PORTS_ID,
				Messages.getString("FilteredPortsPreferencePage.1"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.connectingPortsEditor);
		this.mappingPortsEditor = new BooleanFieldEditor(
				PreferenceConstants.PREF_MAPPING_PORTS_ID,
				Messages.getString("FilteredPortsPreferencePage.2"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.mappingPortsEditor);
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_FILTERED_PORTS_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences() {
		Map<String, String> currentPrefs = new HashMap<String, String>();
		String connectingPorts = String.valueOf(this.connectingPortsEditor.getBooleanValue());
		String mappingPorts = String.valueOf(this.mappingPortsEditor.getBooleanValue());

		currentPrefs.put(PreferenceConstants.PREF_CONNECTING_PORTS_ID, connectingPorts);
		currentPrefs.put(PreferenceConstants.PREF_MAPPING_PORTS_ID, mappingPorts);

		return currentPrefs;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String[]> getCurrentPreferencesSeparated() {
		Map<String, String[]> currentPrefs = new HashMap<String, String[]>();
		
		String[] connectingPorts = new String[] {String.valueOf(this.connectingPortsEditor.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_CONNECTING_PORTS_ID, connectingPorts);
		String[] mappingPorts = new String[] {String.valueOf(this.mappingPortsEditor.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_MAPPING_PORTS_ID, mappingPorts);
		return currentPrefs;
	}

	@Override
	protected void exportPreferences() {
		ImportExportUtils.exportSettings(getPageId(), getCurrentPreferencesSeparated(), true);
	}

	@Override
	protected void importPreferences() {
		Map<String, String> prop = ImportExportUtils.importSettings(PreferenceConstants.PAGE_ID_FILTERED_PORTS_PAGE);
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
	
		this.connectingPortsEditor.load();
		this.mappingPortsEditor.load();

	}
	
}
