/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.ResourcePropertyHandler;

/**
 * This class is a help class for Filtered Silent Events
 * 
 * It was created to easy handling of the changes caused 
 * by the new TITAN log format
 *
 */
public final class FilteredSilentEventUtils {
	
	/**
	 * HashMap to ease the preference conversion between TLV 1.0 and 1.1 (filtered silent events) 
	 */
	private static final Map<String, String> OLD_TO_NEW_SILENT_EVENTS = new HashMap<String, String>();
	static {
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_0, Constants.EVENTTYPE_ERROR);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_1, Constants.EVENTTYPE_FUNCTION);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_2, Constants.EVENTTYPE_ACTION);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_3, Constants.EVENTTYPE_USER);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_4, Constants.EVENTTYPE_WARNING);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_5, Constants.EVENTTYPE_UNKNOWN);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_6, Constants.EVENTTYPE_EXECUTOR);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_7, Constants.EVENTTYPE_TIMEROP);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_8, Constants.EVENTTYPE_VERDICTOP);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_9, Constants.EVENTTYPE_DEFAULTOP);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_10, Constants.EVENTTYPE_PORTEVENT);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_11, Constants.EVENTTYPE_TESTCASE);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_12, Constants.EVENTTYPE_STATISTICS);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_13, Constants.EVENTTYPE_PARALLEL);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_14, Constants.EVENTTYPE_MATCHING);
		OLD_TO_NEW_SILENT_EVENTS.put(PreferenceConstants.PREF_FILTER_SILENTEVENT_15, Constants.EVENTTYPE_DEBUG);
	}

	private FilteredSilentEventUtils() {
		// Protected Constructor
	}
	
	/**
	 * @param preferenceStore the preference store
	 * @return true is the resource has old filter silent event properties, otherwise false
	 */
	public static boolean hasPreferencesOldFilteredSilentEvents(final IPreferenceStore preferenceStore) {
		Set<String> oldPrefKeys = OLD_TO_NEW_SILENT_EVENTS.keySet();
		for (String currPrefKey : oldPrefKeys) {
			if (preferenceStore.contains(currPrefKey)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param preferenceStore the preference store
	 * @return a String with the old preferences 
	 */
	public static String getOldFilteredSilentEventsFromPreferences(final IPreferenceStore preferenceStore) {
		Map<String, Boolean> filteredSilentEvents = getDefaultFilteredSilentEvents();
		// Replace with old filters (that are set)
		addOldFilteredSilentEventsFromPreferences(filteredSilentEvents, preferenceStore);
		// Convert to preference string
		return hashMapToPreferenceString(filteredSilentEvents);
	}
	
	/**
	 * @param preferenceStore the preference store
	 */
	public static void clearOldFilteredSilentEventsInPreferences(final IPreferenceStore preferenceStore) {
		Set<String> oldPrefKeys = OLD_TO_NEW_SILENT_EVENTS.keySet();
		for (String currPrefKey : oldPrefKeys) {
			preferenceStore.setToDefault(currPrefKey);
		}
	}
	
	/**
	 * @param resource the resource
	 * @param pageId the page id
	 * @return true is the resource has old filter silent event properties, otherwise false
	 */
	public static boolean hasPropertiesOldFilteredSilentEvents(final IResource resource, final String pageId) {
		Set<String> oldPrefKeys = OLD_TO_NEW_SILENT_EVENTS.keySet();
		for (String currPrefKey : oldPrefKeys) {
			String currPrefValue = ResourcePropertyHandler.getProperty(resource, pageId, currPrefKey);
			if (currPrefValue != null) {
				return true;
			}
		}
		return false;
	}
	
	public static String getOldFilteredSilentEventsFromResource(final IResource resource, final String pageId) {
		// Start with default
		Map<String, Boolean> filteredSilentEvents = getDefaultFilteredSilentEvents();
		// Replace with old filters (that are set)
		addOldFilteredSilentEventsFromResource(filteredSilentEvents, resource, pageId);
		// Convert to preference string
		return hashMapToPreferenceString(filteredSilentEvents);
	}
	
	public static void clearOldFilteredSilentEventsPropertiesInResource(final IResource resource, final String pageId) {
		Set<String> oldPrefKeys = OLD_TO_NEW_SILENT_EVENTS.keySet();
		for (String currPrefKey : oldPrefKeys) {
			ResourcePropertyHandler.removeProperty(resource, pageId, currPrefKey);
		}
	}
	
	/**
	 * @return a map with the default filtered silent events
	 */
	private static Map<String, Boolean> getDefaultFilteredSilentEvents() {
		// Use default silent events filters to begin with
		Map<String, Boolean> silentEventFilters = new HashMap<String, Boolean>();
		String[] keyValuePairs = PreferenceConstants.SILENT_EVENTS_DEFAULT_CATEGORIES.split(PreferenceConstants.PREFERENCE_DELIMITER);
		for (String keyValuePair : keyValuePairs) {
			String[] currKeyValue = keyValuePair.split(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM);
			if (currKeyValue.length == 2) {
				silentEventFilters.put(currKeyValue[0], Boolean.valueOf(currKeyValue[1]));
			}
		}
		return silentEventFilters;
	}
	
	/**
	 * @param filteredSilentEvents a HashMap with the default filtered silent events
	 * @param preferenceStore the preference store
	 */
	private static void addOldFilteredSilentEventsFromPreferences(
			final Map<String, Boolean> filteredSilentEvents, final IPreferenceStore preferenceStore) {
		SortedMap<String, String[]> eventCategories = Constants.EVENT_CATEGORIES;
		Set<String> oldPrefKeys = OLD_TO_NEW_SILENT_EVENTS.keySet();
		for (String currPrefKey : oldPrefKeys) {
			String currCatName = OLD_TO_NEW_SILENT_EVENTS.get(currPrefKey);
			Boolean currPrefValue = preferenceStore.getBoolean(currPrefKey);
			filteredSilentEvents.put(currCatName, currPrefValue);
			String[] subCategories = eventCategories.get(currCatName);
			for (String subCategory : subCategories) {
				filteredSilentEvents.put(currCatName.concat(PreferenceConstants.SILENT_EVENTS_UNDERSCORE.concat(subCategory)), currPrefValue);
			}
		}
	}
	
	/**
	 * @param filteredSilentEvents a HashMap with the filtered silent events
	 * @return a string with the filtered silent events (delimited)
	 */
	private static String hashMapToPreferenceString(final Map<String, Boolean> filteredSilentEvents) {
		StringBuilder builder = new StringBuilder();
		String filters = ""; //$NON-NLS-1$

		for (Map.Entry<String, Boolean> entry : filteredSilentEvents.entrySet()) {
			if (builder.length() == 0) {
				builder.setLength(0);
				builder.append(entry.getKey()).append(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM).append(entry.getValue());
			} else {
				builder.append(filters).append(PreferenceConstants.PREFERENCE_DELIMITER)
						.append(entry.getKey()).append(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM).append(entry.getValue());
			}
		}
		filteredSilentEvents.clear();
		return filters;
	}
	
	/**
	 * @param filteredSilentEvents a HashMap with the default filtered silent events
	 * @param resource the resource
	 * @param pageId the page id
	 */
	private static void addOldFilteredSilentEventsFromResource(final Map<String, Boolean> filteredSilentEvents,
															   final IResource resource, final String pageId) {
		SortedMap<String, String[]> eventCategories = Constants.EVENT_CATEGORIES;
		Set<String> oldPrefKeys = OLD_TO_NEW_SILENT_EVENTS.keySet();
		for (String currPrefKey : oldPrefKeys) {
			String currCatName = OLD_TO_NEW_SILENT_EVENTS.get(currPrefKey);
			String value = ResourcePropertyHandler.getProperty(resource, pageId, currPrefKey);
			if (value != null) {
				Boolean currPrefValue = Boolean.valueOf(value);
				filteredSilentEvents.put(currCatName, currPrefValue);
				String[] subCategories = eventCategories.get(currCatName);
				for (String subCategory : subCategories) {
					filteredSilentEvents.put(currCatName.concat(PreferenceConstants.SILENT_EVENTS_UNDERSCORE.concat(subCategory)), currPrefValue);
				}
			}
		}
	}
}
