/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.editors.GeneralPairMatcher;
import org.eclipse.titan.designer.editors.GlobalIntervalHandler;
import org.eclipse.titan.designer.editors.HeuristicalIntervalDetector;
import org.eclipse.titan.designer.editors.IReferenceParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReferenceAnalyzer;

/**
 * @author Kristof Szabados
 */
public final class TTCN3ReferenceParser implements IReferenceParser {
	private int ofs;
	private boolean reportErrors;

	public TTCN3ReferenceParser(final boolean reportErrors) {
		this.reportErrors = reportErrors;
	}

	@Override
	public void setErrorReporting(final boolean reportErrors) {
		this.reportErrors = reportErrors;
	}

	public int getReplacementOffset() {
		return ofs;
	}

	@Override
	public Reference findReferenceForCompletion(final IFile file, final int offset, final IDocument document) {
		Reference reference = null;
		ofs = offset - 1;
		if (-1 == ofs) {
			return reference;
		}

		try {
			char currentChar = document.getChar(ofs);
			if (')' == currentChar || '}' == currentChar) {
				return reference;
			}
			GeneralPairMatcher pairMatcher = new TTCN3ReferencePairMatcher();

			int tempOfs = referenceStartOffset(ofs, document, pairMatcher);

			if (-1 == tempOfs) {
				return reference;
			}

			if (tempOfs > 3) {
				if ("any ".equals(document.get(tempOfs - 2, 4)) || "all ".equals(document.get(tempOfs - 2, 4))) {
					tempOfs -= 3;
				}
			}

			ofs = tempOfs;

			// the last character where the loop stopped is not part
			// of the reference
			ofs++;
			String toBeParsed = document.get(ofs, offset - ofs);
			TTCN3ReferenceAnalyzer refAnalyzer = new TTCN3ReferenceAnalyzer();
			reference = refAnalyzer.parseForCompletion(file, toBeParsed);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return reference;
	}

	@Override
	public Reference findReferenceForOpening(final IFile file, final int offset, final IDocument document) {
		Reference reference = null;
		ofs = offset - 1;
		int endoffset = offset;
		if (-1 == ofs) {
			return reference;
		}

		try {
			GeneralPairMatcher pairMatcher = new TTCN3ReferencePairMatcher();

			ofs = referenceStartOffset(ofs, document, pairMatcher);

			if (-1 == ofs) {
				return reference;
			}

			// the last character where the loop stopped is not part
			// of the reference
			ofs++;

			if (endoffset >= document.getLength()) {
				return reference;
			}
			char currentChar = document.getChar(endoffset);

			while (endoffset < document.getLength()
					&& (Character.isLetterOrDigit(currentChar) || currentChar == '(' || currentChar == '_')) {
				if (currentChar == '(') {
					break;
				}
				endoffset++;
				if (endoffset >= document.getLength()) {
					return reference;
				}
				currentChar = document.getChar(endoffset);
			}

			String toBeParsed = document.get(ofs, endoffset - ofs);
			if (toBeParsed.trim().length() == 0) {
				return reference;
			}

			TTCN3ReferenceAnalyzer refAnalyzer = new TTCN3ReferenceAnalyzer();
			reference = refAnalyzer.parse(file, toBeParsed, reportErrors, document.getLineOfOffset(ofs), ofs);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return reference;
	}

	private int referenceStartOffset(final int offset, final IDocument document, final GeneralPairMatcher pairMatcher) throws BadLocationException {
		Interval rootInterval = GlobalIntervalHandler.getInterval(document);
		if (rootInterval == null) {
			rootInterval = (new HeuristicalIntervalDetector()).buildIntervals(document);
			GlobalIntervalHandler.putInterval(document, rootInterval);
		}

		int temporalOffset = offset;
		char currentChar = document.getChar(temporalOffset);

		Interval interval = null;
		boolean foundWhiteSpaces = false;
		boolean foundDot = false;
		while (temporalOffset > 0) {
			if (rootInterval != null) {
				interval = rootInterval.getSmallestEnclosingInterval(temporalOffset);
			}
			currentChar = document.getChar(temporalOffset);
			if (interval != null
					&& (interval_type.SINGLELINE_COMMENT.equals(interval.getType()) || interval_type.MULTILINE_COMMENT
							.equals(interval.getType()))) {
				temporalOffset = interval.getStartOffset();
			} else if (currentChar == ')' || currentChar == ']') {
				if (!foundDot) {
					break;
				}
				foundWhiteSpaces = false;
				IRegion pair = pairMatcher.match(document, temporalOffset + 1);
				if (pair == null) {
					return -1;
				}
				temporalOffset = pair.getOffset();
			} else if ('_' == currentChar || Character.isLetterOrDigit(currentChar)) {
				if (foundWhiteSpaces && !foundDot) {
					break;
				}
				foundWhiteSpaces = false;
				foundDot = false;
			} else if ('.' == currentChar) {
				foundDot = true;
			} else if (' ' == currentChar || '\t' == currentChar || '\n' == currentChar || '\r' == currentChar) {
				foundWhiteSpaces = true;
			} else if ('%' == currentChar) {
				temporalOffset--;
				break;
			} else {
				break;
			}

			temporalOffset--;
		}

		return temporalOffset;
	}
}
