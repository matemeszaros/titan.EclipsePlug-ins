/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * Handles the insertion of closing brackets.
 * 
 * @author Kristof Szabados
 */
public final class BracketCompletionAutoEditStrategy implements IAutoEditStrategy {

	@Override
	public void customizeDocumentCommand(final IDocument document, final DocumentCommand command) {
		if (command.text.length() != 1) {
			return;
		}
		char trigChar = command.text.charAt(0);

		try {
			switch (trigChar) {
			case '[':
				if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CLOSE_SQUARE)) {
					insertSingleChar(command, ']');
				}
				break;
			case '\"':
				if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CLOSE_APOSTROPHE)) {
					handleQuotes(document, command, '"');
				}
				break;
			case '\'':
				if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CLOSE_APOSTROPHE)) {
					handleQuotes(document, command, '\'');
				}
				break;
			case '(':
				if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CLOSE_PARANTHESES)) {
					insertSingleChar(command, ')');
				}
				break;
			case ')':
				handleClosingBracket(document, ')', command);
				break;
			case ']':
				handleClosingBracket(document, ']', command);
				break;
			case '{':
				if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CLOSE_BRACES)) {
					insertSingleChar(command, '}');
				}
				break;
			case '}':
				handleClosingBracket(document, '}', command);
				break;
			default:
				// do nothing
				break;
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Inserts one character and steps over the character (make sense?).
	 * 
	 * @param docCommand
	 *                - the doc command to work upon
	 * @param newChar
	 *                - the character to insert
	 */
	private void insertSingleChar(final DocumentCommand docCommand, final char newChar) {
		docCommand.text += newChar;
		docCommand.caretOffset = docCommand.offset + 1;
		docCommand.shiftsCaret = false;
	}

	private void handleClosingBracket(final IDocument doc, final char bracket, final DocumentCommand docCommand) {
		try {
			if (docCommand.offset + docCommand.length < doc.getLength()) {
				char nextChar = doc.getChar(docCommand.offset + docCommand.length);

				if (nextChar == bracket) {
					stepThrough(docCommand, 1);
				}
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}

	}

	/**
	 * Handles the insertion of quotes by the user. If the user has opened
	 * quotes then it inserts a closing quote after the opened quote and
	 * does not move the caret. If the user is closing some quotes it steps
	 * through the existing quote.
	 * 
	 * @param doc
	 *                - The document that the command is being performed in
	 * @param docCommand
	 *                - the command to modify
	 * @param quoteChar
	 *                - the quote character that triggered this. This allows
	 *                us to handle " and ' quotes.
	 * @throws BadLocationException
	 *                 - ack.
	 */
	private void handleQuotes(final IDocument doc, final DocumentCommand docCommand, final char quoteChar) throws BadLocationException {
		// If the offset is below the file start forget it
		if (docCommand.offset < 0) {
			return;
		}

		// If they typing at the end of the file (or just starting the
		// file) add the new quote
		if (docCommand.offset + docCommand.length >= doc.getLength()) {
			insertSingleChar(docCommand, quoteChar);
			return;
		}

		// if a range is selected enclose it with the quotes
		if (docCommand.length > 0) {
			docCommand.text = quoteChar + doc.get(docCommand.offset, docCommand.length) + quoteChar;
			docCommand.caretOffset = docCommand.offset + docCommand.length + 2;
			docCommand.shiftsCaret = false;
			return;
		}

		char nextChar;
		char previousChar;
		try {
			previousChar = doc.getChar(docCommand.offset + docCommand.length - 1);
			nextChar = doc.getChar(docCommand.offset + docCommand.length);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}

		if (nextChar == quoteChar) {
			stepThrough(docCommand, 1);
			return;
		}

		if (Character.isLetterOrDigit(nextChar) || Character.isLetterOrDigit(previousChar)) {
			return;
		}

		insertSingleChar(docCommand, quoteChar);
		return;
	}

	/**
	 * Steps through a number of characters.
	 * 
	 * @param docCommand
	 *                - the doc command to work upon
	 * @param chars2StepThru
	 *                - number of characters to step through
	 */
	protected void stepThrough(final DocumentCommand docCommand, final int chars2StepThru) {
		docCommand.text = "";
		docCommand.shiftsCaret = false;
		docCommand.offset += chars2StepThru;
		docCommand.caretOffset = docCommand.offset;
	}
}
