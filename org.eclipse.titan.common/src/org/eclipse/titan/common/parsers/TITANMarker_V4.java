/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import org.antlr.v4.runtime.Token;

/**
 * ANTLR 4 version
 * @author Arpad Lovassy
 */
public final class TITANMarker_V4 extends TITANMarker {
	
	/**
	 * Constructor for ANTLR v4 tokens
	 * @param aMessage marker message
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 * @param aSeverity IMarker.SEVERITY_WARNING
	 * @param aPriority IMarker.PRIORITY_NORMAL
	 */
	public TITANMarker_V4(final String message, final Token startToken, final Token endToken, final int severity, final int priority) {
		super( message,
			   (startToken != null) ? startToken.getLine() : -1,
			   (startToken != null) ? startToken.getStartIndex() : -1,
			   (endToken != null) ?	endToken.getStopIndex() : -1,
			   severity, priority );
	}

	/**
	 * Constructor for ANTLR v4 token, where the start and end token is the same
	 * @param aMessage marker message
	 * @param aMessage marker message
	 * @param aToken the start and end token
	 * @param aSeverity IMarker.SEVERITY_WARNING
	 * @param aPriority IMarker.PRIORITY_NORMAL
	 */
	public TITANMarker_V4(final String aMessage, Token aToken, final int aSeverity, final int aPriority) {
		this( aMessage, aToken, aToken, aSeverity, aPriority );
	}
}
