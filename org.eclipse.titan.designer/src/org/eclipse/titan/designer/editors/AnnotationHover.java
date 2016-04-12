/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Kristof Szabados
 * */
public final class AnnotationHover implements IAnnotationHover {

	@Override
	public String getHoverInfo(final ISourceViewer sourceViewer, final int lineNumber) {
		final List<IMarker> markers = getMarkerForLine(sourceViewer, lineNumber);
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < markers.size(); i++) {
			final String message = markers.get(i).getAttribute(IMarker.MESSAGE, (String) null);
			if (message != null && message.trim().length() > 0) {
				if (i != 0) {
					// Perfect newline for TextHoverControl
					// builder.append('\n');
					// DefaultInformationControl needs html
					// like newline
					builder.append("<BR></BR>");
				}
				builder.append(message.trim());
			}
		}
		return builder.toString();
	}

	/**
	 * Helper function for {@link #getHoverInfo(ISourceViewer, int)}.
	 * 
	 * @see #getHoverInfo(ISourceViewer, int)
	 * 
	 * @param sourceViewer
	 *                The viewer whose annotation we wish to find.
	 * @param lineNumber
	 *                The line number in which the annotation(s) are.
	 * @return The list of markers residing in the given line in the given
	 *         editor.
	 */
	protected List<IMarker> getMarkerForLine(final ISourceViewer sourceViewer, final int lineNumber) {
		final List<IMarker> markers = new ArrayList<IMarker>();
		final IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		if (annotationModel == null) {
			return markers;
		}

		final Iterator<?> iterator = annotationModel.getAnnotationIterator();
		while (iterator.hasNext()) {
			final Object o = iterator.next();
			if (o instanceof MarkerAnnotation) {
				final MarkerAnnotation actuaMarkerl = (MarkerAnnotation) o;
				try {
					final int actualLine = sourceViewer.getDocument().getLineOfOffset(
							annotationModel.getPosition(actuaMarkerl).getOffset());
					if (actualLine == lineNumber) {
						markers.add(actuaMarkerl.getMarker());
					}
				} catch (BadLocationException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		}
		return markers;
	}
}
