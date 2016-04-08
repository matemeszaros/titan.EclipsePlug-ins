/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

public final class Assert {

	private Assert() {
		// Hide constructor
	}

	/**
	 * Assert that an object is not null.
	 * @param object the object to check
	 * @param message the exception message
	 * @throws java.lang.IllegalArgumentException if the object is {@code null}
	 */
	public static void notNull(final Object object, final String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
}
