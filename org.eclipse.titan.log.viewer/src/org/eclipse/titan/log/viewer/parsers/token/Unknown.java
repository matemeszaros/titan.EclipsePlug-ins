/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.token;

import org.eclipse.titan.log.viewer.parsers.Constants;

/**
 * Sets the type for an unknown token
 */
public class Unknown extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public Unknown(final String token) {
		super(token);
	}

	@Override
	public int getType() {
		return Constants.UNKNOWN;
	}

}
