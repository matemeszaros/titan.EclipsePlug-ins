/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.data;

import java.io.Serializable;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * This class represent a TITAN test case
 *
 */
public class TestCase implements Serializable {
	
	private static final long serialVersionUID = -4142445231973786330L;
	
	private int testCaseNumber;
	private String testCaseName;
	private long filePointer;
	private int verdict;
	private int startRecordNumber;
	private int endRecordNumber; 
	private URI logFileURI;
	private transient IFile logFile;

	/**
	 * Test Case
	 * 
	 * Note: This constructor is only required for jUnit test!
	 * 
	 * @param logFile			the log file containing the test case
	 * @param testCaseNumber	the number of the test case
	 * @param testCaseName 		the name of the test case
	 * @param filePointer 		the file pointer
	 * @param verdict 			the verdict
	 * @throws Exception 
	 */
	public TestCase(final IFile logFile,
					final int testCaseNumber,
					final String testCaseName,
			        final long filePointer,
			        final int verdict,
			        final int startRecordNumber,
			        final int endRecordNumber) {
		if (Constants.DEBUG) {
			assert logFile != null;
			assert testCaseName != null;
			assert testCaseName.length() > 0;
		}
		this.logFile = logFile;
		this.logFileURI = logFile.getLocationURI();
		this.testCaseNumber = testCaseNumber;
		this.testCaseName = testCaseName;
		this.filePointer = filePointer;
		this.verdict = verdict;
		this.startRecordNumber = startRecordNumber;
		this.endRecordNumber = endRecordNumber;
	}
	
	/**
	 * The TestCase constructor 
	 */
	public TestCase(final IFile logFile) {
		this.logFile = logFile;
		this.logFileURI = logFile.getLocationURI();
	}

	/**
	 * Get the log file that contains this test case
	 * @return the log file
	 */
	public IFile getLogFile() {
		if (logFile != null) {
			return logFile;
		}

		IFile[] logFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(logFileURI);
		if (logFiles.length > 0) {
			logFile = logFiles[0];
		}

		return logFile;
	}
	
	/**
	 * Set the test case number
	 * @param testCaseNumber the test case number
	 */
	public void setTestCaseNumber(final int testCaseNumber) {
		this.testCaseNumber = testCaseNumber;
	}
	
	/**
	 * Get the test case number
	 * @return the test case number
	 */
	public int getTestCaseNumber() {
		return this.testCaseNumber;
	}
	
	/**
	 * Set the test case name
	 * @param testCaseName the test case name
	 */
	public void setTestCaseName(final String testCaseName) {
		if (Constants.DEBUG) {
			assert testCaseName != null;
			assert testCaseName.length() > 0;
		}
		this.testCaseName = testCaseName;
	}
	
	/**
	 * Get the test case name
	 * @return the test case name
	 */
	public String getTestCaseName() {
		return this.testCaseName;
	}
	
	/**
	 * Set the file pointer
	 * @param filePointer the file pointer
	 */
	public void setFilePointer(final long filePointer) {
		this.filePointer = filePointer;
	}
	
	/**
	 * Get the file pointer
	 * @return the file pointer
	 */
	public long getFilePointer() {
		return this.filePointer;
	}
	
	/**
	 * Get the number of rows
	 * @return the number of rows
	 */
	public int getNumberOfRecords() {
		return this.endRecordNumber - this.startRecordNumber + 1;
	}
	 
	/**
	 * Set the test case verdict
	 * @param verdict the test case verdict
	 */
	public void setVerdict(final int verdict) {
		this.verdict = verdict;
	}
	
	/**
	 * Get the test case verdict
	 * @return the test case verdict
	 */
	public int getVerdict() {
		return this.verdict;
	}

	/**
	 * Gets the start record number
	 * @return the start record number
	 */
	public int getStartRecordNumber() {
		return this.startRecordNumber;
	}

	/**
	 * Sets the start record number
	 * @param startRecordNumber the start record number
	 */
	public void setStartRecordNumber(final int startRecordNumber) {
		this.startRecordNumber = startRecordNumber;
	}

	/**
	 * Gets the end record number
	 * @return the end record number
	 */
	public int getEndRecordNumber() {
		return this.endRecordNumber;
	}

	/**
	 * Sets the end record number
	 * @param startRecordNumber the end record number
	 */
	public void setEndRecordNumber(final int stopRecordNumber) {
		this.endRecordNumber = stopRecordNumber;
	}
	
	/**
	 * Returns true if the test case is a control part.
	 * This function depends on {@link testCaseName}. The test case name of a control part is 
	 * in "controlpart n" format where n is a number (n >= 1).
	 * @return true if the test case represents a control part.
	 */
	// FIXME there should be a better way than this, but that might need some restructuring.
	public boolean isControlPart() {
		int indexOfSpace = "controlpart".length();
		return testCaseName != null && testCaseName.length() > indexOfSpace 
				&& testCaseName.charAt(indexOfSpace) == ' ';
	}
}
