/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.designer.editors.FoldingSupport;
import org.eclipse.titan.designer.editors.GlobalIntervalHandler;
import org.eclipse.titan.designer.editors.HeuristicalIntervalDetector;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
//TODO when doing incremental parsing the folding support could be done in a specific way.
public final class TTCN3FoldingSupport extends FoldingSupport {

	@Override
	public List<Position> calculatePositions(final IDocument document) {
		positions.clear();

		this.lastLineIndex = document.getNumberOfLines();
		this.documentText = document.get();
		preferencesService = Platform.getPreferencesService();
		foldingDistance = preferencesService.getInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_DISTANCE, 0, null);

		if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLDING_ENABLED, true, null)) {
			Interval interval = GlobalIntervalHandler.getInterval(document);
			if (interval == null) {
				interval = (new HeuristicalIntervalDetector()).buildIntervals(document);
				GlobalIntervalHandler.putInterval(document, interval);
			}
			for (Interval subintervall : interval.getSubIntervals()) {
				recursiveTokens(subintervall);
			}
		}
		return positions;
	}

}
