/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.consoles;

import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.SubscribedBoolean;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Kristof Szabados
 * */
public final class TITANDebugConsole {
	private static final String TITLE = "TITAN Debug console";
	private static MessageConsole console = null;

	private static SubscribedBoolean displayTimestamp;
	private static SubscribedBoolean logToSysOut;
	private static boolean inHeadLessMode;
	//private static final String TIMESTAMP_FORMAT = "HH:mm:ss:SSS";
	//private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT);

	static {
		displayTimestamp = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_CONSOLE_TIMESTAMP, false);
		logToSysOut = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_CONSOLE_LOG_TO_SYSOUT, false);
		inHeadLessMode = !PlatformUI.isWorkbenchRunning();
	}

	/** private constructor to disable instantiation */
	private TITANDebugConsole() {
	}

	/**
	 * Creates a new TITAN debug console for regular output.
	 *
	 * @return the console to write on.
	 */
	public static synchronized MessageConsole getConsole() {
		if (console == null) {
			console = new MessageConsole(TITLE, null);
//			{
//				@Override
//				public MessageConsoleStream newMessageStream();
//				{
//					return new TitanDebugConsoleStream(this);
//				}
//
//			};
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
		}
		return console;
	}
	
	public static void clearConsole() {
		if(inHeadLessMode) {
			return;
		}
		getConsole().clearConsole();
	}
	
	public static void println(String message, MessageConsoleStream stream) {
		if(inHeadLessMode) {
			return;
		}
		stream.println(message);
		try {
			stream.flush();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
	
	// It creates a MessageStream just for this println
	public static void println(String... message) {
		if(inHeadLessMode) {
			return;
		}
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < message.length; i++) {
			msg.append(message[i]);
			if (i < message.length-1) {
				msg.append(": ");
			}
		}
		println(msg.toString(), getConsole().newMessageStream());
	}
	
	public static void print(String message, MessageConsoleStream stream) {
		if(inHeadLessMode) {
			return;
		}
		stream.print(message);
	}
	
	// It creates a MessageStream just for this println
	public static void print(String message) {
		if(inHeadLessMode) {
			return;
		}
		print(message, getConsole().newMessageStream());
	}
}
