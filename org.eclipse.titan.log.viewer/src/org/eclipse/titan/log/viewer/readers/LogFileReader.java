/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.readers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.text.ParseException;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.parsers.RecordParser;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * This class is able to read and parse log records from a log file.
 */
public class LogFileReader implements ILogReader {

	private RandomAccessFile randomAccessFile;
	private LogRecordIndex[] logRecordIndexes;

	private RecordParser recordParser;

	public LogFileReader(final URI fileURI, final LogRecordIndex[] logRecordIndexes) throws IOException {
		if ((logRecordIndexes == null) || (logRecordIndexes.length == 0)) {
			throw new IOException("Empty log record index!");
		}
		this.logRecordIndexes = logRecordIndexes;
		this.randomAccessFile = new RandomAccessFile(new File(fileURI), MSCConstants.READ_ONLY);
		recordParser = new RecordParser();
	}

	/**
	 * Factory method. Creates a LogFileReader for the given log file.
	 * @param logFile The log file
	 * @return The created reader
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static LogFileReader getReaderForLogFile(final IFile logFile) throws IOException, ClassNotFoundException {
		File logRecordIndexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);
		int numRecords = LogFileCacheHandler.getNumberOfLogRecordIndexes(logRecordIndexFile);
		LogRecordIndex[] logRecordIndexes = LogFileCacheHandler.readLogRecordIndexFile(logRecordIndexFile, 0, numRecords);

		return new LogFileReader(logFile.getLocationURI(), logRecordIndexes);
	}

	@Override
	public LogRecord getRecord(final int position) throws IOException, ParseException {
		LogRecord aRecord;
		try {
			aRecord = recordParser.parse(readRecord(position));
			aRecord.setRecordOffset(logRecordIndexes[position].getFileOffset());
			aRecord.setRecordLength(logRecordIndexes[position].getRecordLength());
			aRecord.setRecordNumber(logRecordIndexes[position].getRecordNumber());
		} catch (ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			ParseException throwable = new ParseException(Messages.getString("TestFileReader.1"), 0);  //$NON-NLS-1$
			throwable.initCause(e);
			throw throwable;
		}
		return aRecord;
	}

	/**
	 * Reads a record
	 * @param index The index of the record. (0 <= index < this.size())
	 */
	private String readRecord(final int index) throws IOException {
		String s = new String(readRecordFromFile(index));
		s = s.trim();
		return s;
	}

	/**
	 * Reads a record from a file into bytes
	 * @param index The index of the record. (0 <= index < this.size())
	 */
	private byte[] readRecordFromFile(final int index) throws IOException {
		int nextLen = this.logRecordIndexes[index].getRecordLength();
		byte[] buffer = new byte[nextLen];
		randomAccessFile.seek(logRecordIndexes[index].getFileOffset());
		this.randomAccessFile.read(buffer, 0, nextLen);
		return buffer;
	}

	@Override
	public int size() {
		return this.logRecordIndexes.length;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(randomAccessFile);
		this.randomAccessFile = null;
	}

	@Override
	public int getPositionFromRecordNumber(final int id) {
		return id;
	}

	@Override
	public LogRecord getRecordById(final int id) throws IOException, ParseException {
		return getRecord(id);
	}

	@Override
	public boolean contains(final int id) {
		return id >= 0 && id < size();
	}
}
