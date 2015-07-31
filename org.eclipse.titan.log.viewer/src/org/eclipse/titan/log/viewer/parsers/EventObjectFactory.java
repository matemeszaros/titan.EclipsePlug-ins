/*******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.parsers;

import java.util.StringTokenizer;

import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.EventType;

class EventObjectFactory {

	public EventObject createEventObject(final EventType type, final LogRecord logRecord,
	                                     MessageAnalyser messageAnalyser1, int timeStampConstant) {

		EventObject eventObject = new EventObject(type);
		if (logRecord != null) {
			// general settings for an eventObject
			// Only send time to the eventObject if the time format is DateTime
			// otherwise send the whole timestamp
			if (timeStampConstant == org.eclipse.titan.log.viewer.utils.Constants.DATETIME_FORMAT_LENGTH) {
				String timestamp = logRecord.getTimestamp();
				eventObject.setTime(getTimeStamp(timestamp));
			} else {
				eventObject.setTime(logRecord.getTimestamp());
			}

			eventObject.setEventType(logRecord.getEventType()); // event type in
																// log file
			eventObject.setRecordOffset(logRecord.getRecordOffset());
			eventObject.setRecordLength(logRecord.getRecordLength());
			eventObject.setRecordNumber(logRecord.getRecordNumber());
			// Set reference if the componentReference exists in the log file
			if ((logRecord.getComponentReference() != null)
					&& (logRecord.getComponentReference().trim().length() > 0)) {
				eventObject.setReference(logRecord.getComponentReference());
			}
		}
		// settings in eventObject depending on event type
		switch (type) {
		// System related events have a "system" component reference in the log
		// and get the name specified by the user in preferences
		case SYSTEM_CREATE:
		case SYSTEM_TERMINATE:
			eventObject.setReference(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE);
			break;

		// As TTCN tests always have a main test component, also here I have
		// hard-wired the name, but this can easily be taken from the actual
		// log.
		case MTC_CREATE:
		case MTC_TERMINATE:
			eventObject.setName(org.eclipse.titan.log.viewer.utils.Constants.MTC);
			eventObject.setReference(org.eclipse.titan.log.viewer.utils.Constants.MTC_REFERENCE);
			break;
		case HC_CREATE:
		case HC_TERMINATE:
			eventObject.setName(org.eclipse.titan.log.viewer.utils.Constants.HC);
			eventObject.setReference(org.eclipse.titan.log.viewer.utils.Constants.HC_REFERENCE);
			break;

		// This event type represents a component creation.
		case PTC_CREATE:
			eventObject.setReference(messageAnalyser1.getComponentCreationReference());
			eventObject.setName(messageAnalyser1.getComponentCreationName());
			break;

		// This event type represents a component done event.
		case PTC_DONE:
			eventObject.setName("done"); //$NON-NLS-1$
			eventObject.setReference(messageAnalyser1.getComponentDoneReference());
			eventObject.setTarget(org.eclipse.titan.log.viewer.utils.Constants.MTC_REFERENCE);
			break;

		// This event type represents a test case start event.
		case TC_START:

			String tcName = messageAnalyser1.getTestcaseName();
			eventObject.setName(tcName);
			break;

		// This event type represents a test case termination event.
		case TC_END:
			eventObject.setName(messageAnalyser1.getTestcaseVerdict());
			break;

		// This event type represents a send event.
		case SEND:
			String sendSource = messageAnalyser1.getSendSource();
			eventObject.setReference(sendSource);
			eventObject.setPort(sendSource);
			break;

		case SILENT_EVENT:
			if (logRecord != null) {
				eventObject.setTarget(logRecord.getComponentReference());
			}
			break;

		default:
			break;
		}

		return eventObject;
	}

	/**
	 * Access method to get the time stamp from a log line if the time format is
	 * of DATETIME
	 *
	 * @return String timestamp
	 */
	private static String getTimeStamp(final String timestamp) {
		StringTokenizer tokenizer = new StringTokenizer(timestamp, " ");
		if (tokenizer.hasMoreTokens()) {
			tokenizer.nextToken();
		}
		if (tokenizer.hasMoreTokens()) {
			return tokenizer.nextToken();
		}

		return "";
	}
}
