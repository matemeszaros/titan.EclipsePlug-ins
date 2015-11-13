/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;

/**
 * This class represents an object which is subscribed to preference changes. 
 * It can be used to follow the changes of a given preference.
 * This class is responsible for automagically update itself, as the given preference changes.
 * {@link #getValue()} always returns the currently active value of the given preference.
 * 
 * This class is intended to be subclassed by clients. 
 * Subclasses must define the {@link #handleChange(IPreferencesService)} function to update the {@link #currentValue} variable.
 * Each time the preference value of this property changes in the preference service,
 *  the {@link #handleChange(IPreferencesService)} method will be called.
 *
 * @param <T> The type of the preference value.
 * 
 * @author Szabolcs Beres
 */
public abstract class SubscribedObject<T> {
	protected final String qualifier;
	protected final String key;
	protected final T defaultValue;
	
	protected T currentValue;

	protected SubscribedObject(final String qualifier, final String key,
			final T defaultValue) {
		this.qualifier = qualifier;
		this.key = key;
		this.defaultValue = defaultValue;
		subscribe();
	}

	public T getValue() {
		return currentValue;
	}

	public void setValue(final T newValue) {
		if (currentValue != newValue) {
			storeNewValue(newValue);
			currentValue = newValue;
		}
	}

	private void subscribe() {
		handleChange(Platform.getPreferencesService());
		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (key.equals(property)) {
						handleChange(Platform.getPreferencesService());
						return;
					}
				}
			});
		}
	}

	/**
	 * This function is called each time the preference with the given property changes.
	 * @param prefService The preference service.
	 */
	protected abstract void handleChange(final IPreferencesService prefService);
	
	protected abstract void storeNewValue(final T newValue);
}