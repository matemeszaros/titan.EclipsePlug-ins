/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * @author Kristof Szabados
 * */
public final class WordDetector implements IWordDetector {

	@Override
	public boolean isWordStart(final char aChar) {
		return Character.isLetter(aChar);
	}

	@Override
	public boolean isWordPart(final char aChar) {
		return Character.isLetterOrDigit(aChar) || '_' == aChar;
	}
}
