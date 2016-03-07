/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * A proposal collector is class that is collecting the proposals for a
 * reference's code completion
 * 
 * @author Kristof Szabados
 * */
public final class ProposalCollector {

	public static final class ProposalComperator implements java.util.Comparator<ICompletionProposal> {

		@Override
		public int compare(final ICompletionProposal o1, final ICompletionProposal o2) {
			String s1 = o1.getDisplayString();
			String s2 = o2.getDisplayString();
			return s1.compareToIgnoreCase(s2);
		}

	}

	/**
	 * A project parser, used by the collector to reach other modules from
	 * the given project. Can be null !!!
	 */
	private ProjectSourceParser projectSourceParser;

	/** the identifier of the context where the proposals will be inserted. */
	private String contextId;

	/** The type identifiers should use to display their content. */
	private Identifier_type targetIdentifierType;

	private TemplateContext templatecontext;

	/** the reference we will try to complete. */
	private final Reference reference;
	/**
	 * The display name of the last part of the reference converted to lower
	 * case Used only for performance reasons.
	 * */
	private final String lastPrefix;

	/**
	 * offset after the last separator character within the expression
	 * (dot).
	 */
	private int replacementOffset;
	private List<ICompletionProposal> proposalList;

	/** offset of the last marked position in the list of proposals. */
	private int lastMarkedPosition = 0;

	/**
	 * Simple constructor initializing the reference and the offset of the
	 * reference.
	 * 
	 * @param targetIdentifierType
	 *                the type identifiers should use to display their
	 *                content
	 * @param doc
	 *                the document to be used
	 * @param ref
	 *                the reference to be completed
	 * @param offset
	 *                the offset on which the reference starts
	 * */
	public ProposalCollector(final Identifier_type targetIdentifierType, final IDocument doc, final Reference ref, final int offset) {
		this(targetIdentifierType, null, null, null, doc, ref, offset);
	}

	/**
	 * Simple constructor initializing the reference and the offset of the
	 * reference.
	 * 
	 * @param targetIdentifierType
	 *                the type identifiers should use to display their
	 *                content
	 * @param contextId
	 *                the identifier of the context where the proposals will
	 *                be inserted
	 * @param contextType
	 *                the templatecontexttype to be used to insert the
	 *                templateproposals into
	 * @param doc
	 *                the document to be used
	 * @param ref
	 *                the reference to be completed
	 * @param offset
	 *                the offset on which the reference starts
	 * */
	public ProposalCollector(final Identifier_type targetIdentifierType, final String contextId, final TemplateContextType contextType,
			final IDocument doc, final Reference ref, final int offset) {
		this(targetIdentifierType, null, contextId, contextType, doc, ref, offset);
	}

	/**
	 * Simple constructor initializing the reference and the offset of the
	 * reference.
	 * 
	 * @param targetIdentifierType
	 *                the type identifiers should use to display their
	 *                content
	 * @param projectSourceParser
	 *                the projects parser or null. Used to collect
	 *                definitions from imported modules too.
	 * @param contextId
	 *                the identifier of the context where the proposals will
	 *                be inserted
	 * @param contextType
	 *                the templatecontexttype to be used to insert the
	 *                templateproposals into
	 * @param doc
	 *                the document to be used
	 * @param ref
	 *                the reference to be completed
	 * @param offset
	 *                the offset on which the reference starts
	 * */
	public ProposalCollector(final Identifier_type targetIdentifierType, final ProjectSourceParser projectSourceParser, final String contextId,
			final TemplateContextType contextType, final IDocument doc, final Reference ref, final int offset) {
		this.targetIdentifierType = targetIdentifierType;
		this.projectSourceParser = projectSourceParser;
		this.contextId = contextId;
		reference = ref;

		List<ISubReference> subreferences = ref.getSubreferences();
		if (!subreferences.isEmpty()) {
			replacementOffset = offset + ref.getLocation().getEndOffset();
			Location location = subreferences.get(subreferences.size() - 1).getLocation();
			replacementOffset -= location.getEndOffset() - location.getOffset();

			lastPrefix = subreferences.get(subreferences.size() - 1).getId().getDisplayName().toLowerCase();
		} else {
			replacementOffset = offset;

			lastPrefix = reference.getModuleIdentifier().getDisplayName().toLowerCase();
		}

		templatecontext = new TITANTemplateContext(contextType, doc, replacementOffset, 0);
		proposalList = new ArrayList<ICompletionProposal>();
	}

	/**
	 * Overwrites the currently set ProjectParser for this object. Should
	 * only be used when the ProjectParser could not be decided at the time
	 * when this object was created.
	 * 
	 * @param projectSourceParser
	 *                the ProjectParser to be used to reach imported modules
	 * */
	public void setProjectParser(final ProjectSourceParser projectSourceParser) {
		this.projectSourceParser = projectSourceParser;
	}

	/**
	 * Returns the ProjectParser associated with this proposal collector.
	 * 
	 * @return the ProjectParser of the actual project
	 * */
	public ProjectSourceParser getProjectParser() {
		return projectSourceParser;
	}

	/**
	 * Returns the context identifier from where the proposals will be
	 * inserted.
	 * 
	 * @return The context identifier from where the proposals will be
	 *         inserted
	 * */
	public String getContextIdentifier() {
		return contextId;
	}

	/**
	 * Return the collected completion proposals sorted by the string they
	 * display to the user.
	 * 
	 * @return the collected completion proposals
	 * */
	public ICompletionProposal[] getCompletitions() {
		return proposalList.toArray(new ICompletionProposal[proposalList.size()]);
	}

	/**
	 * Returns the offset from where the replacement should start.
	 * <p>
	 * This is not the offset of the reference, for example if the reference
	 * has 2 elements, than the replacement offset is the reference's offset
	 * + the reference's first part's length +1
	 * 
	 * @return the replacement offset
	 * */
	public int getReplacementOffset() {
		return replacementOffset;
	}

	/**
	 * Returns the reference the proposals are checked against.
	 * 
	 * @return the Reference to be extended
	 * */
	public Reference getReference() {
		return reference;
	}

	/**
	 * Marks the actual size of the list of proposals as the start of an
	 * interval, which will need to be sorted.
	 * */
	public void markPosition() {
		lastMarkedPosition = proposalList.size();
	}

	/**
	 * Sorts the last part of the list of proposals according to the
	 * displayed string of the proposals. The sorting takes place only
	 * between the last marked position and the end of the proposal list.
	 * */
	public void sortTillMarked() {
		if (lastMarkedPosition < proposalList.size() - 1) {
			List<ICompletionProposal> orderable = proposalList.subList(lastMarkedPosition, proposalList.size() - 1);
			Collections.sort(orderable, new ProposalComperator());
		}
	}

	/**
	 * Sorts the whole list of proposal according to the displayed string of
	 * the proposals.
	 * */
	public void sortAll() {
		Collections.sort(proposalList, new ProposalComperator());
	}

	/**
	 * Adds a proposal to the list of completion proposals.
	 * 
	 * @param proposal
	 *                the completion proposal to be added
	 * */
	public void addProposal(final ICompletionProposal proposal) {
		proposalList.add(proposal);
	}

	/**
	 * Calculates the length on which the reference is the prefix of the
	 * parameter string, if it is a prefix..
	 * <p>
	 * Used to decide if an element might be the extended version of the
	 * reference known.
	 * 
	 * @param string
	 *                the string to check.
	 * @return the length of the parameter string, or -1 if the reference is
	 *         not the prefix of the parameter string.
	 * */
	private int prefixLength(final String string) {
		if (string == null) {
			return -1;
		}

		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.isEmpty()) {
			if (reference.getModuleIdentifier() != null && string.toLowerCase().startsWith(lastPrefix)) {
				return lastPrefix.length();
			}

			return 0;
		}

		if (string.toLowerCase().startsWith(lastPrefix)) {
			return lastPrefix.length();
		}

		return -1;
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based
	 * on the provided information.
	 * 
	 * @param candidate
	 *                the candidate for completion
	 * @param visibleString
	 *                the string to be displayed
	 * @param image
	 *                the image associated with the proposal
	 * */
	public void addProposal(final String candidate, final String visibleString, final Image image) {
		addProposal(candidate, visibleString, image, "");
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based
	 * on the provided information.
	 * 
	 * @param candidate
	 *                the candidate for completion
	 * @param visibleString
	 *                the string to be displayed
	 * @param image
	 *                the image associated with the proposal
	 * @param info
	 *                information about the completion candidate
	 * */
	public void addProposal(final String candidate, final String visibleString, final Image image, final String info) {
		int prefixLength = prefixLength(candidate);
		if (prefixLength != -1) {
			CompletionProposal proposal = new CompletionProposal(candidate, replacementOffset, prefixLength, candidate.length(), image,
					visibleString, null, info);
			proposalList.add(proposal);
		}
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based
	 * on the provided information.
	 * 
	 * @param identifier
	 *                the identifier that will be the candidate for
	 *                completion
	 * @param image
	 *                the image associated with the proposal
	 * @param info
	 *                information about the completion candidate
	 * */
	public void addProposal(final Identifier identifier, final Image image, final String info) {
		addProposal(identifier, "", image, info);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based
	 * on the provided information.
	 * 
	 * @param identifier
	 *                the identifier that will be the candidate for
	 *                completion
	 * @param postfix
	 *                the string to be displayed
	 * @param image
	 *                the image associated with the proposal
	 * @param info
	 *                information about the completion candidate
	 * */
	public void addProposal(final Identifier identifier, final String postfix, final Image image, final String info) {
		if (identifier == null) {
			return;
		}

		String candidate;
		switch (targetIdentifierType) {
		case ID_ASN:
			candidate = identifier.getAsnName();
			break;
		case ID_NAME:
			candidate = identifier.getName();
			break;
		default:
			candidate = identifier.getTtcnName();
			break;
		}
		int prefixLength = prefixLength(candidate);
		if (prefixLength != -1) {
			CompletionProposal proposal = new CompletionProposal(candidate, replacementOffset, prefixLength, candidate.length(), image,
					candidate + postfix, null, info);
			proposalList.add(proposal);
		}
	}

	/**
	 * Creates completion proposals. All fields are initialized based on the
	 * provided information.
	 * 
	 * @param candidates
	 *                the array of candidates to be added
	 * @param image
	 *                the image associated with the proposal
	 * @param info
	 *                information about the candidates
	 * */
	public void addProposal(final String[] candidates, final Image image, final String info) {
		int prefixLength;
		for (String element : candidates) {
			prefixLength = prefixLength(element);
			if (prefixLength != -1) {
				CompletionProposal proposal = new CompletionProposal(element, replacementOffset, prefixLength, element.length(),
						image, element, null, info);
				proposalList.add(proposal);
			}
		}
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based
	 * on the provided information.
	 * 
	 * @param prefixString
	 *                the prefix of the completion candidate, used to decide
	 *                if it is a valid candidate or not.
	 * @param replacementString
	 *                the actual string to be inserted into the document
	 * @param cursorPosition
	 *                the position of the cursor following the insert
	 *                relative to replacementOffset
	 * @param image
	 *                the image to display for this proposal
	 * @param displayString
	 *                the string to be displayed for the proposal
	 * @param contextInformation
	 *                the context information associated with this proposal
	 * @param additionalProposalInfo
	 *                the additional information associated with this
	 *                proposal
	 */
	public void addProposal(final String prefixString, final String replacementString, final int cursorPosition, final Image image,
			final String displayString, final IContextInformation contextInformation, final String additionalProposalInfo) {
		int prefixLength = prefixLength(prefixString);
		if (prefixLength != -1) {
			CompletionProposal proposal = new CompletionProposal(replacementString, replacementOffset, prefixLength, cursorPosition,
					image, displayString, contextInformation, additionalProposalInfo);
			proposalList.add(proposal);
		}
	}

	/**
	 * Adds a new TemplateProposal to the ones already know.
	 * 
	 * @param prefixString
	 *                the prefix of TemplateProposal, using which we can
	 *                decide if the proposal is valid in the given context.
	 * @param candidate
	 *                the proposal candidate to be added
	 * @param image
	 *                the image to use for this proposal in the completion
	 *                popup
	 * */
	public void addTemplateProposal(final String prefixString, final Template candidate, final Image image) {
		int prefixLength = prefixLength(prefixString);
		if (prefixLength != -1) {
			Region region = new Region(replacementOffset, 0);
			proposalList.add(new TemplateProposal(candidate, templatecontext, region, image));
		}
	}
}
