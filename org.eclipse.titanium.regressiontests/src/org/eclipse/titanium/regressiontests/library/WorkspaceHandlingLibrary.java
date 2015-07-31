/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.library;

import java.net.URI;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;

/**
 * This class stores library functions to help writing test that involve the workspace without actual Eclipse knowledge.
 * */
public class WorkspaceHandlingLibrary {
	/** The workspace (most probably the Junit workspace) */
	private static IWorkspace workspace = ResourcesPlugin.getWorkspace();
	
	public static IWorkspace getWorkspace() {
		return workspace;
	}
	
	/**
	 * Imports a "source directory"and all sub elements into the workspace as a project.
	 * <p>
	 * The source directory should be a valid project.
	 * <p>
	 * Please note that the name of the project can be different from the name of the directory.
	 * 
	 * @param projectName the name of the project to be created
	 * @param sourceLocation the location of the directory to be imported
	 * 
	 * @return the imported project
	 * */
	public static IProject importProjectIntoWorkspace(final String projectName, final URI sourceLocation) throws CoreException {
		assertNotNull(projectName);
		assertNotNull(sourceLocation);
		
		IProject project = workspace.getRoot().getProject(projectName);
		IProjectDescription description = workspace.newProjectDescription(projectName);
		description.setLocationURI(sourceLocation);
		project.create(description, null);
		project.open(null);
		ProjectFileHandler handler = new ProjectFileHandler(project);
		handler.loadProjectSettings();
		return project;
	}
	
	/**
	 * Set the state of automatic building in the workspace on or off.
	 * 
	 * @param flag the state of automatic building to be set.
	 * */
	public static void setAutoBuilding(final boolean flag) throws CoreException {
		IWorkspaceDescription workspaceDescrition = workspace.getDescription();
		workspaceDescrition.setAutoBuilding(false);
		workspace.setDescription(workspaceDescrition);
	}
	
	/**
	 * @return the list of projects in the workspace
	 * */
	public static IProject[] getProjectsInWorkspace() {
		return workspace.getRoot().getProjects();
	}
}
