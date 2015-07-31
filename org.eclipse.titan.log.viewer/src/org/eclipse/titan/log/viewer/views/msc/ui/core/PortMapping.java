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
 * Represents a mapping of ports
 *
 */
public class PortMapping extends PortEventNode {

	/**
	 * Constructor
	 * @param sourcePort the source port name
	 * @param targetPort the target port name
	 */
	public PortMapping(final String sourcePort, final String targetPort) {
		super(sourcePort, targetPort);
	}

	@Override
	public Type getType() {
		return Type.PORT_MAPPING;
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(drawPolygon(x, y, direction));
		
		int tempX = x - getWidth();
		if (direction == RIGHT) {
			tempX = tempX + MSCConstants.MESSAGE_SYMBOL_SIZE;
		} else {
			tempX = tempX - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		
		context.fillPolygon(drawPolygon(tempX, y, direction));
		setSymbolText(direction);
	}

	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(drawPolygon(xLeft, yBottom, direction));
		context.fillPolygon(drawPolygon(xLeft, yTop, direction));		
		setSymbolText(direction);
	}
	
	/** 
	 * Sets the text of the symbol
	 * @param direction
	 */
	private void setSymbolText(final int direction) {
		// Prepare text
		if (this.sourcePort.contentEquals(this.targetPort)) {
			setName(this.sourcePort);
		} else if (direction == RIGHT) {
			setName(this.sourcePort + MSCConstants.ARROW_RIGHT + this.targetPort);
		} else { // direction == LEFT
			setName(this.targetPort + MSCConstants.ARROW_LEFT + this.sourcePort);
		}
	}

	/**
	 * Draws the Polygon
	 * @param x
	 * @param y
	 * @param direction
	 * @return
	 */
	private int[] drawPolygon(final int x, final int y, final int direction) {
		
		// Direction LEFT         Direction RIGHT
		//
		//       P1                    P1
		//       / \                   / \
		//      /   \____         ____/   \
		//   P3 \   / P2           P3 \   / P2
		//       \ /                   \ /
		//       P4                    P4

		int[] points = {x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE / 2, y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE, y,
						 x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE / 2, y + MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 x, y};
		return points;
	}
	
}
