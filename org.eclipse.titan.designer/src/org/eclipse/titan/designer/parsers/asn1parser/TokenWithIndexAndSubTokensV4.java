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

public class TokenWithIndexAndSubTokensV4 extends CommonToken {
	private static final long serialVersionUID = 3906412166039744425L;
	List<Token> tokenList = null;
	IFile sourceFile;

	public TokenWithIndexAndSubTokensV4(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
		super(source, type, channel, start, stop);
		this.tokenList = new ArrayList<Token>();
	}

	
	public TokenWithIndexAndSubTokensV4(int t) {
		super(t);
		this.tokenList = new ArrayList<Token>();
	}

	public TokenWithIndexAndSubTokensV4(int t, List<Token> tokenList, IFile sourceFile) {
		super(t);
		this.tokenList = tokenList;
		this.sourceFile = sourceFile;
	}

	public TokenWithIndexAndSubTokensV4(int t, String text) {
		super(t, text);
		this.tokenList = new ArrayList<Token>();
	}

	public TokenWithIndexAndSubTokensV4(Token tok) {
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

	public TokenWithIndexAndSubTokensV4 copy() {
		TokenWithIndexAndSubTokensV4 token = new TokenWithIndexAndSubTokensV4(type, tokenList, sourceFile);
		token.line = line;
		token.charPositionInLine = charPositionInLine;
		token.setText(getText());
		return token;
	}

}

