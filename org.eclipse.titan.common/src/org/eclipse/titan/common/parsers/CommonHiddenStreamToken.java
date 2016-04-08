/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

public class CommonHiddenStreamToken {

	private String mText;
	private CommonHiddenStreamToken mHiddenBefore;
	private CommonHiddenStreamToken mHiddenAfter;

	public CommonHiddenStreamToken(final String aText) {
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
