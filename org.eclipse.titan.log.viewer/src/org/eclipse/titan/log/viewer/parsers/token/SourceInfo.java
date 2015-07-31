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
 * Sets the type, the possible following token types and delimiters for a SourceInfo
 */
public class SourceInfo extends Token {

	public SourceInfo(final String token) {
		super(token);

		setTokenList(Constants.MESSAGE);
		setDelimiterList(Constants.WHITE_SPACE | Constants.END_OF_RECORD);
	}

	@Override
	public int getType() {
		return Constants.SOURCE_INFORMATION;
	}
}
