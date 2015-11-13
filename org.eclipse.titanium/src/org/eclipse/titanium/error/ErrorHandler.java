/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.error;

/**
 * This interface describes a primitive error handler. All classes that can
 * handle error should implement these methods.
 * 
 * @author Gabor Jenei
 */
public interface ErrorHandler {

	/**
	 * Reports an exception to the user
	 * @param context contextual information
	 * @param exception
	 *            : The exception
	 */
	void reportException(String context, Exception exception);

	/**
	 * Reports an error to the user
	 * 
	 * @param text
	 *            : The cause of error
	 */
	void reportErrorMessage(String text);

	/**
	 * Reports a warning to the user
	 * 
	 * @param text
	 *            : The cause of warning
	 */
	void reportWarning(String text);

	/**
	 * Reports an information to the user
	 * 
	 * @param text
	 *            : The information to report
	 */
	void reportInformation(String text);

	/**
	 * This method logs and error inside the class. But writing it out is only
	 * done after calling {@link #writeMessageToLog()}
	 * 
	 * @param message
	 */
	void logError(String message);

	/**
	 * This method logs an exception to the inner log of this class. You need to
	 * call {@link #writeMessageToLog()} to make this message appear somewhere.
	 * 
	 * @param exception
	 *            : The exception to log.
	 */
	void logException(Exception exception);

	/**
	 * This method writes out the inner stored error log.
	 */
	void writeMessageToLog();
}
