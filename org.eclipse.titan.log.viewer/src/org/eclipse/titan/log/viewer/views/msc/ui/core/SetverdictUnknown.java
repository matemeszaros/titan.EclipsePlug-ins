/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Represents a setverdict on an unknown component
 *
 */
public class SetverdictUnknown extends MSCNode {

	private String verdict;
	private int lifelines;
	
	/**
	 * Constructor
	 * @param eventOccurrence the occurrence of this event
	 */
	public SetverdictUnknown(final int eventOccurrence, final String verdict) {
		super(eventOccurrence);
		this.verdict = verdict;
	}

	/**
	 * Sets number of lifelines in the MSC
	 * @param lifelines the number of lifelines
	 */
	public void setNumLifelines(final int lifelines) {
		this.lifelines = lifelines;
	}

	@Override
	public int getX() {
		return MSCConstants.COLUMN_WIDTH + MSCConstants.COLUMN_SPACING / 2;
	}

	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * this.getStartOccurrence() + MSCConstants.ROW_SPACING / 2;
	}
	
	@Override
	public int getWidth() {
		return this.lifelines * MSCConstants.COLUMN_WIDTH - MSCConstants.COLUMN_SPACING + 2 * MSCConstants.SETVERDICT_SHADOW_SIZE;
	}
	
	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - MSCConstants.ROW_SPACING + MSCConstants.SETVERDICT_SHADOW_SIZE;
	}
	
	@Override
	protected void draw(final IGC context) {
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();
		
		context.setLineStyle(context.getLineSolidStyle());
		context.setLineWidth(MSCConstants.NORMAL_LINE_WIDTH);

		if (MSCConstants.DRAW_SHADOW) {
			// Draw shadow
			context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.SETVERDICT_SHADOW_COLOR));
			context.fillRectangle(x + MSCConstants.SETVERDICT_SHADOW_SIZE, 
					  			  y + MSCConstants.SETVERDICT_SHADOW_SIZE,
					  			  width, 
					  			  height);
		}
		
		// Draw box
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.getVerdictColor(this.verdict)));
		if (MSCConstants.DRAW_GRADIENT) {
			context.setGradientColor((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
			context.fillGradientRectangle(x, 
								  		  y, 
								  		  width - MSCConstants.SETVERDICT_SHADOW_SIZE, 
								  		  height - MSCConstants.SETVERDICT_SHADOW_SIZE,
								  		  true);
		} else {
			context.fillRectangle(x, 
			  		  			  y, 
			  		  			  width - MSCConstants.SETVERDICT_SHADOW_SIZE, 
			  		  			  height - MSCConstants.SETVERDICT_SHADOW_SIZE);
		}
		if (MSCConstants.DRAW_BORDER) {
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.SETVERDICT_LINE_COLOR));
			context.drawRectangle(x, 
								  y, 
								  width - MSCConstants.SETVERDICT_SHADOW_SIZE, 
								  height - MSCConstants.SETVERDICT_SHADOW_SIZE);
		}

		// Draw text
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.SETVERDICT_FONT_COLOR));
		context.setFont((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT));
		context.drawTextTruncatedCentred(this.verdict, 
								  		 x,
								  		 y,
								  		 width - MSCConstants.SETVERDICT_SHADOW_SIZE, 
								  		 height - MSCConstants.SETVERDICT_SHADOW_SIZE,
								  		 true);
	}

	@Override
	public Type getType() {
		return Type.SETVERDICT_UNKNOWN;
	}

	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return true;
	}

}
