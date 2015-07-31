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
 * Represents unmapping of ports
 *
 */
public class PortUnmapping extends PortEventNode {

	/**
	 * Constructor
	 * @param sourcePort the source port name
	 * @param targetPort the target port name
	 */
	public PortUnmapping(final String sourcePort, final String targetPort) {
		super(sourcePort, targetPort);
	}

	@Override
	public Type getType() {
		return Type.PORT_UNMAPPING;
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(drawOuterPolygon(x, y, direction));
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillPolygon(drawInnerPolygon(x, y, direction));
		
		// Draw second symbol
		int tempX = x - getWidth();
		if (direction == RIGHT) {
			tempX = tempX + MSCConstants.MESSAGE_SYMBOL_SIZE;
		} else {
			tempX = tempX - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(drawOuterPolygon(tempX, y, direction));
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillPolygon(drawInnerPolygon(tempX, y, direction));

		setSymbolText(direction);
	}

	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		//Draw polygon at bottom of line
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(drawOuterPolygon(xLeft, yBottom, direction));		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillPolygon(drawInnerPolygon(xLeft, yBottom, direction));
		
		//Draw the polygon at top
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillPolygon(drawOuterPolygon(xLeft, yTop, direction));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillPolygon(drawInnerPolygon(xLeft, yTop, direction));

		setSymbolText(direction);
	}
	
	/**
	 * Set symbolText
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
	 * Draws the inner polygon
	 * @param x
	 * @param y
	 * @param direction
	 * @return
	 */
	private int[] drawInnerPolygon(final int x, final int y, final int direction) {

		// Direction LEFT         Direction RIGHT
		//
		//       P1                    P1
		//       /\                   /\
		//      //\\____         ____//\\
		//   P3 \\// P2           P3 \\// P2
		//       \/                   \/
		//       P4                    P4
		int[] pointsB = {x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE / 2, y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2 + 2,
						 x - direction * (MSCConstants.MESSAGE_SYMBOL_SIZE - 2), y,
						 x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE / 2, y + MSCConstants.MESSAGE_SYMBOL_SIZE / 2 - 2,
						 x - direction * 2, y};
		return pointsB;
	}
	
	/**
	 * Draws the outer Polygon
	 * @param x
	 * @param y
	 * @param direction
	 * @return
	 */
	private int[] drawOuterPolygon(final int x, final int y, final int direction) {
		// Direction LEFT         Direction RIGHT
		//
		//       P1                    P1
		//       / \                   / \
		//      /   \____         ____/   \
		//   P3 \   / P2           P3 \   / P2
		//       \ /                   \ /
		//       P4                    P4		
		int[] pointsA = {x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE / 2, y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
				 x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE, y,
				 x - direction * MSCConstants.MESSAGE_SYMBOL_SIZE / 2, y + MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
				 x, y};
		return pointsA;
	}
}
