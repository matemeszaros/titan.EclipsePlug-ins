/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * @author Szabolcs Beres
 * */
public class ToggleComment extends TextEditorAction {

	protected ITextOperationTarget operationTarget;
	private String documentPartitioning;
	private Map<String, String[]> prefixesMap;

	/**
	 * @see org.eclipse.ui.texteditor.TextEditorAction#TextEditorAction(ResourceBundle,
	 *      String, ITextEditor)
	 */
	public ToggleComment(final ResourceBundle bundle, final String prefix, final ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	public void run() {
		if (operationTarget == null || documentPartitioning == null || prefixesMap == null) {
			return;
		}

		final ITextEditor editor = getTextEditor();
		if (editor == null || !validateEditorInputState()) {
			return;
		}

		final int operationCode;
		if (isSelectionCommented(editor.getSelectionProvider().getSelection())) {
			operationCode = ITextOperationTarget.STRIP_PREFIX;
		} else {
			operationCode = ITextOperationTarget.PREFIX;
		}

		final Shell shell = editor.getSite().getShell();
		if (!operationTarget.canDoOperation(operationCode)) {
			if (shell != null) {
				MessageDialog.openError(shell, "Action can not be performed",
						"The action \"Toggle Comment\" can not be performed right now.");
			}
			return;
		}

		Display display = null;
		if (shell != null && !shell.isDisposed()) {
			display = shell.getDisplay();
		}

		
		
		BusyIndicator.showWhile(display, new Runnable() {
			@Override
			public void run() {
				doOperation(operationCode);
			}
		});
	}

	protected void doOperation(final int operationCode) {
		operationTarget.doOperation(operationCode);
	}

	/**
	 * Is the given selection single-line commented?
	 * 
	 * @param selection
	 *                Selection to check
	 * @return <code>true</code> if all selected lines are commented
	 */
	private boolean isSelectionCommented(final ISelection selection) {
		if (!(selection instanceof ITextSelection)) {
			return false;
		}

		final ITextSelection textSelection = (ITextSelection) selection;
		if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0) {
			return false;
		}

		final IDocument document = getTextEditor().getDocumentProvider().getDocument(getTextEditor().getEditorInput());

		try {
			final IRegion block = getTextBlockFromSelection(textSelection, document);
			final ITypedRegion[] regions = TextUtilities.computePartitioning(document, documentPartitioning, block.getOffset(),
					block.getLength(), false);

			// [startline, endline, startline, endline, ...]
			int[] lines = new int[regions.length * 2];
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				// start line of region
				lines[j] = getFirstCompleteLineOfRegion(regions[i], document);
				// end line of region
				final int length = regions[i].getLength();
				int offset = regions[i].getOffset() + length;
				if (length > 0) {
					offset--;
				}
				lines[j + 1] = (lines[j] == -1 ? -1 : document.getLineOfOffset(offset));
			}

			// Perform the check
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				final String[] prefixes = prefixesMap.get(regions[i].getType());
				if (prefixes != null && prefixes.length > 0 && lines[j] >= 0 && lines[j + 1] >= 0) {
					if (!isBlockCommented(lines[j], lines[j + 1], prefixes, document)) {
						return false;
					}
				}
			}

			return true;

		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return false;
	}

	/**
	 * Creates a region describing the text block (something that starts at
	 * the beginning of a line) completely containing the current selection.
	 * 
	 * @param selection
	 *                The selection to use
	 * @param document
	 *                The document
	 * @return the region describing the text block comprising the given
	 *         selection
	 */
	private IRegion getTextBlockFromSelection(final ITextSelection selection, final IDocument document) {

		try {
			final IRegion line = document.getLineInformationOfOffset(selection.getOffset());
			final int length = selection.getLength() == 0 ? line.getLength() : selection.getLength()
					+ (selection.getOffset() - line.getOffset());
			return new Region(line.getOffset(), length);

		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return null;
	}

	/**
	 * Returns the index of the first line whose start offset is in the
	 * given text range.
	 * 
	 * @param region
	 *                the text range in characters where to find the line
	 * @param document
	 *                The document
	 * @return the first line whose start index is in the given range, -1 if
	 *         there is no such line
	 */
	private int getFirstCompleteLineOfRegion(final IRegion region, final IDocument document) {
		try {
			final int startLine = document.getLineOfOffset(region.getOffset());

			int offset = document.getLineOffset(startLine);
			if (offset >= region.getOffset()) {
				return startLine;
			}

			final int nextLine = startLine + 1;
			if (nextLine == document.getNumberOfLines()) {
				return -1;
			}

			offset = document.getLineOffset(nextLine);
			return (offset > region.getOffset() + region.getLength() ? -1 : nextLine);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return -1;
	}

	/**
	 * Determines whether each line is prefixed by one of the prefixes.
	 * 
	 * @param startLine
	 *                Start line in document
	 * @param endLine
	 *                End line in document
	 * @param prefixes
	 *                Possible comment prefixes
	 * @param document
	 *                The document
	 * @return <code>true</code> if each line from <code>startLine</code> to
	 *         and including <code>endLine</code> is prepended by one of the
	 *         <code>prefixes</code>, ignoring whitespace at the begin of
	 *         line
	 */
	private boolean isBlockCommented(final int startLine, final int endLine, final String[] prefixes, final IDocument document) {
		try {
			// check for occurrences of prefixes in the given lines
			for (int i = startLine; i <= endLine; i++) {
				final IRegion line = document.getLineInformation(i);
				final String text = document.get(line.getOffset(), line.getLength());
				final int[] found = TextUtilities.indexOf(prefixes, text, 0);

				if (found[0] == -1) {
					// found a line which is not commented
					return false;
				}

				String s = document.get(line.getOffset(), found[0]);
				s = s.trim();
				if (s.length() != 0) {
					// found a line which is not commented
					return false;
				}
			}
			return true;
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return false;
	}

	/**
	 * Implementation of the <code>IUpdate</code> prototype method discovers
	 * the operation through the current editor's
	 * <code>ITextOperationTarget</code> adapter, and sets the enabled state
	 * accordingly.
	 */
	@Override
	public void update() {
		super.update();

		if (!canModifyEditor()) {
			setEnabled(false);
			return;
		}

		final ITextEditor editor = getTextEditor();
		if (operationTarget == null && editor != null) {
			operationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
		}

		final boolean isEnabled = (operationTarget != null && operationTarget.canDoOperation(ITextOperationTarget.PREFIX) && operationTarget
				.canDoOperation(ITextOperationTarget.STRIP_PREFIX));
		setEnabled(isEnabled);
	}

	@Override
	public void setEditor(final ITextEditor editor) {
		super.setEditor(editor);
		operationTarget = null;
	}

	public void configure(final ISourceViewer sourceViewer, final SourceViewerConfiguration configuration) {
		final String[] types = configuration.getConfiguredContentTypes(sourceViewer);
		prefixesMap = new HashMap<String, String[]>(types.length);
		for (int i = 0; i < types.length; i++) {
			final String type = types[i];
			final String[] defaultPrefixes = configuration.getDefaultPrefixes(sourceViewer, type);
			if (defaultPrefixes != null && defaultPrefixes.length > 0) {
				prefixesMap.put(type, defaultPrefixes);
			}
		}
		documentPartitioning = configuration.getConfiguredDocumentPartitioning(sourceViewer);
	}
}
