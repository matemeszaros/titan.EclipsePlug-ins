/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.designer.editors.GeneralTITANAutoEditStrategy;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * Automatic indentation strategy.
 * 
 * @author Kristof Szabados
 */
public final class SmartIndentAfterNewLineAutoEditStrategy extends GeneralTITANAutoEditStrategy {

	@Override
	public void customizeDocumentCommand(final IDocument document, final DocumentCommand command) {
		refreshAutoEditStrategy();
		initializeRootInterval(document);

		if (rootInterval == null) {
			return;
		}
		try {
			StringBuilder builder = new StringBuilder(document.get());
			if (isWithinMultiLineComment(command.offset) || isWithinString(builder, command.offset)) {
				return;
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		if (command.length == 0 && command.text != null && endsWithDelimiter(document, command.text)) {
			smartIndentAfterNewLine(document, command);
		}
	}

	/**
	 * Smart indentation after new line characters. This method handles two
	 * cases. First when the new line characted is preceded by a closing
	 * bracket. In this case the new line will be appended by the same
	 * whitespace charaters as in the line where the corresponding opening
	 * baracket is. In the second case when an other arbitrary character is
	 * followed by the new line the indentation of the current line is
	 * copied to the new line.
	 *
	 * @param document
	 *                - the document being parsed
	 * @param command
	 *                - the command being performed
	 */
	protected void smartIndentAfterNewLine(final IDocument document, final DocumentCommand command) {

		int docLength = document.getLength();
		if (command.offset == -1 || docLength == 0) {
			return;
		}

		try {
			int p = command.offset == docLength ? command.offset - 1 : command.offset;
			int line = document.getLineOfOffset(p);

			StringBuilder builder = new StringBuilder(command.text);
			String lineDelimeter = document.getLineDelimiter(line);
			if (lineDelimeter == null) {
				lineDelimeter = "";
			}
			// if the carret needs to be shifted we have to
			// calculate with the new line originally in the buffer
			int carretShiftSize = lineDelimeter.length();

			String lineIndent = getIndentOfLine(document, line, command);
			builder.append(lineIndent);
			carretShiftSize += lineIndent.length();

			int start = document.getLineOffset(line);
			int end = start + document.getLineLength(line) - command.text.length();

			// If there is an opening bracket in the line then the
			// new line might
			// will be tabulated.
			if (containsUnclosedInterval(start, command.offset)) {
				Interval endInterval = rootInterval.getSmallestEnclosingInterval(command.offset);
				// no indentation is done on the first level and
				// if we found an interval just beginning
				if (endInterval.getDepth() <= 0 || endInterval.getStartOffset() == command.offset) {
					command.text = builder.toString();
					return;
				}
				builder.append(indentString);
				carretShiftSize += indentString.length();
			}

			boolean willInsertClosingBracket = preferenceStore.getBoolean(PreferenceConstants.CLOSE_BRACES)
					&& canStatementBlockBeOpen(document, command.offset);

			// if we have to move the closing brackets further and
			// there is closing bracket in the actual line, or it is
			// missing and we will insert it
			// Insert the bracket moving new line and indentation
			if (preferenceStore.getBoolean(PreferenceConstants.AUTOMATICALLY_MOVE_BRACES)
					&& (willInsertClosingBracket || containsUnopenedInterval(command.offset, end))) {
				builder.append(lineDelimeter + lineIndent);
				command.caretOffset = command.offset + carretShiftSize;
				command.shiftsCaret = false;
			}

			// insert the closing bracket if needed
			if (willInsertClosingBracket) {
				builder.append('}');
			}

			command.text = builder.toString();
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
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
