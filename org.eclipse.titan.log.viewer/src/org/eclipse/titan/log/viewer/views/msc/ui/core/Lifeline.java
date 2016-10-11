/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
 * Lifeline represents the graphical representation of the life line of a component.
 * 
 */
public final class Lifeline extends MSCNode {
	
	// 0 reserved for time stamp
	private int indexInFrame = 1;
	
	/**
	 * The frame where the lifeline is drawn
	 */
	private Frame frame	= null;
	
	/**
	 * The current event occurrence created in the lifeline
	 */
	private int eventOccurrence = 0;
	
	private ComponentCreation start = null;
	private ComponentTermination stop = null;

	public Lifeline() {
		super(0);
	}

	@Override
	public int getX() {
		return this.indexInFrame * MSCConstants.COLUMN_WIDTH + MSCConstants.COLUMN_WIDTH / 2;
	}

	@Override
	public int getY() {
		if (this.start != null) {
			return this.start.getY();
		}

		return 0;
	}

	@Override
	public int getWidth() {
		return MSCConstants.NORMAL_LINE_WIDTH;
	}

	@Override
	public int getHeight() {
		if (this.start != null && this.stop != null) {
			return this.stop.getY() + this.stop.getHeight() - this.start.getY();
		}

		return 0;
	}
	
	/**
	 * Set the frame on which this lifeline must be drawn
	 * @param parentFrame
	 */
	protected void setFrame(final Frame parentFrame) {
		this.frame = parentFrame;
		if (this.frame.getMaxEventOccurrence() < getEventOccurrence() + 1) {
			this.frame.setMaxEventOccurrence(getEventOccurrence() + 1);
		}
	}
	
	/**
	 * Sets the start 
	 * @param start
	 */
	public void setStart(final ComponentCreation start) {
		this.start = start;
	}
	
	public void setStop(final ComponentTermination stop) {
		this.stop = stop;
	}

	public boolean hasStopBeenSet() {
		return stop != null;
	}

	/**
	 * Returns the frame which this lifeline is drawn
	 * @return the Frame
	 */
	protected Frame getFrame() {
		return this.frame;
	}
	
	/**
	 * Set the lifeline position index in the containing frame
	 * @param index the lifeline X position
	 */
	protected void setIndex(final int index) {
		this.indexInFrame = index;
	}
	
	/**
	 * Returns the lifeline position in de the containing frame
	 * @return the X position
	 */
	public int getIndex() {
		return this.indexInFrame;
	}
	
	/**
	 * Set the lifeline event occurrence to the value given in parameter
	 * This only change the current event occurrence, greater event 
	 * created on this lifeline are still valid and usable.
	 * This also need to inform the frame of the operation mostly to store in the frame the greater
	 * event found in the diagram (used to determine the frame height)
	 * @param eventOcc the new current event occurrence
	 */
	public void setCurrentEventOccurrence(final int eventOcc) {
		if ((this.frame != null) && (this.frame.getMaxEventOccurrence() < eventOcc)) {
			this.frame.setMaxEventOccurrence(eventOcc);
		}
		this.eventOccurrence = eventOcc;
	}
	
	/**
	 * Returns the last created event occurrence along the lifeline.
	 * @return the current event occurrence
	 */
	public int getEventOccurrence()	{
		return this.eventOccurrence;
	}
	
	/**
	 * Creates a new event occurrence along the lifeline.
	 * @return the new created event occurrence
	 */
	public int getNewEventOccurrence() {
		setCurrentEventOccurrence(this.eventOccurrence + 1);
		return this.eventOccurrence;
	}
	
	@Override
	public boolean contains(final int x, final int y) {
		if (this.frame == null) {
			return false;
		}
		return MSCNode.contains(getX(), getY(), getWidth(), getHeight(), x, y);
	}
	
	/**
	 * Force the lifeline to be drawn at the given coordinate
	 * @param context - the context to draw into
	 * @param x - the x coordinate
	 * @param y - the y coordinate
	 */
	public void draw(final IGC context, final int x, final int y) {
 		int visYStart = this.frame.getVisibleAreaY();
 		int visYStop = visYStart + this.frame.getVisibleAreaHeight();
 		int yStart;
 		if (start == null) {
 			yStart = visYStart;
 		} else {
 			yStart = this.start.getY() + this.start.getHeight();
 	 		if (visYStart > yStart) {
 	 			yStart = visYStart;
 	 		}
 		}

 		int yStop;
 		if (stop == null) {
 			yStop = visYStop;
 		} else {
 			yStop = this.stop.getY();
 	 		if (visYStop < yStop) {
 	 			yStop = visYStop; 
 	 		}
 		}

 		
		// Set line style and width
		context.setLineStyle(context.getLineSolidStyle());
		context.setLineWidth(MSCConstants.NORMAL_LINE_WIDTH);
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.LIFELIFE_LINE_COLOR));
		context.drawLine(x, yStart, x, yStop);
		
	}
	
	@Override
	public void draw(final IGC context) {
		draw(context, getX(), getY());
	}
	

	
	@Override
	public Type getType() {
		return Type.LIFE_LINE;
	}

	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return getX() > x;
	}
}
