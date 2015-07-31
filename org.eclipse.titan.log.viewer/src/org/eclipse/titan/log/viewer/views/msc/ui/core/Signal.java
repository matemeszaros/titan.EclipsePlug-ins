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
 * Represents a signal
 *
 */
public abstract class Signal extends BaseMessage {

	protected Signal() {
		super(0);
	}

	@Override
	public Type getType() {
		return Type.SIGNAL;
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		// Draw the arrow
		int[] points = {x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE, y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						x, y,
						x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE, y + MSCConstants.MESSAGE_SYMBOL_SIZE / 2};
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(points);
	}
	
	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		drawSymbol(context, xLeft, yBottom, direction);
	}
}
