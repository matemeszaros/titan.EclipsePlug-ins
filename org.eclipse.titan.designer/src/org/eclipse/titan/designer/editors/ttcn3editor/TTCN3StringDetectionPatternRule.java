/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Kristof Szabados
 * */
public class TTCN3StringDetectionPatternRule implements IPredicateRule {
	/**
	 * @see PatternRule
	 */
	private static class DecreasingCharArrayLengthComparator implements Comparator<char[]> {
		@Override
		public int compare(final char[] o1, final char[] o2) {
			return o2.length - o1.length;
		}
	}

	/** Internal setting for the un-initialized column constraint. */
	private static final int UNDEFINED = -1;

	/** The token to be returned on success. */
	private IToken fToken;
	/** The pattern's column constrain. */
	private int fColumn = UNDEFINED;

	/**
	 * Line delimiter comparator which orders according to decreasing
	 * delimiter length.
	 */
	private Comparator<char[]> fLineDelimiterComparator = new DecreasingCharArrayLengthComparator();
	/** Cached line delimiters. */
	private char[][] fLineDelimiters;
	/** Cached sorted {@linkplain #fLineDelimiters}. */
	private char[][] fSortedLineDelimiters;

	public TTCN3StringDetectionPatternRule(final IToken token) {
		fToken = token;
	}

	/**
	 * Sets a column constraint for this rule. If set, the rule's token will
	 * only be returned if the pattern is detected starting at the specified
	 * column. If the column is smaller then 0, the column constraint is
	 * considered removed.
	 *
	 * @param column
	 *                the column in which the pattern starts
	 *
	 * @see PatternRule#setColumnConstraint(int)
	 */
	public void setColumnConstraint(final int column) {
		if (column < 0) {
			fColumn = UNDEFINED;
		} else {
			fColumn = column;
		}
	}

	/**
	 * Evaluates this rules without considering any column constraints.
	 *
	 * @param scanner
	 *                the character scanner to be used
	 * @return the token resulting from this evaluation
	 *
	 * @see PatternRule#evaluate(ICharacterScanner)
	 */
	protected IToken doEvaluate(final ICharacterScanner scanner) {
		return doEvaluate(scanner, false);
	}

	/**
	 * Evaluates this rules without considering any column constraints.
	 * Resumes detection, i.e. look sonly for the end sequence required by
	 * this rule if the <code>resume</code> flag is set.
	 *
	 * @param scanner
	 *                the character scanner to be used
	 * @param resume
	 *                <code>true</code> if detection should be resumed,
	 *                <code>false</code> otherwise
	 * @return the token resulting from this evaluation
	 *
	 * @see PatternRule#evaluate(ICharacterScanner, boolean)
	 */
	protected IToken doEvaluate(final ICharacterScanner scanner, final boolean resume) {
		if (!resume) {
			int c = scanner.read();
			if (c != '"') {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}

		if (endSequenceDetected(scanner)) {
			return fToken;
		}

		if (!resume) {
			scanner.unread();
		}

		return Token.UNDEFINED;
	}

	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	@Override
	public IToken evaluate(final ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}

	/**
	 * Returns whether the end sequence was detected. As the pattern can be
	 * considered ended by a line delimiter, the result of this method is
	 * <code>true</code> if the rule breaks on the end of the line, or if
	 * the EOF character is read.
	 *
	 * @param scanner
	 *                the character scanner to be used
	 * @return <code>true</code> if the end sequence has been detected
	 */
	private boolean endSequenceDetected(final ICharacterScanner scanner) {

		char[][] originalDelimiters = scanner.getLegalLineDelimiters();
		int count = originalDelimiters.length;
		if (fLineDelimiters == null || originalDelimiters.length != count) {
			fSortedLineDelimiters = new char[count][];
		} else {
			while (count > 0 && fLineDelimiters[count - 1] == originalDelimiters[count - 1]) {
				count--;
			}
		}
		if (count != 0) {
			fLineDelimiters = originalDelimiters;
			System.arraycopy(fLineDelimiters, 0, fSortedLineDelimiters, 0, fLineDelimiters.length);
			Arrays.sort(fSortedLineDelimiters, fLineDelimiterComparator);
		}

		int readCount = 1;
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			if (c == '\\') {
				// Skip escaped character
				scanner.read();
			} else if (c == '"') {
				int c2 = scanner.read();
				if (c2 != '"') {
					scanner.unread();
					return true;
				}
			}

			readCount++;
		}

		for (; readCount > 0; readCount--) {
			scanner.unread();
		}

		return false;
	}

	/**
	 * Returns whether the next characters to be read by the character
	 * scanner are an exact match with the given sequence. No escape
	 * characters are allowed within the sequence. If specified the sequence
	 * is considered to be found when reading the EOF character.
	 *
	 * @param scanner
	 *                the character scanner to be used
	 * @param sequence
	 *                the sequence to be detected
	 * @param eofAllowed
	 *                indicated whether EOF terminates the pattern
	 * @return <code>true</code> if the given sequence has been detected
	 */
	protected boolean sequenceDetected(final ICharacterScanner scanner, final char[] sequence, final boolean eofAllowed) {
		for (int i = 1; i < sequence.length; i++) {
			int c = scanner.read();
			if (c == ICharacterScanner.EOF && eofAllowed) {
				return true;
			} else if (c != sequence[i]) {
				// Non-matching character detected, rewind the
				// scanner back to the start.
				// Do not unread the first character.
				scanner.unread();
				for (int j = i - 1; j > 0; j--) {
					scanner.unread();
				}
				return false;
			}
		}

		return true;
	}

	/*
	 * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
	 */
	@Override
	public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
		if (fColumn == UNDEFINED) {
			return doEvaluate(scanner, resume);
		}

		int c = scanner.read();
		scanner.unread();
		if (c == '"') {
			return fColumn == scanner.getColumn() ? doEvaluate(scanner, resume) : Token.UNDEFINED;
		}
		return Token.UNDEFINED;
	}

	/*
	 * @see IPredicateRule#getSuccessToken()
	 */
	@Override
	public IToken getSuccessToken() {
		return fToken;
	}
}
