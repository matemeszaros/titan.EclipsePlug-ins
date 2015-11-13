/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.CheckBoxTreeEditor;
import org.eclipse.titan.log.viewer.utils.Constants;
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
public class FilteredSilentEventPrefPage extends LogViewerPreferenceRootPage {
	
	private CheckBoxTreeEditor checkBoxTreeEditor;
	
	public FilteredSilentEventPrefPage() {
		super(GRID, true);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("FilteredSilentEventTypes.0")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
		this.checkBoxTreeEditor = new CheckBoxTreeEditor(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES,
													"", //$NON-NLS-1$
													Constants.EVENT_CATEGORIES, 
													getFieldEditorParent());
		this.checkBoxTreeEditor.setPreferenceStore(getPreferenceStore());
		addField(this.checkBoxTreeEditor);
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}
	
	@Override
	protected void performSelectAll() {
		this.checkBoxTreeEditor.selectAll();
		super.performSelectAll();
	}
	
	@Override
	protected void performDeselectAll() {
		this.checkBoxTreeEditor.deselectAll();
		super.performDeselectAll();
	}
	
	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_FILTERED_SILENTEVENT_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences() {
		Map<String, String> currentPrefs = new HashMap<String, String>();
		currentPrefs.put(this.checkBoxTreeEditor.getPreferenceName(),
				ImportExportUtils.arrayToString(this.checkBoxTreeEditor.getElements(), PreferenceConstants.PREFERENCE_DELIMITER));
		return currentPrefs;
	}
	
	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String[]> getCurrentPreferencesSeparated() {
		Map<String, String[]> currentPrefs = new HashMap<String, String[]>();
		currentPrefs.put(this.checkBoxTreeEditor.getPreferenceName(), this.checkBoxTreeEditor.getElements());
		return currentPrefs;
	}
	
	@Override
	protected void exportPreferences() {
		ImportExportUtils.exportSettings(getPageId(), getCurrentPreferencesSeparated(), true);
	}

	@Override
	protected void importPreferences() {
		Map<String, String> prop = ImportExportUtils.importSettings(getPageId());
		//if cancel
		if (prop == null) {
			return;
		}
		setOldPreferences(getCurrentPreferences());
		// Check if new or old
		String prefValue = prop.get(this.checkBoxTreeEditor.getPreferenceName());
		if (prefValue == null) { // Old
			convertOldSilentEventsToNew(prop);
		}
		setProperties(prop);
		// Settings changed -> Enable apply button
		getApplyButton().setEnabled(true);
	}

	@Override
	protected void updatePage() {
		this.checkBoxTreeEditor.load();
	}
	
	private void convertOldSilentEventsToNew(final Map<String, String> prop) {
		// Get default values
		String defaultPrefValue = getPreferenceStore().getDefaultString(this.checkBoxTreeEditor.getPreferenceName());
		String[] defaultValues = defaultPrefValue.split(PreferenceConstants.PREFERENCE_DELIMITER);
		// Add default values
		Map<String, String> prefValues = new HashMap<String, String>();
		for (String defaultValue : defaultValues) {
			String[] current = defaultValue.split(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM);
			if (current.length == 2) {
				prefValues.put(current[0], current[1]);
			}
		}
		addImportedValues(prop, prefValues);
		// Create string with all values
		Set<String> keySet = prefValues.keySet();
		StringBuilder builder = new StringBuilder();
		for (String currKey : keySet) {
			if (builder.length() == 0) {
				builder.setLength(0);
				builder.append(currKey).append(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM).append(prefValues.get(currKey));
			} else {
				builder.append(PreferenceConstants.PREFERENCE_DELIMITER)
						.append(currKey).append(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM).append(prefValues.get(currKey));
			}
		}
		// And set new value
		prop.clear();
		prop.put(this.checkBoxTreeEditor.getPreferenceName(), builder.toString());
	}

	private void addImportedValues(Map<String, String> prop, Map<String, String> prefValues) {
		String value;
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_2);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_ACTION, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_ACTION, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_15);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_DEBUG, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_DEBUG, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_9);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_DEFAULTOP, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_DEFAULTOP, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_0);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_ERROR, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_ERROR, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_6);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_EXECUTOR, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_EXECUTOR, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_1);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_FUNCTION, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_FUNCTION, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_14);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_MATCHING, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_MATCHING, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_13);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_PARALLEL, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_PARALLEL, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_10);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_PORTEVENT, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_PORTEVENT, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_12);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_STATISTICS, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_STATISTICS, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_11);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_TESTCASE, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_TESTCASE, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_7);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_TIMEROP, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_TIMEROP, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_5);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_UNKNOWN, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_UNKNOWN, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_3);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_USER, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_USER, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_8);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_VERDICTOP, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_VERDICTOP, value);
		}
		value = prop.get(PreferenceConstants.PREF_FILTER_SILENTEVENT_4);
		if (value != null) {
			prefValues.put(Constants.EVENTTYPE_WARNING, value);
			updateSubCategories(prefValues, Constants.EVENTTYPE_WARNING, value);
		}
	}

	/**
	 * Updates the sub categories with the given value
	 * 
	 * @param prefs the HashMap containing the preferences
	 * @param category the category which contains sub categories
	 * @param value the value to set
	 */
	private void updateSubCategories(final Map<String, String> prefs, final String category, final String value) {
		SortedMap<String, String[]> eventCategories = Constants.EVENT_CATEGORIES;
		String[] subCategories = eventCategories.get(category);
		for (String subCategory : subCategories) {
			prefs.put(category.concat(PreferenceConstants.SILENT_EVENTS_UNDERSCORE.concat(subCategory)), value);
		}
	}
}
