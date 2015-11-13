/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.preferences;

/**
 * @author Szabolcs Beres
 * */
public final class PreferenceConstants {
	public static final String EXECUTOR_PREFERENCE_PAGE_ID = "org.eclipse.titan.executor.preferences.ExecutorPreferencePage";
	public static final String SET_LOG_FOLDER = "setLogFolder";
	public static final String SET_LOG_FOLDER_LABEL = "Set the default log folder. Can be overriden by the logging section of the config file.";
	public static final String LOG_FOLDER_PATH_NAME = "logFolderPath";
	public static final String LOG_FOLDER_PATH_LABEL = "Path of the log folder. Relative to the working directory.";
	public static final String DELETE_LOG_FILES_NAME = "deleteLogFiles";
	public static final String DELETE_LOG_FILES_LABEL = "Delete log files before execution.";
	public static final String AUTOMATIC_MERGE_NAME = "automaticMergeEnabled";
	public static final String AUTOMATIC_MERGE_LABEL = "Merge the log files after test execution.";
	
	/** private constructor to disable instantiation */
	private PreferenceConstants() {
	}
}
