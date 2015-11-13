/*******************************************************************************
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
import static org.eclipse.titan.log.viewer.views.msc.ui.core.RectangleDrawer.drawText;
import static org.eclipse.titan.log.viewer.views.msc.ui.core.RectangleDrawer.getColor;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * @author Szabolcs Beres
 */
public abstract class TestCaseEventNode extends MSCNode {
	protected int lifelines;

	public TestCaseEventNode(int eventOccurrence, final int lifelines) {
		super(eventOccurrence);
		this.lifelines = lifelines;
	}

	@Override
	public int getX() {
		return MSCConstants.COLUMN_WIDTH + MSCConstants.COLUMN_SPACING / 2;
	}

	@Override
	public int getWidth() {
		return this.lifelines * MSCConstants.COLUMN_WIDTH - MSCConstants.COLUMN_SPACING + 2 * MSCConstants.TESTCASEEND_SHADOW_SIZE;
	}

	@Override
	protected void draw(final IGC context) {
		Rectangle rectangle = new Rectangle(getX(), getY(), getWidth(), getHeight());

		drawShadow(context, rectangle, getColor(MSCConstants.TESTCASEEND_SHADOW_COLOR), MSCConstants.TESTCASEEND_SHADOW_SIZE);
		drawBox(context, rectangle, getBackgroundColor(), getColor(MSCConstants.DEFAULT_BACKGROUND_COLOR), MSCConstants.TESTCASEEND_SHADOW_SIZE);
		drawBorder(context, rectangle, getColor(MSCConstants.TESTCASEEND_LINE_COLOR), MSCConstants.TESTCASEEND_SHADOW_SIZE);
		drawText(context, rectangle, getNodeText(), (Color) Activator.getDefault().getCachedResource(MSCConstants.TESTCASEEND_FONT_COLOR),
				(Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT), MSCConstants.TESTCASEEND_SHADOW_SIZE);
	}

	protected abstract Color getBackgroundColor();

	@Override
	public int getY() {
		return MSCConstants.ROW_HEIGHT * this.getStartOccurrence() + MSCConstants.ROW_SPACING / 2;
	}

	protected abstract String getNodeText();
}
