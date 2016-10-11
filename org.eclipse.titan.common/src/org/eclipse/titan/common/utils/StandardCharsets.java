/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.nio.charset.Charset;

/**
 * This class can be used, to get character sets
 * see {@link java.nio.charset.StandardCharsets}
 * This class should be deleted, when the project will use Java 1.7.
 */
public final class StandardCharsets {
	public static final String UTF8_KEY = "UTF-8";

	public static final Charset UTF8 = Charset.forName(UTF8_KEY);

	private StandardCharsets() {
		// Hide constructor
	}
}
