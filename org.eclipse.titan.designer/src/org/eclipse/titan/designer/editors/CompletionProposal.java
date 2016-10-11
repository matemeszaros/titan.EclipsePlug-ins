/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * The default class for a single completion proposal.
 * <p>
 * For skeleton like proposals the templatecompletion should be used.
 * 
 * @author Kristof Szabados
 * */
public final class CompletionProposal implements ICompletionProposal, ICompletionProposalExtension3 {

	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private String fReplacementString;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;

	/**
	 * Creates a new completion proposal based on the provided information.
	 * The replacement string is considered being the display string too.
	 * All remaining fields are set to <code>null</code>.
	 * 
	 * @param replacementString
	 *                the actual string to be inserted into the document
	 * @param replacementOffset
	 *                the offset of the text to be replaced
	 * @param replacementLength
	 *                the length of the text to be replaced
	 * @param cursorPosition
	 *                the position of the cursor following the insert
	 *                relative to replacementOffset
	 */
	public CompletionProposal(final String replacementString, final int replacementOffset, final int replacementLength, final int cursorPosition) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based
	 * on the provided information.
	 * 
	 * @param replacementString
	 *                the actual string to be inserted into the document
	 * @param replacementOffset
	 *                the offset of the text to be replaced
	 * @param replacementLength
	 *                the length of the text to be replaced
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
	public CompletionProposal(final String replacementString, final int replacementOffset, final int replacementLength, final int cursorPosition,
			final Image image, final String displayString, final IContextInformation contextInformation,
			final String additionalProposalInfo) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fCursorPosition = cursorPosition;
		fImage = image;
		fDisplayString = displayString;
		fContextInformation = contextInformation;
		fAdditionalProposalInfo = additionalProposalInfo;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	@Override
	public void apply(final IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	@Override
	public Point getSelection(final IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	@Override
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		if (fDisplayString != null) {
			return fDisplayString;
		}

		return fReplacementString;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return null;
	}

	@Override
	public int getPrefixCompletionStart(final IDocument document, final int completionOffset) {
		return fReplacementOffset;
	}

	@Override
	public CharSequence getPrefixCompletionText(final IDocument document, final int completionOffset) {
		return fReplacementString;
	}
}
