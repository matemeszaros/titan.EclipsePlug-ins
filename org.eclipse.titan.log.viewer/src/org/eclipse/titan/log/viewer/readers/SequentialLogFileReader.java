/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.readers;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.RecordParser;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;

public class SequentialLogFileReader implements Closeable {

	private File logFile;
	private DataInputStream inputStream;
	private LogRecordIndex[] logRecordIndexes;
	private RecordParser recordParser;
 
	private int currentRecord = -1;

	/**
	 * Reads records from a given start offset to a given end offset in a log file
	 * 
	 * @param fileURI the log file URI
	 * @param logRecordIndexes the array with the log record indexes
	 * @throws IOException in case of file read/seek errors
	 */
	public SequentialLogFileReader(final URI fileURI, final LogRecordIndex[] logRecordIndexes) throws IOException {
		if ((logRecordIndexes == null) || (logRecordIndexes.length == 0)) {
			throw new IOException("Empty log record index!");  //$NON-NLS-1$
		}
		this.logRecordIndexes = logRecordIndexes;
		logFile = new File(fileURI);
		inputStream = new DataInputStream(new FileInputStream(logFile));
		recordParser = new RecordParser();
		currentRecord = -1;
	}
	
	public int size() {
		return this.logRecordIndexes.length;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(inputStream);
	}
	
	/**
	 * Reads a record
	 * @return The record
	 * @throws IOException
	 */
	private String readRecord() throws IOException {
		int nextLen = this.logRecordIndexes[currentRecord].getRecordLength();
		byte[] buffer = new byte[nextLen];
		inputStream.read(buffer);
		String result = new String(buffer);
		return result.trim();
	}
	
	public boolean hasNext() {
		return currentRecord < logRecordIndexes.length - 1;
	}
	
	/**
	 * Returns the next log record or null if the reader is at the end of the file.
	 * @return The parsed log record
	 * @throws ParseException
	 * @throws IOException
	 */
	public LogRecord getNext() throws ParseException, IOException {
		if (!hasNext()) {
			return null;
		}
		
		currentRecord++;
		LogRecord result = recordParser.parse(readRecord());
		result.setRecordOffset(logRecordIndexes[currentRecord].getFileOffset());
		result.setRecordLength(logRecordIndexes[currentRecord].getRecordLength());
		result.setRecordNumber(logRecordIndexes[currentRecord].getRecordNumber());
		return result;
	}
}
