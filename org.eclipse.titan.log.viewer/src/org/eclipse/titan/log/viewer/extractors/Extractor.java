/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.extractors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.utils.Constants;

import org.eclipse.titan.common.utils.IOUtils;

/**
 * Abstract class which enables data extraction from a log file 
 *
 */
public abstract class Extractor extends Observable {

	// Variables
	protected byte[] buffer;
	protected long fileSize;
	protected long filePointer;
	protected long lineCounter;
	protected LogFileMetaData logFileMetaData;
	protected int recordNumber;
	
	/**
	 * Constructor 
	 */
	public Extractor() {
		this.buffer = null;
		this.fileSize = 0;
		this.filePointer = 0;
		this.lineCounter = 0;
		this.recordNumber = 0;
	}
	
	/**
	 * Extracts data from a Log File
	 * 
	 * @param logFileMetaData the log file meta data of the log file to extract
	 * @throws IOException if log file not found or error while extracting
	 */
	protected void extractFromLogFile(final LogFileMetaData logFileMetaData, final IProgressMonitor pMonitor) throws IOException {
		IProgressMonitor monitor = pMonitor == null ? new NullProgressMonitor() : pMonitor;
		this.logFileMetaData = logFileMetaData;
		File logFile = new File(logFileMetaData.getFilePath());
		monitor.beginTask(logFile.getName() + ": Extracting testcases.", 100);
		long monitorStep = logFile.length() / 100;
		long monitorNextTick = monitorStep;
		
		FileInputStream in = null;
		try {
			// initialize
			in = new FileInputStream(logFile);
			this.fileSize = logFile.length();
			this.buffer = new byte[Constants.INITIAL_BUFFER_SIZE];
			int nextChar = 0;
			int startLineIdx = 0;

			// read ahead buffer
			int lengthRead = in.read(this.buffer, 0, Constants.INITIAL_BUFFER_SIZE);
			int charsInBuffer = lengthRead;
			
			while (lengthRead != -1) {
				boolean foundRecord = false;
				int recordsInBlock = 0;
				while (nextChar < charsInBuffer) {
					// consumes the next char in the buffer
					byte aChar = this.buffer[nextChar];
					if (aChar == Constants.LF) {
						foundRecord = true;
						this.lineCounter++;
						recordsInBlock++;
						processRow(startLineIdx, nextChar, this.recordNumber);
						this.recordNumber++;
						int recordLength = nextChar - startLineIdx + 1;
						this.filePointer += recordLength; // filePointer
						if (filePointer >= monitorNextTick) {
							monitor.worked(1);
							monitorNextTick += monitorStep;
						}
						startLineIdx = nextChar + 1;
						if (monitor.isCanceled()) {
							recordNumber = 0;
							filePointer = 0;
							return;
						}
					}
					nextChar++;
				} // while
	
				// here we have a block containing no full record
				if (!foundRecord) {
					byte[] oldBuffer = this.buffer;
					// doubles the buffer size
					this.buffer = new byte [this.buffer.length * 2];
					System.arraycopy(oldBuffer, 0, this.buffer, 0, oldBuffer.length);
					lengthRead = in.read(this.buffer, oldBuffer.length,
							this.buffer.length - oldBuffer.length);
					charsInBuffer = oldBuffer.length + lengthRead;
					continue;
				}
				int lefInBlock = 0;
				
				if (startLineIdx < charsInBuffer) {
					lefInBlock = charsInBuffer - startLineIdx;
					System.arraycopy(this.buffer, startLineIdx, this.buffer, 0,
							lefInBlock);
				}
				lengthRead = in.read(this.buffer, lefInBlock, this.buffer.length - lefInBlock);

				if (lengthRead > 0) {
					if (lefInBlock != 0) { // this means that we can continue
						// from where we are
						startLineIdx = 0;
						nextChar = lefInBlock;
					} else {
						nextChar -= charsInBuffer;
						startLineIdx = nextChar;
					}
					charsInBuffer = lefInBlock + lengthRead;
				}
			} // while

			// check here to see if last line is not terminated by LF
			if (startLineIdx < nextChar) {
				this.lineCounter++;
				processRow(startLineIdx, nextChar, this.recordNumber);
				this.recordNumber++;
				// filePointer now points at EOF
				this.filePointer += (nextChar - startLineIdx);
				monitor.done();
			}

			processRowsFinished(startLineIdx, nextChar, this.recordNumber);
		} finally {
			this.buffer = null;
			IOUtils.closeQuietly(in);
		}
	}

	protected abstract void processRow(int offsetStart, int offsetEnd, int recordNumber) throws IOException;
	protected abstract void processRowsFinished(int offsetStart, int offsetEnd, int recordNumber) throws IOException;
	
	/**
	 * Check to see if valid timestamp is next in the file
	 * 
	 * @param nextChar pointer in the buffer
	 * @param remaining number of bytes in the buffer
	 * @return true if a valid timestamp otherwise false
	 */
	protected boolean hasValidTimeStamp(final int nextChar, final int remaining) {
		if (!Character.isDigit(this.buffer[nextChar])) {
			return false;
		}

		switch (this.logFileMetaData.getTimeStampConstant()) {
		case Constants.DATETIME_FORMAT_LENGTH:
			if (remaining >= Constants.DATETIME_FORMAT_LENGTH) {
				String time = new String(this.buffer, nextChar, Constants.DATETIME_FORMAT_LENGTH);
				return Pattern.matches(Constants.REGEXP_DATETIME_FORMAT, time);

			}
			break;
		case Constants.TIME_FORMAT_LENGTH:
			if (remaining >= Constants.TIME_FORMAT_LENGTH) {
				String time = new String(this.buffer, nextChar, Constants.TIME_FORMAT_LENGTH);
				return Pattern.matches(Constants.REGEXP_TIME_FORMAT, time);

			}
			break;
		case Constants.SECONDS_FORMAT_LENGTH:
		default:
			// this is the min length of seconds. It can be longer
			// so a special handling must be performed.
			if (remaining >= Constants.SECONDS_FORMAT_LENGTH) {
				boolean foundWhiteSpace = false;
				int endPos = nextChar;
				int lastPos = nextChar + remaining;
				while (endPos < lastPos) {
					if (Character.isWhitespace(this.buffer[endPos])) {
						foundWhiteSpace = true;
						break;
					}
					endPos++;
				}
				if (foundWhiteSpace) {
					String seconds = new String(this.buffer, nextChar, endPos - nextChar);
					if (Pattern.matches(Constants.REGEXP_SECONDS_FORMAT, seconds)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected int findPos(final char[] ser, final int start, final int stop) {
		int matchPos = 0;
		for (int ix = start; ix < stop; ix++) {
			if (this.buffer[ix] == ser[matchPos]) {
				matchPos++;
				if (matchPos == ser.length) {
					return ix - matchPos;
				}
			} else {
				matchPos = 0;
			}
		}
		return -1;
	}

	protected boolean contains(final char[] ser, final int start, final int stop) {
		int matchPos = 0;
		for (int ix = start; ix < stop; ix++) {
			if (this.buffer[ix] == ser[matchPos]) {
				matchPos++;
				if (matchPos == ser.length) {
					return true;
				}
			} else {
				matchPos = 0;
			}
		}
		return false;
	}
}
