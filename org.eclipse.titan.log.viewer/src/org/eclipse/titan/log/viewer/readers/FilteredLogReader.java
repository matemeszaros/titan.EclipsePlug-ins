/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.readers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;

public class FilteredLogReader implements ILogReader {

	private final ILogReader logReader;

	private List<Integer> filteredRecords = null;

	private FilterPattern filterPattern;
	private boolean filtered = false;

	public FilteredLogReader(final ILogReader logReader) throws IOException {
		this.logReader = logReader;

		this.filtered = false;
	}

	public void runFilter(final FilterPattern filterPattern, final IProgressMonitor monitor) throws ParseException, IOException {
		monitor.beginTask("Filtering", logReader.size());

		if (filterPattern == null || filterPattern.equals(this.filterPattern)
				|| logReader.size() == 0) {
			monitor.done();
			return;
		}

		List<Integer> tmpFilteredRecords = new ArrayList<Integer>();


		for (int i = 0; i < logReader.size(); ++i) {
			if (monitor.isCanceled()) {
				monitor.done();
				return;
			}
			LogRecord aRecord;
			try {
				aRecord = logReader.getRecord(i);

			} catch (ParseException e) {
				ErrorReporter.logExceptionStackTrace(e);
				ParseException throwable = new ParseException("Could not parse the " + i +"th record ", 0); //$NON-NLS-1$
				throwable.initCause(e);
				monitor.done();
				throw throwable;
			}
			if (filterPattern.match(aRecord)) {
				tmpFilteredRecords.add(i);
			}
			monitor.worked(1);
		}

		filtered = true;
		filteredRecords = tmpFilteredRecords;
		this.filterPattern = filterPattern;
		monitor.done();
	}

	@Override
	public int size() {
		return filtered ? filteredRecords.size() : logReader.size();
	}

	public boolean isFiltered() {
		return filtered;
	}

	/**
	 * Returns true if the reader contains the given record.
	 * This function assumes that the logRecordIndexes and filteredRecords arrays are sorted.
	 * @param id The id of the record
	 * @return true if the reader contains the record
	 */
	@Override
	public boolean contains(final int id) {
		if (id < 0) {
			return false;
		}

		if (!filtered) {
			return logReader.contains(id);
		}

		return Collections.binarySearch(filteredRecords, id) >= 0;
	}

	@Override
	public int getPositionFromRecordNumber(final int id) {
		if (logReader.size() == 0 || id < 0) {
			return -1;
		}

		final int positionInChildReader = logReader.getPositionFromRecordNumber(id);

		if (filtered) {
			int index = Collections.binarySearch(filteredRecords, positionInChildReader);
			return index >= 0 ? index : -1;
		}

		return positionInChildReader;
	}

	@Override
	public LogRecord getRecord(final int position) throws IOException, ParseException {
		if (filtered) {
			return logReader.getRecord(filteredRecords.get(position));
		}

		return logReader.getRecord(position);
	}

	@Override
	public LogRecord getRecordById(final int id) throws IOException, ParseException {
		final int position = getPositionFromRecordNumber(id);
		if (position == -1) {
			return null;
		}

		return logReader.getRecordById(id);
	}

	@Override
	public void close() {
		logReader.close();
	}
}
