/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.models;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Class that holds all info needed for representing a log file
 *
 */
public class LogFileMetaData implements Serializable {
	
	private static final long serialVersionUID = -8935133702928939531L;
	private String version;
	private long size;
	private long lastModified;
	private String timeStampFormat;
	private String executionMode;
	private boolean eventType;
	private URI filePath;
	private String projectName;
	private String projectRelativePath;
	private int timeStampConstant = -1;
	private int fileFormat;
	private String option;
	private Map<String, String> optionsSettings = new HashMap<String, String>();

	private transient IFile logfile = null;

	public IFile getLogfile() {
		if (logfile == null) {
			final IFile[] logfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(filePath);
			if (logfiles.length > 0) {
				logfile = logfiles[0];
			}
		}
		return logfile;
	}

	public void setLogfile(final IFile logfile) {
		this.logfile = logfile;
		this.filePath = logfile.getLocationURI();
	}

	/**
	 * Gets the execution mode
	 * @return the string representation of the execution mode
	 */
	public String getExecutionMode() {
		return this.executionMode;
	}
	
	/**
	 * Set the execution mode
	 * @param executionMode the string representation of the execution mode
	 */
	public void setExecutionMode(final String executionMode) {
		this.executionMode = executionMode;
	}
	
	/**
	 * Gets the path of log file 
	 * @return the full log file path
	 */
	public URI getFilePath() {
		return this.filePath;
	}
	
	/**
	 * Sets the path of the log file
	 * @param filePath the full log file path
	 */
	public void setFilePath(final URI filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Gets the last modified date of the log file
	 * @return the last modified date of the log file as a long
	 */
	public long getLastModified() {
		return this.lastModified;
	}
	
	/**
	 * Sets the last modified date of the log file
	 * @param lastModified the last modified date of the log file as a long
	 */
	public void setLastModified(final long lastModified) {
		this.lastModified = lastModified;
	}
	
	/**
	 * Gets the project name
	 * @return the name of the project that the log file exists in
	 */
	public String getProjectName() {
		return this.projectName;
	}
	
	/**
	 * Sets the project name
	 * @param projectName the name of the project that the log file exists in
	 */
	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Gets the project relative path of the log file
	 * @return the project relative path of the log file
	 */
	public String getProjectRelativePath() {
		return this.projectRelativePath;
	}
	
	/**
	 * Sets the project relative path of the log file
	 * @param projectRelativePath the project relative path of the log file (example: \MyProject\logs\myLogFile.log)
	 */
	public void setProjectRelativePath(final String projectRelativePath) {
		this.projectRelativePath = projectRelativePath;
	}
	
	/**
	 * Gets the file size of the log file
	 * @return the log file size
	 */
	public long getSize() {
		return this.size;
	}
	
	/**
	 * Sets the file size of the log file
	 * @param size the log file size
	 */
	public void setSize(final long size) {
		this.size = size;
	}
	
	/**
	 * Gets the timestamp format of the log file
	 * @return the string representation of the timestamp of the log file
	 */
	public String getTimeStampFormat() {
		return this.timeStampFormat;
	}
	
	/**
 	 * Sets the timestamp format of the log file
	 * @param timeStamp the string representation of the timestamp of the log file
	 */
	public void setTimeStampFormat(final String timeStamp) {
		this.timeStampConstant = timeStamp.length();
		this.timeStampFormat = timeStamp;
	}
	
	/**
	 * Gets the version of TITAN Log Viewer
	 * @return the version of TITAN Log Viewer used when creating the log file meta info
	 */
	public String getVersion() {
		return this.version;
	}
	
	/**
	 * Sets the version of TITAN Log Viewer
	 * @param version the version of TITAN Log Viewer used when creating the log file meta info
	 */
	public void setVersion(final String version) {
		this.version = version;
	}
	
	/**
	 * Returns a flag indicating if log file has logged event types or not 
	 * @return true if log file has logged event types, otherwise false
	 */
	public boolean hasLoggedEventTypes() {
		return this.eventType;
	}
	
	/**
	 * Sets a flag indicating if log file has logged event types or not 
	 * @param eventType the flag indication if the log file has logged event types or not
	 */
	public void setHasLoggedEventTypes(final boolean eventType) {
		this.eventType = eventType;
	}

	/**
	 * Return a constant for the time stamp or -1 i not set
	 * @return time stamp constant
	 */
	public int getTimeStampConstant() {
		return this.timeStampConstant;
	}

	/**
	 * Returns the file format 
	 * @return file format
	 */
	public int getFileFormat() {
		return this.fileFormat;
	}

	/**
	 * Sets the file format of the file. 
	 * @param fileFormat
	 */
	public void setFileFormat(final int fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * Getter for option
	 * @return option
	 */
	public String getOption() {
		return this.option;
	}

	/**
	 * Setter for option
	 * @param option
	 */
	public void setOption(final String option) {
		this.option = option;
		if ((option != null) && (option.trim().length() > 0)) {
			String[] strings = option.split(";"); //$NON-NLS-1$
			for (String string : strings) {
				String[] values = string.split(":="); //$NON-NLS-1$
				if (values.length >= 2) {
					this.optionsSettings.put(values[0], values[1]);
				}
			}
		}		
	}

	/***
	 * Fetch 
	 * @param key
	 * @return the option
	 */
	public String getOptionsSettings(final String key) {
		String value = null;
		if (this.optionsSettings != null) {
			value = this.optionsSettings.get(key);
		}
		return value;
	}

}
