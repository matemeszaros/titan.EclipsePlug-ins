/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * This class tries weather a project underwent such changes between 2 build
 * operations that might require a dependency update or not.
 * 
 * @author Kristof Szabados
 * */
public final class GlobalProjectStructureTracker {
	private static Map<IProject, ProjectStructureDataCollector> savedInformations = new HashMap<IProject, ProjectStructureDataCollector>();
	private static Map<IProject, ProjectStructureDataCollector> dynamicInformations = new HashMap<IProject, ProjectStructureDataCollector>();
	private static Map<IProject, Boolean> dependencyChanges = new HashMap<IProject, Boolean>();

	/** private constructor to disable instantiation */
	private GlobalProjectStructureTracker() {
	}

	/**
	 * This function is called when a project is changed.
	 * <p>
	 * Compares the actually known information to the ones known to be
	 * saved, and if a difference exists the dependency changes is noted.
	 * 
	 * @param file
	 *                the file the was just now saved.
	 * */
	public static void projectChanged(final IProject project) {
		if (project == null) {
			return;
		}

		dependencyChanges.remove(project);

		if (!dynamicInformations.containsKey(project)) {
			return;
		}

		ProjectStructureDataCollector dynamicCollector = dynamicInformations.get(project);
		ProjectStructureDataCollector savedCollector = savedInformations.get(project);
		savedInformations.put(project, dynamicCollector);
		dynamicInformations.remove(project);

		if (dynamicCollector == null || savedCollector == null) {
			dependencyChanges.put(project, Boolean.TRUE);
			return;
		}

		if (savedCollector.importations.size() != dynamicCollector.importations.size()) {
			dependencyChanges.put(project, Boolean.TRUE);
			return;
		}

		List<String> savedImports;
		List<String> dynamicimports;
		for (String from : savedCollector.importations.keySet()) {
			if (!dynamicCollector.importations.containsKey(from)) {
				dependencyChanges.put(project, Boolean.TRUE);
				return;
			}

			savedImports = savedCollector.importations.get(from);
			dynamicimports = dynamicCollector.importations.get(from);

			if (savedImports.size() != dynamicimports.size()) {
				dependencyChanges.put(project, Boolean.TRUE);
				return;
			}

			for (int i = 0; i < savedImports.size(); i++) {
				if (!savedImports.get(i).equals(dynamicimports.get(i))) {
					dependencyChanges.put(project, Boolean.TRUE);
					return;
				}
			}
		}

		dependencyChanges.put(project, Boolean.FALSE);
	}

	/**
	 * This function is called when an editor is saving a file to the disc.
	 * <p>
	 * Compares the actually known information to the ones known to be
	 * saved, and if a difference exists the dependency changes is noted.
	 * 
	 * @param file
	 *                the file the was just now saved.
	 * */
	public static void saveFile(final IFile file) {
		if (file == null) {
			return;
		}

		IProject project = file.getProject();
		projectChanged(project);
	}

	/**
	 * Returns a project structure data collector for the given project.
	 * 
	 * @param project
	 *                the project that is to be analyzed
	 * 
	 * @return a project structure data collector related to the project
	 *         (might be new or might need update only)
	 * */
	public static ProjectStructureDataCollector getDataCollector(final IProject project) {
		if (dynamicInformations.containsKey(project)) {
			return dynamicInformations.get(project);
		}

		ProjectStructureDataCollector collector = new ProjectStructureDataCollector();
		dynamicInformations.put(project, collector);
		return collector;
	}

	/**
	 * Returns a project structure data collector for the given project with
	 * information about the saved state of the project.
	 * 
	 * @param project
	 *                the project whose information is requested.
	 * 
	 * @return a project structure data collector related to the project
	 *         (might be null)
	 * */
	public static ProjectStructureDataCollector getSavedDataCollector(final IProject project) {
		if (savedInformations.containsKey(project)) {
			return savedInformations.get(project);
		}

		return null;
	}

	/**
	 * Returns whether the project provided has changed its dependency since
	 * the last time or not.
	 * 
	 * @param project
	 *                the project in question.
	 * 
	 * @return false if the dependencies of the project did not change for
	 *         sure, true otherwise.
	 * */
	public static boolean dependencyChanged(final IProject project) {
		if (dependencyChanges.containsKey(project)) {
			Boolean temp = dependencyChanges.get(project);
			return temp.booleanValue();
		}

		return true;
	}
}
