/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.product;

public final class ProductConstants {

	public static final String TITAN_PREFIX = "org.eclipse.titan";
	public static final String PRODUCT_ID_COMMON = TITAN_PREFIX + ".common";
	public static final String PRODUCT_ID_DESIGNER = TITAN_PREFIX + ".designer";
	public static final String PRODUCT_ID_EXECUTOR = TITAN_PREFIX + ".executor";
	public static final String PRODUCT_ID_LOGVIEWER = TITAN_PREFIX + ".log.viewer";
	public static final String PRODUCT_ID_TITANIUM = "org.eclipse.titanium";
	
	/**
	 * Global configuration flag for usage statistics sending, true if usage statistics is sent 
	 */
	public static final boolean USAGE_STAT_SENDING = false;

	/**
	 * Global configuration flag for license file checking.
	 *   true: TITAN License file check is done,
	 *   false: TITAN works without TITAN license file
	 */
	public static final boolean LICENSE_NEEDED = false;
	
	private ProductConstants() {
		// Do nothing
	}
}
