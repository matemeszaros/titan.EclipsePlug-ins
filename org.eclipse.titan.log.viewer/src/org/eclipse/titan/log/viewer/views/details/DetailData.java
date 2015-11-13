/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import org.eclipse.titan.common.utils.ObjectUtils;

/**
 * Class for details data  
 *
 */
public class DetailData {

	private String name;
	private String port;
	private String line;
	private String testCaseName;
	private String eventType;
	private String sourceInfo;
	
	/**
	 * Constructor
	 * 
	 * @param name the name
	 * @param port the port
	 * @param line the line (message)
	 * @param testCaseName the test case name
	 */
	public DetailData(final String name, final String port, final String line,
					  final String testCaseName, final String eventType, final String sourceInfo) {
		this.name = name;
		this.port = port;
		this.line = line;
		this.testCaseName = testCaseName;
		this.eventType = eventType;
		this.sourceInfo = sourceInfo;
	}
	
	public DetailData() {
		// Do nothing
	}

	/**
	 * @param line the line (message)
	 */
	public void setLine(final String line) {
		this.line = line;
	}

	/**
	 * @param name the name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param port the port
	 */
	public void setPort(final String port) {
		this.port = port;
	}

	/**
	 * @param testCaseName the name of the test case 
	 */
	public void setTestCase(final String testCaseName) {
		this.testCaseName = testCaseName;
	}
	
	/**
	 * @param eventType the event type
	 */
	public void setEventType(final String eventType) {
		this.eventType = eventType;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return this.port;
	}
	
	/**
	 * @return the line (message)
	 */
	public String getLine() {
		return this.line;
	}
	
	/**
	 * @return the name of the test case
	 */
	public String getTestCaseName() {
		return this.testCaseName;
	}
	
	/**
	 * @return the event type
	 */
	public String getEventType() {
		return this.eventType;
	}

	/**
	 * @param dd the detail data to compare this with
	 * @return true if equal, false if not
	 */
	public boolean isEqualTo(final DetailData dd) {
		return dd != null
				&& ObjectUtils.equals(name, dd.getName())
				&& ObjectUtils.equals(port, dd.getPort())
				&& ObjectUtils.equals(line, dd.getLine())
				&& ObjectUtils.equals(testCaseName, dd.getTestCaseName());
	}

	public String getSourceInfo() {
		return this.sourceInfo;
	}

	public void setSourceInfo(final String sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
}
