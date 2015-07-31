/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.models;

import org.eclipse.titan.common.utils.ObjectUtils;

public class TimeInterval {
	private String start;
	private String end;
	private String timestampFormat;
	
	public TimeInterval(final String start, final String end, final String timestampFormat) {
		this.start = start;
		this.end = end;
		this.timestampFormat = timestampFormat;
	}
	
	public TimeInterval(final TimeInterval timeInterval) {
		start = timeInterval.start;
		end = timeInterval.end;
		timestampFormat = timeInterval.timestampFormat;
	}
	
	public String getStart() {
		return start;
	}
	
	public void setStart(final String start) {
		this.start = start;
	}
	
	public String getEnd() {
		return end;
	}
	
	public void setEnd(final String end) {
		this.end = end;
	}
	
	public String getTimeStampFormat() {
		return timestampFormat;
	}

	public boolean contains(final String timestamp) {
		if (start.isEmpty()) {
			return end.isEmpty() ||
					end.compareTo(timestamp) >= 0;

		}

		if (end.isEmpty()) {
			return start.compareTo(timestamp) <= 0;
		}

		return start.compareTo(timestamp) <= 0 && end.compareTo(timestamp) >= 0;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		
		TimeInterval rhs = (TimeInterval) o;
		return ObjectUtils.equals(timestampFormat, rhs.timestampFormat)
				&& ObjectUtils.equals(start, rhs.start)
				&& ObjectUtils.equals(end, rhs.end);
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hash(start, end, timestampFormat);
	}
}
