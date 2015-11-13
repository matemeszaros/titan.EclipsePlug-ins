/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.library;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.utils.AnalyzerCache;

/**
 * This class stores library functions to help writing test that involve projects without actual Eclipse knowledge.
 * */
public class ProjectHandlingLibrary {
	/** The project associated with this library object */
	private IProject project;
	
	public ProjectHandlingLibrary(final IProject project) {
		this.project = project;
	}
	
	/** Starts the analysis of its project and waits until it finishes. */
	public void analyzeProject() {
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		WorkspaceJob job = sourceParser.analyzeAll();
		
		while (job == null) {
			try {
				Thread.sleep(500);
				job = sourceParser.analyzeAll();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		
		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		
		ProjectConfigurationParser configParser = GlobalParser.getConfigSourceParser(project);
		job = configParser.analyzeAll();
		
		while (job == null) {
			try {
				Thread.sleep(500);
				job = configParser.analyzeAll();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		
		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		
		AnalyzerCache.withPreference().analyzeProject(new NullProgressMonitor(), project).showAll();
		
		try {
			Thread.sleep(2000); // Wait for the markers to show up
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
	
	/**
	 * Collects markers of a given kind from the resources contained in this project.
	 * 
	 * @param type the kind of marker to collect
	 * 
	 * @return the markers found in the project with the given type
	 * */
	public IMarker[] getMarkers(final String type) throws CoreException {
		return project.findMarkers(type, true, IResource.DEPTH_INFINITE);
	}
}
