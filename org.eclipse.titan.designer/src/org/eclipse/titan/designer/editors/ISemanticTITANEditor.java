/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * This interface represents an abstract editor that a syntactic/semantic
 * analyzer can an effect on, based on the results of its operation.
 * <p>
 * Implementors of this interface should expect the supported functions to be
 * called when a semantic checking operation ends (which can be about any time).
 * 
 * @author Kristof Szabados
 * */
public interface ISemanticTITANEditor {
	IDocument getDocument();

	/**
	 * Updates the folding structure.
	 * <p>
	 * Works as a temporary call, that receives the positions of the new
	 * foldable positions, and adds the annotationmodell and old foldable
	 * positions of the actual editor
	 * 
	 * @param positions
	 *                The new folding regions
	 */
	void updateFoldingStructure(List<Position> positions);

	/**
	 * Invalidates the presentation of text inside this editor, forcing the
	 * editor to redraw itself.
	 * <p>
	 * This function practically enables the on-the-fly parser to redraw the
	 * texts, according to the information it has collected
	 * */
	void invalidateTextPresentation();

	/**
	 * Updates the outline view attached to this editor.
	 * <p>
	 * This function practically enables the on-the-fly parser to redraw the
	 * outline view, according to the information it has collected
	 * */
	void updateOutlinePage();
}
