/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.readers;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.text.ParseException;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.RecordParser;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * This class is responsible for reading records from the log file
 */
//FIXME make faster with caches.
public class TestFileReader implements Closeable {

	private RandomAccessFile randomAccessFile; 
	private LogRecordIndex[] logRecordIndexes;
	private int currentRecord;
 
	/**
	 * Reads records from a given start offset to a given end offset in a log file
	 * 
	 * @param fileURI the URI of the log file
	 * @param logRecordIndexes the array with the log record indexes
	 * @throws IOException in case of file read/seek errors
	 */
	public TestFileReader(final URI fileURI, final  LogRecordIndex[] logRecordIndexes) throws IOException {
		if ((logRecordIndexes == null) || (logRecordIndexes.length == 0)) {
			throw new IOException("Log record index array is empty!");
		}

		this.currentRecord = 0;
		this.logRecordIndexes = logRecordIndexes;
		long fileOffset = logRecordIndexes[this.currentRecord].getFileOffset();
		this.randomAccessFile = new RandomAccessFile(new File(fileURI), MSCConstants.READ_ONLY);
		this.randomAccessFile.seek(fileOffset);
	}

	public void setCurrentLogRecord(final int currentRecord) throws IOException {
		this.currentRecord = currentRecord;
		long fileOffset = logRecordIndexes[this.currentRecord].getFileOffset();
		this.randomAccessFile.seek(fileOffset);
	}

	/**
	 * Returns true if reader has more records, otherwise false
	 * @return true if reader has more records, otherwise false
	 */
	public boolean hasNextRecord() {
		return this.currentRecord < this.logRecordIndexes.length;
	}

	/**
	 * @return the amount of the logrecord indices
	 * */
	public int size() {
		return logRecordIndexes.length;
	}

	/**
	 * Reads a record
	 * 
	 * REQUIRES that hasNextRecord() is true 
	 * 
	 * @return a record or null
	 * @throws IOException
	 * @throws ParseException
	 */ 
	public LogRecord getNextRecord() throws IOException, ParseException {
		String logData = readNextRecord();
		LogRecord aRecord;
		try {
			RecordParser recordParser = new RecordParser();
			aRecord = recordParser.parse(logData);
			aRecord.setRecordOffset(this.logRecordIndexes[this.currentRecord].getFileOffset());
			aRecord.setRecordLength(this.logRecordIndexes[this.currentRecord].getRecordLength());
			aRecord.setRecordNumber(this.currentRecord);
			this.currentRecord++;
		} catch (ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			ParseException throwable = new ParseException("Could not parse the " + currentRecord +"th record ", 0);  //$NON-NLS-1$
			throwable.initCause(e);
			throw throwable;
		}
		return aRecord;
	}
	
	/**
	 * Cleanup, use clean
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		clean();
	}
	
	/**
	 * Reads next record
	 * @return a record or null
	 * @throws IOException
	 */ 
	private String readNextRecord() throws IOException {
		String s = new String(getNextRecordFromFile());
		return s.trim();
	}

	/**
	 * Read a line from a file into bytes
	 * @return byte array
	 * @throws IOException
	 */
	private byte[] getNextRecordFromFile() throws IOException {
		int nextLen = this.logRecordIndexes[this.currentRecord].getRecordLength();
		byte[] buffer = new byte[nextLen];
		this.randomAccessFile.read(buffer, 0, nextLen);
		return buffer;
	}

	/**
	 * Cleaning up files and buffers
	 */
	private void clean() {
		IOUtils.closeQuietly(randomAccessFile);
		randomAccessFile = null;
	}
}

