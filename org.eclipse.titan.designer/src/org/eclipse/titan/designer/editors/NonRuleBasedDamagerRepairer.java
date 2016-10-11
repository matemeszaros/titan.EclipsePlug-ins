/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Kristof Szabados
 * */
public final class NonRuleBasedDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

	private IDocument fDocument;

	private TextAttribute fDefaultTextAttribute;

	public NonRuleBasedDamagerRepairer(final TextAttribute defaultTextAttribute) {
		fDefaultTextAttribute = defaultTextAttribute;
	}

	@Override
	public void setDocument(final IDocument document) {
		fDocument = document;
	}

	/**
	 * Helper function for
	 * {@link #getDamageRegion(ITypedRegion, DocumentEvent, boolean)}.
	 * 
	 * @param offset
	 *                The point on which we are in the document.
	 * @return The next line ending relative to the offset parameter.
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	protected int endOfLineOf(final int offset) throws BadLocationException {
		IRegion info = fDocument.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength()) {
			return info.getOffset() + info.getLength();
		}

		final int line = fDocument.getLineOfOffset(offset);
		try {
			info = fDocument.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException x) {
			return fDocument.getLength();
		}
	}

	@Override
	public IRegion getDamageRegion(final ITypedRegion partition, final DocumentEvent event, final boolean documentPartitioningChanged) {
		if (!documentPartitioningChanged) {
			try {
				final IRegion info = fDocument.getLineInformationOfOffset(event.getOffset());
				final int start = Math.max(partition.getOffset(), info.getOffset());
				int end = event.getOffset() + (event.getText() == null ? event.getLength() : event.getText().length());
				if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
					end = info.getOffset() + info.getLength();
				} else {
					end = endOfLineOf(end);
				}
				end = Math.min(partition.getOffset() + partition.getLength(), end);
				return new Region(start, end - start);
			} catch (BadLocationException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		return partition;
	}

	@Override
	public void createPresentation(final TextPresentation presentation, final ITypedRegion region) {
		addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextAttribute);
	}

	/**
	 * Helper function for
	 * {@link #createPresentation(TextPresentation, ITypedRegion)}.
	 * 
	 * @see #createPresentation(TextPresentation, ITypedRegion)
	 * @param presentation
	 *                The presentation to add a region to.
	 * @param offset
	 *                The start of the style range.
	 * @param length
	 *                The length of the style range
	 * @param attr
	 *                The attribute to set.
	 */
	protected void addRange(final TextPresentation presentation, final int offset, final int length, final TextAttribute attr) {
		if (attr != null) {
			presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr.getStyle()));
		}
	}
}
