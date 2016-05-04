/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * Parse tree type for adding new strings to an existing ParseTree structure,
 * which was build during a parsing.
 * It has NO info about its start and end tokens, it just contains a string
 * It has NO children
 * @author Arpad Lovassy
 */
public class AddedParseTree implements ParseTree {

	/** text of the parse tree node. It doesn't have to be 1 token, it can be any string. */
	private String mText;
	
	/** parent rule */
	private ParseTree mParent;
	
	public AddedParseTree( final String aText ) {
		mText = aText;
	}
	
	@Override
	public Interval getSourceInterval() {
		return null;
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public Object getPayload() {
		return null;
	}

	@Override
	public String toStringTree() {
		return mText;
	}

	@Override
	public <T> T accept(ParseTreeVisitor<? extends T> arg0) {
		return null;
	}

	@Override
	public ParseTree getChild( int i ) {
		return null;
	}

	@Override
	public ParseTree getParent() {
		return mParent;
	}

	@Override
	public String getText() {
		return mText;
	}

	@Override
	public String toStringTree(Parser arg0) {
		return mText;
	}

	public void setText(String aText) {
		mText = aText;
	}
	
	public void setParent( final ParseTree aParent ) {
		mParent = aParent;
	}


}
