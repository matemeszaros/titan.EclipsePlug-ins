/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
 * Represents a connection of ports
 *
 */
public class PortConnection extends PortEventNode {

	/**
	 * Constructor
	 * @param sourcePort the source port name
	 * @param targetPort the target port name
	 */
	public PortConnection(final String sourcePort, final String targetPort) {
		super(sourcePort, targetPort);
	}

	@Override
	public Type getType() {
		return Type.PORT_CONNECTION;
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		// Draw the circle
		int tempX = x;
		if (direction == RIGHT) {
			tempX = x - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillOval(tempX,
						 y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 MSCConstants.MESSAGE_SYMBOL_SIZE, 
						 MSCConstants.MESSAGE_SYMBOL_SIZE);

		
		// Draw circle of other end
		tempX = tempX - getWidth();
		if (direction == RIGHT) {
			tempX = tempX + MSCConstants.MESSAGE_SYMBOL_SIZE;
		} else {
			tempX = tempX - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.fillOval(tempX,
				 		 y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE, 
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE);
		

		// Prepare text
		if (this.sourcePort.contentEquals(this.targetPort)) {
			setName(this.sourcePort);
		} else if (direction == RIGHT) {
			setName(this.sourcePort + MSCConstants.ARROW_RIGHT + this.targetPort);
		} else { // direction == LEFT
			setName(this.targetPort + MSCConstants.ARROW_LEFT + this.sourcePort);
		}
	}

	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight,  final int yTop, final int yBottom, final int direction) {
		// Draw the circle
		int tempX = xLeft;
		if (direction == RIGHT) {
			tempX = xLeft - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillOval(tempX,
						 yTop - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 MSCConstants.MESSAGE_SYMBOL_SIZE, 
						 MSCConstants.MESSAGE_SYMBOL_SIZE);

		//Draw a filled circle at top of line
		context.fillOval(tempX,
				 		 yBottom - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE, 
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE);

		// Prepare text
		if (this.sourcePort.contentEquals(this.targetPort)) {
			setName(this.sourcePort);
		} else if (direction == RIGHT) {
			setName(this.sourcePort + MSCConstants.ARROW_RIGHT + this.targetPort);
		} else { // direction == LEFT
			setName(this.targetPort + MSCConstants.ARROW_LEFT + this.sourcePort);
		}
	}
}
