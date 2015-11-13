/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.mctr.cli;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This thread is used to stimulate the mctr_cli executor timely, to ask the real Main Controller for information.
 * <p>
 * On this way the even if the mctr_cli losses sync with the Main Controller, it will be able to recover it within a user defined timeout limit.
 * 
 * @author Kristof Szabados
 * */
public final class BackgroundThread extends Thread {
	/** Represents whether the counter was restarted because of some interrupt or not. */
	private boolean restarted = false;

	/** Is the thread active or should it stop its execution. */
	private boolean active = true;

	// The executor to refresh timely
	private CliExecutor executor;

	/** The background thread delay. */
	private int delay;

	/** a dummy object to lock. */
	private final Object lock = new Object();

	public BackgroundThread(final String name, final int delay, final CliExecutor executor) {
		super(name);
		setPriority(Thread.MIN_PRIORITY);
		setDaemon(true);
		this.delay = delay * 1000;
		this.executor = executor;
	}

	/** Signal the thread that it should stop as soon as possible. */
	public void cancel() {
		active = false;
	}

	/** Reset the timer of the thread, if it is no longer needed to perform the actually coming refresh operation. */
	public void reset() {
		synchronized (this) {
			restarted = true;
		}

		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public void run() {
		while (active) {
			synchronized (lock) {
				try {
					lock.wait(delay);
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}

			synchronized (this) {
				if (restarted) {
					restarted = false;
					continue;
				}
			}

			if (null != executor) {
				if (executor.isTerminated()) {
					active = false;
				} else {
					executor.info();
				}
			}
		}
	}
}
