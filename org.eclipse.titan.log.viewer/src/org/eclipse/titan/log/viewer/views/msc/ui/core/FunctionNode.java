/*******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;

/**
 * Created by Szabolcs Beres
 */
public abstract class FunctionNode extends BaseMessage {

	public FunctionNode() {
		super(0);
	}

	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		drawSymbol(context, xLeft, yBottom, direction);
	}
}
