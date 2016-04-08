/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.log.merge;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * Represents a merge.
 * */
final class MergeAble implements Closeable {

	private static final Pattern UNIFIEDPATTERN = Pattern.compile("(\\d\\d\\d\\d/\\w*/\\d\\d )?(\\d\\d:\\d\\d:\\d)?\\d\\.\\d\\d\\d\\d\\d\\d");

	private final IFile file;
	private final BufferedReader reader;

	/** whether to use the fast or the precise version of the algorithm */
	private final boolean fast;
	private StringBuilder unprocessedPart = new StringBuilder(1 << 16);
	private String line;
	private LogRecord actualRecord = null;
	private boolean hasMore = true;

	private TimestampFormat selfFormat;
	private boolean erroneous = false;
	private String componentID = "";

	public MergeAble(final IFile file, final BufferedReader reader, final boolean fast) {
		this.file = file;
		this.reader = reader;
		this.fast = fast;
		this.componentID = getComponentID(file.getName());
		try {
			line = reader.readLine();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Read problem while processing file:" + file.getName(), e);
			erroneous = true;
			return;
		}
		unprocessedPart.append(line).append('\n');
		// Search for the first timestamp
		final Matcher m = UNIFIEDPATTERN.matcher(unprocessedPart);
		if (m.find()) {
			final String timestamp = unprocessedPart.substring(m.start(), m.end());
			// identify the type of the timestamp
			if (TimestampFormat.DATETIME_FORMAT.getPattern().matcher(timestamp).matches()) {
				selfFormat = TimestampFormat.DATETIME_FORMAT;
			} else if (TimestampFormat.TIME_FORMAT.getPattern().matcher(timestamp).matches()) {
				selfFormat = TimestampFormat.TIME_FORMAT;
			} else if (TimestampFormat.SECOND_FORMAT.getPattern().matcher(timestamp).matches()) {
				selfFormat = TimestampFormat.SECOND_FORMAT;
			} else {
				ErrorReporter.logError("Could not recognise the timestamp in the file '" + file.getLocation().toOSString() + "'");
				erroneous = true;
				return;
			}

			// Read the first record
			next();
		} else {
			// error: there is no timestamp in the first row.
			ErrorReporter.logError("Could not find the timestamp in the file '" + file.getLocation().toOSString() + "'");
			erroneous = true;
			hasMore = false;
		}
	}

	private String getComponentID(final String fileName) {
		int idStart = 0;
		int idEnd = fileName.length() - 1;
		final int dashLoc = fileName.lastIndexOf('-');
		if (dashLoc == -1) {
			int temp = fileName.lastIndexOf('.');
			if (temp != -1) {
				idEnd = temp;
			}
			temp = fileName.lastIndexOf('.', idEnd - 1);
			if (temp != -1) {
				idStart = temp + 1;
			}
		} else {
			idStart = dashLoc + 1;
			final int temp = fileName.indexOf('.', dashLoc);
			if (temp != -1) {
				idEnd = temp;
			}
		}

		return fileName.substring(idStart, idEnd);
	}

	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Problem while closing " + file.getLocationURI(), e);
			erroneous = true;
		}
		hasMore = false;
	}

	public TimestampFormat getTimestampFormat() {
		return selfFormat;
	}

	public boolean hasNext() {
		return hasMore;
	}

	public boolean isErroneous() {
		return erroneous;
	}

	// find the next record
	public void next() {
		if (!hasMore) {
			return;
		}

		if (unprocessedPart.length() == 0) {
			hasMore = false;
			return;
		}

		final Matcher m = selfFormat.getPattern().matcher(unprocessedPart);
		if (!m.find()) {
			// error: there is no timestamp in the first row.
			ErrorReporter.logError("Could not find the timestamp in the file '" + file.getLocation().toOSString() + "'");
			erroneous = true;
			hasMore = false;
			return;
		}

		final String timestamp = unprocessedPart.substring(m.start(), m.end());
		processLines(m, timestamp);
	}

	private void processLines(final Matcher m, final String timestamp) {
		String text = null;
		boolean found = false;

		int end = fast ? unprocessedPart.length() : m.end();
		if (m.find(end)) {
			// If there is an other record in the unprocessed part
			found = true;
			hasMore = true;
			text = unprocessedPart.substring(0, m.start());
			unprocessedPart.delete(0, m.start());
		}
		while (line != null && !found) {
			readLine();
			if (line == null) {
				text = unprocessedPart.toString();
			} else {
				unprocessedPart.append(line).append('\n');
				if (m.find(end)) {
					found = true;
					hasMore = true;
					text = unprocessedPart.substring(0, m.start());
					unprocessedPart.delete(0, m.start());
				} else {
					end = fast ? unprocessedPart.length() : unprocessedPart.length() - selfFormat.getFormatSize();
				}
			}
		}
		if (!found && unprocessedPart.length() > 0) {
			text = unprocessedPart.toString();
			unprocessedPart = new StringBuilder();
		}
		actualRecord = new LogRecord(timestamp, text);
	}

	private void readLine() {
		try {
			line = reader.readLine();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Problem while reading " + file.getLocationURI(), e);
			erroneous = true;
			line = null;
		}
	}

	public void remove() {
		// Not implemented in this iterator
	}

	public LogRecord getActualRecord() {
		return actualRecord;
	}

	public String getComponentID() {
		return componentID;
	}

	public void setComponentID(final String componentID) {
		this.componentID = componentID;
	}

	public IFile getFile() {
		return file;
	}
}
