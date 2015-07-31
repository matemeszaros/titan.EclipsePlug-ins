/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;


import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IResource;

/**
 * The Location class represents a location in the source code.
 * <p>
 * This class is mainly used to: locate language elements, build structures based on their textual positions.
 * This class is also used to report some kind of warning or error to a given location.
 * <p>
 * ANTLR 4 version
 * 
 * @author Arpad Lovassy
 */
public class Location_V4 extends Location {

	public Location_V4(final Location location) {
		super(location);
	}

	public Location_V4(final IResource file) {
		super(file);
	}

	public Location_V4(final IResource file, final int line, final int offset, final int endOffset) {
		setLocation(file, line, offset, endOffset);
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
	public Location_V4( final IResource aFile, final Token aStartToken, final Token aEndToken ) {
		setLocation( aFile, aStartToken.getLine(), aStartToken.getStartIndex(),
				     aEndToken.getStopIndex()+1);
	}

	/**
	 * Constructor for ANTLR v4 token, where the start and end token is the same
	 * @param aFile the parsed file
	 * @param aToken the start and end token
	 */
	public Location_V4( final IResource aFile, final Token aToken ) {
		this( aFile, aToken, aToken );
	}

	/**
	 * Constructor for ANTLR v4 token, where location is based on another location, and end token is modified
	 * @param aBaseLocation base location
	 * @param aEndToken the new end token
	 */
	public Location_V4( final Location aBaseLocation, final Token aEndToken ) {
		super( aBaseLocation );
		this.setEndOffset( aEndToken );
	}

	/**
	 * Sets the offset with an ANTLR v4 end token
	 * @param aEndToken the new end token
	 */
	public final void setOffset(final Token aToken) {
		this.setOffset( aToken.getStartIndex() );
	}

	/**
	 * Sets the end offset with an ANTLR v4 end token
	 * @param aEndToken the new end token
	 */
	public final void setEndOffset( final Token aEndToken ) {
		this.setEndOffset( aEndToken.getStopIndex() + 1 ); // after the last character of the aEndToken
	}
}
