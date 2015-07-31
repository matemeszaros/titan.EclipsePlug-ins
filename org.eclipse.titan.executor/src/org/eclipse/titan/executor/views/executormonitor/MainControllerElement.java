/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeBranch;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeLeaf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Kristof Szabados
 * */
public final class MainControllerElement extends TreeLeaf implements ITreeBranch {
	private final BaseExecutor executor;
	private boolean terminated;

	private InformationElement stateInfo;
	private InformationElement pauseInfo;
	private InformationElement consoleLoggingInfo;

	private List<HostControllerElement> hostControllers;

	public MainControllerElement(final String name, final BaseExecutor executor) {
		super(name);
		this.executor = executor;
		terminated = false;
		hostControllers = new ArrayList<HostControllerElement>(1);
	}

	public BaseExecutor executor() {
		return executor;
	}

	public boolean getTerminated() {
		return terminated;
	}

	public void setTerminated() {
		terminated = true;
		stateInfo = null;
		pauseInfo = null;
		consoleLoggingInfo = null;
		hostControllers.clear();
	}

	public void setStateInfo(final InformationElement stateInfo) {
		this.stateInfo = stateInfo;
		stateInfo.parent(this);
	}

	public void setPauseInfo(final InformationElement pauseInfo) {
		this.pauseInfo = pauseInfo;
		pauseInfo.parent(this);
	}

	public void setConsoleLoggingInfo(final InformationElement consoleLoggingInfo) {
		this.consoleLoggingInfo = consoleLoggingInfo;
		consoleLoggingInfo.parent(this);
	}

	public void addHostController(final HostControllerElement hostController) {
		hostControllers.add(hostController);
		hostController.parent(this);
	}

	@Override
	public List<ITreeLeaf> children() {
		final List<ITreeLeaf> result = new ArrayList<ITreeLeaf>();

		if (null != stateInfo) {
			result.add(stateInfo);
		}
		if (null != pauseInfo) {
			result.add(pauseInfo);
		}
		if (null != consoleLoggingInfo) {
			result.add(consoleLoggingInfo);
		}

		result.addAll(hostControllers);

		return result;
	}

	/**
	 * Transfers the data of a main controller into the actual one.
	 *
	 * @param other the other main controller
	 * */
	public void transferData(final MainControllerElement other) {
		stateInfo = other.stateInfo;
		if (null != stateInfo) {
			stateInfo.parent(this);
		}
		pauseInfo = other.pauseInfo;
		if (null != pauseInfo) {
			pauseInfo.parent(this);
		}
		consoleLoggingInfo = other.consoleLoggingInfo;
		if (null != consoleLoggingInfo) {
			consoleLoggingInfo.parent(this);
		}

		final List<HostControllerElement> oldHCs = hostControllers;
		hostControllers = new ArrayList<HostControllerElement>(other.hostControllers.size());
		for (HostControllerElement tempElement : other.hostControllers) {
			boolean found = false;
			for (Iterator<HostControllerElement> iterator = oldHCs.iterator(); iterator.hasNext() && !found;) {
				final HostControllerElement oldElement = iterator.next();
				if (oldElement.isSame(tempElement)) {
					oldElement.transferData(tempElement);
					hostControllers.add(oldElement);
					iterator.remove();
					found = true;
				}
			}

			if (!found) {
				hostControllers.add(tempElement);
				tempElement.parent(this);
			}
		}
		other.hostControllers.clear();
	}
}
