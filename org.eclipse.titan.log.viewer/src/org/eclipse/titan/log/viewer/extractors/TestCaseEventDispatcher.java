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
 * TestCaseEvent Dispatcher
 *
 */
public class TestCaseEventDispatcher extends Observable implements Observer {
	
	/**
	 * Constructor
	 */
	public TestCaseEventDispatcher() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(final Observable observer, final Object event) {
		setChanged();
		notifyObservers(event);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Display.getCurrent().readAndDispatch();
			}
		});
	}	
}
