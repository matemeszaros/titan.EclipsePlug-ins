/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;

/**
 * @author Kristof Szabados
 * */
public final class TITANDebugConsole {
	private static final String TITLE = "TITAN RUNTIME Debug console";
	private static MessageConsole console = null;

	private TITANDebugConsole() {
	}

	public static synchronized MessageConsole getConsole() {
		if (null == console) {
			console = new MessageConsole(TITLE, null);
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
		}
		return console;
	}
}
