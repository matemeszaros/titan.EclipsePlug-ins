/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import static org.eclipse.titan.log.viewer.views.msc.ui.core.RectangleDrawer.getColor;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Representation of a silent event in the sequence diagram
 *
 */
public class SilentEvent extends MSCNode {

	private Lifeline lifeline;
	private String type;

	/**
	 * Constructor
	 * 
	 * @param eventOccurrence the event occurrence for the creation of this silent event 
	 * @param type the silent event type
	 */
	public SilentEvent(final int eventOccurrence, final Lifeline lifeline, final String type) {
		super(eventOccurrence);
		this.lifeline = lifeline;
		this.type = type;
	}

	@Override
	public int getX() {
		return MSCConstants.COLUMN_WIDTH * this.lifeline.getIndex() + 3 * MSCConstants.COLUMN_WIDTH / 8;
	}

	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * this.getStartOccurrence() + MSCConstants.ROW_SPACING;
	}

	@Override
	public int getWidth() {
		return MSCConstants.COLUMN_WIDTH / 4 + MSCConstants.SILENT_EVENT_SHADOW_SIZE;
	}

	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - 2 * MSCConstants.ROW_SPACING + MSCConstants.SILENT_EVENT_SHADOW_SIZE;
	}

	@Override
	public void draw(final IGC context) {
		Rectangle rectangle = new Rectangle(getX(), getY(), getWidth(), getHeight());

		context.setLineStyle(context.getLineSolidStyle());

		RectangleDrawer.drawShadow(context, rectangle, getColor(MSCConstants.SILENT_EVENT_SHADOW_COLOR), MSCConstants.SILENT_EVENT_SHADOW_SIZE);
		RectangleDrawer.drawBox(context, rectangle, getBackgroundColor(),
				getColor(MSCConstants.SILENT_EVENT_BG_COLOR), MSCConstants.SILENT_EVENT_SHADOW_SIZE);
		RectangleDrawer.drawBorder(context, rectangle, getColor(MSCConstants.SILENT_EVENT_LINE_COLOR), MSCConstants.SILENT_EVENT_SHADOW_SIZE);
	}

	private Color getBackgroundColor() {
		if (this.type.contains("_")) { // //$NON-NLS-1$ // Silent Event with sub-category
			this.type = this.type.substring(0, this.type.indexOf("_")); //$NON-NLS-1$
		}
		return getColor(MSCConstants.SILENT_EVENT_COLORS.get(this.type));
	}

	@Override
	public Type getType() {
		return Type.SILENT_EVENT;
	}

	@Override
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return true;
	}
}
