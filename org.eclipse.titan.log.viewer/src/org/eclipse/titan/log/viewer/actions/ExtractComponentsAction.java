/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.console.ConsoleWriter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
import org.eclipse.titan.log.viewer.extractors.ComponentEvent;
import org.eclipse.titan.log.viewer.extractors.ComponentEventDispatcher;
import org.eclipse.titan.log.viewer.extractors.ComponentExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.utils.LogFileHandler;
import org.eclipse.titan.log.viewer.utils.Messages;

/**
 * Class for the action which extracts test cases
 * from a TITAN Log Viewer log file 
 *
 */
public class ExtractComponentsAction implements IRunnableWithProgress, Observer {

	private IFile logFile;
	private ComponentExtractor componentExtractor;
	private LogFileHandler logFileHandler;
	private LogFileMetaData logFileMetaData;
	private IProgressMonitor monitor;
	private int lastWorked;
	private List<String> components;
	private ComponentEventDispatcher componentEventDispatcher;

	/**
	 * Extracts components from a log file
	 * @param logFile The input file full path
	 */
	public ExtractComponentsAction(final IFile logFile) {
		this.logFile = logFile;
		this.componentExtractor = new ComponentExtractor();
		this.componentEventDispatcher = new ComponentEventDispatcher();
		this.componentExtractor.addObserver(this.componentEventDispatcher);
		
		this.logFileHandler = new LogFileHandler(logFile);
		this.logFileMetaData = null;
		this.lastWorked = 0;
		
		// Add this viewer as an observer
		this.componentEventDispatcher.addObserver(this);
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		this.monitor = monitor;
		this.components = null;

		// First of all, verify that the file is a TITAN supported log file
		try {
			this.logFileMetaData = this.logFileHandler.autoDetect();
		} catch (TechnicalException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new UserException(e.getMessage()));
			return;
		}
		
		try {
			if (this.monitor != null) {
				this.monitor.beginTask(Messages.getString("ExtractComponentsAction.0"), 100); //$NON-NLS-1$
			}

			long start = System.currentTimeMillis();
			this.componentExtractor.extractComponentsFromLogFile(this.logFileMetaData, monitor);
			long stop = System.currentTimeMillis();

			this.components = this.componentExtractor.getComponents();
			// SUT and MTC is expected to always exist in log file
			int numComp = this.components.size() + 2;
			ConsoleWriter.getInstance().writeToConsole(
					Messages.getString("ExtractComponentsAction.1")
							+ numComp + Messages.getString("ExtractComponentsAction.4")
							+ this.logFile.getName() + Messages.getString("ExtractComponentsAction.5")
							+ (stop - start) / 1000.0 + Messages.getString("ExtractComponentsAction.2"),
					this.logFileMetaData.getProjectName());
			ConsoleWriter.getInstance().writeToConsole("", this.logFileMetaData.getProjectName()); //$NON-NLS-1$
		} catch (final IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			// Generate Technical Error
			String errorMsg = Messages.getString("ExtractComponentsAction.3") + //$NON-NLS-1$
			this.logFile.getName()
			+ "Reason: " + e.getMessage(); //$NON-NLS-1$
			TitanLogExceptionHandler.handleException(new TechnicalException(errorMsg));
		} finally {
			this.componentEventDispatcher.deleteObserver(this);
			this.componentExtractor.deleteObserver(this.componentEventDispatcher);
			if (this.monitor != null) {
				this.monitor.done();
			}
		}
	}

	@Override
	public void update(final Observable observable, final Object event) {
		if (event instanceof ComponentEvent) {
			ComponentEvent componentEvent = (ComponentEvent) event;
			if (this.monitor != null) {
				this.monitor.subTask(componentEvent.getCompName());
			}
			int worked = componentEvent.getProgress();
			if (this.monitor != null) {
				this.monitor.worked(worked - this.lastWorked);
			}
			this.lastWorked = worked;
		}
	}
	
	/**
	 * Returns the extracted components
	 * @return the extracted components
	 */
	public List<String> getComponents() {
		return this.components;
	}
}
