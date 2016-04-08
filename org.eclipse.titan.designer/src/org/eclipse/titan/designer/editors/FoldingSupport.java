/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class enables support for folding for every text based file.
 * 
 * @author Kristof Szabados
 */
public class FoldingSupport {
	protected int foldingDistance;
	protected List<Position> positions = new ArrayList<Position>();
	protected IPreferencesService preferencesService;
	protected String documentText;
	protected int lastLineIndex;

	/**
	 * Calculates the list of folding intervals.
	 * 
	 * @param document
	 *                The document where the search for folding regions
	 *                should take place.
	 * @return The list of identified folding regions.
	 */
	public List<Position> calculatePositions(final IDocument document) {
		positions.clear();

		this.lastLineIndex = document.getNumberOfLines();
		this.documentText = document.get();
		preferencesService = Platform.getPreferencesService();
		foldingDistance = preferencesService.getInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_DISTANCE, 0, null);

		if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLDING_ENABLED, true, null)) {
			Interval interval = GlobalIntervalHandler.getInterval(document);
			if (interval == null) {
				return positions;
			}
			for (Interval subintervall : interval.getSubIntervals()) {
				recursiveTokens(subintervall);
			}
		}
		return positions;
	}

	protected void recursiveTokens(final Interval interval) {
		int endOffset = interval.getEndOffset();
		if (documentText.length() <= endOffset || documentText.length() <= interval.getStartOffset()) {
			return;
		} else if (endOffset == -1) {
			endOffset = documentText.length() - 1;
		}
		if (endOffset <= interval.getStartOffset()) {
			ErrorReporter.INTERNAL_ERROR();
			return;
		}
		int endline = interval.getEndLine();
		if (endline > lastLineIndex) {
			return;
		} else if (endline == -1) {
			endline = lastLineIndex;
		}

		int distance = endline - interval.getStartLine();
		if (distance >= foldingDistance) {
			switch (documentText.charAt(interval.getStartOffset())) {
			case '{':
				if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_STATEMENT_BLOCKS, true, null)) {
					positions.add(new Position(interval.getStartOffset(), endOffset - interval.getStartOffset()));
				}
				break;
			case '(':
				if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_PARENTHESIS, true, null)) {
					positions.add(new Position(interval.getStartOffset(), endOffset - interval.getStartOffset()));
				}
				break;
			case '/':
			case '#':
				if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_COMMENTS, true, null)) {
					positions.add(new Position(interval.getStartOffset(), endOffset - interval.getStartOffset()));
				}
				break;
			default:
				break;
			}
		}

		for (Interval subIntervall : interval.getSubIntervals()) {
			recursiveTokens(subIntervall);
		}
	}

	/**
	 * Updates the folding structure of the provided projection's annotation
	 * model.
	 * <ul>
	 * <li>If a region existed before it is not touched.
	 * <li>If a region disappeared, then it is removed.
	 * <li>If a new region appeared, then it is added.
	 * </ul>
	 * 
	 * The algorithm extensively uses the fact the the new positions are
	 * always sort ascending by their starting offset. The algorithm also
	 * uses the fact that the old annotations array is ALMOST sorted. It is
	 * not fully sorted, but as each batch added is sorted ascending, it is
	 * safe and efficient to use methods that assume it is sorted.
	 * 
	 * @param annotationModel
	 *                the annotation model to be updated
	 * @param oldAnnotations
	 *                the list of the old annotations to be updated
	 * @param newPositions
	 *                The new folding regions
	 */
	public static synchronized void updateFoldingStructure(final ProjectionAnnotationModel annotationModel,
			final List<Annotation> oldAnnotations, final List<Position> newPositions) {
		if (oldAnnotations == null || annotationModel == null) {
			return;
		}

		Annotation[] deletedAnnotations = null;

		if (!oldAnnotations.isEmpty()) {
			deletedAnnotations = new Annotation[oldAnnotations.size()];

			int foundIndex;
			int deletedIndex = 0;
			Position pos;

			for (int i = oldAnnotations.size() - 1; i >= 0; i--) {
				pos = annotationModel.getPosition(oldAnnotations.get(i));
				foundIndex = -1;
				if (pos != null) {
					for (int j = newPositions.size() - 1; j >= 0; j--) {
						if (pos.offset == newPositions.get(j).offset) {
							foundIndex = j;
							break;
						}
					}
				}
				if (foundIndex >= 0) {
					newPositions.remove(foundIndex);
				} else {
					deletedAnnotations[deletedIndex++] = oldAnnotations.remove(i);
				}
			}
		}

		Map<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();
		ProjectionAnnotation annotation;

		List<ProjectionAnnotation> newAnnotations2 = new ArrayList<ProjectionAnnotation>(newAnnotations.size());
		Position position;
		for (int i = 0; i < newPositions.size(); i++) {
			position = newPositions.get(i);
			if (position != null) {
				annotation = new ProjectionAnnotation();
				newAnnotations.put(annotation, position);
				newAnnotations2.add(annotation);
			}
		}

		try {
			annotationModel.modifyAnnotations(deletedAnnotations, newAnnotations, null);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		oldAnnotations.addAll(newAnnotations2);
	}
}
