/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.IntervalDetector;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.designer.editors.IHeuristicalIntervalDetector;

/**
 * This class can be used to build an interval hierarchy from file types, for
 * which the needed lexers are not yet developed.
 * 
 * @author Kristof Szabados
 * */
public final class HeuristicalIntervalDetector extends IntervalDetector implements IHeuristicalIntervalDetector {

	/**
	 * This function builds an interval hierarchy from its input.
	 * <p>
	 * This not a fully fledged lexer, on some heuristics are used.
	 * 
	 * @param document
	 *                the document to be used as the input
	 * 
	 * @return the root of the interval tree.
	 * */
	public synchronized Interval buildIntervals(final IDocument document) {
		final String text = document.get();
		final int rangeEnd = text.length();
		initRootInterval(text.length());

		int nextPos = 0;
		int actualLine = 0;
		boolean insideString = false;
		try {
			while (nextPos < rangeEnd) {
				switch (text.charAt(nextPos)) {
				case '(':
					if (!insideString && !actualInterval.isComment()) {
						pushInterval(nextPos, actualLine, interval_type.PARAMETER);
					}
					break;
				case ')':
					if (!insideString && interval_type.PARAMETER.equals(actualInterval.getType())) {
						popInterval(nextPos, actualLine);
					}
					break;
				case '[':
					if (!insideString && !actualInterval.isComment()) {
						pushInterval(nextPos, actualLine, interval_type.INDEX);
					}
					break;
				case ']':
					if (!insideString && interval_type.INDEX.equals(actualInterval.getType())) {
						popInterval(nextPos, actualLine);
					}
					break;
				case '{':
					if (!insideString && !actualInterval.isComment()) {
						pushInterval(nextPos, actualLine, interval_type.NORMAL);
					}
					break;
				case '}':
					if (!insideString && interval_type.NORMAL.equals(actualInterval.getType())) {
						popInterval(nextPos, actualLine);
					}
					break;
				case '/':
					if (!insideString && !actualInterval.isComment() && nextPos + 1 < rangeEnd) {
						switch (text.charAt(nextPos + 1)) {
						case '*':
							pushInterval(nextPos, actualLine, interval_type.MULTILINE_COMMENT);
							nextPos++;
							break;
						case '/':
							pushInterval(nextPos, actualLine, interval_type.SINGLELINE_COMMENT);
							nextPos++;
							break;
						default:
							break;
						}
					}
					break;
				case '*':
					if (!insideString && interval_type.MULTILINE_COMMENT.equals(actualInterval.getType())
							&& nextPos + 1 < rangeEnd && '/' == text.charAt(nextPos + 1)) {
						popInterval(nextPos, actualLine);
						nextPos++;
					}
					break;
				case '"':
					if (nextPos - 1 > 0 && '\\' != text.charAt(nextPos - 1) && !actualInterval.isComment()) {
						insideString = !insideString;
					}
					break;
				case '#':
					if (!insideString && !actualInterval.isComment()) {
						pushInterval(nextPos, actualLine, interval_type.SINGLELINE_COMMENT);
					}
					break;
				case '\n':
					if (interval_type.SINGLELINE_COMMENT.equals(actualInterval.getType())) {
						int actualPosition = nextPos;
						boolean whitespace = true;
						int linesCrossed = 0;
						while (nextPos < rangeEnd && whitespace && linesCrossed < 2) {
							switch (text.charAt(nextPos)) {
							case '\n':
								linesCrossed++;
								nextPos++;
								actualLine++;
								break;
							case '\r':
								nextPos++;
								if (nextPos < rangeEnd && text.charAt(nextPos) == '\n') {
									nextPos++;
									linesCrossed++;
									actualLine++;
								}
								break;
							case ' ':
							case '\t':
								nextPos++;
								break;
							default:
								whitespace = false;
								break;
							}
						}

						if (linesCrossed >= 2 || nextPos + 1 >= rangeEnd) {
							popInterval(actualPosition, actualLine);
						} else {
							char temp = text.charAt(actualInterval.getStartOffset());
							if (temp == '/' && (text.charAt(nextPos) != '/' || text.charAt(nextPos + 1) != '/')) {
								popInterval(actualPosition, actualLine);
							} else if (temp == '#' && (text.charAt(nextPos) != '#')) {
								popInterval(actualPosition, actualLine);
							}
						}
						nextPos--;
					} else {
						actualLine++;
					}
					break;
				default:
					break;
				}
				nextPos++;
			}
		} catch (IndexOutOfBoundsException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		handleFinalCorrection();

		return rootInterval;
	}

	/**
	 * The method determines if the given offset is within a string in the
	 * document.
	 * 
	 * @param document
	 *                the document being parsed
	 * @param offset
	 *                the position where parsing starts from
	 * @param enclosingInterval
	 *                an interval, which encloses the offset.
	 * @return Whether offset is within a comment.
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	// FIXME needs correction
	@Override
	public boolean isWithinString(final StringBuilder document, final int offset, final Interval enclosingInterval) throws BadLocationException {
		Interval interval = enclosingInterval.getSmallestEnclosingInterval(offset);
		if (interval_type.MULTILINE_COMMENT.equals(interval.getType()) || interval_type.SINGLELINE_COMMENT.equals(interval.getType())) {
			return false;
		}

		int start = interval.getStartOffset();
		int counter = 0;
		while (start < offset) {
			char curr = document.charAt(start);
			if (curr == '"' || curr == '\'') {
				counter++;
			}
			start++;
		}
		return counter % 2 != 0;
	}
}
