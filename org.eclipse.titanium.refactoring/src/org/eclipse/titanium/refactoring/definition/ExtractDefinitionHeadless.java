/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titanium.refactoring.Utils;

/**
 * Class for operating the refactoring from a headless environment.
 * <p>
 * Use {@link #run(IProject, Definition, String)} to perform the refactoring operation.
 * </p>
 * 
 * @author Viktor Varga, Istvan Bohm
 * */
public class ExtractDefinitionHeadless {

	private URI location;
	
	public void run(final IProject sourceProj, final Definition selection, final String targetProjName) {
		
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject newProj = workspace.getRoot().getProject(targetProjName);
		final IProjectDescription description = workspace.newProjectDescription(targetProjName);
		description.setLocationURI(location);
		TITANNature.addTITANNatureToProject(description);
		
		if (newProj == null) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Target project is null. ");
			return;
		}
		
		try {
			if (Utils.createProject(description, newProj)) {
				
				try {
					TITANNature.addTITANBuilderToProject(newProj);	
				} catch (CoreException ce) {
					ErrorReporter.logExceptionStackTrace(ce);
					return;
				}
				
				// copy project settings to new project
				final ProjectFileHandler pfh = new ProjectFileHandler(sourceProj);
				if (pfh.projectFileExists()) {
					//IResource.copy(...) is used because ProjectFileHandler.getDocumentFromFile(...) is not working
					final IFile settingsFile = sourceProj.getFile("/" + ProjectFileHandler.XML_TITAN_PROPERTIES_FILE);
					final IFile settingsCopy = newProj.getFile("/" + ProjectFileHandler.XML_TITAN_PROPERTIES_FILE);
					try {
						if (settingsCopy.exists()) {
							settingsCopy.delete(true, new NullProgressMonitor());
						}
						settingsFile.copy(settingsCopy.getFullPath(), true, new NullProgressMonitor());
					} catch (CoreException ce) {
						ErrorReporter.logError("ExtractDefinitionHeadless: Copying project settings to new project failed.");
					}
				}
				
				final ExtractDefinitionRefactoring refactoring = new ExtractDefinitionRefactoring(sourceProj, selection);
				refactoring.setTargetProject(newProj);
				refactoring.perform();
			}
		} catch (CoreException e) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Target project creation was unsuccessful. ");
			ErrorReporter.logExceptionStackTrace(e);
		}	
	}

	public void setLocation(final URI location) {
		this.location = location;
	}
}
