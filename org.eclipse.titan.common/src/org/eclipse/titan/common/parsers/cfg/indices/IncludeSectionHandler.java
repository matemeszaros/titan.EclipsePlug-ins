/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class IncludeSectionHandler {

	private ParseTree mLastSectionRoot = null;
	private List<ParseTree> mFiles = new ArrayList<ParseTree>();

	public ParseTree getLastSectionRoot() {
		return mLastSectionRoot;
	}

	public void setLastSectionRoot( final ParseTree lastSectionRoot ) {
		this.mLastSectionRoot = lastSectionRoot;
	}

	public void setLastSectionRoot( final Token aToken ) {
		mLastSectionRoot = new TerminalNodeImpl( aToken );
	}
	
	public List<ParseTree> getFiles() {
		return mFiles;
	}

	public void addFile(final ParserRuleContext aIncludeFile ) {
		mFiles.add( aIncludeFile );
	}


}
