/*******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

public class RectangleDrawer {

	private RectangleDrawer() {
		// Hide constructor
	}

	public static void drawBox(IGC context, Rectangle rectangle, Color backgroundColor, Color gradientColor, int shadowSize) {
		context.setBackground(backgroundColor);
		if (MSCConstants.DRAW_GRADIENT) {
			context.setGradientColor(gradientColor);
			context.fillGradientRectangle(rectangle.x, rectangle.y, rectangle.width - shadowSize, rectangle.height - shadowSize, true);
		} else {
			context.fillRectangle(rectangle.x, rectangle.y, rectangle.width - shadowSize, rectangle.height - shadowSize);
		}
	}

	public static void drawBorder(IGC context, Rectangle rectangle, Color lineColor, int shadowSize) {
		if (MSCConstants.DRAW_BORDER) {
			context.setForeground(lineColor);
			context.drawRectangle(rectangle.x, rectangle.y, rectangle.width - shadowSize, rectangle.height - shadowSize);
		}
	}

	public static void drawShadow(IGC context, Rectangle rectangle, Color shadowColor, int shadowSize) {
		if (MSCConstants.DRAW_SHADOW) {
			context.setLineStyle(context.getLineSolidStyle());
			context.setLineWidth(MSCConstants.NORMAL_LINE_WIDTH);
			context.setBackground(shadowColor);
			context.fillRectangle(rectangle.x + shadowSize, rectangle.y + shadowSize, rectangle.width, rectangle.height);
		}
	}

	public static Color getColor(String key) {
		return (Color) Activator.getDefault().getCachedResource(key);
	}

	public static void drawText(IGC context, Rectangle rectangle, String nodeText, Color fontColor, Font font, int shadowSize) {
		context.setForeground(fontColor);
		context.setFont(font);
		context.drawTextTruncatedCentred(nodeText, rectangle.x, rectangle.y, rectangle.width - shadowSize, rectangle.width - shadowSize, true);
	}
}
