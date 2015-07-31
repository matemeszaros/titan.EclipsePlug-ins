/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.hostcontrollers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.HostController;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.titan.executor.GeneralConstants.HOSTCOMMANDS;
import static org.eclipse.titan.executor.GeneralConstants.HOSTEXECUTABLES;
import static org.eclipse.titan.executor.GeneralConstants.HOSTNAMES;
import static org.eclipse.titan.executor.GeneralConstants.HOSTWORKINGDIRECTORIES;

/**
 * @author Kristof Szabados
 * */
public final class HostControllerContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		HostController[] elements = new HostController[0];
		ILaunchConfiguration config = (ILaunchConfiguration) inputElement;
		List<String> hostNames;
		List<String> hostWorkingDirectories;
		List<String> hostExecutables;
		List<String> hostLoginCommands;
		try {
			hostNames = config.getAttribute(HOSTNAMES, (ArrayList<String>) null);
			hostWorkingDirectories = config.getAttribute(HOSTWORKINGDIRECTORIES, (ArrayList<String>) null);
			hostExecutables = config.getAttribute(HOSTEXECUTABLES, (ArrayList<String>) null);
			hostLoginCommands = config.getAttribute(HOSTCOMMANDS, (ArrayList<String>) null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return elements;
		}

		if (null == hostNames || hostNames.isEmpty()) {
			return elements;
		}

		if (null == hostWorkingDirectories || hostWorkingDirectories.size() != hostNames.size()) {
			return elements;
		}

		if (null == hostExecutables || hostExecutables.size() != hostNames.size()) {
			return elements;
		}

		if (null == hostLoginCommands || hostLoginCommands.size() != hostNames.size()) {
			return elements;
		}

		elements = new HostController[hostNames.size()];
		for (int i = 0, size = hostNames.size(); i < size; i++) {
			elements[i] = new HostController(hostNames.get(i), hostWorkingDirectories.get(i), hostExecutables.get(i), hostLoginCommands.get(i));
		}

		return elements;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}

}
