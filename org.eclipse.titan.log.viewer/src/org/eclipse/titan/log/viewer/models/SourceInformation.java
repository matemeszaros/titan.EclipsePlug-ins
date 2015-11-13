/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.models;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This class is responsible for storing and parsing the source information of a log record.
 * Only the source file path and the line number of the last stack element is stored.
 * (Parsing and storing the whole stack is not needed right now)
 * An instance can be created with the {@link #createInstance(String)} factory function.
 */
public class SourceInformation {

	private String sourceFileName;
	private int lineNumber;

	private SourceInformation(final String sourceFileName, final int lineNumber) {
		this.sourceFileName = sourceFileName;
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	/**
	 * Creates and initializes a SourceInformation object from the given source information string.
	 *
	 * @param sourceInformationString The string to parse.
	 * @return The created object
	 * @throws InvalidSourceInformationException If the given string can not be recognized as a source information string.
	 */
	public static SourceInformation createInstance(final String sourceInformationString) throws InvalidSourceInformationException {
		if (isNullOrEmpty(sourceInformationString)) {
			throw new InvalidSourceInformationException();
		}

		String lastStackElem = getLastElementOfCallStack(sourceInformationString);

		String pathAndLineNumber = getPathAndLineNumber(lastStackElem);

		// This colon separates the path and the line number
		final int indexOfColon = pathAndLineNumber.indexOf(':');
		if (indexOfColon == -1) {
			throw new InvalidSourceInformationException();
		}

		final String newPath = pathAndLineNumber.substring(0, indexOfColon);

		String lineInfo = pathAndLineNumber.substring(indexOfColon + 1);

		// For this format: 14:30:03.912762 something.ttcn:3449: Some message
		lineInfo = lineInfo.replace(":", "");

		try {
			final int newLineNumber = Integer.parseInt(lineInfo);

			String fileName = newPath;
			int lastSeparator = newPath.lastIndexOf('\\');
			if (lastSeparator == -1) {
				lastSeparator = newPath.lastIndexOf('/');
			}
			if (lastSeparator != -1) {
				fileName = newPath.substring(lastSeparator + 1);
			}

			return new SourceInformation(fileName, newLineNumber);
		} catch (Exception e) {
			// This should not happen
			ErrorReporter.logExceptionStackTrace(e);
			throw new InvalidSourceInformationException();
		}
	}

	/**
	 * For this format: timestamp someEvent SIP_Examples.ttcn:171(controlpart:SIP_Examples) "ver1"
	 */
	private static String getPathAndLineNumber(String lastStackElem) {
		final int indexOfPar = lastStackElem.indexOf("(");
		if (indexOfPar != -1) {
			return lastStackElem.substring(0, indexOfPar);
		}
		return lastStackElem;
	}

	private static String getLastElementOfCallStack(String sourceInformationString) {
		int index = sourceInformationString.lastIndexOf("->");
		if (index != -1) {
			return sourceInformationString.substring(index + 2);
		}
		return sourceInformationString;
	}

	@SuppressWarnings("serial")
	public static class InvalidSourceInformationException extends Exception {
	}

}
