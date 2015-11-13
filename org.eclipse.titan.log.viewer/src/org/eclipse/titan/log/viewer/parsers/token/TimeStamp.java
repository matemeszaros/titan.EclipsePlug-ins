/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.token;

import org.eclipse.titan.log.viewer.parsers.Constants;

/**
 * Sets the type, the possible following token types and delimiters for a Timestamp
 */
public class TimeStamp extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public TimeStamp(final String token) {
		super(token);

		setTokenList(Constants.COMPONENT_REFERENCE | Constants.SOURCE_INFORMATION | Constants.EVENT_TYPE | Constants.MESSAGE);
		setDelimiterList(Constants.WHITE_SPACE | Constants.END_OF_RECORD);
	}

	@Override
	public int getType() {
		return Constants.TIME_STAMP;
	}

}
