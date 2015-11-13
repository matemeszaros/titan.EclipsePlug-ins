/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.console;

import java.io.IOException;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.parsers.Parser;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Class responsible for writing messages to the console
 *
 */
public final class ConsoleWriter {

	//Singleton instance holder
	private static ConsoleWriter instance;

	private MessageConsole console;
	private MessageConsoleStream out;

	private ConsoleWriter() {
		this.console = findConsole(IConsoleConstants.ID_CONSOLE_VIEW);
		if (this.console != null) {
			this.out = this.console.newMessageStream();
			this.out.setActivateOnWrite(true);
		}
	}

	/**
	 * Singleton Constructor
	 * @return
	 */
	public static synchronized ConsoleWriter getInstance() {
		if (instance == null) {
			instance = new ConsoleWriter();
			instance.out.setActivateOnWrite(false);
		}
		return instance;
	}

	/**
	 * Writes to the console
	 * @param msg, message to be written to the console
	 */
	public void writeToConsole(final String msg, final String projectName) {
		// Check with preference store
		PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(projectName);
		if (preferences.getVerbosePrintoutsEnabled()) {
			try {
				this.out.println(msg);
				this.out.flush();

			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler
				.handleException(new TechnicalException(Messages.getString("ConsoleWriter.0") + e.getMessage())); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Disposes the console writer 
	 */
	public void dispose() {
		IOUtils.closeQuietly(out);
	}

	/**
	 * Try to get a console with a given name. If a Console is not found
	 * a new console will be created
	 * @param name of the given console
	 * @return The Console
	 */
	private MessageConsole findConsole(final String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		// Protection, Activator.stop exception...
		if (plugin == null) {
			return null;
		}
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (IConsole anExisting : existing) {
			if (name.equals(anExisting.getName())) {
				return (MessageConsole) anExisting;
			}
		}
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}
	
	public void writeModelData(final String projectName, final Parser parser, final ExecutionModel model, final String filepath) {
		writeToConsole(Messages.getString("ExecutionModel.0") + filepath, projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.1") + parser.getTestCaseRecords(), projectName); //$NON-NLS-1$		
		writeToConsole(Messages.getString("ExecutionModel.2") + model.getNumberOfEvents(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.3") + parser.getPtcs(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.4") + parser.getMaps(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.5") + parser.getCons(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.6") + parser.getSends(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.7") + parser.getRecs(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.10") + parser.getEnqs(), projectName); //$NON-NLS-1$
		writeToConsole(Messages.getString("ExecutionModel.8")
				+ (float) (parser.getEnd() - parser.getStart()) / 1000 + Messages.getString("ExecutionModel.9"), projectName);
		if (parser.wasCanceled()) {
			writeToConsole("The loading of the log data was canceled", projectName);
		}
		writeToConsole("", projectName); //$NON-NLS-1$
	}
}
