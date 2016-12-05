/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.properties.data.CCompilerOptionsData;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titan.designer.properties.data.TTCN3PreprocessorOptionsData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Wizard for the 'Extract modulepar' operation.
 *
 * @author Viktor Varga
 */
public class ExtractModuleParWizard extends BasicNewResourceWizard implements IExecutableExtension {

	private static final String WIZ_WINDOWTITLE = "Extract module parameters into a new project";
	private static final String WIZ_TITLE = "Create a new project to extract module parameters into";
	private static final String WIZ_DESCRIPTION = "Extract all module parameters and their dependencies into a new project";
	private static final String WORKING_DIR = "bin";
	private static final String SOURCE_DIR = "src";
	private static final String CREATING_PROJECT = "creating project";
	private static final String CREATION_FAILED = "Project creation failed";

	private final String windowTitle;

	private IProject newProject;

	private final boolean wasAutoBuilding;
	private boolean isCreated;
	private IConfigurationElement config;
	private ExtractModuleParWizardMainPage mainPage;

	ExtractModuleParWizard() {
		windowTitle = WIZ_WINDOWTITLE;
		final IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		wasAutoBuilding = description.isAutoBuilding();
		description.setAutoBuilding(false);
		try {
			ResourcesPlugin.getWorkspace().setDescription(description);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		Activator.getDefault().pauseHandlingResourceChanges();
		isCreated = false;
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(windowTitle);
	}

	public IProject getProject() {
		return newProject;
	}

	public boolean getSaveModuleParsOption() {
		if (mainPage != null) {
			return mainPage.getSaveModuleParsOption();
		}
		return false;
	}

	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
		this.config = config;
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new ExtractModuleParWizardMainPage(windowTitle);
		mainPage.setTitle(WIZ_TITLE);
		mainPage.setDescription(WIZ_DESCRIPTION);
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		if (!isCreated) {
			createNewProject();
		}
		if (newProject == null) {
			resetAutobuildOption();
			Activator.getDefault().resumeHandlingResourceChanges();
			return false;
		}
		try {
			TITANNature.addTITANBuilderToProject(newProject);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		try {
			newProject.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY), WORKING_DIR);

			String tempExecutableName = newProject.getName();
			tempExecutableName = tempExecutableName.replace(' ', '_');
			String executable;

			if (newProject.getLocation() == null) {
				final URI projectURI = newProject.getLocationURI();
				final URI uri = TITANPathUtilities.resolvePath(WORKING_DIR, projectURI);

				executable = URIUtil.append(uri, tempExecutableName).toString();
			} else {
				final URI uri = TITANPathUtilities.resolvePathURI(WORKING_DIR, newProject.getLocation().toOSString());
				final IPath workingDir = org.eclipse.core.filesystem.URIUtil.toPath(uri);

				executable = workingDir.append(tempExecutableName).toOSString();
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					executable += ".exe";
				}
			}
			newProject.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY), executable);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}

		ProjectDocumentHandlingUtility.createDocument(newProject);
		ProjectFileHandler pfHandler;
		pfHandler = new ProjectFileHandler(newProject);
		final WorkspaceJob job = pfHandler.saveProjectSettingsJob();

		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		try {
			newProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				/*
				 		//popup project settings dialog
					Shell shell = new Shell(Display.getDefault());
					PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, newProject,
							GeneralConstants.PROJECT_PROPERTY_PAGE, null, null);
					if (dialog != null) {
						dialog.open();
					}
				*/
				resetAutobuildOption();
				Activator.getDefault().resumeHandlingResourceChanges();
				BasicNewProjectResourceWizard.updatePerspective(config);
				selectAndReveal(newProject);
			}
		});

		return true;
	}

	private IProject createNewProject() {
		final IProject tempProjectHandle = mainPage.getProjectHandle();

		URI location = null;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final String tempExecutableName = tempProjectHandle.getName();

		final IProject newProjectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(tempExecutableName);

		final IProjectDescription description = workspace.newProjectDescription(tempExecutableName);
		description.setLocationURI(location);
		TITANNature.addTITANNatureToProject(description);

		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(final IProgressMonitor monitor) throws CoreException {
				createProject(description, newProjectHandle, monitor);

				String sourceFolder = SOURCE_DIR;
				IFolder folder = newProjectHandle.getFolder(sourceFolder);
				if (!folder.exists()) {
					try {
						folder.create(true, true, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}

				newProjectHandle.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY), "true");
				newProjectHandle.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY), "cpp");
				newProjectHandle.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						CCompilerOptionsData.CXX_COMPILER_PROPERTY), "g++");
			}
		};

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (final InvocationTargetException e) {
			final Throwable t = e.getTargetException();
			if (t != null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(new Shell(Display.getDefault()), CREATION_FAILED, t.getMessage());
					}
				});
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}

	private void createProject(final IProjectDescription description, final IProject projectHandle, final IProgressMonitor monitor)
			throws CoreException {
		final IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		try {
			internalMonitor.beginTask(CREATING_PROJECT, 2000);

			projectHandle.create(description, new SubProgressMonitor(internalMonitor, 1000));

			if (internalMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(internalMonitor, 1000));

			projectHandle.refreshLocal(IResource.DEPTH_ONE, internalMonitor);
			isCreated = true;
		} finally {
			internalMonitor.done();
		}
	}

	private void resetAutobuildOption() {
		final IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		if (description.isAutoBuilding() != wasAutoBuilding) {
			description.setAutoBuilding(wasAutoBuilding);
			try {
				ResourcesPlugin.getWorkspace().setDescription(description);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
	}

}
