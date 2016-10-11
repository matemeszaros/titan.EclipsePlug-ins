/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.actions.FirstCharAction;

/**
 * The TITANTemplateContext class represents a context for templates, where they
 * can be transformed according to the rules of TITAN.
 * 
 * @author Kristof Szabados
 * */
public final class TITANTemplateContext extends DocumentTemplateContext {

	public TITANTemplateContext(final TemplateContextType type, final IDocument document, final int offset, final int length) {
		super(type, document, offset, length);
	}

	@Override
	public TemplateBuffer evaluate(final Template template) throws BadLocationException, TemplateException {
		if (!canEvaluate(template)) {
			return null;
		}

		TemplateTranslator translator = new TemplateTranslator();
		TemplateBuffer buffer = translator.translate(template);

		getContextType().resolve(buffer, this);

		if (isReadOnly()) {
			// if it is read only we should not modify it
			return buffer;
		}

		// calculate base indentation prefix
		IDocument document = getDocument();
		String prefixString = "";
		String delimeter = null;
		try {
			IRegion lineRegion = document.getLineInformationOfOffset(getCompletionOffset());
			int firstCharLocation = FirstCharAction.firstVisibleCharLocation(document, lineRegion);
			if (firstCharLocation != -1) {
				prefixString = document.get(lineRegion.getOffset(), firstCharLocation - lineRegion.getOffset());
			}
			delimeter = document.getLineDelimiter(document.getLineOfOffset(getCompletionOffset()));
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		TemplateVariable[] variables = buffer.getVariables();

		// apply the base indentation prefix to every line but the first
		IDocument temporalDocument = new Document(buffer.getString());
		MultiTextEdit edit = new MultiTextEdit(0, temporalDocument.getLength());
		List<RangeMarker> positions = variablesToPositions(variables);
		for (int i = temporalDocument.getNumberOfLines() - 1; i > 0; i--) {
			edit.addChild(new InsertEdit(temporalDocument.getLineOffset(i), prefixString));
		}
		edit.addChildren(positions.toArray(new TextEdit[positions.size()]));

		// replace line delimeters with the ones at the insertion
		String delimeterZero = temporalDocument.getLineDelimiter(0);
		if(delimeter != null && delimeterZero != null && !delimeter.equals(delimeterZero)) {
			FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(temporalDocument);
			int startOffset = 0;
			IRegion region = adapter.find(startOffset, delimeterZero, true, false, false, false);
			while (region != null) {
				edit.addChild(new ReplaceEdit(region.getOffset(), region.getLength(), delimeter));
				
				startOffset = region.getOffset() + region.getLength();
				region = adapter.find(startOffset, delimeterZero, true, false, false, false);
			}
		}

		edit.apply(temporalDocument, TextEdit.UPDATE_REGIONS);

		positionsToVariables(positions, variables);

		buffer.setContent(temporalDocument.get(), variables);
		return buffer;
	}

	private static List<RangeMarker> variablesToPositions(final TemplateVariable[] variables) {
		List<RangeMarker> positions = new ArrayList<RangeMarker>(5);
		for (int i = 0; i != variables.length; i++) {
			int[] offsets = variables[i].getOffsets();
			for (int j = 0; j != offsets.length; j++) {
				positions.add(new RangeMarker(offsets[j], 0));
			}
		}

		return positions;
	}

	private static void positionsToVariables(final List<RangeMarker> positions, final TemplateVariable[] variables) {
		Iterator<RangeMarker> iterator = positions.iterator();

		for (int i = 0; i != variables.length; i++) {
			TemplateVariable variable = variables[i];

			int[] offsets = new int[variable.getOffsets().length];
			for (int j = 0; j != offsets.length; j++) {
				offsets[j] = ((TextEdit) iterator.next()).getOffset();
			}

			variable.setOffsets(offsets);
		}
	}
}
