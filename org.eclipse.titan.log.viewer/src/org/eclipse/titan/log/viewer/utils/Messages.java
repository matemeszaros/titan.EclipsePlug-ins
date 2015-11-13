/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class for getting external messages 
 *
 */
public final class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.titan.log.viewer.utils.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * Protected constructor 
	 */
	private Messages() {
	}

	/**
	 * Gets the string for a given key
	 * @param key the key for the constructor
	 * @return the value for the given key if any
	 */
	public static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
