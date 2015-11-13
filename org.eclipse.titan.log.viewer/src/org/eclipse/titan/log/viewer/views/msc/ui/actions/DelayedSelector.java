/*******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.swt.widgets.Display;

import org.eclipse.titan.common.logging.ErrorReporter;

class DelayedSelector extends Thread {

	private DelayedSelectable selectable;
	private volatile boolean shouldRun = true;
	private final Object lock = new Object();

	public DelayedSelector(DelayedSelectable selectable) {
		this.selectable = selectable;
	}

	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public Object getLock() {
		return lock;
	}

	@Override
	public void run() {
		while (shouldRun) {
			if (selectable.getDelayedSelection() == null) {
				try {
					synchronized (lock) {
						lock.wait();
					}
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}

			selectable.selectionChanged(selectable.getDelayedSelection());
			selectable.setDelayedSelection(null);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					selectable.run();
				}
			});
			try {
				sleep(100);
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
	}

}
