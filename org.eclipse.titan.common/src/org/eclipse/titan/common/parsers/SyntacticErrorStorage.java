/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.FailedPredicateException;

/**
 * @author Kristof Szabados
 * 
 */
public class SyntacticErrorStorage {
	public final int lineNumber;
	public final int charStart;
	public final int charEnd;
	public final String message;
	public final ExceptionType exceptionType;

	public enum ExceptionType {
		LEXER_NOVIABLEALT_EXCEPTION, NOVIABLEALT_EXCEPTION, INPUTMISMATCH_EXCEPTION, FAILEDPREDICATE_EXCEPTION, INVALID_EXCEPTIONTYPE
	}

	public SyntacticErrorStorage(final int lineNumber, final int charStart, final int charEnd, final String message, final RecognitionException e) {
		this.lineNumber = lineNumber;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.message = message;

		if (e instanceof LexerNoViableAltException) {
			exceptionType = ExceptionType.LEXER_NOVIABLEALT_EXCEPTION;
		} else if (e instanceof NoViableAltException) {
			exceptionType = ExceptionType.NOVIABLEALT_EXCEPTION;
		} else if (e instanceof FailedPredicateException) {
			exceptionType = ExceptionType.FAILEDPREDICATE_EXCEPTION;
		} else if (e instanceof InputMismatchException) {
			exceptionType = ExceptionType.INPUTMISMATCH_EXCEPTION;
		} else {
			exceptionType = ExceptionType.INVALID_EXCEPTIONTYPE;
		}
	}
}
