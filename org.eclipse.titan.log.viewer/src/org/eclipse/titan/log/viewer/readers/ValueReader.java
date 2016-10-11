/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.parsers.ConnectedRecord;
import org.eclipse.titan.log.viewer.parsers.RecordParser;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.EventType;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Responsible for reading a value message from a log file 
 *
 */
public final class ValueReader {

	private static ValueReader instance = null;

	private static Map<Long, LogRecord> cache = new HashMap<Long, LogRecord>();

	private ValueReader() {
		// Protected constructor
	}
	
	/**
	 * Returns the instance of ValueReader
	 * @return the instance of ValueReader
	 */
	public static ValueReader getInstance() {
		if (instance == null) {
			instance = new ValueReader();
		}
		return instance;
	}

	public LogRecord readLogRecordFromLogFileCached(final URI logFilePath, final EventObject event) throws IOException, ParseException {

		if (cache.containsKey(event.getRecordOffset())) {
			return cache.get(event.getRecordOffset());
		}

		LogRecord temp = readLogRecordFromLogFile(logFilePath, event);
		cache.put(event.getRecordOffset(), temp);

		return temp;
	}

	public LogRecord readLogRecordFromLogFile(final URI logFilePath, final EventObject event) throws IOException, ParseException {
		long offset = event.getRecordOffset();
		int length = event.getRecordLength();

		LogRecord logrecord = getLogRecord(logFilePath, offset, length);
		String message = logrecord.getMessage();
		EventType type = event.getType();
		switch (type) {
		case SEND:
			message = readSendEvent(message);
			break;
		case RECEIVE:
			message = readReceiveEvent(message);
			break;
		case SILENT_EVENT:
			message = getValue(logFilePath, offset, length);
			break;
		case ENQUEUED:
			message = readEnqueuedEvent(message);
			break;
		case SETVERDICT:
			ConnectedRecord[] connectedRecords = event.getConnectedRecords();
			
			if (connectedRecords != null) {
				StringBuilder messageBuilder = new StringBuilder("{ message := " + message.trim());
				if (connectedRecords.length > 0) {
					messageBuilder.append(", causedBy := { ");
				}
				for (int i = 0; i < connectedRecords.length; i++) {
					int eventNumber = i + 1;
					ConnectedRecord connectedEvent = connectedRecords[i];
					logrecord = getLogRecord(logFilePath, connectedEvent.getRecordOffset(), connectedEvent.getRecordLength());
					messageBuilder.append("event" + eventNumber
							+ " := { timestamp := " + logrecord.getTimestamp()
							+ ", componentRef := " + logrecord.getComponentReference()
							+ ", eventType := " + logrecord.getEventType()
							+ ", sourceInfo := " + logrecord.getSourceInformation()
							+ ", message := {" + logrecord.getMessage().trim() + "} }");
				}

				if (connectedRecords.length > 0) {
					messageBuilder.append(" }");
				}
				message = messageBuilder.toString();
			}

			message = message + "}\n"; //$NON-NLS-1$
			break;
		case PTC_CREATE:
		default:
			break;
		}
		logrecord.setMessage(message);
		return logrecord;
	}

	private String readEnqueuedEvent(String message) {
		if (message.contains(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE + "(")) { //$NON-NLS-1$
			int stopIndex = message.indexOf(")"); //$NON-NLS-1$
			message = message.substring(stopIndex + 1);
		}
		if (message.contains(":")) { //$NON-NLS-1$
			message = message.substring(message.indexOf(":") + 2); //$NON-NLS-1$
		}
		return message;
	}

	private String readReceiveEvent(String message) {
		if (message.contains(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE + "(")) { //$NON-NLS-1$
			int stopIndex = message.indexOf(")"); //$NON-NLS-1$
			message = message.substring(stopIndex + 1);
			if (message.contains(":")) { //$NON-NLS-1$
				message = message.substring(message.indexOf(":") + 1); //$NON-NLS-1$
			}
		}
		String[] strings = message.split("with queue id \\d+: \\S++ : "); //$NON-NLS-1$
		if (strings.length > 1) {
			message = strings[1];
		}
		strings = message.split(" from \\S++ \\S++ : "); //$NON-NLS-1$
		if (strings.length > 1) {
			message = strings[1];
		}

		strings = message.split(" from \\S++ \\S++ "); //$NON-NLS-1$
		if (strings.length > 1) {
			message = strings[1];
		}
		return message;
	}

	private String readSendEvent(String message) {
		if (message.contains(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE + "(")) { //$NON-NLS-1$
			int stopIndex = message.indexOf(")"); //$NON-NLS-1$
			message = message.substring(stopIndex + 1);
			if (message.contains(":")) { //$NON-NLS-1$
				message = message.substring(message.indexOf(":") + 1); //$NON-NLS-1$
			}
		} else {
			String[] strings = message.split(" to \\S++ \\S++ : ");  //$NON-NLS-1$
			if (strings.length > 1) {
				message = strings[1];
			}
			strings = message.split(" to \\S++ \\S++ "); //$NON-NLS-1$
			if (strings.length > 1) {
				message = strings[1];
			}
		}
		return message;
	}

	private String getValue(final URI logFilePath, final long offset, final int length) throws IOException, ParseException {
		RandomAccessFile random = null;
		String message = null;
		try {
			random = new RandomAccessFile(new File(logFilePath), MSCConstants.READ_ONLY);
			random.seek(offset);
			byte[] buffer = new byte [length];
			random.read(buffer, 0, length);
			RecordParser recordParser = new RecordParser();
			LogRecord logRecord = recordParser.parse(buffer);
			message = logRecord.getMessage();
		} finally {
			IOUtils.closeQuietly(random);
		}
		return message;
	}
	
	private LogRecord getLogRecord(final URI logFilePath, final long offset, final int length) throws IOException, ParseException {
		RandomAccessFile random = null;
		LogRecord logRecord = null;

		try {
			random = new RandomAccessFile(new File(logFilePath), MSCConstants.READ_ONLY);
			random.seek(offset);
			byte[] buffer = new byte [length];
			random.read(buffer, 0, length);
			RecordParser recordParser = new RecordParser();
			logRecord = recordParser.parse(buffer);
		} finally {
			IOUtils.closeQuietly(random);
		}
		return logRecord;
	}
}
