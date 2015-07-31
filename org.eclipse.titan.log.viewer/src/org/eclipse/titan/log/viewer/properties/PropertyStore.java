/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.properties;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.utils.ResourcePropertyHandler;

/**
 * Property Store
 *
 */
public class PropertyStore extends PreferenceStore {
	private IResource resource;
	private IPreferenceStore workbenchStore;
	private String pageId;
	private boolean inserting = false;
	
	/**
	 * Constructor
	 * @param resource the resource
	 * @param workbenchStore the workbench store
	 * @param pageId the page id
	 */
	public PropertyStore(final IResource resource, final IPreferenceStore workbenchStore, final String pageId) {
		this.resource = resource;
		this.workbenchStore = workbenchStore;
		this.pageId = pageId;
	}

	@Override
	public void save() throws IOException {
		writeProperties();

	}

	@Override
	public void save(final OutputStream out, final String header) throws IOException {
		writeProperties();
	}

	/**
	 * Writes modified preferences into resource properties.
	 */
	private void writeProperties() throws IOException {
		String[] preferences = super.preferenceNames();
		for (int i = 0; i < preferences.length; i++) {
			String propertyKey = preferences[i];
			try {
				ResourcePropertyHandler.setProperty(this.resource, this.pageId, propertyKey, getString(propertyKey));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				throw new IOException(Messages.getString("PropertyStore.0") + propertyKey); //$NON-NLS-1$
			}
		}
	}

	/*** Get default values (Delegate to workbench store) ***/

	@Override
	public boolean getDefaultBoolean(final String name) {
		return this.workbenchStore.getDefaultBoolean(name);
	}

	@Override
	public double getDefaultDouble(final String name) {
		return this.workbenchStore.getDefaultDouble(name);
	}

	@Override
	public float getDefaultFloat(final String name) {
		return this.workbenchStore.getDefaultFloat(name);
	}

	@Override
	public int getDefaultInt(final String name) {
		return this.workbenchStore.getDefaultInt(name);
	}

	@Override
	public long getDefaultLong(final String name) {
		return this.workbenchStore.getDefaultLong(name);
	}

	@Override
	public String getDefaultString(final String name) {
		return this.workbenchStore.getDefaultString(name);
	}

	@Override
	public boolean getBoolean(final String name) {
		insertValue(name);
		return super.getBoolean(name);
	}

	@Override
	public double getDouble(final String name) {
		insertValue(name);
		return super.getDouble(name);
	}

	@Override
	public float getFloat(final String name) {
		insertValue(name);
		return super.getFloat(name);
	}

	@Override
	public int getInt(final String name) {
		insertValue(name);
		return super.getInt(name);
	}

	@Override
	public long getLong(final String name) {
		insertValue(name);
		return super.getLong(name);
	}

	@Override
	public String getString(final String name) {
		insertValue(name);
		return super.getString(name);
	}

	private synchronized void insertValue(final String propertyKey) {
		if (this.inserting) {
			return;
		}
		if (super.contains(propertyKey)) {
			return;
		}
		this.inserting = true;
		String prop = null;
		prop = ResourcePropertyHandler.getProperty(this.resource, this.pageId, propertyKey);
		if (prop == null) {
			prop = this.workbenchStore.getString(propertyKey);
		}
		if (prop != null) {
			setValue(propertyKey, prop);
		}
		this.inserting = false;
	}

	@Override
	public boolean contains(final String name) {
		return this.workbenchStore.contains(name);
	}

	@Override
	public void setToDefault(final String name) {
		setValue(name, getDefaultString(name));
	}

	@Override
	public boolean isDefault(final String name) {
		String defaultValue = getDefaultString(name);
		if (defaultValue == null) {
			return false;
		}
		return defaultValue.equals(getString(name));
	}
}
