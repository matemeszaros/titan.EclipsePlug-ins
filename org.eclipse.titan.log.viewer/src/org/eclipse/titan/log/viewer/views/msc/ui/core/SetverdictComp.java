/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.graphics.Color;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Represents a setverdict on a known component
 *
 */
public class SetverdictComp extends MSCNode {

	private Lifeline lifeline;
	private String verdict;
	
	/**
	 * Constructor
	 * @param eventOccurrence the occurrence of this event
	 */
	public SetverdictComp(final int eventOccurrence, final Lifeline lifeline, final String verdict) {
		super(eventOccurrence);
		this.lifeline = lifeline;
		this.verdict = verdict;
	}

	@Override
	public int getX() {
		return MSCConstants.COLUMN_WIDTH * this.lifeline.getIndex()
		       + MSCConstants.COLUMN_WIDTH / 2
		       - getWidth() / 2 + MSCConstants.SETVERDICT_SHADOW_SIZE;
	}

	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * this.getStartOccurrence() + MSCConstants.ROW_SPACING / 2
				+ getHeight() / 2 - 2 * MSCConstants.SETVERDICT_SHADOW_SIZE;
	}

	@Override
	public int getWidth() {
		return getHeight() + MSCConstants.SETVERDICT_SIZE;
	}

	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - 2 * MSCConstants.ROW_SPACING + 2 * MSCConstants.SETVERDICT_SHADOW_SIZE;
	}
	
	@Override
	protected void draw(final IGC context) {
		int x = getX();
		int y = getY();
		int width = getWidth() - MSCConstants.SETVERDICT_SHADOW_SIZE;
		int height = getHeight() - MSCConstants.SETVERDICT_SHADOW_SIZE;
		int size = MSCConstants.SETVERDICT_SIZE;
		
		context.setLineStyle(context.getLineSolidStyle());
		context.setLineWidth(size / 2);
		
		
		if (MSCConstants.DRAW_SHADOW) {
			// Draw shadow
			context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.SETVERDICT_SHADOW_COLOR));
			x = x + MSCConstants.SETVERDICT_SHADOW_SIZE;
			y = y + MSCConstants.SETVERDICT_SHADOW_SIZE;
			int[] shadowPointsA = {x, y,
								   x + size, y,
								   x + width, y + height,
								   x + width - size, y + height};
			int[] shadowPointsB = {x + width, y,
					   			   x + width - size, y,
					   			   x, y + height,
					   			   x + size, y + height};
			context.fillPolygon(shadowPointsA);
			context.fillPolygon(shadowPointsB);
			x = x - MSCConstants.SETVERDICT_SHADOW_SIZE;
			y = y - MSCConstants.SETVERDICT_SHADOW_SIZE;
		}
		
		// Draw X
		width = width - MSCConstants.SETVERDICT_SHADOW_SIZE;
		height = height - MSCConstants.SETVERDICT_SHADOW_SIZE;
		
		int[] pointsA = {x, y,
					 	 x + size, y,
					 	 x + width, y + height,
					 	 x + width - size, y + height};
		int[] pointsB = {x + width, y,
			 		 	 x + width - size, y,
			 		 	 x, y + height,
			 		 	 x + size, y + height};	
		
		if (MSCConstants.DRAW_BORDER) {
			// Draw borders of cross
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.SETVERDICT_LINE_COLOR));
			context.drawPolygon(pointsA);
			context.drawPolygon(pointsB);
		}

		// Fill cross
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.getVerdictColor(this.verdict)));
		context.fillPolygon(pointsA);
		context.fillPolygon(pointsB);
	}

	@Override
	public Type getType() {
		return Type.SETVERDICT_COMPONENT;
	}

	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return true;
	}

}
