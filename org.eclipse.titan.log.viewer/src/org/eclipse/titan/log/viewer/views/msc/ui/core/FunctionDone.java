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
 * Represents a Function done
 *
 */
public class FunctionDone extends FunctionNode {

	@Override
	public Type getType() {
		return Type.FUNCTION_DONE;
	}

	@Override
	public void drawSymbol(final IGC context, final int x, final int y, final int direction) {
		int tempX = x;
		if (direction == RIGHT) {
			tempX = tempX - MSCConstants.MESSAGE_SYMBOL_SIZE;
		}
		
		// Draw the square
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.MESSAGE_LINE_COLOR));
		context.fillRectangle(tempX,
				  			  y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2,
				  			  MSCConstants.MESSAGE_SYMBOL_SIZE, 
				  			  MSCConstants.MESSAGE_SYMBOL_SIZE);
		
		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
		context.fillRectangle(tempX + 2,
	  			  			  y - MSCConstants.MESSAGE_SYMBOL_SIZE / 2 + 2,
	  			  			  MSCConstants.MESSAGE_SYMBOL_SIZE - 4, 
	  			  			  MSCConstants.MESSAGE_SYMBOL_SIZE - 4);
	}

}
