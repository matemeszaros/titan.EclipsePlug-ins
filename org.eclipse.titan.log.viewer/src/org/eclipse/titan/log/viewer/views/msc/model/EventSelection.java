/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.model;

import org.eclipse.jface.viewers.ISelection;

/**
 * This class represents an event selection in the MSC View
 *
 */
public class EventSelection implements ISelection {

	private EventObject eventObject = null;
	private String currTestCase = null;
	
	/**
	 * Constructor 
	 */
	public EventSelection(final EventObject eventObject, final String currTestCase) {
		this.eventObject = eventObject;
		this.currTestCase = currTestCase;
	}
	
	@Override
	public boolean isEmpty() {
		return this.eventObject == null;
	}
	
	/**
	 * Returns the event object
	 * @return the event object (which can be null)
	 */
	public EventObject getEventObject() {
		return this.eventObject;
	}
	
	/**
	 * Returns the test case name
	 * @return the test case name
	 */
	public String getTestCaseName() {
		return this.currTestCase;
	}

}
