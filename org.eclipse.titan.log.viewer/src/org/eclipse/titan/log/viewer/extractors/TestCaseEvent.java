/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.extractors;

import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * Event used during extraction of test cases from log file 
 *
 */
public class TestCaseEvent {
	private String testCaseName;
	private int progress;

	/**
	 * Constructor
	 */
	public TestCaseEvent() {
		this.testCaseName = null;
		this.progress = 0;
	}
	
	/**
	 * Constructor
	 * 
	 * @param testCaseName the name of the test case
	 * @param progress the progress
	 */
	public TestCaseEvent(final String testCaseName, final int progress) {
		if (Constants.DEBUG) {
			assert testCaseName != null;
			assert (progress >= 0) && (progress <= 100);
 		}
		this.testCaseName = testCaseName;
		this.progress = progress;
	}

	/**
	 * Returns the progress (0 - 100)
	 * @return the progress
	 */
	public int getProgress() {
		return this.progress;
	}

	/**
	 * Sets the progress, which should be within range (0 - 100)
	 * @param progress the progress
	 */
	public void setProgress(final int progress) {
		if (Constants.DEBUG) {
			assert (progress >= 0) && (progress <= 100);
 		}
		this.progress = progress;
	}

	/**
	 * Returns the name of the test case
	 * @return the testCaseName
	 */
	public String getTestCaseName() {
		return this.testCaseName;
	}

	/**
	 * Sets the name of the test case, should not be null
	 * @param testCaseName the name of the test case
	 */
	public void setTestCaseName(final String testCaseName) {
		if (Constants.DEBUG) {
			assert testCaseName != null;
 		}
		this.testCaseName = testCaseName;
	}
}
