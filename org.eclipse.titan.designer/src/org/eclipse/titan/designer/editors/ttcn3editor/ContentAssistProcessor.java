/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.Set;

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
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Timer;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
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
	private final AbstractDecoratedTextEditor editor;
	private final String sortingpolicy;

	public ContentAssistProcessor(final AbstractDecoratedTextEditor editor) {
		this.editor = editor;
		sortingpolicy = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.CONTENTASSISTANT_PROPOSAL_SORTING);
	}

	// FIXME add semantic check guard on project level.
	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		if (editor == null) {
			return new ICompletionProposal[] {};
		}

		IDocument doc = viewer.getDocument();

		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			return new ICompletionProposal[] {};
		}

		TTCN3ReferenceParser refParser = new TTCN3ReferenceParser(true);
		Reference ref = refParser.findReferenceForCompletion(file, offset, doc);

		Scope scope = null;

		if (ref == null || ref.getSubreferences().isEmpty()) {
			return new ICompletionProposal[] {};
		}

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

		IPreferencesService prefs = Platform.getPreferencesService();
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
			TITANDebugConsole.println("parsed the reference: " + ref);
		}

		TemplateContextType contextType = new TemplateContextType(TTCN3CodeSkeletons.CONTEXT_IDENTIFIER, TTCN3CodeSkeletons.CONTEXT_NAME);
		ProposalCollector propCollector = new ProposalCollector(Identifier_type.ID_TTCN, TTCN3CodeSkeletons.CONTEXT_IDENTIFIER, contextType,
				doc, ref, refParser.getReplacementOffset());

		propCollector.setProjectParser(projectSourceParser);
		if (moduleName == null) {
			// rootless behavior
			if (ref.getModuleIdentifier() == null) {
				Set<String> moduleNames = projectSourceParser.getKnownModuleNames();
				Module module;
				for (String name : moduleNames) {
					module = projectSourceParser.getModuleByName(name);
					if (module != null) {
						propCollector.addProposal(name, name, ImageCache.getImage("ttcn.gif"), TTCN3Module.MODULE);
						module.getAssignments().addProposal(propCollector);
					}
				}
			} else {
				Module module = projectSourceParser.getModuleByName(ref.getModuleIdentifier().getName());
				if (module != null) {
					module.getAssignments().addProposal(propCollector);
				}
			}
		} else {
			/*
			 * search for the best scope in the module's scope
			 * hierarchy and call proposal adding function on the
			 * found scope instead of what can be found here
			 */
			if (scope != null) {
				scope.addProposal(propCollector);
			}
		}

		propCollector.sortTillMarked();
		propCollector.markPosition();

		if (ref.getSubreferences().size() != 1) {
			if (PreferenceConstantValues.SORT_ALPHABETICALLY.equals(sortingpolicy)) {
				propCollector.sortAll();
			}
			return propCollector.getCompletitions();
		}

		Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		for (String knownModuleName : knownModuleNames) {
			Identifier tempIdentifier = new Identifier(Identifier_type.ID_NAME, knownModuleName);
			Module tempModule = projectSourceParser.getModuleByName(knownModuleName);
			propCollector.addProposal(tempIdentifier, ImageCache.getImage(tempModule.getOutlineIcon()), "module");
		}
		propCollector.sortTillMarked();
		propCollector.markPosition();

		if (ref.getModuleIdentifier() == null) {
			if (scope == null) {
				TTCN3CodeSkeletons.addSkeletonProposals(doc, refParser.getReplacementOffset(), propCollector);
			} else {
				scope.addSkeletonProposal(propCollector);
			}

			propCollector.addTemplateProposal("refers",
					new Template("refers( function/altstep/testcase name )", "", propCollector.getContextIdentifier(),
							"refers( ${fatName} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("derefers", new Template("derefers( function/altstep/testcase name )(parameters)", "",
					propCollector.getContextIdentifier(), "derefers( ${fatName} ) ( ${parameters} );", false),
					TTCN3CodeSkeletons.SKELETON_IMAGE);

			propCollector.sortTillMarked();
			propCollector.markPosition();

			TTCN3CodeSkeletons.addPredefinedSkeletonProposals(doc, refParser.getReplacementOffset(), propCollector);

			if (scope == null) {
				TTCN3Keywords.addKeywordProposals(propCollector);
			} else {
				scope.addKeywordProposal(propCollector);
			}

			propCollector.sortTillMarked();
			propCollector.markPosition();
		} else {
			String fakeModule = ref.getModuleIdentifier().getName();
			if (scope == null || !(scope instanceof StatementBlock)) {
				if (PreferenceConstantValues.SORT_ALPHABETICALLY.equals(sortingpolicy)) {
					propCollector.sortAll();
				}
				return propCollector.getCompletitions();
			}

			if ("any component".equals(fakeModule) || "all component".equals(fakeModule)) {
				Component_Type.addAnyorAllProposal(propCollector, 0);
			} else if ("any port".equals(fakeModule) || "all port".equals(fakeModule)) {
				PortTypeBody.addAnyorAllProposal(propCollector, 0);
			} else if ("any timer".equals(fakeModule) || "all timer".equals(fakeModule)) {
				Timer.addAnyorAllProposal(propCollector, 0);
			}
		}

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
		return new char[] { '.' };
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
