/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.designer.parsers.asn1parser;

/**
 * @author Laszlo Baji
 * */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.WritableToken;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Lexer2;

public class ModuleLevelTokenStreamTrackerV4 extends BufferedTokenStream {
	private HashSet<Integer> discardMask = new HashSet<Integer>();
	private IFile sourceFile;

	public ModuleLevelTokenStreamTrackerV4(TokenSource source) {
		super(source);
		this.sourceFile = null;
	}
	
	public void setActualFile(IFile sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public void discard(int ttype) {
		discardMask.add(Integer.valueOf(ttype));
	}

	@Override
	public int fetch(int n) {
		if (fetchedEOF) {
			return 0;
		}
		Token t;
		Token first;

		int i = 0;

		do {
			t = getTokenSource().nextToken();
	        if ( t instanceof WritableToken ) {
	        	((WritableToken)t).setTokenIndex(tokens.size());
	        }
 			first = t;
			if (t.getType() == Token.EOF) {
				fetchedEOF = true;
		        tokens.add(new TokenWithIndexAndSubTokensV4(t));
				return ++i;
			} else if (discardMask.contains(Integer.valueOf(t.getType()))) {
				// discard this Token
				assert(true);
			} else if (t.getType() == ASN1Lexer2.BEGINCHAR) {
				fetchedEOF = getBlock(first);
				if (fetchedEOF) return ++i;
				++i;
				--n;
			} else {
				tokens.add(t);
				++i;
				--n;
			}
		} while (0 < n);
		return i;
	}

	private boolean getBlock(Token first) { // return true if EOF hit
		Token t;
		TokenWithIndexAndSubTokensV4 result;
		
		t = getTokenSource().nextToken();
		if ( t instanceof WritableToken ) {
			((WritableToken)t).setTokenIndex(tokens.size());
		}

		List<Token> tokenList = new ArrayList<Token>();
		int nofUnclosedParanthesis = 1;

		while(t != null && t.getType() != Token.EOF) {
			if(t.getType() == ASN1Lexer2.BEGINCHAR) {
				nofUnclosedParanthesis++;
			} else if(t.getType() == ASN1Lexer2.ENDCHAR) {
				nofUnclosedParanthesis--;
				if(nofUnclosedParanthesis == 0) {
					result = new TokenWithIndexAndSubTokensV4(ASN1Lexer2.BLOCK, tokenList, sourceFile);
					result.setCharPositionInLine(first.getCharPositionInLine());
					result.setLine(first.getLine());
					result.setStartIndex(((TokenWithIndexAndSubTokensV4) first).getStopIndex());
					result.setStopIndex(((TokenWithIndexAndSubTokensV4) t).getStopIndex());
					result.setText(makeString(tokenList));
					tokens.add(result);
					return false;
				}
			}
			if(!discardMask.contains(Integer.valueOf(t.getType()))) {
				tokenList.add(new TokenWithIndexAndSubTokensV4(t));
			}
			t = getTokenSource().nextToken();
		}
		result = new TokenWithIndexAndSubTokensV4(ASN1Lexer2.BLOCK, tokenList, sourceFile);
		result.setCharPositionInLine(first.getCharPositionInLine());
		result.setLine(first.getLine());
		result.setStartIndex(((TokenWithIndexAndSubTokensV4) first).getStopIndex());
		if (t != null) {
			result.setStopIndex(((TokenWithIndexAndSubTokensV4) t).getStopIndex());
		}
		tokens.add(result);
		return true;
	}
	
	private String makeString(List<Token> list) {
		StringBuilder text = new StringBuilder();
		for (Token t : list) {
			text.append(t.getText());
		}
		return text.toString();
	}
}
