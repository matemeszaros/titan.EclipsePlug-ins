/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;

/**
 * This class is responsible for handling verification of log files
 *
 */
public class LogFileHandler {
	
	private LogFileMetaData fileMetaInfo;

	/**
	 * Constructor
	 * @param filePath the log file full path
	 * @param projectName the name of the project that the log file exist in
	 * @param projectRelativePath the project relative path of the log file
	 */
	public LogFileHandler(final URI filePath, final String projectName,
			final String projectRelativePath) {
		this.fileMetaInfo = new LogFileMetaData();
		this.fileMetaInfo.setFilePath(filePath);
		this.fileMetaInfo.setProjectName(projectName);
		this.fileMetaInfo.setProjectRelativePath(projectRelativePath);
		this.fileMetaInfo.setVersion(Constants.CURRENT_VERSION);
	}

	public LogFileHandler(final IFile logFile) {
		this(logFile.getLocationURI(),
			 logFile.getProject().getName(),
			 File.separator + logFile.getProject().getName() + File.separator + logFile.getProjectRelativePath().toOSString());
	}
	
	/**
	 * Method for determining time format from the beginning of string
	 * @param logString the string containing the time stamp
	 * @return log format pattern or null if not found
	 */
	private String getTimeStampFormat(final String logString) {
		// Log string must be longer than the shortest time format
		if (logString.length() < Constants.SECONDS_FORMAT.length()) {
			return null;
		}

		String[] strings = logString.split("\\s"); //$NON-NLS-1$
		if (strings.length < 1) {
			return null;
		}
		String seconds = strings[0];
		// Second format: s.SSSSSS
		if (Pattern.matches(Constants.REGEXP_SECONDS_FORMAT, seconds)) {
			return Constants.SECONDS_FORMAT;
		}

		//Second shortest format
		if (logString.length() < Constants.TIME_FORMAT.length()) {
			return null;
		}

		String time = logString.substring(0, Constants.TIME_FORMAT.length());
		//Time format: HH:mm:ss.SSSSSS
		if (Pattern.matches(Constants.REGEXP_TIME_FORMAT, time)) {
			return Constants.TIME_FORMAT;
		}

		//longest format
		if (logString.length() < Constants.DATETIME_FORMAT.length()) {
			return null;
		}

		String date = logString.substring(0, Constants.DATETIME_FORMAT.length());
		//Date format: yyyy/MMM/dd HH:mm:ss.SSSSSS
		if (Pattern.matches(Constants.REGEXP_DATETIME_FORMAT, date)) {
			return Constants.DATETIME_FORMAT;
		}

		return null;
	}

	/**
	 * Validate a time stamp, returns the time stamp string or null
	 * @param logLine line read by the parser
	 * @param format
	 * @return
	 */
	public static String validateTimeStamp(final String logLine, final String format) {	
		String temp;		
		switch (format.length()) {
		case Constants.DATETIME_FORMAT_LENGTH:
			if (logLine.length() < Constants.DATETIME_FORMAT.length()) {
				break;
			}
			temp = logLine.substring(0, Constants.DATETIME_FORMAT.length());
			if (validateDateTime(temp)) {
				return temp;
			}
			break;
			
		case Constants .TIME_FORMAT_LENGTH:
			if (logLine.length() < Constants.TIME_FORMAT.length()) {
				break;
			}
			temp = logLine.substring(0, Constants.TIME_FORMAT.length());
			if (validateTime(temp)) {
				return temp;
			}
			break;
			
		case Constants.SECONDS_FORMAT_LENGTH:
			if (logLine.length() < Constants.SECONDS_FORMAT.length()) {
				break;
			}
			String[] strings = logLine.split("\\s"); //$NON-NLS-1$
			if (strings.length < 1) {
				return null;
			}
			temp = strings[0];	
			if (validateSeconds(temp)) {
				return temp;
			}
			break;

		default:
			break;
		}
		
		return null;
	}
	
	/**
	 * Validate a date time format
	 * @param dateFormat
	 * @return
	 */
	private static boolean validateDateTime(final String dateFormat) {
		return Pattern.matches(Constants.REGEXP_DATETIME_FORMAT, dateFormat);
	}
	/**
	 * Validate a time format
	 * @param timeFormat
	 * @return
	 */
	private static boolean validateTime(final String timeFormat) {
		return Pattern.matches(Constants.REGEXP_TIME_FORMAT, timeFormat);
	}
	/**
	 * Validate a second format
	 * @param secondFormat
	 * @return
	 */
	private static boolean validateSeconds(final String secondFormat) {
		return Pattern.matches(Constants.REGEXP_SECONDS_FORMAT, secondFormat);
	}
	
	/**
	 * This method looks in a log and populate a meta data object
	 * with data from the log file. Throws exception if no valid log
	 * file format is found
	 * @return FileMetaInfo
	 * @throws TechnicalException invalid log file
	 */
	public LogFileMetaData autoDetect() throws TechnicalException {
		String logLine = null;
		BufferedReader bufferedReader = null;
		File logFile = new File(this.fileMetaInfo.getFilePath());
		
		// check if the log file exists
		if (!logFile.exists()) {
			throw new TechnicalException(Messages.getString("LogFileHandler.6")); //$NON-NLS-1$
		}
		
		// check if .log file extension
		int fileSize = logFile.getName().length();
		String logExt = "." + Constants.LOG_EXTENSION; //$NON-NLS-1$
		if (fileSize > logExt.length()) { // ".log".length -> 4
			String fileExtension = logFile.getName().substring(fileSize - logExt.length(), fileSize);
			if (!fileExtension.equalsIgnoreCase(logExt)) {
				throw new TechnicalException(Messages.getString("LogFileHandler.4")); //$NON-NLS-1$
			}
		}
		
		// check if the file is empty
		if (logFile.length() == 0) {
			throw new TechnicalException(Messages.getString("LogFileHandler.0")); //$NON-NLS-1$
		}
		
		// check if the file is readable
		if (!logFile.canRead()) {
			throw new TechnicalException(Messages.getString("LogFileHandler.5")); //$NON-NLS-1$
		}

		// read first line in the log file
		try {
			bufferedReader = new BufferedReader(new FileReader(logFile));
			try {
				logLine = bufferedReader.readLine();
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
				throw new TechnicalException(Messages.getString("LogFileHandler.5")); //$NON-NLS-1$
			}

			// check if a valid time format can be found
			String timeStampFormat = logLine != null ? getTimeStampFormat(logLine) : null;
			if (timeStampFormat == null) {
				throw new TechnicalException(Messages.getString("LogFileHandler.1")); //$NON-NLS-1$
			}
			this.fileMetaInfo.setTimeStampFormat(timeStampFormat);

			// remove timestamp from the log line
			logLine = logLine.substring(timeStampFormat.length(), logLine.length()).trim();
			String[] logLineArray = logLine.split(" "); //$NON-NLS-1$

			// only timestamp is present in the log
			if (logLineArray.length < 1) {
				throw new TechnicalException(Messages.getString("LogFileHandler.2")); //$NON-NLS-1$
			}

			// check for event types
			this.fileMetaInfo.setHasLoggedEventTypes(isEventTypePresent(logLine));

			// check for execution mode, single or parallel
			// (parallel merged is left out)
			String executionMode = getExecutionMode(logLine);
			if (executionMode == null) {
				throw new TechnicalException(Messages.getString("LogFileHandler.3")); //$NON-NLS-1$
			}
			this.fileMetaInfo.setExecutionMode(executionMode);

			// set file size
			this.fileMetaInfo.setSize(logFile.length());

			// set last modified 
			this.fileMetaInfo.setLastModified(logFile.lastModified());


			//set file format
			this.fileMetaInfo.setFileFormat(Constants.FILEFORMAT_1);

		} catch (FileNotFoundException e) {
			throw new TechnicalException("File not found: " + this.fileMetaInfo.getFilePath(), e);
		} finally {
			IOUtils.closeQuietly(bufferedReader);
		}
		return this.fileMetaInfo;
	}

	/**
	 * Checks if an event type is present in the log file
	 * @param logLine
	 * @return true or false
	 */
	private boolean isEventTypePresent(final String logLine) {
		// We only need to check for main category, because ALL sub
		// categories has main in their name
		SortedMap<String, String[]> eventCategories = Constants.EVENT_CATEGORIES;
		Set<String> categories = eventCategories.keySet();
		for (String category : categories) {
			if (logLine.contains(category)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks which execution mode is used
	 * @param logLine
	 * @return execution mode
	 */
	private String getExecutionMode(final String logLine) {
		for (int i = 0; i < Constants.MESSAGE_START_LINES_SINGLE.length; i++) {
			if (logLine.contains(Constants.MESSAGE_START_LINES_SINGLE[i])) {
				return Constants.EXECUTION_MODE_SINGLE;
			}
		}
		for (int i = 0; i < Constants.MESSAGE_START_LINES_PARALLEL.length; i++) {
			if (logLine.contains(Constants.MESSAGE_START_LINES_PARALLEL[i])) {
				return Constants.EXECUTION_MODE_PARALLEL;
			}
		}
		//if execution mode not can be determined set the execution mode to parallel
		return Constants.EXECUTION_MODE_PARALLEL;
	}
}
