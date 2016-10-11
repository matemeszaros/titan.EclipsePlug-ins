/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.readers;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;

import org.eclipse.titan.log.viewer.parsers.data.LogRecord;

public interface ILogReader extends Closeable {

	/**
	 * Returns the number of record contained by this reader
	 * @return the  number of records
	 */
	int size();

	/**
	 * Reads a log record at a given position. The position is relative to this reader.
	 * 
	 * @param position the position to read the log record from
	 * @return the log record or <code>null</code> if the record does not exist in the reader
	 * @throws IOException in case of file I/O errors
	 * @throws ParseException if the log record can not be parsed
	 */
	LogRecord getRecord(final int position)  throws IOException, ParseException;

	/**
	 * Reads the log record with the given id
	 * 
	 * @param id The id of the record (the position in the log file)
	 * @return the log record or <code>null</code> if the record does not exist in the reader
	 * @throws IOException in case of file I/O errors
	 * @throws ParseException if the log record can not be parsed
	 */
	LogRecord getRecordById(final int id)  throws IOException, ParseException;

	/**
	 * Returns the records position or -1 if the reader does not contain it.
	 * This function assumes that the logRecordIndexes and filteredRecords arrays are sorted.
	 * @param recordNumber The number of the record
	 * @return The position if the reader contains the record. -1 it it does not
	 */
	
	/**
	 * Returns the position of the record identified with the given id.
	 * <br/> e.g.: <code>getRecord(getPositionFromRecordNumber(someIntVariable)) == getRecordById(someIntVatiable)</code> 
	 * @param id The identifier of the record (The position in the log file)
	 * @return The position of the record in this LogReader or -1 if it does not exist
	 */
	int getPositionFromRecordNumber(final int id);

	/**
	 * Returns true if the reader contains the record with the given id.
	 * @param id The id of the record
	 * @return <code>true</code> if the reader contains the record with the given id,
	 *  <code>false</code> otherwise.
	 */
	boolean contains(final int id);
	
	/**
	 * Closes the reader. The reader can not be used, after calling this function.
	 */
	void close();
}
