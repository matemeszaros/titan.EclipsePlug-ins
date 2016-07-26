/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.common.parsers.cfg.CfgParseTreePrinter;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class IntervalDetector {
	/** the root interval of the interval hierarchy tree. */
	protected Interval rootInterval = null;
	/** the actual interval, that can be modified directly. */
	protected Interval actualInterval = null;

	private int maxLength;

	/**
	 * Creates and initializes the root interval, and sets it as the actual interval.
	 *
	 * @param length the length of the root interval
	 * */
	public void initRootInterval(final int length) {
		actualInterval = new Interval(null, interval_type.NORMAL);
		rootInterval = actualInterval;
		actualInterval.setStartOffset(0);
		actualInterval.setStartLine(0);
		actualInterval.setEndOffset(length);
		maxLength = length;
	}

	/**
	 * @return the pre-set maximum length any interval can reach.
	 * */
	protected int getMaxLength() {
		return maxLength;
	}

	/**
	 * Sets the maximum length an interval can reach.
	 * Used to correct the last intervals, if needed.
	 *
	 * @param maxLength the length of the processed document.
	 * */
	protected void setMaxLength(final int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * Creates and pushes a new interval onto the stack of intervals. This new interval becomes the actual one.
	 * <p>
	 * The ending offset of this interval is not yet set. @see #popInterval(int)
	 *
	 * @param offset the offset at which the new interval should start
	 * @param line the line at which the new interval should start
	 * @param type the type of the interval
	 */
	public final void pushInterval(final int offset, final int line, final interval_type type) {
		actualInterval = new Interval(actualInterval, type);
		if (rootInterval == null) {
			rootInterval = actualInterval;
		}
		actualInterval.setStartOffset(offset);
		actualInterval.setStartLine(line);
	}

	/**
	 * Creates and pushes a new interval onto the stack of intervals. This new interval becomes the actual one.
	 * <p>
	 * The ending offset of this interval is not yet set. @see #popInterval(int)
	 *
	 * @param aToken the first token of the interval
	 * @param aTokenStream token stream to get the list of tokens for searching hidden tokens
	 * @param aType the type of the interval
	 */
	public final void pushInterval(final Token aToken, final TokenStream aTokenStream, final interval_type aType ) {
		pushInterval( aToken.getCharPositionInLine(), aToken.getLine(), aType );
	}

	/**
	 * Pops the actual interval off of the stack, making its parent the actual interval. The ending offset of the popped off interval is set here.
	 * <p>
	 * If the actual interval is the root interval, than it is not popped off the stack. This situation can only happen in case of a syntactically
	 * invalid file.
	 *
	 * @param offset the offset value to be set as the ending offset of the actual interval
	 * @param line the line to be set as the ending line of the actual interval
	 * */
	public final void popInterval(final int offset, final int line) {
		if (actualInterval != null) {
			actualInterval.setEndOffset(offset);
			actualInterval.setEndLine(line);
			if (rootInterval.equals(actualInterval)) {
				actualInterval.setErroneous();
			} else {
				actualInterval = actualInterval.getParent();
			}
		}
	}

	/**
	 * Pops the actual interval off of the stack, making its parent the actual interval. The ending offset of the popped off interval is set here.
	 * <p>
	 * If the actual interval is the root interval, than it is not popped off the stack. This situation can only happen in case of a syntactically
	 * invalid file.
	 *
	 * @param aToken the last token of the interval
	 * @param aTokenStream token stream to get the list of tokens for searching hidden tokens
	 */
	public final void popInterval(final Token aToken, final TokenStream aTokenStream ) {
		popInterval( aToken.getCharPositionInLine(), aToken.getLine() );
	}

	/**
	 * Pops the actual interval off of the stack, making its parent the actual interval. The ending offset of the popped off interval is set here.
	 * <p>
	 * If the actual interval is the root interval, than it is not popped off the stack. This situation can only happen in case of a syntactically
	 * invalid file.
	 * <p>
	 * The last non-hidden token will be the end of the interval.
	 *
	 * @param aTokenStream token stream to get the list of tokens for searching hidden tokens
	 */
	public final void popInterval( final CommonTokenStream aTokenStream ) {
		final int nonHiddenIndex = getNonHiddenTokensBefore( aTokenStream.index() - 1, aTokenStream.getTokens() );
		final Token t = aTokenStream.get( nonHiddenIndex );
		popInterval( t, aTokenStream );
	}

	/**
	 * @param aIndex the token index, where the search is started
	 * @param aTokens token list from token stream
	 * @return the index of the first non-hidden token to the left
	 */
	private final int getNonHiddenTokensBefore( final int aIndex,
												final List<Token> aTokens ) {
		// 1st non hidden token to the left
		int nonHiddenIndex = aIndex;
		while ( CfgParseTreePrinter.isHiddenToken( nonHiddenIndex, aTokens ) ) {
			nonHiddenIndex--;
		}
		return nonHiddenIndex;
	}
	
	/**
	 * @return the root interval of this interval hierarchy tree.
	 * */
	public final Interval getRootInterval() {
		return rootInterval;
	}

	/**
	 * This function shall be called after the reading of the source of the intervals has finished. If the actual interval is not the root interval,
	 * or if its ending offset is not set, this indicates syntax errors in the file. To make the built interval hierarchy is usable, the ending offset of
	 * the actual interval and all of its parents is set to one less than the root intervals ending offset.
	 * */
	public void handleFinalCorrection() {
		if (rootInterval == null) {
			return;
		}

		while (actualInterval != null && !actualInterval.equals(rootInterval)) {
			actualInterval.setErroneous();

			// TODO the ending line should be set correctly.
			popInterval(maxLength - 1, -1);
		}
	}
}
