/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.actions.IndentationSupport;

/**
 * @author Kristof Szabados
 * */
public abstract class GeneralTITANAutoEditStrategy implements IAutoEditStrategy {
	public static final String EMPTY_STRING = "";
	protected static final String TRUE = "true";
	protected String indentString;

	protected Interval rootInterval;
	protected IHeuristicalIntervalDetector heuristicIntervalDetector;
	protected IPreferenceStore preferenceStore;

	/**
	 * Refreshing AutoEditStrategy from the preference store.
	 */
	protected final void refreshAutoEditStrategy() {
		indentString = IndentationSupport.getIndentString();
		preferenceStore = Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes the root interval for this auto edit strategy.
	 * 
	 * @param document
	 *                the document to use when initializing the interval.
	 * */
	protected final void initializeRootInterval(final IDocument document) {
		rootInterval = GlobalIntervalHandler.getInterval(document);
		if (rootInterval == null) {
			rootInterval = (new HeuristicalIntervalDetector()).buildIntervals(document);
			GlobalIntervalHandler.putInterval(document, rootInterval);
		}
	}

	public final void setHeuristicIntervalDetector(final IHeuristicalIntervalDetector heuristicIntervalDetector) {
		this.heuristicIntervalDetector = heuristicIntervalDetector;
	}

	/**
	 * Determines if the given string parameter ends with a legal line
	 * delimiter of document d.
	 * 
	 * @param d
	 *                - the document with the certain line delimiters we are
	 *                concerned with
	 * @param txt
	 *                - the string to be examined if it is ended by a line
	 *                delimiter
	 * @return Returns a boolean value according to the presence of the line
	 *         delimiter
	 */
	protected final boolean endsWithDelimiter(final IDocument d, final String txt) {
		String[] delimiters = d.getLegalLineDelimiters();
		for (int i = 0; i < delimiters.length; i++) {
			if (txt.endsWith(delimiters[i])) {
				return true;
			}
		}
		return false;
	}

	protected final int delimiterLength() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return 2;
		}

		return 1;
	}

	/**
	 * The method determines if the given offset is within a multy line
	 * comment in the document.
	 * 
	 * @param offset
	 *                the position where parsing starts from
	 * @return Whether offset is within a comment.
	 */
	protected final boolean isWithinMultiLineComment(final int offset) {
		Interval interval = rootInterval.getSmallestEnclosingInterval(offset);
		if ((interval_type.MULTILINE_COMMENT.equals(interval.getType())) && interval.getStartOffset() != offset) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the whitespace characters at the end of the given line.
	 * 
	 * @param document
	 *                - the document being parsed
	 * @param line
	 *                - the line being searched
	 * @param command
	 *                - the command being handled.
	 * @return the String with the leading whitespace characters of the
	 *         given line
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	protected final String getIndentOfLine(final IDocument document, final int line, final DocumentCommand command) throws BadLocationException {
		if (line > -1) {
			int start = document.getLineOffset(line);
			int end = start + document.getLineLength(line) - 1;
			int whiteend = findEndOfWhiteSpace(document, start, end);
			// The end of whitespace characters is counted only up
			// to the cursor position.
			// Otherwise the remaining whitespace characters
			// following the cursor will also
			// be considered as being part of the indentation of the
			// next line causing ever
			// growing line length and bad cursor positioning.
			if (whiteend > command.offset) {
				whiteend = command.offset;
			}
			return document.get(start, whiteend - start);
		}

		return EMPTY_STRING;
	}

	/**
	 * Returns the offset of the first non-whitespace character in the
	 * examined document region.
	 * 
	 * @param document
	 *                - the document to be searched for non-whitespace
	 *                characters
	 * @param offset
	 *                - the offset where the search start
	 * @param end
	 *                - the end of the region
	 * @return Returns the offset of the first non-whitespace character
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	protected final int findEndOfWhiteSpace(final IDocument document, final int offset, final int end) throws BadLocationException {
		int offset2 = offset;
		while (offset2 < end) {
			char c = document.getChar(offset2);
			if (c != ' ' && c != '\t') {
				return offset2;
			}
			offset2++;
		}
		return end;
	}

	/**
	 * Returns the line number of the next bracket after end.
	 * 
	 * @returns the line number of the next matching bracket after end
	 * @param document
	 *                - the document being parsed
	 * @param end
	 *                - the end position to search back from (exclusive
	 *                limit)
	 * @return The position of the matching open bracket
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	protected final int findMatchingOpenBracket(final IDocument document, final int end) throws BadLocationException {
		Interval interval;
		interval = rootInterval.getSmallestEnclosingInterval(end);
		return document.getLineOfOffset(interval.getStartOffset());
	}

	/**
	 * Returns the whether there is an interval starting within the
	 * specified range, but ending outside it.
	 * 
	 * @param start
	 *                the beginning of the range
	 * @param end
	 *                the end of the range
	 * 
	 * @return whether there is such an interval or not.
	 * */
	protected final boolean containsUnclosedInterval(final int start, final int end) {
		Interval endInterval = rootInterval.getSmallestEnclosingInterval(end);
		if (endInterval.getStartOffset() >= start && interval_type.NORMAL.equals(endInterval.getType())) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the whether there is an interval ending within the specified
	 * range, but starting outside it.
	 * 
	 * @param start
	 *                the beginning of the range
	 * @param end
	 *                the end of the range
	 * 
	 * @return whether there is such an interval or not.
	 * */
	protected final boolean containsUnopenedInterval(final int start, final int end) {
		Interval startInterval = rootInterval.getSmallestEnclosingInterval(start);
		if (startInterval.getEndOffset() <= end && interval_type.NORMAL.equals(startInterval.getType())) {
			return true;
		}

		return false;
	}

	/**
	 * This method determines whether the offset position given in the
	 * second parameter could be within a statement block that is not closed
	 * by a closing curly bracket.
	 * 
	 * @param document
	 *                the document being parsed
	 * @param start
	 *                starting offset within the document
	 * @return Returns a boolean value. It is true if the statement block
	 *         might not be closed.
	 * @throws BadLocationException
	 */
	protected final boolean canStatementBlockBeOpen(final IDocument document, final int start) {
		Interval interval = rootInterval.getSmallestEnclosingInterval(start);
		if (!interval_type.NORMAL.equals(interval.getType())) {
			return false;
		}

		while (!rootInterval.equals(interval)) {
			if (interval.getErroneous()) {
				return true;
			}
			interval = interval.getParent();
		}

		return false;
	}

	/**
	 * The method determines if the given offset is within a string in the
	 * document.
	 * 
	 * @param document
	 *                the document being parsed
	 * @param offset
	 *                the position where parsing starts from
	 * @return Whether offset is within a comment.
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	protected final boolean isWithinString(final StringBuilder document, final int offset) throws BadLocationException {
		if (heuristicIntervalDetector != null) {
			heuristicIntervalDetector.isWithinString(document, offset, rootInterval);
		}

		return false;
	}
}
