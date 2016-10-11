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
 * Sets the type, the possible following token types and delimiters for a Message
 */
public class Message extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public Message(final String token) {
		super(token);

		// expects no tokens after this!
		setTokenList(0);
		setDelimiterList(Constants.END_OF_RECORD);
	}

	@Override
	public int getType() {
		return Constants.MESSAGE;
	}
}
