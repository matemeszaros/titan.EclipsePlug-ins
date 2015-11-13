/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;

public class ResourceUtils {

	private ResourceUtils() {
		// Hide constructor
	}

	/**
	 * Reads the boolean persistent property of the given resource.
	 *
	 * @param resource
	 *            The resource
	 * @param qualifier
	 *            The key of the property
	 * @param localName
	 *            The localName part of the key
	 * @return the stored value
	 */
	public static boolean getBooleanPersistentProperty(final IResource resource, final String qualifier, final String localName) {
		try {
			return "true".equalsIgnoreCase(resource.getPersistentProperty(new QualifiedName(qualifier, localName)));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while reading persistent property of resource: " + resource.getName(), e);
		}
		return false;
	}

	/**
	 * Reads the persistent property of the given resource.
	 *
	 * @param resource
	 *            The resource
	 * @param key
	 *            The key of the property
	 * @return the stored value
	 */
	public static String getPersistentProperty(final IResource resource, final QualifiedName key) {
		try {
			return resource.getPersistentProperty(key);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while reading persistent property of resource: " + resource.getName(), e);
		}
		return null;
	}

	/**
	 * Reads the persistent property of the given resource.
	 *
	 * @param resource
	 *            The resource
	 * @param qualifier
	 *            The qualifier of the key
	 * @param localName
	 *            The localName part of the key
	 * @return the stored value
	 */
	public static String getPersistentProperty(final IResource resource, final String qualifier, final String localName) {
		return getPersistentProperty(resource, new QualifiedName(qualifier, localName));
	}

	/**
	 * Stores the given value for the given resource as a persistent property. If an exception occurs it will be logged.
	 *
	 * @param resource
	 *            The resource of the property
	 * @param qualifier
	 *            The qualifier of the property
	 * @param localName
	 *            The localName part of the key
	 * @param value
	 *            The value to store
	 * @return <code>true</code> if the operation succeeded, <code>false</code> otherwise.
	 */
	public static boolean setPersistentProperty(final IResource resource, final String qualifier, final String localName, final boolean value) {
		return setPersistentProperty(resource, qualifier, localName, String.valueOf(value));
	}

	/**
	 * Stores the given value for the given resource as a persistent property. If an exception occurs it will be logged.
	 *
	 * @param resource
	 *            The resource of the property
	 * @param key
	 *            The key of the property
	 * @param value
	 *            The value to store
	 * @return <code>true</code> if the operation succeeded, <code>false</code> otherwise.
	 */
	public static boolean setPersistentProperty(final IResource resource, final QualifiedName key, final String value) {
		try {
			resource.setPersistentProperty(key, value);
			return true;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while setting persistent property of resource: " + resource.getName(), e);
			return false;
		}
	}

	/**
	 * Stores the given value for the given resource as a persistent property. If an exception occurs it will be logged.
	 * It has the same effect as {@link #setPersistentProperty(IResource, QualifiedName, String)}
	 *
	 * @param resource
	 *            The resource of the property
	 * @param qualifier
	 *            The qualifier of the key
	 * @param localName
	 *            The localName part of the key
	 * @param value
	 *            The value to store
	 * @return <code>true</code> if the operation succeeded, <code>false</code> otherwise.
	 */
	public static boolean setPersistentProperty(final IResource resource, final String qualifier, final String localName, final String value) {
		return setPersistentProperty(resource, new QualifiedName(qualifier, localName), value);
	}

	public static boolean removePersistentProperty(final IResource resource, final QualifiedName key) {
		return setPersistentProperty(resource, key, null);
	}

	/**
	 * Refreshes the given resources. If and logs the exception if needed.
	 *
	 * @see IResource#refreshLocal(int, org.eclipse.core.runtime.IProgressMonitor)
	 * @param resources
	 *            The resources to refresh
	 */
	public static void refreshResources(final List<? extends IResource> resources) {
		for (IResource resource : resources) {
			try {
				resource.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Error while refreshing " + resource.getLocationURI(), e);
			}
		}
	}

}
