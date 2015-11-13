/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.library;

import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class stores library functions to help writing test that involve projects without actual Eclipse knowledge.
 */
public class ProjectHandlingLibrary {

	private static final Logger LOGGER = Logger.getLogger(ProjectHandlingLibrary.class.getName());

	/**
	 * The project associated with this library object
	 */
	private IProject project;

	public ProjectHandlingLibrary(final IProject project) {
		this.project = project;
	}

	/**
	 * Starts the analysis of its project and waits until it finishes.
	 */
	public void analyzeProject() {
		LOGGER.info("Analyzing project: " + project.getName());
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		WorkspaceJob semanticInfoCleanerJob = sourceParser.clearSemanticInformation();
		try {
			semanticInfoCleanerJob.join();
		} catch (InterruptedException e1) {
			ErrorReporter.logExceptionStackTrace("", e1);
		}

		GlobalParser.clearAllInformation();
		LOGGER.info("Semantic analysis started on: " + project.getName());
		WorkspaceJob job = sourceParser.analyzeAll();

		for (int i = 0; i < 10 && job == null; ++i) {
			try {
				Thread.sleep(500);
				job = sourceParser.analyzeAll();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("", e);
			}
		}

		if (job != null) {
			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("", e);
			}
		} else {
			LOGGER.severe("Couldn't analyze the project");
		}

		LOGGER.info("Semantic analysis finished on: " + project.getName());

		LOGGER.info("Analysis of configuration files started");
		ProjectConfigurationParser configParser = GlobalParser.getConfigSourceParser(project);
		job = configParser.analyzeAll();

		for (int i = 0; i < 10 && job == null; ++i) {
			try {
				Thread.sleep(500);
				job = configParser.analyzeAll();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("", e);
			}
		}
		if (job != null) {
			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("", e);
			}
		} else {
			LOGGER.severe("Couldn't analyze configuration files");
		}

		LOGGER.info("Analysis of configuration files finished");

		try {
			Thread.sleep(5000); // Wait for the markers to show up
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace("", e);
		}
		LOGGER.info("Project analyzed: " + project.getName());
	}

	/**
	 * Collects markers of a given kind from the resources contained in this project.
	 *
	 * @param type the kind of marker to collect
	 * @return the markers found in the project with the given type
	 */
	public IMarker[] getMarkers(final String type) throws CoreException {
		return project.findMarkers(type, true, IResource.DEPTH_INFINITE);
	}

	public void clearMarkers(final String type) throws CoreException {
		project.deleteMarkers(type, true, IResource.DEPTH_INFINITE);
	}
}

