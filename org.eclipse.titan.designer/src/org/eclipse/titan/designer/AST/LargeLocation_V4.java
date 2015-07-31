/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.titan.common.parsers.ILocationAST;

/**
 * ANTLR 4 version
 * @author Arpad Lovassy
 */
public class LargeLocation_V4 extends LargeLocation {

	public LargeLocation_V4(final IResource file, final ILocationAST startToken, final ILocationAST endToken) {
		super(file, startToken, endToken);
	}

	/**
	 * Constructor for ANTLR v4 tokens
	 * @param aFile the parsed file
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 */
	public LargeLocation_V4(IFile aFile, Token aStartToken, Token aEndToken ) {
		super(aFile, aStartToken.getLine(), aStartToken.getStartIndex(), -1);
		endLine = -1;

		if (aEndToken != null) {
			setEndOffset( aEndToken.getStopIndex() + 1 );
			endLine = aEndToken.getLine();
		}
	}
}
