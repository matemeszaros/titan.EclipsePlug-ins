/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.data;

import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * This class represents a log record
 *
 */
public class LogRecord {

	private String timestamp;
	private String eventType = Constants.EVENTTYPE_UNKNOWN;
	private String componentReference = ""; //$NON-NLS-1$
	private String sourceInformation = ""; //$NON-NLS-1$
	private String message = ""; //$NON-NLS-1$
	private long recordOffset;
	private int recordLength;
	private int recordNumber;
	
	/**
	 * Returns the component reference
	 * @return the component reference
	 */
	public String getComponentReference() {
		return this.componentReference;
	}
	
	/**
	 * Sets the component reference
	 * @param componentReference the component reference
	 */
	public void setComponentReference(final String componentReference) {
		this.componentReference = componentReference;
	}
	
	/**
	 * Returns the event type
	 * @return the event type
	 */
	public String getEventType() {
		return this.eventType;
	}
	
	/**
	 * Sets the event type
	 * @param eventType the event type
	 */
	public void setEventType(final String eventType) {
		this.eventType = eventType;
	}
	
	/**
	 * Returns the message
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * Sets the message
	 * @param message the message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}
	
	/**
	 * Returns the source information
	 * @return the source information
	 */
	public String getSourceInformation() {
		return this.sourceInformation;
	}
	
	/**
	 * Sets the source information
	 * @param sourceInformation the source information
	 */
	public void setSourceInformation(final String sourceInformation) {
		this.sourceInformation = sourceInformation;
	}
	
	/**
	 * Returns the time stamp
	 * @return the time stamp
	 */
	public String getTimestamp() {
		return this.timestamp;
	}
	
	/**
	 * Sets the time stamp
	 * @param timestamp the time stamp
	 */
	public void setTimestamp(final String timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Returns the record length
	 * @return the record length
	 */
	public int getRecordLength() {
		return this.recordLength;
	}
	
	/**
	 * Sets the record length
	 * @param recordLength the record length
	 */
	public void setRecordLength(final int recordLength) {
		this.recordLength = recordLength;
	}
	
	/**
	 * Returns the record offset
	 * @return the record offset
	 */
	public long getRecordOffset() {
		return this.recordOffset;
	}
	
	/**
	 * Sets  the record offset
	 * @param recordOffset the record offset
	 */
	public void setRecordOffset(final long recordOffset) {
		this.recordOffset = recordOffset;
	}
	
	/**
	 * Returns the record number
	 * @return the record number
	 */
	public int getRecordNumber() {
		return this.recordNumber;
	}
	
	/**
	 * Sets the record number
	 * @param recordNumber the record number
	 */
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}
}
