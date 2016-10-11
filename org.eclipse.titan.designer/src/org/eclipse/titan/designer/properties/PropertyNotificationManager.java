/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;

/**
 * This class creates a connection between places that can change TITAN related properties
 *   and places which might wish to react to these changes.
 *   
 *   @author Kristof Szabados
 * */
public class PropertyNotificationManager {
	private static List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

	/**
	 * Disabled constructor
	 * */
	private PropertyNotificationManager () {
		// Do nothing
	}

	/**
	 * Registers a listener for property changes.
	 * 
	 * @param listener the listener to call when something changes.
	 * */
	public static synchronized void addListener(final IPropertyChangeListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes an already registered listener.
	 * 
	 * @param listener the listener to be removed.
	 * */
	public static synchronized void removeListener(final IPropertyChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notifies every listener signed up for property changes,
	 * that a property of the provided resource has changed.
	 * 
	 * @param resource the resource that has changed.
	 * */
	public static synchronized void firePropertyChange(final IResource resource) {
		if (resource == null) {
			return;
		}

		final List<IPropertyChangeListener> save = new ArrayList<IPropertyChangeListener>(listeners);
		for (final IPropertyChangeListener listener : save) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(final Throwable e) {
					// exception logged in SafeRunner#run
				}

				public void run() throws Exception {
					listener.propertyChanged(resource);
				}
			});
		}
	}
}
