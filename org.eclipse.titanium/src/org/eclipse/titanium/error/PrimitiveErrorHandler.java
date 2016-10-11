/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.error;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This class implements the most primitive error handling capability. We
 * provide that such kind of error handling is <b>ALWAYS</b> available,
 * independently from the environment
 * 
 * @author Gabor Jenei
 */
public class PrimitiveErrorHandler implements ErrorHandler {
	public static final String ERROR_PATH = "error.log";

	private ErrorHandler handler;

	public PrimitiveErrorHandler() {
		try {
			final File errorLog = new File(ERROR_PATH);
			final PrintStream errorStream = new PrintStream(errorLog);
			handler = new ConsoleErrorHandler(errorStream, errorStream, errorStream);
		} catch (IOException ex) {
			ErrorReporter.logExceptionStackTrace("Error while printing to " + ERROR_PATH, ex);
			handler = new ConsoleErrorHandler();
		}
	}

	@Override
	public void reportException(final String context, final Exception exception) {
		handler.reportException(context, exception);
	}

	@Override
	public void reportErrorMessage(final String text) {
		handler.reportErrorMessage(text);
	}

	@Override
	public void reportWarning(final String text) {
		handler.reportWarning(text);
	}

	@Override
	public void reportInformation(final String text) {
		handler.reportInformation(text);
	}

	@Override
	public void logError(final String message) {
		handler.logError(message);
	}

	@Override
	public void logException(final Exception exception) {
		handler.logException(exception);
	}

	@Override
	public void writeMessageToLog() {
		handler.writeMessageToLog();
	}

}
