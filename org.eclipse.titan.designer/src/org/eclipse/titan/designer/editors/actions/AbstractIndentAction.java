/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.designer.editors.GlobalIntervalHandler;
import org.eclipse.titan.designer.editors.HeuristicalIntervalDetector;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kristof Szabados
 * */
public abstract class AbstractIndentAction extends AbstractHandler implements IEditorActionDelegate {
	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

	private MultiTextEdit multiEdit;
	private List<String> indentArray = new ArrayList<String>();

	@Override
	public final void run(final IAction action) {
		doIndent();
	}

	@Override
	public final void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public final void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	protected abstract int lineIndentationLevel(IDocument document, int realStartOffset, int lineEndOffset, Interval startEnclosingInterval)
			throws BadLocationException;

	protected abstract IDocument getDocument();

	protected final IEditorPart getTargetEditor() {
		return targetEditor;
	}

	/**
	 * Indents a line with a given indentation level.
	 * 
	 * @param document
	 *                the document being processed.
	 * @param lineStart
	 *                the offset at which the target line starts
	 * @param indentationLevel
	 *                the indentation level to use
	 * 
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 * */
	private void setIndentLevel(final IDocument document, final int lineStart, final int indentationLevel) throws BadLocationException {
		int lastWhiteSpace = lineStart;
		char c;
		while (lastWhiteSpace < document.getLength()) {
			c = document.getChar(lastWhiteSpace);
			if (c == ' ' || c == '\t') {
				lastWhiteSpace++;
			} else {
				break;
			}
		}

		for (int i = indentArray.size() - 1; i < indentationLevel; i++) {
			indentArray.add(indentArray.get(i) + IndentationSupport.getIndentString());
		}

		if (!document.get(lineStart, lastWhiteSpace - lineStart).equals(indentArray.get(indentationLevel))) {
			multiEdit.addChild(new ReplaceEdit(lineStart, lastWhiteSpace - lineStart, indentArray.get(indentationLevel)));
		}
	}

	/**
	 * Returns whether the provided text contains non-whitespace characters
	 * or not.
	 * 
	 * @param text
	 *                the text to check
	 * @return true, if the text contains non-whitespace characters.
	 * */
	protected final boolean containsNonWhiteSpace(final String text) {
		for (int i = text.length() - 1; i >= 0; i--) {
			switch (text.charAt(i)) {
			case '\t':
			case '\n':
			case ' ':
				break;
			default:
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the offset of the first character on the line, or the
	 * beginning of the line if none found.
	 * 
	 * @param document
	 *                the document being processed.
	 * @param startOffset
	 *                the offset of the beginning of the actual line.
	 * @param lineLength
	 *                the length of the actual line.
	 * @return the offset of the first character on the line, or the start
	 *         offset parameter if none found.
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 * */
	private int getRealLineStart(final IDocument document, final int startOffset, final int lineLength) throws BadLocationException {
		char c;
		for (int j = startOffset; j < startOffset + lineLength; j++) {
			c = document.getChar(j);
			if (c != ' ' && c != '\t') {
				return j;
			}
		}

		return startOffset;
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		doIndent();

		return null;
	}

	/**
	 * Do the actual indentation work.
	 * */
	private void doIndent() {
		if (targetEditor == null) {
			return;
		}

		final IDocument document = getDocument();

		if (null == document) {
			return;
		}

		Interval rootInterval = GlobalIntervalHandler.getInterval(document);
		if (rootInterval == null) {
			rootInterval = (new HeuristicalIntervalDetector()).buildIntervals(document);
			GlobalIntervalHandler.putInterval(document, rootInterval);
		}
		if (rootInterval == null) {
			return;
		}

		int startLine = -1;
		int endLine = -1;
		if (!selection.isEmpty()) {
			if (selection instanceof TextSelection) {
				final TextSelection tSelection = (TextSelection) selection;
				if (tSelection.getLength() != 0) {
					startLine = tSelection.getStartLine();
					endLine = tSelection.getEndLine();
				}
			}
		}
		if (startLine == -1 || endLine == -1) {
			startLine = 0;
			endLine = document.getNumberOfLines() - 1;
		}

		indentArray.clear();
		indentArray.add("");

		final int nofLines = endLine - startLine;
		int offset;
		int realOffset;
		int lineLength;
		Interval interval;
		try {
			final int regionStartOffset = document.getLineOffset(startLine);
			final int regionLength = document.getLineOffset(endLine) + document.getLineLength(endLine) - regionStartOffset;
			multiEdit = new MultiTextEdit(regionStartOffset, regionLength);
			final RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(document, multiEdit, TextEdit.UPDATE_REGIONS
					| TextEdit.CREATE_UNDO);
			for (int i = nofLines; i >= 0; i--) {
				lineLength = document.getLineLength(startLine + i);
				offset = document.getLineOffset(startLine + i);
				realOffset = getRealLineStart(document, offset, lineLength);
				interval = rootInterval.getSmallestEnclosingInterval(realOffset);
				final int indentLevel = lineIndentationLevel(document, realOffset, offset + lineLength, interval);
				setIndentLevel(document, document.getLineOffset(startLine + i), indentLevel);
			}

			performEdits(processor);

		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return;
	}

	/**
	 * Perform the calculated text modification in the actual editor.
	 * 
	 * @param processor
	 *                the processor responsible to do the changes.
	 * 
	 * @throws BadLocationException
	 *                 Is thrown if one of the edits in the tree can't be
	 *                 executed. The state of the document is undefined if
	 *                 this exception is thrown.
	 * */
	protected abstract void performEdits(final RewriteSessionEditProcessor processor) throws BadLocationException;
}
