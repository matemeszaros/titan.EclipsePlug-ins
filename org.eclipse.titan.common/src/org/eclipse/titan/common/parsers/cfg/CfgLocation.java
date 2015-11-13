/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.parsers.ILocationAST;

/**
 * The Location class represents a location in configuration files.  It was
 * stolen from "org.eclipse.titan.designer.AST.Location".  It stores only
 * location information.
 * 
 * @author eferkov
 * @author Arpad Lovassy
 */
public final class CfgLocation {
	private IFile file;
	private int line;
	private int offset;
	private int endOffset;
	
	public CfgLocation(final CfgLocation location) {
		setLocation(location);
	}
	
	public CfgLocation(final IFile file) {
		this(file, (ILocationAST)null, (ILocationAST)null);
	}
	
	public CfgLocation(final IFile file, final int line, final int offset, final int endOffset) {
		setLocation(file, line, offset, endOffset);
	}
	
	public CfgLocation(final IFile file, final ILocationAST startTok, final ILocationAST endTok) {
		setLocation(file, startTok, endTok);
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
	public CfgLocation( final IFile aFile, final Token aStartToken, final Token aEndToken ) {
		setLocation( aFile, aStartToken.getLine(), aStartToken.getStartIndex(),
				     aEndToken.getStopIndex()+1);
	}
	
	private final void setLocation(final IFile file, final ILocationAST startTok, final ILocationAST endTok) {
		this.file = file;
		if (startTok == null) {
			line = -1;
			offset = -1;
		} else {
			line =  startTok.getLine();
			offset = startTok.getOffset();
		}
		if (endTok == null) {
			endOffset = -1;
		} else {
			endOffset = endTok.getEndOffset();
		}
	}
	
	private final void setLocation(final CfgLocation location) {
		file = location.getFile();
		line = location.getLine();
		offset = location.getOffset();
		endOffset = location.getEndOffset();
	}
	
	private final void setLocation(final IFile file, final int line, final int offset, final int endOffset) {
		this.file = file;
		this.line = line;
		this.offset = offset;
		this.endOffset = endOffset;
	}
	
	public IFile getFile() {
		return file;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getEndOffset() {
		return endOffset;
	}
	
	public String toString() {
		return "{ " + file + ", "  + line + ", " + offset + ", " + endOffset + " }";
	}
}
