/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.token;

import org.eclipse.titan.log.viewer.parsers.Constants;

/**
 * Sets the type EOR (End of record)
 */
public class EOR extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public EOR(final String token) {
		super(token);
	}

	@Override
	public int getType() {
		return Constants.END_OF_RECORD;
	}

}
