/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.error;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This class implements an error handler for graphical environments. Note that
 * in console environments (for eg. headless mode) this way of error handling
 * will not work. You may only provide an instance of this class if you are sure
 * that your class will be run from graphical environment.
 * 
 * @author Gabor Jenei
 */
public class GUIErrorHandler implements ErrorHandler {
	private StringBuilder collectedMessage;

	/**
	 * Constructor
	 */
	public GUIErrorHandler() {
		collectedMessage = new StringBuilder();
	}

	@Override
	public void reportException(String context, Exception exception) {
		ErrorMessage.show("Exception", exception.getMessage() + ErrorMessage.LOG_ENTRY_NOTE, MessageDialog.ERROR);
		ErrorReporter.logExceptionStackTrace("", exception);
	}

	@Override
	public void reportErrorMessage(String text) {
		ErrorMessage.show("Error", text, MessageDialog.ERROR);
	}

	@Override
	public void reportWarning(String text) {
		ErrorMessage.show("Warning", text, MessageDialog.WARNING);
	}

	@Override
	public void reportInformation(String text) {
		ErrorMessage.show("Information", text, MessageDialog.INFORMATION);
	}

	/**
	 * This method reports a possibly bad setting, it also provides a button to
	 * set this setting to a correct value.
	 * 
	 * @param windowTitle
	 *            : The title of error window to show
	 * @param message
	 *            : The message to show in the window
	 * @param buttonTitle
	 *            : The title of the button that pops up the preference page on
	 *            click
	 * @param prefPage
	 *            : The string that provides link to the preference page (for
	 *            eg. <code>org.eclipse.titanium.preferences.pages...</code>
	 */
	public void reportBadSetting(String windowTitle, String message, String buttonTitle, String prefPage) {
		ErrorMessage.show(windowTitle, message, buttonTitle, prefPage,MessageDialog.ERROR);
	}

	/**
	 * This method writes to a message log that is stored inside the class. This
	 * message isn't written anywhere without calling
	 * {@link #writeMessageToLog()}
	 * 
	 * @param message
	 *            : The message to add
	 */
	@Override
	public void logError(String message) {
		collectedMessage.append(message).append('\n');
	}

	/**
	 * This method writes the collected errors to the log, and pops up a window
	 * that warns the user to read the log. This method also makes the message
	 * string to be empty.
	 */
	@Override
	public void writeMessageToLog() {
		if (collectedMessage.length() != 0) {
			ErrorReporter.logError(collectedMessage.toString());
			collectedMessage.setLength(0);
			collectedMessage.trimToSize();
			ErrorMessage.show("Error log", "Various errors happened! For details see the error log.",MessageDialog.ERROR);
		}
	}

	@Override
	public void logException(Exception exception) {
		ErrorReporter.logExceptionStackTrace("", exception);
	}

}
