/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

public class TokenWithIndexAndSubTokensFactory implements	TokenFactory<TokenWithIndexAndSubTokens> {
	public static final TokenFactory<TokenWithIndexAndSubTokens> DEFAULT = new TokenWithIndexAndSubTokensFactory();
	protected final boolean copyText;

	public TokenWithIndexAndSubTokensFactory() {
		this(false);
	}

	public TokenWithIndexAndSubTokensFactory(boolean copyText) {
		this.copyText = copyText;
	}

	@Override
	public TokenWithIndexAndSubTokens create(Pair<TokenSource, CharStream> source, int type, String text,
							  int channel, int start, int stop,
							  int line, int charPositionInLine)
	{
		TokenWithIndexAndSubTokens t = new TokenWithIndexAndSubTokens(source, type, channel, start, stop);
		t.setLine(line);
		t.setCharPositionInLine(charPositionInLine);
		if ( text!=null ) {
			t.setText(text);
		} else if ( copyText && source.b != null ) {
			t.setText(source.b.getText(Interval.of(start,stop)));
		}

		return t;
	}

	@Override
	public TokenWithIndexAndSubTokens create(int type, String text) {
		return new TokenWithIndexAndSubTokens(type, text);
	}

	
}
