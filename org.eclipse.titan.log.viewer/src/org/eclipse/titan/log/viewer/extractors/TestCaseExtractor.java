/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.extractors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.console.TITANDebugConsole;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;

/**
 * This class creates an index of all included Test Cases
 * - For each test case there is a index created for each record containing the file offset and the length of the record
 * 
 * There is a distinction between a record and a line
 * - A line a array of bytes that is terminated with a LF
 * - A record begins with valid time stamp and is terminate with a LF followed by a timestamp or EOF
 * 		- A record can consists of one or several lines 
 */
public class TestCaseExtractor extends Extractor {
	private IFile logFile;
	private List<LogRecordIndex> logRecordIndexVector;
	private List<TestCase> testCaseVector;
	private TestCase currentTestCase;
	private int currentProgress;
	private boolean withinTestCase;
	private int currentTestCaseNumber;
	private int endRecordNumber;
	private LogRecordIndex lastRecordIndex;
	private boolean optionSet = false;
	private boolean crashed = false;

	private int currentControlPartNumber = 1;
	private boolean firstActivation = true;
	
	public TestCaseExtractor() {
		this.withinTestCase = false;
		this.currentTestCaseNumber = 0;
		this.endRecordNumber = 0;
		this.testCaseVector = new ArrayList<TestCase>();
		this.logRecordIndexVector = new ArrayList<LogRecordIndex>();
	}

	/**
	 * Extracts test cases from a log file which already has a previously created index file
	 * 
	 * @param logFile the log file to extract test cases from, can NOT be null
	 * @throws IOException if file IO or parse errors occur
	 * @throws ClassNotFoundException if test cases can not be read from the index file
	 */
	public void extractTestCasesFromIndexedLogFile(final IFile logFile) throws IOException, ClassNotFoundException {
		this.logFile = logFile;
		File indexFile = LogFileCacheHandler.getIndexFileForLogFile(logFile);
		if (indexFile.length() == 0) {
			throw new IOException();
		}
		
		ObjectInputStream indexFileInputStream = null;
		try {
			indexFileInputStream = new ObjectInputStream(new FileInputStream(indexFile));
			Object o = indexFileInputStream.readObject();
			if (o instanceof List) {
				List<?> testCases = (List<?>) o;
				double sizeFactor = 100.0 / testCases.size();
				int i = 0;
				for (Object testCase : testCases) {
					i++;
					if (testCase instanceof TestCase) {
						this.currentTestCase = (TestCase) testCase;
						this.currentProgress = (int) (sizeFactor * i);
						addTestCase();
					} else {
						throw new ClassNotFoundException("Class not found!"); //$NON-NLS-1$
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(indexFileInputStream);
		}
	}
	
	
	/**
	 * Fetches a given test case with a passed id from a previously created index file 
	 * 
	 * @param indexFile the index file to extract test cases from, can NOT be null  
	 * @param testCaseNumber number of the test case to fetch
	 * @throws IOException if file IO or parse errors occur
	 * @throws ClassNotFoundException if test cases can not be read from the index file
	 */
	
	public static TestCase getTestCaseFromIndexFile(final File indexFile, final int testCaseNumber)
			throws IOException, TechnicalException, ClassNotFoundException {
		
		ObjectInputStream indexFileInputStream = null;
		try {
			indexFileInputStream = new ObjectInputStream(new FileInputStream(indexFile));
			
			Object o = indexFileInputStream.readObject();
			if (o instanceof List) {
				List<?> testCases = (List<?>) o;
				
				// the vector is zero based but the testCase number starts numbering on 1
				// so there must be an alignment
				int testCasePosition = testCaseNumber - 1;
				
				if (testCases.size() < testCasePosition) {
					throw new TechnicalException(Messages.getString("TestCaseExtractor.4")); //$NON-NLS-1$
				}
				return (TestCase) testCases.get(testCasePosition);
			}
		} finally {
			IOUtils.closeQuietly(indexFileInputStream);
		}
		return null;
	}
	
	/**
	 * Extracts Test Cases from a Log File
	 * @param logFileMetaData meta data about the log file
	 * @throws IOException if log file not found or error while extracting
	 */
	public void extractTestCasesFromLogFile(final LogFileMetaData logFileMetaData, final IProgressMonitor pMonitor) throws IOException {
		IProgressMonitor monitor = pMonitor == null ? new NullProgressMonitor() : pMonitor;
		this.logFile = logFileMetaData.getLogfile();
		if (logFile == null) {
			throw new IOException("Log file not found.");
		}

		extractFromLogFile(logFileMetaData, monitor);
		if (monitor.isCanceled()) {
			testCaseVector = null;
			logRecordIndexVector = null;
			crashed = true;
			return;
		}
		if (this.crashed) {
			if (!testCaseVector.isEmpty()) {
				testCaseVector.get(testCaseVector.size() - 1).setEndRecordNumber(this.endRecordNumber);
			}
		} else if (this.withinTestCase) {
			// Last test case finished missing!
			this.crashed = true;
			this.testCaseVector.add(new TestCase(logFile, -1, Messages.getString("TestCaseExtractor.3"),
					this.filePointer, Constants.VERDICT_CRASHED, this.currentTestCase.getStartRecordNumber(), this.endRecordNumber));
		}
		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("Total line " + this.lineCounter); //$NON-NLS-1$
			TITANDebugConsole.getConsole().newMessageStream().println("Total record " + this.logRecordIndexVector.size()); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the test case vector created during extraction of the log file
	 * 
	 * @return the test case vector
	 */
	public List<TestCase> getTestCases() {
		return this.testCaseVector;
	}
	
	/**
	 * Returns the log record index vector created during extraction of the log file
	 * 
	 * @return the log record index vector
	 */
	public List<LogRecordIndex> getLogRecordIndexes() {
		return this.logRecordIndexVector;
	}
	
	/**
	 * Returns a boolean flag which indicates if extraction did failed or not
	 * Should be called AFTER calling extractTestCasesFromLogFile 
	 * 
	 * @return true if failed otherwise true
	 */
	public boolean failedDuringExtraction() {
		return this.crashed;
	}

	//FIXME implement a better testcase detection
	@Override
	protected void processRow(final int offsetStart, final int offsetEnd, final int recordNumber) throws IOException {
		// Add log record to index
		addLogRecordIndex(this.filePointer, offsetStart, offsetEnd, recordNumber);
		
		// Check if the R7B header exists in the log file.
		if (!this.optionSet && contains(Constants.LOG_FORMAT, offsetStart, offsetEnd)) {
			int startPos = findPos(Constants.LOG_FORMAT_OPTION, offsetStart, offsetEnd);
			startPos = startPos + Constants.LOG_FORMAT_OPTION.length + 1;
			String option = new String(this.buffer, startPos, offsetEnd - startPos);
			this.logFileMetaData.setOption(option);
			this.logFileMetaData.setFileFormat(Constants.FILEFORMAT_2);
			this.optionSet = true;
		}

		if (firstActivation) {
			firstActivation = false;
			createTestcase(this.logFile, "controlpart 1", recordNumber);
			currentControlPartNumber++;
			withinTestCase = false;
		}

		if (this.crashed) {
			int newProgress =  (int) (this.filePointer * (100.0 / this.fileSize));
			if (newProgress > this.currentProgress) {
				this.currentProgress = newProgress;
				setChanged();
				notifyObservers(new TestCaseEvent("", this.currentProgress)); //$NON-NLS-1$
			}
			return;
		}
		int tcPos = findPos(Constants.TEST_CASE, offsetStart, offsetEnd);
		if (tcPos == -1) {
			return;
		}
		// If test case finished
		if (contains(Constants.TEST_CASE_FINISHED, offsetStart, offsetEnd)) {

			// If start has not been found
			if (!this.withinTestCase) {
				this.currentTestCase = null;
				addCrashedTestCase();
				return;
			}

			// Search for test case finished position
			int testCaseEndPos = findPos(Constants.TEST_CASE_FINISHED, offsetStart, offsetEnd);
			if ((this.currentTestCase == null) || !this.currentTestCase.getTestCaseName().contentEquals(
					new String(this.buffer, tcPos + Constants.TEST_CASE.length + 2,
							testCaseEndPos - tcPos - Constants.TEST_CASE.length - 2))) {
				addCrashedTestCase();
				return;
			}

			this.withinTestCase = false;
			int verdictPos = findPos(Constants.VERDICT, offsetStart, offsetEnd);
			String verdict;
			if (verdictPos != -1) {
				int off = verdictPos + Constants.VERDICT.length + 2;
				verdict = new String(this.buffer, off, offsetEnd - off);
				final int spacePos = verdict.indexOf(' '); // In case there is a verdict reason e.g.: "Verdict: inconc reason: SUT Response guard timer timed out"
				if (spacePos > 0) {
					verdict = verdict.substring(0, spacePos);
				}
				this.currentTestCase.setVerdict(getVerdict(verdict));
			}
			this.currentTestCase.setEndRecordNumber(recordNumber);
			addTestCase();

			createTestcase(this.logFile, "controlpart " + currentControlPartNumber, recordNumber + 1);
			currentControlPartNumber++;
			withinTestCase = false;
			return;
		}

		// Search for test case started position
		int tcPosEnd = findPos(Constants.TEST_CASE_STARTED, offsetStart, offsetEnd);
		if (tcPosEnd != -1) {

			// If end has not been found
			if (this.withinTestCase) {
				addCrashedTestCase();
				return;
			}

			if (this.currentTestCase.getStartRecordNumber() >= recordNumber - 1 && !withinTestCase) {
				currentControlPartNumber--;
				currentTestCaseNumber--;
			} else {
				this.currentTestCase.setEndRecordNumber(recordNumber - 1);
				addTestCase();
			}

			this.withinTestCase = true;
			tcPos += 2;
			createTestcase(this.logFile, new String(this.buffer,
					tcPos + Constants.TEST_CASE_STARTED.length + 2,
					tcPosEnd - tcPos - Constants.TEST_CASE.length), recordNumber);

			return;
		}
	}

	@Override
	protected void processRowsFinished(final int offsetStart, final int offsetEnd, final int recordNumber) throws IOException {
		if (this.currentTestCase != null) {
			this.currentTestCase.setEndRecordNumber(recordNumber - 1);
			addTestCase();
		}
	}

	private void createTestcase(final IFile logFile, final String name, final int recordNumber) {
		this.currentTestCaseNumber++;
		this.currentTestCase = new TestCase(logFile);
		this.currentTestCase.setTestCaseNumber(this.currentTestCaseNumber);
		
		this.currentProgress = (int) (this.filePointer * (100.0 / this.fileSize));
		this.currentTestCase.setTestCaseName(name);

		this.currentTestCase.setFilePointer(this.filePointer);
		this.currentTestCase.setStartRecordNumber(recordNumber);
	}

	/**
	 * Adds a test case to the test case vector and notifies the observers 
	 */
	private void addTestCase() {
		String message = ""; //$NON-NLS-1$
		if (!this.crashed) {
			this.testCaseVector.add(this.currentTestCase);
			message = this.currentTestCase.getTestCaseName();
		}
		setChanged();
		
		final String fMessage = message;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				notifyObservers(new TestCaseEvent(fMessage, currentProgress));
			}
		});
	}
	
	private void addCrashedTestCase() {
		if (!this.crashed) {
			this.crashed = true;
			this.testCaseVector.add(
					new TestCase(logFile, -1,
							Messages.getString("TestCaseExtractor.3"),
							this.filePointer, Constants.VERDICT_CRASHED,
							this.recordNumber, this.recordNumber));
		}
	}

	private void addLogRecordIndex(final long filePointer, final int offsetStart, final int offsetEnd, final int recordNumber) {
		int recordLength = offsetEnd - offsetStart + 1;
		this.endRecordNumber = recordNumber;

		// Check if current line contains a valid time-stamp, if not the record continues...
		if (hasValidTimeStamp(offsetStart, recordLength)) {
			if (Constants.DEBUG) {
				if (!Character.isDigit(this.buffer[offsetStart])) {
					TITANDebugConsole.getConsole().newMessageStream().println("--Faulty " + this.buffer[offsetStart]); //$NON-NLS-1$
				}
				if (this.buffer[offsetEnd] != '\n') {
					TITANDebugConsole.getConsole().newMessageStream().println("><<Faulty " + this.buffer[offsetEnd]); //$NON-NLS-1$
				}
			}
			// Time-stamp found, create new log record index
			LogRecordIndex currentLogRecordIndex = new LogRecordIndex(filePointer, recordLength, recordNumber);
			this.logRecordIndexVector.add(currentLogRecordIndex);
			this.lastRecordIndex = currentLogRecordIndex; // keep the last found record
		} else {
			if (this.lastRecordIndex != null) {
				this.lastRecordIndex.addRecordLen(recordLength);
				this.recordNumber--;
			}
		}
	}

	private int getVerdict(final String verdict) {
		Integer verdictConstant = Constants.TEST_CASE_VERDICTS.get(verdict.trim());
		if (verdictConstant != null) {
			return verdictConstant;
		}

		return Constants.VERDICT_NONE;
	}
}
