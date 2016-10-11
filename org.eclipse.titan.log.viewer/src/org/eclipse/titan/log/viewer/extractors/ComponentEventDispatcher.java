/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.extractors;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Display;

/**
 * ComponentEvent Dispatcher
 *
 */
public class ComponentEventDispatcher extends Observable implements Observer {
	
	/**
	 * Constructor
	 */
	public ComponentEventDispatcher() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(final Observable observer, final Object event) {
		setChanged();
		notifyObservers(event);
		Display.getCurrent().readAndDispatch();
	}	
}
