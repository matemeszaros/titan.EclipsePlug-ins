/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4;


/**
 * Represents a block of tokens.
 * <p>
 * In ASN.1 most of the tokens inside blocks can not be analyzed directly in
 * parse time. For this reason we are collecting them in such blocks, and when
 * the semantics are, these blocks are processed.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class BlockV4 extends Block implements Token, TokenSource {
	/**
	 * The list of the tokens contained inside the actual block. There might
	 * be sub-blocks in this list, but it does not contain its own '{' '}'
	 * enclosing tokens
	 * */
	private final List<Token> tokenList;
	private int index = 0;

	public BlockV4(List<Token> tokenList, final Location location) {
		super( location );
		this.tokenList = tokenList;
	}

	public BlockV4(final Token token) {
		if (token instanceof TokenWithIndexAndSubTokensV4) {
			tokenList = ((TokenWithIndexAndSubTokensV4) token).getSubTokens();
			final IFile sourceFile = ((TokenWithIndexAndSubTokensV4) token).getSourceFile();
			setLocation( new Location(sourceFile, token.getLine(), token.getStartIndex(), token.getStopIndex()) );
		}
		else {
			setLocation( NULL_Location.INSTANCE );
			tokenList = ((TokenWithIndexAndSubTokensV4) token).getSubTokens();
		}
	}
	
	public List<Token> getTokenList() {
		return tokenList;
	}

	@Override
	public int getCharPositionInLine() {
		return tokenList.get(0).getCharPositionInLine();
	}

	@Override
	public CharStream getInputStream() {
		assert(false);
		return null;
	}

	@Override
	public int getLine() {
		return tokenList.get(0).getLine();
	}

	@Override
	public int getChannel() {
		return Token.DEFAULT_CHANNEL;
	}

	@Override
	public int getStartIndex() {
		return tokenList.get(0).getStartIndex();
	}

	@Override
	public int getStopIndex() {
		return tokenList.get(tokenList.size() - 1).getStopIndex();
	}

	@Override
	public String getText() {
		StringBuilder text = new StringBuilder();
		for (Token t : tokenList) {
			text.append(t.getText());
		}
		return text.toString();
	}

	@Override
	public int getTokenIndex() {
		assert(false);
		return -1;
	}

	@Override
	public TokenSource getTokenSource() {
		assert(false);
		return null;
	}

	@Override
	public int getType() {
		return ASN1Lexer2.BLOCK;
	}

	@Override
	public String getSourceName() {
		return IntStream.UNKNOWN_SOURCE_NAME;
	}

	@Override
	public TokenFactory<?> getTokenFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Token nextToken() {
		return tokenList.get(index++);
	}

	@Override
	public void setTokenFactory(TokenFactory<?> arg0) {
		assert(false);
	}

	@Override
	public int getTokenListSize() {
		return tokenList.size();
	}
}
