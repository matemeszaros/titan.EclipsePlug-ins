package org.eclipse.titan.designer.parsers.asn1parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

public class TokenWithIndexAndSubTokensV4Factory implements	TokenFactory<TokenWithIndexAndSubTokensV4> {
	public static final TokenFactory<TokenWithIndexAndSubTokensV4> DEFAULT = new TokenWithIndexAndSubTokensV4Factory();
	protected final boolean copyText;
	public TokenWithIndexAndSubTokensV4Factory() {
		this(false);
	}

	public TokenWithIndexAndSubTokensV4Factory(boolean copyText) { this.copyText = copyText; }
	@Override
	public TokenWithIndexAndSubTokensV4 create(Pair<TokenSource, CharStream> source, int type, String text,
							  int channel, int start, int stop,
							  int line, int charPositionInLine)
	{
		TokenWithIndexAndSubTokensV4 t = new TokenWithIndexAndSubTokensV4(source, type, channel, start, stop);
		t.setLine(line);
		t.setCharPositionInLine(charPositionInLine);
		if ( text!=null ) {
			t.setText(text);
		}
		else if ( copyText && source.b != null ) {
			t.setText(source.b.getText(Interval.of(start,stop)));
		}

		return t;
	}

	@Override
	public TokenWithIndexAndSubTokensV4 create(int type, String text) {
		return new TokenWithIndexAndSubTokensV4(type, text);
	}

	
}
