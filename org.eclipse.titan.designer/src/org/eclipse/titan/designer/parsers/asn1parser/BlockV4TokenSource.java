package org.eclipse.titan.designer.parsers.asn1parser;

import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

public class BlockV4TokenSource implements TokenSource {
	private List<Token> tokenList;
	int index;
	
	BlockV4TokenSource(List<Token> tokenList) {
		this.tokenList = tokenList;
		this.index = 0;
	}
	
	@Override
	public int getCharPositionInLine() {
		return -1;
	}

	@Override
	public CharStream getInputStream() {
		return null;
	}

	@Override
	public int getLine() {
		return 0;
	}

	@Override
	public String getSourceName() {
		return IntStream.UNKNOWN_SOURCE_NAME;
	}

	@Override
	public TokenFactory<?> getTokenFactory() {
		return null;
	}

	@Override
	public Token nextToken() {
		return tokenList.get(index++);
	}

	@Override
	public void setTokenFactory(TokenFactory<?> arg0) {
	}
		
}

