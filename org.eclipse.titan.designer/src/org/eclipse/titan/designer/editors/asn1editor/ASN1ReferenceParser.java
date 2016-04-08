/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
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
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Listener;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Lexer;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.ModuleLevelTokenStreamTracker;
import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensFactory;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1ReferenceParser implements IReferenceParser {
	private int ofs;

	public ASN1ReferenceParser() {
	}

	@Override
	public final void setErrorReporting(final boolean reportErrors) {
		//Do nothing
	}

	public final int getReplacementOffset() {
		return ofs;
	}

	@Override
	public final Reference findReferenceForCompletion(final IFile file, final int offset, final IDocument document) {
		Reference reference = null;
		ofs = offset - 1;
		if (-1 == ofs) {
			return reference;
		}

		try {
			char currentChar = document.getChar(ofs);
			if (']' == currentChar || ')' == currentChar || '}' == currentChar) {
				return reference;
			}
			GeneralPairMatcher pairMatcher = new ASN1ReferencePairMatcher();

			ofs = referenceStartOffset(ofs, document, pairMatcher);

			if (-1 == ofs) {
				return reference;
			}

			// the last character where the loop stopped is not part
			// of the reference
			ofs++;
			String toBeParsed = document.get(ofs, offset - ofs);

			ASN1ReferenceParser refAnalyzer = newInstance();

			reference = refAnalyzer.parseReference(file, toBeParsed, document.getLineOfOffset(ofs), ofs);

		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return reference;
	}

	@Override
	public final Reference findReferenceForOpening(final IFile file, final int offset, final IDocument document) {
		Reference reference = null;
		ofs = offset - 1;
		int endoffset = offset;
		if (-1 == ofs) {
			return reference;
		}

		try {
			char currentChar = document.getChar(ofs);
			GeneralPairMatcher pairMatcher = new ASN1ReferencePairMatcher();

			ofs = referenceStartOffset(ofs, document, pairMatcher);

			// the last character where the loop stopped is not part
			// of the reference
			ofs++;

			if (endoffset >= document.getLength()) {
				return reference;
			}
			currentChar = document.getChar(endoffset);

			while (endoffset < document.getLength()
					&& (Character.isLetterOrDigit(currentChar) || currentChar == '(' || currentChar == '_' || currentChar == '-')) {
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

			ASN1ReferenceParser refAnalyzer = newInstance();

			reference = refAnalyzer.parseReference(file, toBeParsed, document.getLineOfOffset(ofs)+1,ofs );
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return reference;
	}

	private final int referenceStartOffset(final int offset, final IDocument document, final GeneralPairMatcher pairMatcher) throws BadLocationException {
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
			} else if (currentChar == '}') {
				if (foundWhiteSpaces && !foundDot) {
					break;
				}
				foundWhiteSpaces = false;
				foundDot = false;
				IRegion pair = pairMatcher.match(document, temporalOffset + 1);
				if (pair == null) {
					return -1;
				}
				temporalOffset = pair.getOffset();
			} else if ('-' == currentChar || '_' == currentChar || Character.isLetterOrDigit(currentChar)) {
				if (foundWhiteSpaces && !foundDot) {
					break;
				}
				foundWhiteSpaces = false;
				foundDot = false;
			} else if ('.' == currentChar) {
				foundDot = true;
			} else if (' ' == currentChar || '\t' == currentChar || '\n' == currentChar || '\r' == currentChar) {
				foundWhiteSpaces = true;
			} else {
				break;
			}

			temporalOffset--;
		}

		return temporalOffset;
	}

	private Reference parseReference(final IFile file, final String input, final int line, final int offset) {
		Reference reference = null;
		StringReader reader = new StringReader(input);
		CharStream charStream = new UnbufferedCharStream(reader);
		Asn1Lexer lexer = new Asn1Lexer(charStream);
		lexer.setTokenFactory(new TokenWithIndexAndSubTokensFactory(true));
		ASN1Listener lexerListener = new ASN1Listener();
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		lexer.addErrorListener(lexerListener);
		ModuleLevelTokenStreamTracker tracker = new ModuleLevelTokenStreamTracker(lexer);
		tracker.discard(Asn1Lexer.WS);
		tracker.discard(Asn1Lexer.MULTILINECOMMENT);
		tracker.discard(Asn1Lexer.SINGLELINECOMMENT);
		Asn1Parser parser = new Asn1Parser(tracker);
		parser.setProject(file.getProject());
		parser.setActualFile(file);
		parser.setLine(line);
		parser.setOffset(offset);
		parser.setBuildParseTree(false);
		ASN1Listener parserListener = new ASN1Listener();
		parser.removeErrorListeners(); // remove ConsoleErrorListener
		parser.addErrorListener(parserListener);
		reference = parser.pr_parseReference().reference;
		return reference;
	}
	
	private ASN1ReferenceParser newInstance() {
		return new ASN1ReferenceParser();
	}

}