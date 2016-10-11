/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.extractors;

/**
 * Holder for ComponentsFetcher events
 *
 */
public class ComponentEvent {

	private String compName;
	private int progress;
	
	/**
	 * Constructor
	 * 
	 * @param compName the name of the component
	 * @param progress the current progress (0 to 100)
	 */
	public ComponentEvent(final String compName, final int progress) {
		this.compName = compName;
		this.progress = progress;
	}

	/**
	 * Returns the component name
	 * @return the component name
	 */
	public String getCompName() {
		return this.compName;
	}

	/**
	 * Returns the current progress
	 * @return the current progress
	 */
	public int getProgress() {
		return this.progress;
	}
}
