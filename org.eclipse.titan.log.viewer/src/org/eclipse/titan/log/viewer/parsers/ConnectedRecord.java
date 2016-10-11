/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

/**
 * This class defines the offset and length for an event
 *
 */
public class ConnectedRecord {
	private long recordOffset;
	private int recordLength;
	private int recordNumber;
	
	/**
	 * Protected constructor 
	 */
	public ConnectedRecord(final long recordOffset, final int recordLength, final int recordNumber) {
		this.recordOffset = recordOffset;
		this.recordLength = recordLength;
		this.recordNumber = recordNumber;
	}
	public int getRecordLength() {
		return this.recordLength;
	}
	public void setRecordLength(final int recordLength) {
		this.recordLength = recordLength;
	}
	public int getRecordNumber() {
		return this.recordNumber;
	}
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}
	public long getRecordOffset() {
		return this.recordOffset;
	}
	public void setRecordOffset(final long recordOffset) {
		this.recordOffset = recordOffset;
	}
	
}
