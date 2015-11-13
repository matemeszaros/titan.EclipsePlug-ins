/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcnppeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Keywords;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * @author Kristof Szabados
 * */
public final class ContentAssistProcessor implements IContentAssistProcessor {
	private static final String REFERENCE_SPLITTER = "\\.";

	private final AbstractDecoratedTextEditor editor;

	public ContentAssistProcessor(final AbstractDecoratedTextEditor editor) {
		this.editor = editor;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		IDocument doc = viewer.getDocument();

		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);

		int ofs = findWordStart(offset, doc);
		String incompleteString = "";

		try {
			if (doc != null && offset >= ofs) {
				incompleteString = doc.get(ofs, offset - ofs);
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		String[] reference = incompleteString.trim().split(REFERENCE_SPLITTER, -1);
		Reference ref = new Reference(null);
		ref.setLocation(new Location(file, 0, 0, offset - ofs));
		FieldSubReference subref = new FieldSubReference(new Identifier(Identifier_type.ID_TTCN, reference[0]));
		subref.setLocation(new Location(file, 0, 0, reference[0].length()));
		ref.addSubReference(subref);
		if (reference.length > 1) {
			subref = new FieldSubReference(new Identifier(Identifier_type.ID_TTCN, reference[1]));
			subref.setLocation(new Location(file, 0, reference[0].length() + 1, offset - ofs));
			ref.addSubReference(subref);
		}
		TemplateContextType contextType = new TemplateContextType(TTCN3CodeSkeletons.CONTEXT_IDENTIFIER, TTCN3CodeSkeletons.CONTEXT_NAME);
		ProposalCollector propCollector = new ProposalCollector(Identifier_type.ID_TTCN, TTCN3CodeSkeletons.CONTEXT_IDENTIFIER, contextType,
				doc, ref, ofs);

		TTCN3CodeSkeletons.addSkeletonProposals(doc, offset, propCollector);
		TTCN3Keywords.addKeywordProposals(propCollector);

		String sortingpolicy = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.CONTENTASSISTANT_PROPOSAL_SORTING);
		if (PreferenceConstantValues.SORT_ALPHABETICALLY.equals(sortingpolicy)) {
			propCollector.sortAll();
		}

		return propCollector.getCompletitions();
	}

	/**
	 * Helper to decide if the character can separate words in a reference.
	 * 
	 * @param c
	 *                the character to check.
	 * */
	private boolean isSeparatingChar(final char c) {
		switch (c) {
		case '.':
		case ')':
		case ']':
		case '_':
		case '}':
			return true;
		default:
			return false;
		}
	}

	/**
	 * Finds the start of the reference like thing that should be completed.
	 * 
	 * @param offset
	 *                the offset from where it has to search backwards.
	 * @param document
	 *                the document in which the search must take place
	 * 
	 * @return the starting offset of the found reference like thing.
	 * */
	private int findWordStart(final int offset, final IDocument document) {
		int ofs = offset - 1;
		if (-1 == ofs) {
			return 0;
		}
		try {
			char currentChar = document.getChar(ofs);
			while (ofs > 0 && (Character.isLetterOrDigit(currentChar) || isSeparatingChar(currentChar))) {
				ofs--;
				currentChar = document.getChar(ofs);
			}
			if (ofs != 0) {
				ofs++;
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return ofs;
	}

	@Override
	public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {
		return new ContextInformation[] {};
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] {};
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

}
