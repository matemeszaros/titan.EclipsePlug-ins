/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
 * Representation of a lifeline header in the sequence diagram
 *
 */
public class LifelineHeader extends MSCNode {

	private Lifeline lifeline;
	private Frame frame;
	private boolean dragAndDrop;

	/**
	 * Constructor
	 * 
	 * @param horizontalIndex the horizontal index (of the life line)
	 */
	public LifelineHeader(final Lifeline lifeline, final Frame frame) {
		super(0);
		this.lifeline = lifeline;
		this.frame = frame;
	}

	/**
	 * Sets drag and drop mode
	 * @param dragAndDrop the flag indication status of drag and drop mode
	 */
	public void setDragAndDropMode(final boolean dragAndDrop) {
		this.dragAndDrop = dragAndDrop;
	}
	
	/**
	 * Gets the drag and drop mode
	 * @return the flag indication status of drag and drop mode
	 */
	public boolean getDragAndDropMode() {
		return this.dragAndDrop;
	}

	/**
	 * Gets the horizontal index
	 * @return the horizontal index
	 */
	public int getIndex() {
		return this.lifeline.getIndex();
	}
	
	/**
	 * Gets the lifeline which this headers represents
	 * @return the lifeline
	 */
	public Lifeline getLifeline() {
		return this.lifeline;
	}
	
	@Override
	public int getX() {
		return this.lifeline.getIndex() * MSCConstants.COLUMN_WIDTH + MSCConstants.COLUMN_SPACING / 4;
	}

	@Override
	public int getY() {
		return this.frame.getVisibleAreaY() + MSCConstants.ROW_SPACING / 2;
	}

	@Override
	public int getWidth() {
		return MSCConstants.COLUMN_WIDTH - MSCConstants.COLUMN_SPACING / 2 + MSCConstants.HEADER_SHADOW_SIZE;
	}

	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - MSCConstants.ROW_SPACING + MSCConstants.HEADER_SHADOW_SIZE;
	}
	
	@Override
	public void draw(final IGC context) {
		if (this.dragAndDrop) {
			return;
		}
		draw(context, getX(), getY(), getWidth(), getHeight());
	}
	
	/**
	 * Draws the header
	 * @param context the context to draw on
	 * @param dragX the x coordinate to draw the header on
	 * @param dragY the y coordinate to draw the header on
	 */
	public void draw(final IGC context, final int dragX, final int dragY) {
		draw(context, dragX, getY(), getWidth(), getHeight());
	}
	
	private void draw(final IGC context, final int x, final int y, final int width, final int height) { 		
		context.setLineStyle(context.getLineSolidStyle());
		context.setLineWidth(MSCConstants.NORMAL_LINE_WIDTH);
		
		if (MSCConstants.DRAW_SHADOW) {
			// Draw shadow
			context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.LIFELIFE_HEADER_SHADOW_COLOR));
			context.fillRectangle(x + MSCConstants.HEADER_SHADOW_SIZE, 
								  y + MSCConstants.HEADER_SHADOW_SIZE,
								  width, 
								  height);
		}
		
		// Draw box
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.LIFELIFE_HEADER_BG_COLOR));
		if (MSCConstants.DRAW_GRADIENT) {
			context.setGradientColor((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
			context.fillGradientRectangle(x, 
								  		  y, 
								  		  width - MSCConstants.HEADER_SHADOW_SIZE, 
								  		  height - MSCConstants.HEADER_SHADOW_SIZE,
								  		  true);
		} else {
			context.fillRectangle(x, 
			  		  			  y, 
			  		  			  width - MSCConstants.HEADER_SHADOW_SIZE, 
			  		  			  height - MSCConstants.HEADER_SHADOW_SIZE);
		}
		if (MSCConstants.DRAW_BORDER) {
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.LIFELIFE_HEADER_LINE_COLOR));
			context.drawRectangle(x, 
								  y, 
								  width - MSCConstants.HEADER_SHADOW_SIZE, 
								  height - MSCConstants.HEADER_SHADOW_SIZE);
		}

		// Draw text
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.LIFELIFE_HEADER_FONT_COLOR));
		context.setFont((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT));
		context.drawTextTruncatedCentred(getName(), 
										 x,
										 y,
										 width - MSCConstants.HEADER_SHADOW_SIZE, 
										 height - MSCConstants.HEADER_SHADOW_SIZE,
										 true);
	}
	
	@Override
	public Type getType() {
		return Type.LIFE_LINE_HEADER;
	}

	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return true; 
	}

}
