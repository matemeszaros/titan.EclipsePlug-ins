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
 * Represents disconnection of ports
 *
 */
public class PortDisconnection extends PortEventNode {

	/**
	 * Constructor
	 * @param sourcePort the source port name
	 * @param targetPort the target port name
	 */
	public PortDisconnection(final String sourcePort, final String targetPort) {
		super(sourcePort, targetPort);
	}

	@Override
	public Type getType() {
		return Type.PORT_DISCONNECTED;
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		int tempX = x;
		// Draw the circle
		if (direction == RIGHT) {
			tempX = tempX - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillOval(tempX,
						 y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 MSCConstants.MESSAGE_SYMBOL_SIZE, 
						 MSCConstants.MESSAGE_SYMBOL_SIZE);
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillOval(tempX + 2,
				 		 y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2 + 2,
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4, 
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4);

		// Draw circle of other end
		tempX = tempX - getWidth();
		if (direction == RIGHT) {
			tempX = tempX + MSCConstants.MESSAGE_SYMBOL_SIZE;
		} else {
			tempX = tempX - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillOval(tempX,
						 y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 MSCConstants.MESSAGE_SYMBOL_SIZE, 
						 MSCConstants.MESSAGE_SYMBOL_SIZE);
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillOval(tempX + 2,
				 		 y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2 + 2,
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4, 
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4);
		
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
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		int tempXLeft = xLeft;
		// Draw the circle
		if (direction == RIGHT) {
			tempXLeft = tempXLeft - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		context.setForeground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillOval(tempXLeft,
						 yBottom - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 MSCConstants.MESSAGE_SYMBOL_SIZE, 
						 MSCConstants.MESSAGE_SYMBOL_SIZE);
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillOval(tempXLeft + 2,
				 		 yBottom - MSCConstants.MESSAGE_SYMBOL_SIZE / 2 + 2,
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4, 
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4);

		//Draw symbol at top 
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillOval(tempXLeft,
						 yTop - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
						 MSCConstants.MESSAGE_SYMBOL_SIZE, 
						 MSCConstants.MESSAGE_SYMBOL_SIZE);
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillOval(tempXLeft + 2,
				 		 yTop - MSCConstants.MESSAGE_SYMBOL_SIZE / 2 + 2,
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4, 
				 		 MSCConstants.MESSAGE_SYMBOL_SIZE - 4);
		
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
