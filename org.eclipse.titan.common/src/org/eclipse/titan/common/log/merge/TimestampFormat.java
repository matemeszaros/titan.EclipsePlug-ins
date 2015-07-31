/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.log.merge;

import java.util.regex.Pattern;

enum TimestampFormat {
	DATETIME_FORMAT("DateTime", "\\d\\d\\d\\d/\\w*/\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\d\\d\\d", 27),
	TIME_FORMAT("Time", "\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\d\\d\\d", 15),
	SECOND_FORMAT("Second", "\\d\\.\\d\\d\\d\\d\\d\\d", 8);

	private final String formatName;
	private final Pattern timestampPattern;
	private final int formatSize;

	TimestampFormat(final String formatName, final String format, final int formatSize) {
		this.formatName = formatName;
		this.timestampPattern = Pattern.compile(format);
		this.formatSize = formatSize;
	}

	public String getFormatName() {
		return formatName;
	}

	public Pattern getPattern() {
		return timestampPattern;
	}

	public int getFormatSize() {
		return formatSize;
	}
}
