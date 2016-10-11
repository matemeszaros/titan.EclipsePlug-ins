/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Abstract base class for all messages
 *
 */
public abstract class BaseMessage extends MSCNode {
	
	private Lifeline startLifeline = null;
	private Lifeline endLifeline = null;
	
	protected static final int LEFT = -1;
	protected static final int RIGHT = 1;

	protected BaseMessage(int eventOccurrence) {
		super(eventOccurrence);
	}
	
	/**
	 * Returns the line style of the given message.
	 * @return the line style which will be one of the constants 
	 * 			SWT.LINE_SOLID, SWT.LINE_DASH, SWT.LINE_DOT, SWT.LINE_DASHDOT or SWT.LINE_DASHDOTDOT
	 */
	public int getLineStyle() {
		return SWT.LINE_SOLID;
	}
	
	@Override
	public int getX() {
		if (startLifeline == null) {
			return -1;
		}

		return this.startLifeline.getX();
	}
	
	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * getStartOccurrence() + MSCConstants.ROW_SPACING / 2;
	}
	
	@Override
	public int getWidth() {
		if (startLifeline == null) {
			return -1;
		}

		int width = 0;
		if (this.startLifeline == this.endLifeline) {
			width = MSCConstants.COLUMN_WIDTH;
		} else {
			width = this.endLifeline.getX() - this.startLifeline.getX();
		}
		return width;
	}
	
	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - MSCConstants.ROW_SPACING;
	}
		
	@Override
	public boolean isVisible(final int vx, final int vy, final int vwidth, final int vheight) {
		int x = getX();
		int y = getY();
		int width = getWidth();
		if (width < 0) {
			width = -width;
			x = x - width;
		}
		int height = getHeight();
		if (((x + width) < vx) || // To the left 
			(x > (vx + vwidth)) || // To the right
			((y + height) < vy) || // Above
			(y > (vy + vheight))) { // Below
			return false;
		}
		return true;
	}

	/**
	 * Set the lifeline from which this message has been sent.
	 * @param lifeline - the message sender
	 */
	public void setStartLifeline(final Lifeline lifeline) {
		this.startLifeline = lifeline;
		if (getEndLifeline() == null) {
			eventOccurrence = getStartLifeline().getEventOccurrence();
		}
	}
	
	/**
	 * Returns the lifeline from which this message has been sent.
	 * @return the message sender
	 */
	public Lifeline getStartLifeline() {
		return this.startLifeline;
	}
	
	/**
	 * Set the lifeline which has receive this message.
	 * @param lifeline the message receiver
	 */
	public void setEndLifeline(final Lifeline lifeline) {
		this.endLifeline = lifeline;
		if (getStartLifeline() == null) {
			eventOccurrence = getEndLifeline().getEventOccurrence();
		}
	}
	
	/**
	 * Returns the lifeline which has received this message.
	 * @return the message receiver
	 */
	public Lifeline getEndLifeline() {
		return this.endLifeline;
	}
	
	@Override
	public boolean contains(final int oldX, final int oldY) {
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();
		return MSCNode.contains(x, y, width, height, oldX, oldY);
	}
	
	@Override
	public void draw(final IGC context) {
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();
		context.setLineWidth(MSCConstants.NORMAL_LINE_WIDTH);
		context.setLineStyle(getLineStyle());
		
		// it is self message (always drawn at the left side of the owning lifeLifeline)
		if ((this.startLifeline != null) && (this.endLifeline != null) && (this.startLifeline == this.endLifeline))	{

			// Draw lines
			//       1
			//   ---------
			//            |
			//            | 2
			//            |
			// 4 <--------
			//       3
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
			int xLeft = x;
			int xRight = x + MSCConstants.INTERNAL_MESSAGE_WIDTH;
			int yTop = y + (height - MSCConstants.INTERNAL_MESSAGE_WIDTH) / 2;
			int yBottom = y + MSCConstants.INTERNAL_MESSAGE_WIDTH + (height - MSCConstants.INTERNAL_MESSAGE_WIDTH) / 2;
			
			// Draw 1
			context.drawLine(xLeft, yTop, xRight, yTop);
			// Draw 2
			context.drawLine(xRight, yTop, xRight, yBottom);
			// Draw 3
			context.drawLine(xRight, yBottom, xLeft, yBottom);
			// Draw 4
			drawSymbol(context, xLeft, xRight, yTop, yBottom, LEFT);
			
			//drawSymbol(context, xLeft, yBottom, this.LEFT); // Always left direction
			
			// Draw the message label to the right of the message and centered
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_FONT_COLOR));
			context.setFont((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT));
			context.drawTextTruncatedCentred(getName(), 
											 x + MSCConstants.INTERNAL_MESSAGE_WIDTH + MSCConstants.INTERNAL_MESSAGE_H_MARGIN, 
											 y, 
											 width - MSCConstants.INTERNAL_MESSAGE_WIDTH - 2 * MSCConstants.INTERNAL_MESSAGE_H_MARGIN,
											 height,
											 true);

		// Regular message
		} else if ((this.startLifeline != null) && (this.endLifeline != null)) {
			
			// Draw lines
			//       1
			//   ---------> 2
			//
			// or...
			//       1
			// 2 <---------
			//
			// Draw 1 (the message main line)
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
			context.drawLine(x, y + height, x + width, y + height);

			// Compute the coordinates of the two little lines which make the arrow part of the message
			int spaceBTWStartEnd = this.endLifeline.getX() - this.startLifeline.getX();
			int direction = RIGHT;
			if (spaceBTWStartEnd < 0) {
				direction = LEFT;
			}

			// Draw 2
			drawSymbol(context, x + width, y + height, direction);
			
			// Draw the message label above the message and centered
			context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_FONT_COLOR));
			context.setFont((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT));
			if (spaceBTWStartEnd > 0) {
				context.drawTextTruncatedCentred(getName(), 
												 x, 
												 y, 
												 width,
												 height,
												 true);
			} else {
				context.drawTextTruncatedCentred(getName(), 
												 x + width, 
												 y, 
												 -width,
												 height,
												 true);
			}
		}
	}
	
	/**
	 * Draws the symbol specific for the event type
	 * Should be implemented by all classes that derives from BaseMessage 
	 * @param context the context to draw on
	 * @param x the x coordinate of the to symbol
	 * @param y the y coordinate of the symbol
	 * @param direction the directing of the symbol
	 */
	public abstract void drawSymbol(IGC context, int x, int y, int direction);
	
	public abstract void drawSymbol(IGC context, int xTop, int xBottom, int yTop, int yBottom, int direction);
	
	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return true;
	}
	
}
