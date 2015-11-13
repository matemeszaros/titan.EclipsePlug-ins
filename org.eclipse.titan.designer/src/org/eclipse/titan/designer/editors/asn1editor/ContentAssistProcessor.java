/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * @author Kristof Szabados
 * */
public final class ContentAssistProcessor implements IContentAssistProcessor {
	private static final String KEYWORD = "keyword";

	private final AbstractDecoratedTextEditor editor;

	public ContentAssistProcessor(final AbstractDecoratedTextEditor editor) {
		this.editor = editor;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		IDocument doc = viewer.getDocument();

		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);

		ASN1ReferenceParser refParser = new ASN1ReferenceParser();
		Reference ref = refParser.findReferenceForCompletion(file, offset, doc);

		IPreferencesService prefs = Platform.getPreferencesService();
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
			TITANDebugConsole.println("parsed the reference: " + ref);
		}

		if (ref == null || ref.getSubreferences().isEmpty()) {
			return new ICompletionProposal[] {};
		}

		Scope scope = null;
		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		String moduleName = projectSourceParser.containedModule(file);
		if (moduleName != null) {
			Module tempModule = projectSourceParser.getModuleByName(moduleName);
			if (tempModule != null) {
				scope = tempModule.getSmallestEnclosingScope(refParser.getReplacementOffset());
				ref.setMyScope(scope);
				ref.detectModid();
			}
		}

		TemplateContextType contextType = new TemplateContextType(ASN1CodeSkeletons.CONTEXT_IDENTIFIER, ASN1CodeSkeletons.CONTEXT_NAME);
		ProposalCollector propCollector = new ProposalCollector(Identifier_type.ID_ASN, ASN1CodeSkeletons.CONTEXT_IDENTIFIER, contextType,
				doc, ref, refParser.getReplacementOffset());

		if (scope != null) {
			scope.addProposal(propCollector);

			propCollector.sortTillMarked();
			propCollector.markPosition();
		}

		if (ref.getSubreferences().size() != 1) {
			return propCollector.getCompletitions();
		}

		if (scope == null) {
			propCollector.addProposal(CodeScanner.TAGS, null, KEYWORD);
		} else {
			ASN1CodeSkeletons.addSkeletonProposals(doc, refParser.getReplacementOffset(), propCollector);
		}

		propCollector.sortTillMarked();
		propCollector.markPosition();

		propCollector.addProposal(CodeScanner.VERBS, null, KEYWORD);
		propCollector.addProposal(CodeScanner.COMPARE_TYPES, null, KEYWORD);
		propCollector.addProposal(CodeScanner.STATUS_TYPE, null, KEYWORD);
		propCollector.addProposal(CodeScanner.KEYWORDS, null, KEYWORD);

		propCollector.addProposal(CodeScanner.STORAGE, null, KEYWORD);
		propCollector.addProposal(CodeScanner.MODIFIER, null, KEYWORD);
		propCollector.addProposal(CodeScanner.ACCESS_TYPE, null, KEYWORD);

		propCollector.sortTillMarked();

		String sortingpolicy = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.CONTENTASSISTANT_PROPOSAL_SORTING);
		if (PreferenceConstantValues.SORT_ALPHABETICALLY.equals(sortingpolicy)) {
			propCollector.sortAll();
		}

		return propCollector.getCompletitions();
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
