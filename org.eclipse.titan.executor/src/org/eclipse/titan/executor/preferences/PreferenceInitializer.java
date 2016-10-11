/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.executor.Activator;

/**
 * @author Szabolcs Beres
 * */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = getPreference();
		
		preferenceStore.setDefault(PreferenceConstants.SET_LOG_FOLDER, true);
		preferenceStore.setDefault(PreferenceConstants.LOG_FOLDER_PATH_NAME, ".." + File.separator + "log" + File.separator);
		preferenceStore.setDefault(PreferenceConstants.DELETE_LOG_FILES_NAME, false);
		preferenceStore.setDefault(PreferenceConstants.AUTOMATIC_MERGE_NAME, true);
	}
	
	public IPreferenceStore getPreference() {
		return Activator.getDefault().getPreferenceStore();
	}
}
