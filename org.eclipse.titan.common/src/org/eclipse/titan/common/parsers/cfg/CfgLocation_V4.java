/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IFile;

/**
 * The Location class represents a location in configuration files.  It was
 * stolen from "org.eclipse.titan.designer.AST.Location".  It stores only
 * location information.
 * ANTLR 4 version
 * 
 * @author Arpad Lovassy
 **/
public final class CfgLocation_V4 extends CfgLocation {
	
	/**
	 * Constructor for ANTLR v4 tokens
	 * @param aFile the parsed file
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 */
	public CfgLocation_V4( final IFile aFile, final Token aStartToken, final Token aEndToken ) {
		setLocation( aFile, aStartToken.getLine(), aStartToken.getStartIndex(),
				     aEndToken.getStopIndex()+1);
	}
}
