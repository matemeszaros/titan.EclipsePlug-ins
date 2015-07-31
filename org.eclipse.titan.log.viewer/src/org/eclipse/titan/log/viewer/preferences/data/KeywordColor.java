/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.data;

import org.eclipse.swt.graphics.RGB;

public class KeywordColor {

	private String keyword;
	private RGB color;

	public KeywordColor(String keyword, RGB color) {
		this.keyword = keyword;
		this.color = color;
	}

	public RGB getColor() {
		return this.color;
	}
	public String getKeyword() {
		return this.keyword;
	}
}
