/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenSource;
import org.eclipse.core.resources.IFile;

/**
 * @author Laszlo Baji
 */

public class TokenWithIndexAndSubTokens extends CommonToken {
	private static final long serialVersionUID = 3906412166039744425L;
	List<Token> tokenList = null;
	IFile sourceFile;

	public TokenWithIndexAndSubTokens(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
		super(source, type, channel, start, stop);
		this.tokenList = new ArrayList<Token>();
	}

	
	public TokenWithIndexAndSubTokens(int t) {
		super(t);
		this.tokenList = new ArrayList<Token>();
	}

	public TokenWithIndexAndSubTokens(int t, List<Token> tokenList, IFile sourceFile) {
		super(t);
		this.tokenList = tokenList;
		this.sourceFile = sourceFile;
	}

	public TokenWithIndexAndSubTokens(int t, String text) {
		super(t, text);
		this.tokenList = new ArrayList<Token>();
	}

	public TokenWithIndexAndSubTokens(Token tok) {
		super(tok);
		tokenList = new ArrayList<Token>();
		super.setStartIndex(tok.getStartIndex());
		super.setStopIndex(tok.getStopIndex());
	}

	public IFile getSourceFile() {
		return sourceFile;
	}

	@Override
	public void setText(String s) {
		super.setText(s);
	}

	public List<Token> getSubTokens() {
		return tokenList;
	}

	@Override
	public String toString() {
		return"[:\"" + getText() + "\",<" + getType() + ">,line=" + line + ",col=" + charPositionInLine + ",start=" + start + ",stop=" + stop +"]\n";
	}

	public TokenWithIndexAndSubTokens copy() {
		TokenWithIndexAndSubTokens token = new TokenWithIndexAndSubTokens(type, tokenList, sourceFile);
		token.line = line;
		token.charPositionInLine = charPositionInLine;
		token.setText(getText());
		return token;
	}

}

