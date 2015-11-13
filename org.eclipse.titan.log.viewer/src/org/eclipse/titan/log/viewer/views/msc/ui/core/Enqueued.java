/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import static org.eclipse.titan.log.viewer.views.msc.ui.core.RectangleDrawer.drawBorder;
import static org.eclipse.titan.log.viewer.views.msc.ui.core.RectangleDrawer.drawBox;
import static org.eclipse.titan.log.viewer.views.msc.ui.core.RectangleDrawer.drawShadow;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Represents an enqueued event in the sequence chart
 *
 */
public class Enqueued extends BaseMessage {

	public Enqueued() {
		super(0);
	}

	@Override
	public int getX() {
		return MSCConstants.COLUMN_WIDTH * this.getEndLifeline().getIndex() + 3 * MSCConstants.COLUMN_WIDTH / 8;
	}

	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * this.eventOccurrence + MSCConstants.ROW_SPACING;
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

		drawShadow(context, rectangle,
				RectangleDrawer.getColor(MSCConstants.SILENT_EVENT_SHADOW_COLOR), MSCConstants.SILENT_EVENT_SHADOW_SIZE);
		drawBox(context, rectangle,
				getBackgroundColor(), RectangleDrawer.getColor(MSCConstants.SILENT_EVENT_BG_COLOR), MSCConstants.SILENT_EVENT_SHADOW_SIZE );
		drawBorder(context, rectangle,
				RectangleDrawer.getColor(MSCConstants.SILENT_EVENT_LINE_COLOR), MSCConstants.SILENT_EVENT_SHADOW_SIZE);
	}

	private Color getBackgroundColor() {
		return RectangleDrawer.getColor(MSCConstants.SILENT_EVENT_COLORS.get(Constants.EVENTTYPE_PORTEVENT));
	}

	@Override
	public Type getType() {
		return Type.ENQUEUED;
	}

	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		drawSymbol(context, xLeft, yBottom, direction);
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		// Do nothing
	}

}
