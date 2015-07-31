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
 * Sets the type, the possible following token types and delimiters for a ComponentReference
 */
public class ComponentReference extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public ComponentReference(final String token) {
		super(token);
		setTokenList(Constants.EVENT_TYPE | Constants.SOURCE_INFORMATION | Constants.MESSAGE);
		setDelimiterList(Constants.WHITE_SPACE);
	}

	@Override
	public int getType() {
		return Constants.COMPONENT_REFERENCE;
	}

}
