/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titan.designer.wizards.GUIProjectImporter.IncludedProject;
import org.eclipse.titan.designer.wizards.GUIProjectImporter.ProjectInformation;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Kristof Szabados
 * */
public class TITANProjectImportWizard extends BasicNewResourceWizard implements IImportWizard {
	private static final String NEWPROJECT_WINDOWTITLE = "Import new TITAN Project from .prj file";
	private static final String NEWPROJECT_TITLE = "Create a TITAN Project";
	private static final String NEWPROJECT_DESCRIPTION = "Create a new TITAN project in the workspace or in an external location";

	private TITANProjectImportMainPage mainPage;
	private TITANProjectImportPage newProjectPage;
	private TITANProjectImportRecursivelyPage recursivelyPage;
	private boolean wasAutoBuilding;
	private IConfigurationElement config;

	public TITANProjectImportWizard() {
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		wasAutoBuilding = description.isAutoBuilding();
		description.setAutoBuilding(false);
		try {
			ResourcesPlugin.getWorkspace().setDescription(description);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		Activator.getDefault().pauseHandlingResourceChanges();
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new TITANProjectImportMainPage(NEWPROJECT_WINDOWTITLE);
		mainPage.setTitle(NEWPROJECT_TITLE);
		mainPage.setDescription(NEWPROJECT_DESCRIPTION);
		addPage(mainPage);
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		if (page == mainPage) {
			if (newProjectPage == null) {
				newProjectPage = new TITANProjectImportPage(NEWPROJECT_WINDOWTITLE);
				newProjectPage.setTitle(NEWPROJECT_TITLE);
				newProjectPage.setDescription(NEWPROJECT_DESCRIPTION);
				mainPage.setNewProjectPage(newProjectPage);
				addPage(newProjectPage);
			}

			return newProjectPage;
		} else if (page == newProjectPage) {
			if (recursivelyPage == null) {
				recursivelyPage = new TITANProjectImportRecursivelyPage(NEWPROJECT_WINDOWTITLE);
				recursivelyPage.setTitle(NEWPROJECT_TITLE);
				recursivelyPage.setDescription(NEWPROJECT_DESCRIPTION);
				addPage(recursivelyPage);
			}

			return recursivelyPage;
		}

		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		if (newProjectPage == null) {
			return false;
		}

		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		URI targetLocation = null;
		if (!newProjectPage.useDefaults()) {
			targetLocation = newProjectPage.getLocationURI();
		}
		final IProject newProject = GUIProjectImporter.createNewProject(newProjectPage.getProjectHandle(), mainPage.getInformation(),
				targetLocation);

		if (newProject == null) {
			IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
			if (description.isAutoBuilding() != wasAutoBuilding) {
				description.setAutoBuilding(wasAutoBuilding);
				try {
					ResourcesPlugin.getWorkspace().setDescription(description);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
			Activator.getDefault().resumeHandlingResourceChanges();
			return true;
		}

		try {
			TITANNature.addTITANBuilderToProject(newProject);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		ProjectFileHandler pfHandler;
		pfHandler = new ProjectFileHandler(newProject);
		pfHandler.saveProjectSettings();

		try {
			newProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		BasicNewProjectResourceWizard.updatePerspective(config);
		selectAndReveal(newProject);

		ProjectInformation information = mainPage.getInformation();

		List<IncludedProject> includedProjects = information.getIncludedProjects();
		if (!includedProjects.isEmpty() && (recursivelyPage == null || recursivelyPage.getRecursiveImport())) {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			List<String> processedProjectFiles = new ArrayList<String>();
			processedProjectFiles.add(information.getSourceFile());
			List<IPath> projectFilesToBeProcessed = new ArrayList<IPath>();

			for (IncludedProject includedProject : includedProjects) {
				IPath temp = includedProject.getAbsolutePath();
				if (temp != null) {
					projectFilesToBeProcessed.add(temp);
				}
			}
			while (!projectFilesToBeProcessed.isEmpty()) {
				IPath tempPath = projectFilesToBeProcessed.remove(projectFilesToBeProcessed.size() - 1);
				if (processedProjectFiles.contains(tempPath.toOSString())) {
					continue;
				}

				GUIProjectImporter importer = new GUIProjectImporter();
				ProjectInformation tempProjectInformation = importer
						.loadProjectFile(tempPath.toOSString(), new NullProgressMonitor(),false); //false: not headless
				IPath tempPath2 = tempPath.removeFileExtension();
				String includedProjectName = tempPath2.lastSegment();

				IProject tempProject = workspace.getRoot().getProject(includedProjectName);
				if (tempProject.exists()) {
					continue;
				}

				tempProject = GUIProjectImporter.createNewProject(tempProject, tempProjectInformation, targetLocation);
				if (tempProject == null) {
					continue;
				}

				try {
					TITANNature.addTITANBuilderToProject(tempProject);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}

				pfHandler = new ProjectFileHandler(tempProject);
				pfHandler.saveProjectSettings();

				try {
					tempProject.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}

				includedProjects = tempProjectInformation.getIncludedProjects();
				for (IncludedProject includedProject : includedProjects) {
					IPath temp = includedProject.getAbsolutePath();
					if (temp != null) {
						projectFilesToBeProcessed.add(temp);
					}
				}
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(null, newProject,
						GeneralConstants.PROJECT_PROPERTY_PAGE, null, null);
				if (dialog != null) {
					dialog.open();
				}

				IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
				if (description.isAutoBuilding() != wasAutoBuilding) {
					description.setAutoBuilding(wasAutoBuilding);
					try {
						ResourcesPlugin.getWorkspace().setDescription(description);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
				Activator.getDefault().resumeHandlingResourceChanges();
			}
		});

		return true;
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.selection = selection;
		setNeedsProgressMonitor(true);
		setWindowTitle(NEWPROJECT_WINDOWTITLE);

		super.init(workbench, selection);
	}
}
