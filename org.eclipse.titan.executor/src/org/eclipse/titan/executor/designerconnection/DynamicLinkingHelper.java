/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.designerconnection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.StringUtils;

import java.util.List;

/**
 * @author Kristof Szabados
 * */
public final class DynamicLinkingHelper {
	/** private constructor to disable instantiation */
	private DynamicLinkingHelper() {
	}

	/**
	 * Calculates and returns a list of all reachable projects from the provided one.
	 *
	 * @param referenceChain a reference chain used to detect project reference cycles.
	 * @param actualProject the project being checked.
	 * @param knownProjects the projects already known, they shall not be added any more to the list.
	 * */
	public static void getAllReachableProjects(final List<IProject> referenceChain, final IProject actualProject, final List<IProject> knownProjects) {
		if (knownProjects.contains(actualProject)) {
			return;
		}

		if (referenceChain.contains(actualProject)) {
			knownProjects.add(actualProject);
			return;
		}

		IProject[] referencedProjects;
		if (actualProject.isAccessible()) {
			try {
				referencedProjects = actualProject.getReferencedProjects();
			} catch (CoreException e) {
				referencedProjects = new IProject[]{};
			}
		} else {
			referencedProjects = new IProject[]{};
		}

		if (0 == referencedProjects.length) {
			knownProjects.add(actualProject);
		}

		final int oldSize = referenceChain.size();
		referenceChain.add(actualProject);
		for (IProject tempProject : referencedProjects) {
			getAllReachableProjects(referenceChain, tempProject, knownProjects);
		}
		referenceChain.remove(oldSize);

		if (!knownProjects.contains(actualProject)) {
			knownProjects.add(actualProject);
		}
	}

	/**
	 * Tries to find a project with the provided name.
	 *
	 * @param projectName the name of the project to search for
	 * @return the project selected or null if none.
	 * */
	public static IProject getProject(final String projectName) {
		if (StringUtils.isNullOrEmpty(projectName)) {
			return null;
		}

		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			if (project.isAccessible() && project.getName().equals(projectName)) {
				try {
					if (project.hasNature(DesignerHelper.NATURE_ID)) {
						return project;
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		}

		return null;
	}
}
