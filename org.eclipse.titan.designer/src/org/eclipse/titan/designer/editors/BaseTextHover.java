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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.actions.DeclarationCollectionHelper;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.actions.OpenDeclarationHelper;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class BaseTextHover implements ITextHover, ITextHoverExtension2 {

	protected abstract ISourceViewer getSourceViewer();

	protected abstract IEditorPart getTargetEditor();

	protected abstract IReferenceParser getReferenceParser();

	@Override
	public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		if (hoverRegion == null || textViewer == null) {
			return null;
		}

		IAnnotationModel annotationModel = getSourceViewer().getAnnotationModel();
		if (annotationModel != null) {
			Iterator<?> iterator = annotationModel.getAnnotationIterator();
			List<String> messages = new ArrayList<String>();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof MarkerAnnotation) {
					MarkerAnnotation actualMarker = (MarkerAnnotation) o;
					Position markerPosition = annotationModel.getPosition(actualMarker);
					if (markerPosition != null && markerPosition.getOffset() <= hoverRegion.getOffset()
							&& markerPosition.getOffset() + markerPosition.getLength() >= hoverRegion.getOffset()) {
						String message = actualMarker.getText();
						if ( message != null ) {
							// Marker error text hover (or tooltip in other words) handles error message
							// in HTML format, and there can be situation, when the message contains
							// < and > characters, which are handled as HTML control tags, so they
							// are not visible. So these < and > characters are removed.
							// Example: ANTLR sends the following error message during parsing:
							//   "mismatched input 'control' expecting <EOF>"
							message = message.replaceAll( "\\<([A-Z]+)\\>", "$1" );
						} else {
							ErrorReporter.INTERNAL_ERROR("BaseTextHover.getHoverInfo(): message == null");
						}
						messages.add( message );
					}
				}
			}
			if (!messages.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append(messages.get(0));
				for (int i = 1; i < messages.size() && i <= 3; i++) {
					builder.append("<br></br>");
					builder.append(messages.get(i));
				}
				if (messages.size() > 3) {
					builder.append("<br></br>...");
				}
				return builder.toString();
			}
		}
		ErrorReporter.parallelDisplayInStatusLine(getTargetEditor(),null);
		
		DeclarationCollector declarationCollector = OpenDeclarationHelper.findVisibleDeclarations(getTargetEditor(), getReferenceParser(),
				textViewer.getDocument(), hoverRegion.getOffset(), false);

		if (declarationCollector == null) {
			return null;
		}

		List<DeclarationCollectionHelper> collected = declarationCollector.getCollected();

		// To handle reference problem in T3Doc
		if (T3Doc.isT3DocEnable()) {
			String string = T3Doc.getCommentStringBasedOnReference(declarationCollector, collected, getTargetEditor(), hoverRegion,
					getReferenceParser(), textViewer);
			if (string != null) {
				return string;
			}
		}

		if (collected.isEmpty()) {
			return null;
		}

		DeclarationCollectionHelper declaration = collected.get(0);

		// Check whether the T3Doc is enabled in the preferences window
		if (!T3Doc.isT3DocEnable()) {
			return declaration.description;
		}

		if (declaration.node != null) {

			if (declaration.node.getT3Doc(declaration.location) == null) {
				return "";
			}

			if (declaration.description != null) {
				return declaration.description + "<BR></BR>" + declaration.node.getT3Doc(declaration.location).toString();
			}

			return declaration.node.getT3Doc(declaration.location).toString();

		}

		if (declaration.scope != null) {

			if (declaration.description != null) {
				return declaration.description + declaration.scope.getT3Doc(declaration.location).toString();
			}

			return declaration.scope.getT3Doc(declaration.location).toString();

		}

		// return declaration.description;
		return "";
	}

	@Override
	public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
		// Start with the string returned by the older getHoverInfo()
		final String selection = getHoverInfo(textViewer, hoverRegion);
		return selection;
	}

	@Override
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		return new Region(offset, 0);
	}
}
