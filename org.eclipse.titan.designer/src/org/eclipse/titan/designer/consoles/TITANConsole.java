/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.consoles;

import java.io.IOException;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Kristof Szabados
 * */
public final class TITANConsole {
	private static final String TITLE = "TITAN console";
	private static MessageConsole console = null;
	private static boolean inHeadLessMode;

	static {
		inHeadLessMode = !PlatformUI.isWorkbenchRunning();
	}

	/** private constructor to disable instantiation */
	private TITANConsole() {
	}

	/**
	 * Creates a new TITAN console for regular output.
	 *
	 * @return the console to write on.
	 */
	public static synchronized MessageConsole getConsole() {
		if (console == null) {
			console = new MessageConsole(TITLE, null);
//			{
//				@Override
//				public MessageConsoleStream newMessageStream() 
//				{
//					return new TitanConsoleStream(this);
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
	
	public static void println(final String message, final MessageConsoleStream stream) {
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
	public static void println(final String message) {
		if(inHeadLessMode) {
			return;
		}
		println(message, getConsole().newMessageStream());
	}
	
	public static void print(final String message, final MessageConsoleStream stream) {
		if(inHeadLessMode) {
			return;
		}
		stream.print(message);
	}
	
	// It creates a MessageStream just for this println
	public static void print(final String message) {
		if(inHeadLessMode) {
			return;
		}
		print(message, getConsole().newMessageStream());
	}
}
