/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.parsers.ILocationAST;

/**
 * The Location class represents a location in configuration files.  It was
 * stolen from "org.eclipse.titan.designer.AST.Location".  It stores only
 * location information.
 * 
 * @author eferkov
 **/
public class CfgLocation {
	private IFile file;
	private int line;
	private int offset;
	private int endOffset;
	
	protected CfgLocation() {
		file = null;
		line = -1;
		offset = -1;
		endOffset = -1;
	}
	
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
	
	protected final void setLocation(final IFile file, final ILocationAST startTok, final ILocationAST endTok) {
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
	
	protected final void setLocation(final CfgLocation location) {
		file = location.getFile();
		line = location.getLine();
		offset = location.getOffset();
		endOffset = location.getEndOffset();
	}
	
	protected final void setLocation(final IFile file, final int line, final int offset, final int endOffset) {
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
}
