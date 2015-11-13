/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.titan.common.logging.ErrorReporter;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author Szabolcs Beres
 * */
public class PropertyStore extends PreferenceStore {

	private IResource resource;
	private IPreferenceStore workbenchStore;
	private String pageId;
	private boolean inserting = false;

	public PropertyStore(
		final IResource resource,
		final IPreferenceStore workbenchStore,
		final String pageId) {
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
	private void writeProperties() {
		String[] preferences = super.preferenceNames();
		for (String name : preferences) {
			try {
				setProperty(name, getString(name));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
	}

	/**
	 * Convenience method to set a property
	 * @param name - the preference name
	 * @param value - the property value or null to delete the property
	 * @throws CoreException
	 */
	private void setProperty(final String name, final String value) throws CoreException {
		resource.setPersistentProperty(new QualifiedName(pageId, name), value);
	}

	@Override
	public boolean getDefaultBoolean(final String name) {
		return workbenchStore.getDefaultBoolean(name);
	}

	@Override
	public double getDefaultDouble(final String name) {
		return workbenchStore.getDefaultDouble(name);
	}

	@Override
	public float getDefaultFloat(final String name) {
		return workbenchStore.getDefaultFloat(name);
	}

	@Override
	public int getDefaultInt(final String name) {
		return workbenchStore.getDefaultInt(name);
	}

	@Override
	public long getDefaultLong(final String name) {
		return workbenchStore.getDefaultLong(name);
	}

	@Override
	public String getDefaultString(final String name) {
		return workbenchStore.getDefaultString(name);
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

	private synchronized void insertValue(final String name) {
		if (inserting) {
			return;
		}

		if (super.contains(name)) {
			return;
		}

		inserting = true;
		String prop = null;
		try {
			prop = getProperty(name);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		if (prop == null) {
			prop = workbenchStore.getString(name);
		}

		if (prop != null) {
			setValue(name, prop);
		}

		inserting = false;
	}

	private String getProperty(final String name) throws CoreException {
		return resource.getPersistentProperty(new QualifiedName(pageId, name));
	}

	@Override
	public boolean contains(final String name) {
		return workbenchStore.contains(name);
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
