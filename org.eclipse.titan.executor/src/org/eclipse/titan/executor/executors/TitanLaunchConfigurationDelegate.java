/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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

/**
 * @author Szabolcs Beres
 * */
public abstract class TitanLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public ILaunch getLaunch(final ILaunchConfiguration configuration, final String mode)
			throws CoreException {
		return new Launch(configuration, mode, null);
	}

	@Override
	protected IProject[] getBuildOrder(final ILaunchConfiguration configuration, final String mode)
			throws CoreException {

		IResource[] resources = configuration.getMappedResources();
		final List<IProject> result = new ArrayList<IProject>();
		for (final IResource resource : resources) {
			if (resource instanceof IProject) {
				result.add((IProject) resource);
			}
		}

		return computeReferencedBuildOrder(result.toArray(new IProject[result.size()]));
	}

	@Override
	protected IProject[] getProjectsForProblemSearch(final ILaunchConfiguration configuration,
			final String mode) throws CoreException {
		return getBuildOrder(configuration, mode);
	}

}
