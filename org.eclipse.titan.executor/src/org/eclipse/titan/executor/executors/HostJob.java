/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.TITANConsole;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.titan.executor.views.notification.Notification;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Date;
import java.util.Formatter;

/**
 * Connects to the a started Host Controllers and redirects its output to the notification view and the TITANConsole.
 * 
 * @author Kristof Szabados
 * */
public final class HostJob extends Job {
	private static final String EMPTY = "";

	private final Process proc;
	private final BaseExecutor executor;

	private boolean firstOutput = false;

	private final StringBuilder builder = new StringBuilder();
	private String fastLine;
	private int fastOffset;

	public HostJob(final String name, final Process proc, final BaseExecutor executor) {
		super(name);
		this.proc = proc;
		this.executor = executor;

		setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		IProcess process = DebugPlugin.newProcess(executor.getLaunchStarted(), proc, getName());

		final IStreamsProxy proxy = process.getStreamsProxy();
		if (null != proxy) {
			final IStreamMonitor outputStreamMonitor = proxy.getOutputStreamMonitor();
			final IStreamListener listener = new IStreamListener() {

				@Override
				public void streamAppended(final String text, final IStreamMonitor monitor) {
					processConsoleOutput(text);
				}

			};
			if (null != outputStreamMonitor) {
				final String temp = outputStreamMonitor.getContents();
				processConsoleOutput(temp);
				outputStreamMonitor.addListener(listener);
			}
		}

		final MessageConsoleStream stream = TITANConsole.getConsole().newMessageStream();
		String line;
		final BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		final BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		try {
			final int exitVal = proc.waitFor();

			if (0 == exitVal) {
				executor.addNotification(new Notification((new Formatter()).format(BaseExecutor.PADDEDDATETIMEFORMAT, new Date()).toString(), EMPTY,
						EMPTY, "Host Controller executed successfully"));
			} else {
				if (stderr.ready()) {
					final String tempDate = (new Formatter()).format(BaseExecutor.PADDEDDATETIMEFORMAT, new Date()).toString();
					executor.addNotification(new Notification(tempDate, EMPTY, EMPTY, "Host Controller execution failed"));
					executor.addNotification(new Notification(tempDate, EMPTY, EMPTY, "  returned with value:" + exitVal));
					executor.addNotification(new Notification(tempDate, EMPTY, EMPTY, "Sent the following error messages:"));
					line = stderr.readLine();
					while (null != line) {
						executor.addNotification(new Notification(tempDate, EMPTY, EMPTY, line));
						line = stderr.readLine();
					}
				}
			}
			proc.destroy();
		} catch (IOException e) {
			stream.println("execution failed beacuse of interrupion");
			ErrorReporter.logExceptionStackTrace(e);
			return Status.CANCEL_STATUS;
		} catch (InterruptedException e) {
			stream.println("execution failed beacuse of interrupion");
			ErrorReporter.logExceptionStackTrace(e);
			return Status.CANCEL_STATUS;
		} finally {
			try {
				stdout.close();
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			try {
				stderr.close();
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		return Status.OK_STATUS;
	}

	public void dispose() {
		proc.destroy();
		done(Status.OK_STATUS);
	}

	/**
	 * Processes the output of the Main Controller
	 * <p>
	 * Note that the output is not reported in full lines.
	 *
	 * @param text the newly reported text
	 *
	 * @see #readFullLineOnly(BufferedReader)
	 * */
	private void processConsoleOutput(final String text) {
		builder.append(text);
		final StringReader reader = new StringReader(builder.toString());
		final BufferedReader stdout = new BufferedReader(reader);
		fastOffset = 0;
		readFullLineOnly(stdout);
		while (null != fastLine) {
			executor.addNotification(new Notification((new Formatter()).format(BaseExecutor.PADDEDDATETIMEFORMAT, new Date()).toString(), "", "", fastLine));
			builder.delete(0, fastOffset);
			if (!firstOutput && null != Activator.getMainView()) {
				Activator.getMainView().refreshIfSelected(executor.mainControllerRoot);
				firstOutput = true;
			}
			fastOffset = 0;
			readFullLineOnly(stdout);
		}

		try {
			stdout.close();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		reader.close();
	}

	/**
	 * Reads and removes a full line from the output. The read line is reported in the fastLine variable.
	 *
	 * @param stdout the buffered output to be processed.
	 *
	 * @see #processConsoleOutput(String)
	 * */
	public void readFullLineOnly(final BufferedReader stdout) {
		try {
			fastLine = stdout.readLine();
			if (null == fastLine) {
				return;
			}
			fastOffset += fastLine.length();
			if (builder.length() < fastOffset + 1) {
				fastLine = null;
				return;
			}
			char c = builder.charAt(fastOffset);
			if ('\n' == c) {
				fastOffset++;
				return;
			} else if ('\r' == c) {
				if (builder.length() > fastOffset + 1 && '\n' == builder.charAt(fastOffset + 1)) {
					fastOffset++;
				}
				fastOffset++;
				return;
			}
			fastLine = null;
			return;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return;
	}
}
