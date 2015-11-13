/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	private IPreferenceStore preferenceStore;
	
	@Override
	public void initializeDefaultPreferences() {
		this.preferenceStore = Activator.getDefault().getPreferenceStore();
		// Initialize default values
		this.preferenceStore.setDefault(PreferenceConstants.PREF_VERBOSE_ID, PreferenceConstants.TRUE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SUT_ID, PreferenceConstants.SUT_DEFAULT);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_COMPONENT_ORDER_ID, PreferenceConstants.COMP_ORDER_DEFAULT);		
		this.preferenceStore.setDefault(PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT, PreferenceConstants.PREF_ASN1_TREEVIEW);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID, PreferenceConstants.PREF_ADD_COMP_TO_VIS_ORDER_APPEND);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID, PreferenceConstants.TRUE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY, PreferenceConstants.PREF_MSCVIEW_TOP);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES, PreferenceConstants.SILENT_EVENTS_DEFAULT_CATEGORIES);
		
		this.preferenceStore.setDefault(PreferenceConstants.PREF_PROJECTTAB_DEFAULT, PreferenceConstants.PREF_PROJECTTAB_DEFAULT_EXTRACT_TESTCASES);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_TESTCASETAB_DEFAULT, PreferenceConstants.PREF_TESTCASETAB_DEFAULT_OPEN_MSCVIEW);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_MSCVIEW_DEFAULT, PreferenceConstants.PREF_MSCVIEW_DEFAULT_VALUEVIEW);

		// Silent Events filters changed from 1.0 to 1.1. -> Support upgrade
		String version = this.preferenceStore.getString(PreferenceConstants.PREF_KEY_LV_VERSION);
		if (!version.contentEquals(Constants.LOG_VIEWER_CURRENT_VERSION)) {
			// First time new version is used or new workspace
			this.preferenceStore.setValue(PreferenceConstants.PREF_KEY_LV_VERSION, Constants.LOG_VIEWER_CURRENT_VERSION);
			
			if (FilteredSilentEventUtils.hasPreferencesOldFilteredSilentEvents(this.preferenceStore)) {
				// Upgrade! - Set new value based on old filters
				String preferenceValue = FilteredSilentEventUtils.getOldFilteredSilentEventsFromPreferences(this.preferenceStore);
				this.preferenceStore.setValue(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES, preferenceValue);
				// Clear preference keys for old silent event filters
				FilteredSilentEventUtils.clearOldFilteredSilentEventsInPreferences(this.preferenceStore);
			}
		}
		
		//setverdict
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SETVERDICT_ERROR_ID, PreferenceConstants.TRUE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SETVERDICT_FAIL_ID, PreferenceConstants.TRUE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SETVERDICT_INCONC_ID, PreferenceConstants.TRUE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SETVERDICT_NONE_ID, PreferenceConstants.FALSE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_SETVERDICT_PASS_ID, PreferenceConstants.FALSE);
		this.preferenceStore.setDefault(PreferenceConstants.PREF_ERROR_CAUSED_BY_ID, PreferenceConstants.SETVERDICT_ERROR_DEFAULT);		
		this.preferenceStore.setDefault(PreferenceConstants.PREF_FAIL_CAUSED_BY_ID, PreferenceConstants.SETVERDICT_FAIL_DEFAULT);	

		//Filteterd ports
		this.preferenceStore.setDefault(PreferenceConstants.PREF_CONNECTING_PORTS_ID, PreferenceConstants.FALSE);	
		this.preferenceStore.setDefault(PreferenceConstants.PREF_MAPPING_PORTS_ID, PreferenceConstants.FALSE);
	}
}
