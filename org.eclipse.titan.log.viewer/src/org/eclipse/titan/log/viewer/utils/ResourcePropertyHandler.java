/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.log.viewer.preferences.FilteredSilentEventUtils;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;

public final class ResourcePropertyHandler {
	
	private static final int PROPERTY_MAX_SIZE = 2 * Constants.K;
	private static final String PROPERTY_SIZE_KEY = "propertySize"; //$NON-NLS-1$
	
	private ResourcePropertyHandler() {
		// Protected Constructor
	}
	
	/**
	 * Convenience method for getting resource properties
	 * 
	 * @param resource the resource
	 * @param pageId the page if of the property page
	 * @param propertyKey the property key
	 * @return the property value or null if not found
	 */
	public static String getProperty(final IResource resource, final String pageId, final String propertyKey) {
		try {
			String numSubValues = getSubValues(resource, pageId, propertyKey);
			if (numSubValues != null) { // Sub values used
				int numberOfSubValues = 0;
				try {
					numberOfSubValues = Integer.parseInt(getSubValues(resource, pageId, propertyKey));
				} catch (NumberFormatException nfe) {
					return null;
				}
				StringBuilder value = new StringBuilder();
				for (int i = 0; i < numberOfSubValues; i++) {
					value.append(resource.getPersistentProperty(new QualifiedName(pageId, propertyKey + i)));
				}
				return value.toString();
			} else { // No sub values
				String value = resource.getPersistentProperty(new QualifiedName(pageId, propertyKey));
				// Upgrade Protection 1.0 -> 1.1 (Silent Events)
				if (propertyKey.contentEquals(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES) && (value == null)) {
					// Value not set, look for old values
					if (FilteredSilentEventUtils.hasPropertiesOldFilteredSilentEvents(resource, pageId)) {
						// Old values found
						value = FilteredSilentEventUtils.getOldFilteredSilentEventsFromResource(resource, pageId);
						setProperty(resource, pageId, PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES, value);
						FilteredSilentEventUtils.clearOldFilteredSilentEventsPropertiesInResource(resource, pageId);
					}
				}
				return value;
			}
		} catch (CoreException ce) {
			// Do nothing
		}
		return null;
	}
	
	/**
	 * Convenience method for setting resource properties
	 * 
	 * @param resource the resource
	 * @param pageId the page if of the property page
	 * @param propertyKey the property key
	 * @param propertyValue the property value
	 * @throws CoreException if an error occurs while setting the property
	 */
	public static void setProperty(final IResource resource, final String pageId, final String propertyKey, final String propertyValue) throws CoreException {
		int propertySize = getPropertySize(propertyValue);
		if (propertySize > 1) { // sub values needed
			// create number of sub values key
			resource.setPersistentProperty(new QualifiedName(pageId, propertyKey + PROPERTY_SIZE_KEY), String.valueOf(propertySize));
			// create sub values
			int start;
			int stop;
			for (int i = 0; i < propertySize; i++) {
				start = i * PROPERTY_MAX_SIZE;
				stop = (i + 1) * PROPERTY_MAX_SIZE;
				if (propertyValue.length() < stop) {
					stop = propertyValue.length();
				}
				resource.setPersistentProperty(new QualifiedName(pageId, propertyKey + i), propertyValue.substring(start, stop));
			}
		} else { // otherwise set value directly
			// reset any current value
			resource.setPersistentProperty(new QualifiedName(pageId, propertyKey + PROPERTY_SIZE_KEY), null);
			resource.setPersistentProperty(new QualifiedName(pageId, propertyKey), propertyValue);
		}
	}
	
	/**
	 * Convenience method for removing resource properties
	 * 
	 * @param resource the resource
	 * @param pageId the page if of the property page
	 * @param propertyKey the property key
	 */
	public static void removeProperty(final IResource resource, final String pageId, final String propertyKey) {
		try {
			String numSubValues = getSubValues(resource, pageId, propertyKey);
			if (numSubValues != null) { // Sub values used
				int numberOfSubValues = 0;
				try {
					numberOfSubValues = Integer.parseInt(getSubValues(resource, pageId, propertyKey));
				} catch (NumberFormatException nfe) {
					return;
				}
				for (int i = 0; i < numberOfSubValues; i++) {
					resource.setPersistentProperty(new QualifiedName(pageId, propertyKey + i), null);
				}
			} else { // No sub values
				resource.setPersistentProperty(new QualifiedName(pageId, propertyKey), null);
			}
		} catch (CoreException ce) {
			// Do nothing
		}
	}

	private static String getSubValues(IResource resource, String pageId, String propertyKey) throws CoreException {
		return resource.getPersistentProperty(new QualifiedName(pageId, propertyKey + PROPERTY_SIZE_KEY));
	}

	private static int getPropertySize(final String value) {
		int propSize = value.length() / PROPERTY_MAX_SIZE;
		// Round upwards
		if (value.length() > propSize * PROPERTY_MAX_SIZE) {
			propSize++;
		}
		return propSize;
	}
}
