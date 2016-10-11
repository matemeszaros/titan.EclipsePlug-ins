/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.models;

import java.io.Serializable;

/**
 * Holds meta data about a specific log record
 *
 */
public class LogRecordIndex implements Serializable {
	
	private static final long serialVersionUID = -1043079242496342572L;
	private long fileOffset; // We support files up to ~9 EB ((2^64)/2 -1 bytes)
	private int recordLength; // Each record can be up to ~2 GB ((2^32)/2 -1 bytes)
	private int recordNumber; // ...and have up to ~2 billion records
 
	/**
	 * Constructor
	 * 
	 * @param fileOffset - The offset to the record from the <b>beginning</b> of file
	 * @param recordLength - the number of bytes in the record
	 */
	public LogRecordIndex(final long fileOffset, final int recordLength, final int recordNumber) {
		this.fileOffset = fileOffset;
		this.recordLength = recordLength;
		this.recordNumber = recordNumber;
	}
	
	/**
	 * Gets the offset within the file
	 * 
	 * @return long address to the beginning of the index
	 */
	public long getFileOffset() {
		return this.fileOffset;
	}
	
	/**
	 * Gets the record length
	 * 
	 * @return the length of the record
	 */
	public int getRecordLength() {
		return this.recordLength;
	}

	/**
	 * Adds to the record length
	 * 
	 * @param additionalRecord the additional record length to add 
	 */
	public void addRecordLen(final int additionalRecord) {
		this.recordLength += additionalRecord;
	}

	/***
	 * Gets the number the record is in the log file
	 * @return
	 */
	public int getRecordNumber() {
		return this.recordNumber;
	}

	/***
	 * Sets the number the record is in the file
	 * @param recordNumber
	 */
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}
}
