/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.model;

import org.eclipse.titan.log.viewer.parsers.ConnectedRecord;
import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * Class to represent a log event
 *
 */
//FIXME fields should only exist where they actually hold a real value
public class EventObject implements IEventObject {
	private long recordOffset;
	private int recordLength;
	private int recordNumber;
	/** The type of the event. */
	private EventType type;
	/** The time stamp for the event. */
	private String time = "";
	/** The name of the event. //$NON-NLS-1$ */
	private String name = "";
	/** The reference of the event. //$NON-NLS-1$ */
	private String reference = "";
	/** The target of the event. //$NON-NLS-1$ */
	private String target = "";
	/** The optional port.//$NON-NLS-1$ */
	private String port = "";
	/** the port name on the target //$NON-NLS-1$ */
	private String targetport = "";
	private String eventType = Constants.EVENTTYPE_UNKNOWN;
	/** used fro setverdict events to get connected events */
	private ConnectedRecord[] connectedRecords;
	private int eventNumber;

	public EventObject(EventType type) {
		this.type = type;
	}
	
	/**
	 * The methods below are trivial access
	 * methods for information found in an instance of this test event class.
	 * The names of these methods are enough to explain what the methods
	 * actually do.
	 */

	public void setEventNumber(final int eventNumber) {
		this.eventNumber = eventNumber;
	}

	public int getEventNumber() {
		return eventNumber;
	}

	/**
	 * Getter for type
	 * @return The type of the event
	 */
	@Override
	public EventType getType() {
		return this.type;
	}
	
	/**
	 * Setter for time
	 * @param time The time stamp for the event
	 */
	public void setTime(final String time) {
		this.time = time;
	}
	
	/**
	 * Getter for time
	 * @return The time stamp for the event
	 */
	public String getTime() {
		return this.time;
	}

	/**
	 * Setter for name
	 * @param name the name of the event
	 */
	public void setName(final String name) {
		this.name = name; // TODO minimising the String retainance should not be done here.
	}
	
	/**
	 * Getter for name
	 * @return The name of the event
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setter for port
	 * @param port The optional port
	 */
	public void setPort(final String port) {
		this.port = port;
	}
	
	/**
	 * Getter for port
	 * @return The optional port
	 */
	public String getPort() {
		return this.port;
	}

	/**
	 * Setter for target
	 * @param pTarget the target of the event
	 */
	public void setTarget(final String pTarget) {
		//FIXME should be handled correctly at source location.
		this.target = pTarget;
	}
	
	/**
	 * Getter for target
	 * @return The target of the event
	 */
	public String getTarget() {
		return this.target;
	}

	/**
	 * Setter for event
	 * @param pReference the reference of the event
	 */
	public void setReference(final String pReference) {
		//FIXME should be handle correctly at source location.
		this.reference = pReference;
	}
	
	/**
	 * Getter for reference
	 * @return the reference of the event
	 */
	@Override
	public String getReference() {
		return this.reference;
	}

	/**
	 * Setter for event type
	 * @param eventType the event type
	 */
	public void setEventType(final String eventType) {
		this.eventType = eventType;
	}
	
	/**
	 * Getter for event type
	 * @return the event type
	 */
	public String getEventType() {
		return this.eventType;
	}
	
	/**
	 * Setter for record length
	 * @param recordLength the length of the record
	 */
	public void setRecordLength(final int recordLength) {
		this.recordLength = recordLength;
	}

	/**
	 * Getter for record length
	 * @return the length of the record
	 */
	public int getRecordLength() {
		return this.recordLength;
	}
	
	/**
	 * Setter for record offset
	 * @param recordOffset the record offset
	 */
	public void setRecordOffset(final long recordOffset) {
		this.recordOffset = recordOffset;
	}
	
	/**
	 * Getter for record offset
	 * @return the record offset
	 */
	public long getRecordOffset() {
		return this.recordOffset;
	}
	
	/**
	 * Setter for record number
	 * @param recordNumber the record number
	 */
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}
	
	/**
	 * Getter for record number
	 * @return the record number
	 */
	@Override
	public int getRecordNumber() {
		return this.recordNumber;
	}

	public ConnectedRecord[] getConnectedRecords() {
		return this.connectedRecords;
	}

	public void setConnectedRecords(final ConnectedRecord[] connectedRecords) {
		this.connectedRecords = connectedRecords;
	}

	public String getTargetPort() {
		return this.targetport;
	}

	public void setTargetPort(final String targetPort) {
		this.targetport = targetPort;
	}

}
