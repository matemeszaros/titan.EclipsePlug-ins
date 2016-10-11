/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * @author Szabolcs Beres
 * @author Jeno Balasko
 * */
public abstract class TitanLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public ILaunch getLaunch(final ILaunchConfiguration configuration, final String mode) throws CoreException {
		return new Launch(configuration, mode, null);
	}

	@Override
	protected IProject[] getBuildOrder(final ILaunchConfiguration configuration, final String mode) throws CoreException {

		IResource[] resources = configuration.getMappedResources();
		if( resources == null) { return null; }
		final List<IProject> result = new ArrayList<IProject>();
		for (final IResource resource : resources) {
			if (resource instanceof IProject) {
				result.add((IProject) resource);
			}
		}

		return computeReferencedBuildOrder(result.toArray(new IProject[result.size()]));
	}

	@Override
	protected IProject[] getProjectsForProblemSearch(final ILaunchConfiguration configuration, final String mode) throws CoreException {
		return getBuildOrder(configuration, mode);
	}

	/**
	 * Shows the Titan Executing Perspective to the user if the running is not
	 * in headless mode
	 */
	protected void showExecutionPerspective() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return; // headless mode
		}
		IWorkbench workbench = PlatformUI.getWorkbench();

		try {
			workbench.showPerspective("org.eclipse.titan.executor.perspectives.ExecutingPerspective", workbench.getActiveWorkbenchWindow());
		} catch (WorkbenchException e) {
			ErrorDialog.openError(null, "Selecting Titan Executor Perspective failed", e.getMessage(), e.getStatus());
		}
	}

}
