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
import org.eclipse.titan.common.parsers.LocationAST;

/**
 * @author Kristof Szabados
 * */
public final class IncludeSectionHandler {

	private LocationAST lastSectionRoot = null;
	private List<LocationAST> files = new ArrayList<LocationAST>();

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	//TODO: remove
	public void setLastSectionRoot(LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public void setLastSectionRoot(ParserRuleContext lastSectionRoot) {
		this.lastSectionRoot = new LocationAST(lastSectionRoot);
	}

	public void setLastSectionRoot(Token i) {
		// TODO: implement
	}
	
	public List<LocationAST> getFiles() {
		return files;
	}

	public void addFile( ParserRuleContext aIncludeFile ) {
		files.add( new LocationAST( aIncludeFile ) );
	}

}
