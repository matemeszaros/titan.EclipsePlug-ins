/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.readers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.Assert;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;

/**
 * This class is responsible for reading records from the log file
 * It also holds a cache to minimize file access and increase performance
 */
public class CachedLogReader implements ILogReader {

	private final ILogReader logReader;

	private LogRecord[] cachedRecords = null;
	private int minIndex = 0;
	private int maxIndex = 0;

	public CachedLogReader(final ILogReader logReader) throws IOException {
		Assert.notNull(logReader, "logReader should not be null");
		this.logReader = logReader;
	}

	/**
	 * A preemptive method to cache a sequence of records which will be needed soon.
	 * Calling this before the actual read allows us to read into our cache lots of records,
	 * that will soon be need, with a single read (single access to the hard drive).
	 * */
	public void cacheRecords(final int minIndex, final int maxIndex) throws IOException, ParseException {
		if (isCached(minIndex, maxIndex)) {
			return;
		}

		int tempMin = minIndex;
		int tempMax = maxIndex;
		if (tempMax >= size()) {
			tempMax = size() - 1;
		}
		LogRecord[] newRecords = new LogRecord[tempMax - tempMin + 1];

		if (minIndex > this.minIndex) {
			if (minIndex >= this.maxIndex) {
				tempMin = minIndex;
			} else {
				final int temp = tempMin - this.minIndex;
				System.arraycopy(cachedRecords, temp, newRecords, 0, cachedRecords.length - temp);
				tempMin = this.maxIndex + 1;
			}
		} else if (maxIndex < this.maxIndex) {
			if (maxIndex <= this.minIndex) {
				tempMax = maxIndex;
			} else {
				System.arraycopy(cachedRecords, 0, newRecords, tempMax - minIndex - (tempMax - this.minIndex), tempMax - this.minIndex + 1);
				tempMax = this.minIndex - 1;
			}
		}

		cachedRecords = newRecords;
		if (tempMin < size()) {
			fillCache(minIndex, tempMin, tempMax);
		}
		this.minIndex = minIndex;
		this.maxIndex = maxIndex;
	}

	private boolean isCached(final int minIndex, final int maxIndex) {
		return minIndex >= this.minIndex && maxIndex <= this.maxIndex;
	}

	/**
	 * Loads the given records to the cache. The parameters minRecordIndex, and maxRecordIndex specify
	 * a range in the <code>logRecordIndexes</code> array. The records in the given range will be loaded.
	 * 
	 * @param minIndex The index of the first record in the cache
	 * @param minRecordIndex the lower bound of the range (inclusive)
	 * @param maxRecordIndex the upper bound of the range (inclusive)
	 */
	protected void fillCache(final int minIndex, final int minRecordIndex, final int maxRecordIndex) throws IOException, ParseException {
		for (int i = minRecordIndex; i <= maxRecordIndex; i++) {
			try {
				cachedRecords[i - minIndex] = logReader.getRecord(i);
			} catch (ParseException e) {
				ErrorReporter.logExceptionStackTrace(e);
				cachedRecords[i - minIndex] = null;
				ParseException throwable = new ParseException("Could not parse the " + i +"th record ", 0);  //$NON-NLS-1$
				throwable.initCause(e);
				throw throwable;
			}
		}
	}

	@Override
	public LogRecord getRecord(final int index) throws IOException, ParseException {
		if (index < 0) {
			throw new IndexOutOfBoundsException();
		}

		if (isCached(index)) {
			return cachedRecords[index - minIndex];
		}

		return logReader.getRecord(index);
	}

	@Override
	public int size() {
		return logReader.size();
	}

	private boolean isCached(final int index) {
		return index >= minIndex && index <= maxIndex;
	}

	@Override
	public void close() {
		logReader.close();
		cachedRecords = null;
	}

	@Override
	public LogRecord getRecordById(final int id) throws IOException, ParseException {
		if (id < cachedRecords[minIndex].getRecordNumber() || id > cachedRecords[maxIndex].getRecordNumber()) {
			return logReader.getRecordById(id);
		}

		LogRecord tmp = new LogRecord();
		tmp.setRecordNumber(id);
		int index = Arrays.binarySearch(cachedRecords, tmp, new Comparator<LogRecord>() {
			@Override
			public int compare(final LogRecord o1, final LogRecord o2) {
				return o1.getRecordNumber() - o2.getRecordNumber();
			}
		});

		if (index > 0) {
			return cachedRecords[index];
		}

		return null;
	}

	@Override
	public int getPositionFromRecordNumber(final int id) {
		return logReader.getPositionFromRecordNumber(id);
	}

	@Override
	public boolean contains(final int id) {
		return logReader.contains(id);
	}
}

