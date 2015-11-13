/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.DoubleClickStrategy;

/**
 * Our double click strategy selects the word under the cursor.
 * 
 * @author Kristof Szabados
 */
public final class ASN1DoubleClickStrategy extends DoubleClickStrategy {

	@Override
	protected void selectWord(final int caretPos) {
		final IDocument doc = fText.getDocument();
		int startPos;
		int endPos;

		try {
			char c;
			for (startPos = caretPos; startPos >= 0; startPos--) {
				c = doc.getChar(startPos);
				if (c != '_' && c != '-' && !Character.isLetterOrDigit(c)) {
					break;
				}
			}

			int length = doc.getLength();
			for (endPos = caretPos; endPos < length; endPos++) {
				c = doc.getChar(endPos);
				if (c != '_' && c != '-' && !Character.isLetterOrDigit(c)) {
					break;
				}
			}

			fText.setSelectedRange(startPos + 1, endPos - startPos - 1);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
