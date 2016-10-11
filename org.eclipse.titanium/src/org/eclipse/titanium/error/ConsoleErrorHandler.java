/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.error;

import java.io.PrintStream;

/**
 * This class implements a console error handling. It needs streams where
 * messages can be written in order
 * 
 * @author Gabor Jenei
 */
public class ConsoleErrorHandler implements ErrorHandler {
	private PrintStream errorStream;
	private PrintStream warningStream;
	private PrintStream informationStream;
	private StringBuilder collectedMessage;

	/**
	 * This constructor sets {@link System#err} for error messages and
	 * {@link System#out} for all other messages
	 */
	public ConsoleErrorHandler() {
		this(System.err, System.out, System.out);
	}

	/**
	 * Constructor
	 * 
	 * @param errorStream
	 *            : The stream of error messages (including exception traces)
	 * @param warningStream
	 *            : The stream of warning messages
	 * @param informationStream
	 *            : The stream of information messages
	 */
	public ConsoleErrorHandler(final PrintStream errorStream, final PrintStream warningStream, final PrintStream informationStream) {
		this.errorStream = errorStream;
		this.warningStream = warningStream;
		this.informationStream = informationStream;
		collectedMessage = new StringBuilder();
	}

	@Override
	public void reportException(final String context, final Exception exception) {
		errorStream.println("An exception occured, the stack trace is:\n");
		exception.printStackTrace(errorStream);
	}

	@Override
	public void reportErrorMessage(final String text) {
		errorStream.println(text);
	}

	@Override
	public void reportWarning(final String text) {
		warningStream.println(text);
	}

	@Override
	public void reportInformation(final String text) {
		informationStream.println(text);
	}

	@Override
	public void logError(final String message) {
		collectedMessage.append(message);
	}

	@Override
	public void logException(final Exception exception) {
		collectedMessage.append("Exception:\n" + exception.getMessage() + "\nStack trace:\n");
		for (final StackTraceElement elem : exception.getStackTrace()) {
			collectedMessage.append(elem.toString()).append('\n');
		}
	}

	@Override
	public void writeMessageToLog() {
		errorStream.println(collectedMessage.toString());
		collectedMessage.setLength(0);
		collectedMessage.trimToSize();
	}

}
