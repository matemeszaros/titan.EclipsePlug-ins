/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.Token;

/**
 * Modified version of the <code>PatternRule</code> class. Is capable of
 * detecting a pattern which begins with a given start sequence and ends with a
 * given set of end sequences.
 * 
 * @see PatternRule
 * 
 * @author Kristof Szabados
 */
public final class StringDetectionPatternRule implements IPredicateRule {
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
	/** The pattern's start sequence. */
	private char[] fStartSequence;
	/** The pattern's end sequence. */
	private char[][] fEndSequences;
	/** The pattern's column constrain. */
	private int fColumn = UNDEFINED;
	/** The pattern's escape character. */
	private char fEscapeCharacter;
	/** Indicates whether the escape character continues a line. */
	private boolean fEscapeContinuesLine;
	/** Indicates whether end of line terminates the pattern. */
	private boolean fBreaksOnEOL;
	/** Indicates whether end of file terminates the pattern. */
	private boolean fBreaksOnEOF;

	/**
	 * Line delimiter comparator which orders according to decreasing
	 * delimiter length.
	 */
	private Comparator<char[]> fLineDelimiterComparator = new DecreasingCharArrayLengthComparator();
	/** Cached line delimiters. */
	private char[][] fLineDelimiters;
	/** Cached sorted {@linkplain #fLineDelimiters}. */
	private char[][] fSortedLineDelimiters;

	public StringDetectionPatternRule(final String startSequence, final char[][] endSequence, final IToken token) {
		this(startSequence, endSequence, token, (char) 0);
	}

	public StringDetectionPatternRule(final String startSequence, final char[][] endSequence, final IToken token, final char escapeCharacter) {
		this(startSequence, endSequence, token, escapeCharacter, false);
	}

	/**
	 * @see PatternRule#PatternRule(String, String, IToken, char, boolean)
	 * 
	 * @param startSequence
	 *                the pattern's start sequence
	 * @param endSequence
	 *                the pattern's end sequence, <code>null</code> is a
	 *                legal value
	 * @param token
	 *                the token which will be returned on success
	 * @param escapeCharacter
	 *                any character following this one will be ignored
	 * @param breaksOnEOL
	 *                indicates whether the end of the line also terminates
	 *                the pattern
	 */
	public StringDetectionPatternRule(final String startSequence, final char[][] endSequence, final IToken token, final char escapeCharacter,
			final boolean breaksOnEOL) {
		Assert.isTrue(startSequence != null && startSequence.length() > 0);
		Assert.isTrue(endSequence != null || breaksOnEOL);
		Assert.isNotNull(token);

		fStartSequence = startSequence == null ? new char[0] : startSequence.toCharArray();
		fEndSequences = endSequence;
		fToken = token;
		fEscapeCharacter = escapeCharacter;
		fBreaksOnEOL = breaksOnEOL;
	}

	/**
	 * @see PatternRule#PatternRule(String, String, IToken, char, boolean,
	 *      boolean)
	 * 
	 * @param startSequence
	 *                the pattern's start sequence
	 * @param endSequence
	 *                the pattern's end sequence, <code>null</code> is a
	 *                legal value
	 * @param token
	 *                the token which will be returned on success
	 * @param escapeCharacter
	 *                any character following this one will be ignored
	 * @param breaksOnEOL
	 *                indicates whether the end of the line also terminates
	 *                the pattern
	 * @param breaksOnEOF
	 *                indicates whether the end of the file also terminates
	 *                the pattern
	 */
	public StringDetectionPatternRule(final String startSequence, final char[][] endSequence, final IToken token, final char escapeCharacter,
			final boolean breaksOnEOL, final boolean breaksOnEOF) {
		this(startSequence, endSequence, token, escapeCharacter, breaksOnEOL);
		fBreaksOnEOF = breaksOnEOF;
	}

	/**
	 * @see PatternRule#PatternRule(String, String, IToken, char, boolean,
	 *      boolean, boolean)
	 * 
	 * @param startSequence
	 *                the pattern's start sequence
	 * @param endSequence
	 *                the pattern's end sequence, <code>null</code> is a
	 *                legal value
	 * @param token
	 *                the token which will be returned on success
	 * @param escapeCharacter
	 *                any character following this one will be ignored
	 * @param breaksOnEOL
	 *                indicates whether the end of the line also terminates
	 *                the pattern
	 * @param breaksOnEOF
	 *                indicates whether the end of the file also terminates
	 *                the pattern
	 * @param escapeContinuesLine
	 *                indicates whether the specified escape character is
	 *                used for line continuation, so that an end of line
	 *                immediately after the escape character does not
	 *                terminate the pattern, even if <code>breakOnEOL</code>
	 *                is set
	 */
	public StringDetectionPatternRule(final String startSequence, final char[][] endSequence, final IToken token, final char escapeCharacter,
			final boolean breaksOnEOL, final boolean breaksOnEOF, final boolean escapeContinuesLine) {
		this(startSequence, endSequence, token, escapeCharacter, breaksOnEOL, breaksOnEOF);
		fEscapeContinuesLine = escapeContinuesLine;
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

		if (resume) {

			if (endSequenceDetected(scanner)) {
				return fToken;
			}

		} else {

			int c = scanner.read();
			if (c == fStartSequence[0]) {
				if (sequenceDetected(scanner, fStartSequence, false)) {
					if (endSequenceDetected(scanner)) {
						return fToken;
					}
				}
			}
		}

		scanner.unread();
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
			if (c == fEscapeCharacter) {
				// Skip escaped character(s)
				if (fEscapeContinuesLine) {
					c = scanner.read();
					for (int i = 0; i < fSortedLineDelimiters.length; i++) {
						if (c == fSortedLineDelimiters[i][0] && sequenceDetected(scanner, fSortedLineDelimiters[i], true)) {
							break;
						}
					}
				} else {
					scanner.read();
				}
			} else {
				boolean found = false;
				for (int i = 0; i < fEndSequences.length; i++) {
					if (fEndSequences[i].length > 0 && c == fEndSequences[i][0]) {
						found = true;
						// Check if the specified end
						// sequence has been found.
						if (sequenceDetected(scanner, fEndSequences[i], true)) {
							return true;
						}
					}
				}
				if (!found && fBreaksOnEOL) {
					// Check for end of line since it can be
					// used to terminate the pattern.
					for (int i = 0; i < fSortedLineDelimiters.length; i++) {
						if (c == fSortedLineDelimiters[i][0] && sequenceDetected(scanner, fSortedLineDelimiters[i], true)) {
							return true;
						}
					}
				}
			}
			readCount++;
		}

		if (fBreaksOnEOF) {
			return true;
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
	private boolean sequenceDetected(final ICharacterScanner scanner, final char[] sequence, final boolean eofAllowed) {
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
		if (c == fStartSequence[0]) {
			return (fColumn == scanner.getColumn() ? doEvaluate(scanner, resume) : Token.UNDEFINED);
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
