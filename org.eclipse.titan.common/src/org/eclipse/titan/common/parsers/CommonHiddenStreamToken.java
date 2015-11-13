/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import org.antlr.v4.runtime.Token;

public class CommonHiddenStreamToken {

	private String mText;
	private int mTokenType = Token.INVALID_TYPE;
    private CommonHiddenStreamToken mHiddenBefore;
    private CommonHiddenStreamToken mHiddenAfter;
    
    /**
     * @param aTokenType token type according to CfgLexer.java (for example: CfgLexer.WS)
     * @param aText
     */
	public CommonHiddenStreamToken(int aTokenType, String aText) {
		mTokenType = aTokenType;
		mText = aText;
	}

	public CommonHiddenStreamToken(String aText) {
		mText = aText;
	}

	public String getText() {
		return mText;
	}

	public CommonHiddenStreamToken getHiddenBefore() {
		return mHiddenBefore;
	}

	public CommonHiddenStreamToken getHiddenAfter() {
		return mHiddenAfter;
	}

}
