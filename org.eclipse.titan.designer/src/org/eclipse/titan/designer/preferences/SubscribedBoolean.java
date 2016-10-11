/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.designer.Activator;

/**
 * @author Szabolcs Beres
 * */
public final class SubscribedBoolean extends SubscribedObject<Boolean> {
	public SubscribedBoolean(final String qualifier, final String key, final Boolean defaultValue) {
		super(qualifier, key, defaultValue);
	}

	@Override
	protected void handleChange(final IPreferencesService prefService) {
		currentValue = prefService.getBoolean(qualifier, key, defaultValue, null);
	}

	@Override
	protected void storeNewValue(final Boolean newValue) {
		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().setValue(qualifier + key, newValue);
		}
	}

}