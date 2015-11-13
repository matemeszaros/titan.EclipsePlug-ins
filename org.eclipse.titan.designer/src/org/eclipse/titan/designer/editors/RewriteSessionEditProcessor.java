/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.UndoEdit;

/**
 * 
 * FIXME this class was taken from JFace as the one there only appeared in
 * Eclipse 3.3, please remove this class once support for earlier versions is
 * dropped on our side.
 * */
public final class RewriteSessionEditProcessor extends TextEditProcessor {

	/**
	 * Constructs a new edit processor for the given document.
	 * 
	 * @param document
	 *                the document to manipulate
	 * @param root
	 *                the root of the text edit tree describing the
	 *                modifications. By passing a text edit a a text edit
	 *                processor the ownership of the edit is transfered to
	 *                the text edit processors. Clients must not modify the
	 *                edit (e.g adding new children) any longer.
	 * @param style
	 *                {@link TextEdit#NONE}, {@link TextEdit#CREATE_UNDO} or
	 *                {@link TextEdit#UPDATE_REGIONS})
	 */
	public RewriteSessionEditProcessor(final IDocument document, final TextEdit root, final int style) {
		super(document, root, style);
	}

	@Override
	public UndoEdit performEdits() throws BadLocationException {
		IDocument document = getDocument();
		if (!(document instanceof IDocumentExtension4)) {
			return super.performEdits();
		}

		IDocumentExtension4 extension = (IDocumentExtension4) document;
		DocumentRewriteSession session = extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
		try {
			return super.performEdits();
		} finally {
			extension.stopRewriteSession(session);
		}
	}
}
