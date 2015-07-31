/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.preferences;

/**
 * Interface for classes that can be used by the user to configure the risk
 * limits of metrics.
 * <p>
 * Implementing classes should be widgets, that can be displayed on the
 * Preference pages. Implementing classes should use the
 * {@link PreferenceManager} static object to access the preference store.
 * 
 * @author poroszd
 * 
 */
public interface IRiskFieldEditor {
	/**
	 * Load the user-set values from the preference store.
	 */
	void load();

	/**
	 * Load the deafult values from the preference store.
	 */
	void loadDefault();

	/**
	 * Save the user-set values on the widget in the preference store.
	 */
	void store();

	/**
	 * Check whether the currently set values on the widget are valid.
	 * 
	 * @return <code>true</code> if values do not confront.
	 */
	boolean isValid();

	/**
	 * Set the listener. This listener should be noted any time something is
	 * changed by the user on the widget, so it can be updated.
	 * 
	 * @param page
	 *            The page on which the widget resides.
	 */
	void setPropListener(IRiskEditorPropertyListener listener);

	/**
	 * Listener will be notified when risk method changes.
	 * 
	 * @param listener
	 *            this should react (typically with <code>layout()</code>
	 */
	void addRiskEditorListener(IRiskEditorListener listener);
}
