/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.GeneralPairMatcher;

/**
 * Our double click strategy selects the word under the cursor.
 * 
 * @author Kristof Szabados
 */
public class DoubleClickStrategy implements ITextDoubleClickStrategy {
	protected ITextViewer fText;

	@Override
	public final void doubleClicked(final ITextViewer part) {
		int pos = part.getSelectedRange().x;
		fText = part;

		if (fText.getDocument().getLength() < pos) {
			return;
		}

		if (pos < 0) {
			return;
		}

		GeneralPairMatcher pairMatcher = new PairMatcher();

		IRegion region = pairMatcher.match(fText.getDocument(), pos);
		if (region != null) {
			fText.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
			return;
		}

		selectWord(pos);
	}

	/**
	 * Searches for the actual selection range, and sets it.
	 * 
	 * @see #doubleClicked(ITextViewer)
	 * @param caretPos
	 *                The position of the mouse cursor.
	 */
	protected void selectWord(final int caretPos) {
		final IDocument doc = fText.getDocument();
		int startPos;
		int endPos;

		try {
			char c;
			for (startPos = caretPos - 1; startPos >= 0; startPos--) {
				c = doc.getChar(startPos);
				if (c != '_' && !Character.isLetterOrDigit(c)) {
					break;
				}
			}

			int length = doc.getLength();
			for (endPos = caretPos; endPos < length; endPos++) {
				c = doc.getChar(endPos);
				if (c != '_' && !Character.isLetterOrDigit(c)) {
					break;
				}
			}

			fText.setSelectedRange(startPos + 1, endPos - startPos - 1);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
