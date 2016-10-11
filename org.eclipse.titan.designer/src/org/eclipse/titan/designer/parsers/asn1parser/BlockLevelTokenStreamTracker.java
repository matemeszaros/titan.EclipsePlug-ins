/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Lexer;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Listener;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;

public class BlockLevelTokenStreamTracker extends BufferedTokenStream {
	private HashSet<Integer> discardMask = new HashSet<Integer>();
	private IFile sourceFile;
	private int index;
	private List<Token> oldList;

	protected BlockLevelTokenStreamTracker(Block aBlock, int aStartIndex) {
		super(aBlock); 
		this.index = aStartIndex;
		this.oldList = aBlock.getTokenList();
	}

	public void setActualFile(IFile sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public void discard(int ttype) {
		discardMask.add(Integer.valueOf(ttype));
	}

	public int getActualIndex() {
		return index;
	}

	@Override
	public int fetch(int n) {
		if (fetchedEOF) {
			return 0;
		}	

		Token t;
		Token first;
		int i = 0;
		
		if (oldList == null || index >= oldList.size()) {
			tokens.add(new TokenWithIndexAndSubTokens(Token.EOF));
			return ++i;
		}
		
		do {
			t = oldList.get(index++);
			first = t;
			if (t == null) {
				return 0;
			} else if (discardMask.contains(Integer.valueOf(t.getType()))) {
				// discard this Token
				assert(true); // TODO: remove it if it proves OK (in Lexer done it)
			} else if (t.getType() == Asn1Lexer.BEGINCHAR) {
				boolean exit = getBlock(first);
				if (exit) {
					return ++i;
				}
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

	public static Asn1Parser getASN1ParserForBlock(Block aBlock) {
		return getASN1ParserForBlock(aBlock, 0);
	}
	
	public static Asn1Parser getASN1ParserForBlock(Block aBlock, int startIndex) {
		if(aBlock == null || aBlock.getLocation() == null) {
			return null;
		}
		
		BlockLevelTokenStreamTracker tracker = new BlockLevelTokenStreamTracker(aBlock, startIndex);
		tracker.discard(Asn1Lexer.WS);
		tracker.discard(Asn1Lexer.MULTILINECOMMENT);
		tracker.discard(Asn1Lexer.SINGLELINECOMMENT);
		
		Asn1Parser parser = new Asn1Parser(tracker);
		
		tracker.setActualFile((IFile) aBlock.getLocation().getFile());
		parser.setActualFile((IFile) aBlock.getLocation().getFile());
		parser.setBuildParseTree(false);
		ASN1Listener parserListener = new ASN1Listener(parser);
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);		
		return parser;
	}
	
	private boolean getBlock(Token first) { // return true if it were out of bond
		if(index >= oldList.size()) {
			tokens.add(first);
			return true;
		}

		TokenWithIndexAndSubTokens result;
		Token t = oldList.get(index++);
		List<Token> tokenList = new ArrayList<Token>();
		int nofUnclosedParanthesis = 1;
		while(t != null && t.getType() != Token.EOF && index < oldList.size()) {
			if(t.getType() == Asn1Lexer.BEGINCHAR) {
				nofUnclosedParanthesis++;
			} else if(t.getType() == Asn1Lexer.ENDCHAR) {
				nofUnclosedParanthesis--;
				if(nofUnclosedParanthesis == 0) {
					result = new TokenWithIndexAndSubTokens(Asn1Lexer.BLOCK, tokenList, sourceFile);
					result.setCharPositionInLine(first.getCharPositionInLine());
					result.setLine(first.getLine());
					result.setStartIndex(((TokenWithIndexAndSubTokens) first).getStopIndex());
					result.setStopIndex(((TokenWithIndexAndSubTokens) t).getStopIndex());
					result.setText(makeString(tokenList));
					tokens.add(result);
					return false;
				}
			}
			if(!discardMask.contains(Integer.valueOf(t.getType()))) {
				tokenList.add(t);
			}
			t = oldList.get(index++);
		}
		result = new TokenWithIndexAndSubTokens(Asn1Lexer.BLOCK, tokenList, sourceFile);
		result.setCharPositionInLine(first.getCharPositionInLine());
		result.setLine(first.getLine());
		result.setStartIndex(((TokenWithIndexAndSubTokens) first).getStopIndex());
		if (t != null) {
			result.setStopIndex(((TokenWithIndexAndSubTokens) t).getStopIndex());
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
