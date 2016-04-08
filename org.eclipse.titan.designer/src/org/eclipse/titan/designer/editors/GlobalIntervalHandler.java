/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.parsers.Interval;

/**
 * Single class used to store and access the interval lists related to documents.
 * 
 * Usually parsers detect the interval hierarchy.
 * And they are used by folding in the editors.
 * 
 * @author Kristof Szabados
 * */
public class GlobalIntervalHandler {
	/**
	 * The set of documents we know about right now, and the root of the
	 * interval tree present in each
	 */
	private static final Map<IDocument, Interval> INTERVAL_MAP = new WeakHashMap<IDocument, Interval>();


	// Disabled constructor
	private GlobalIntervalHandler() {
		// Do nothing
	}

	/**
	 * Puts in the root of an interval tree into the map of known intervals.
	 * This must be here as it is impossible to find out which project an
	 * IDocument object belongs to.
	 * 
	 * @param doc
	 *                the document from which the interval tree was
	 *                extracted
	 * @param interval
	 *                the root of the extracted interval tree
	 * */
	public static void putInterval(final IDocument doc, final Interval interval) {
		INTERVAL_MAP.put(doc, interval);
	}

	/**
	 * Returns the root of the interval tree which was extracted from the
	 * provided IDocument instance.
	 * 
	 * @param doc
	 *                the document
	 * @return the root of the extracted interval tree
	 * */
	public static Interval getInterval(final IDocument doc) {
		return INTERVAL_MAP.get(doc);
	}
}
