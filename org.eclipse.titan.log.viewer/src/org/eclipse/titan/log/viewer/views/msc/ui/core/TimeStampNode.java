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
 * Represents a time stamp
 *
 */
public class TimeStampNode extends MSCNode {

	/**
	 * Constructor
	 * @param eventOccurrence the occurrence of this event
	 * @param time the time stamp
	 */
	public TimeStampNode(final int eventOccurrence, final String time) {
		super(eventOccurrence);
		setName(time);
	}

	@Override
	public int getX() {
		return MSCConstants.COLUMN_SPACING / 2;
	}

	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * this.getStartOccurrence() + MSCConstants.ROW_SPACING / 2;
	}
	
	@Override
	public int getWidth() {
		return MSCConstants.COLUMN_WIDTH - MSCConstants.COLUMN_SPACING / 2;
	}
	
	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - MSCConstants.ROW_SPACING;
	}
	
	@Override
	protected void draw(final IGC context) {
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();
		
		// Draw time stamp
		context.setFont((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT));
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.TIMESTAMP_FONT_COLOR));
		context.drawTextTruncatedLeft(getName(),
										 x,
										 y, 
										 width, 
										 height, 
										 true);
	}

	@Override
	public Type getType() {
		return Type.TIMESTAMP;
	}

	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return true;
	}

}
