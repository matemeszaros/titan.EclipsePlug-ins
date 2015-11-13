/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.productUtilities;

/**
 * This is a class storing product identification strings.
 * So that all packages depending on this information, does not automatically depend on unnecessary things too.
 * 
 * @author Kristof Szabados
 * */
public final class ProductConstants {
	public static final String TITAN_PREFIX = "org.eclipse.titan";
	public static final String PRODUCT_ID_DESIGNER = TITAN_PREFIX + ".designer";

	private ProductConstants() {
		// Hide constructor
	}
}
