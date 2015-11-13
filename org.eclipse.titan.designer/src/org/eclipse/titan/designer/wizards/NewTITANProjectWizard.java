/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.CCompilerOptionsData;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titan.designer.properties.data.TTCN3PreprocessorOptionsData;
import org.eclipse.titan.designer.samples.SampleProject;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Kristof Szabados
 * */
public final class NewTITANProjectWizard extends BasicNewResourceWizard implements IExecutableExtension {

	public static final String NEWTITANPROJECTWIZARD = ProductConstants.PRODUCT_ID_DESIGNER + ".wizards.NewTITANProjectWizard";

	private NewTITANProjectCreationPage mainPage;
	private NewTITANProjectOptionsWizardPage optionsPage;
	private NewTITANProjectContentPage contentPage;
	private IProject newProject;
	private static final String NEWPROJECT_WINDOWTITLE = "New TITAN Project";
	private static final String NEWPROJECT_TITLE = "Create a TITAN Project";
	private static final String NEWPROJECT_DESCRIPTION = "Create a new TITAN project in the workspace or in an external location";
	private static final String CREATING_PROJECT = "creating project";
	private static final String CREATION_FAILED = "Project creation failed";
	private static final String TRUE = "true";

	private boolean wasAutoBuilding;
	private boolean isCreated;
	private IConfigurationElement config;

	public NewTITANProjectWizard() {
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
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
	public void addPages() {
		super.addPages();

		mainPage = new NewTITANProjectCreationPage(NEWPROJECT_WINDOWTITLE);
		mainPage.setTitle(NEWPROJECT_TITLE);
		mainPage.setDescription(NEWPROJECT_DESCRIPTION);
		addPage(mainPage);
		optionsPage = new NewTITANProjectOptionsWizardPage();
		addPage(optionsPage);
		contentPage = new NewTITANProjectContentPage();
		addPage(contentPage);
	}

	/**
	 * @return the path of the project to be created.
	 * */
	IPath getProjectPath() {
		IPath path = mainPage.getLocationPath();
		String name = mainPage.getProjectName();

		return path.append(name);
	}

	/**
	 * Creating a new project.
	 * 
	 * @return the new project created.
	 */
	private IProject createNewProject() {
		IProject tempProjectHandle = mainPage.getProjectHandle();

		URI location = null;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String tempExecutableName = tempProjectHandle.getName();

		final IProject newProjectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(tempExecutableName);

		final IProjectDescription description = workspace.newProjectDescription(tempExecutableName);
		description.setLocationURI(location);

		/*
		 * A new project description in normal conditions does not
		 * contain any natures but as internal behavior tends to change
		 * without notification we can not rely on it.
		 */
		List<String> newIds = new ArrayList<String>();
		newIds.addAll(Arrays.asList(description.getNatureIds()));
		int index = newIds.indexOf(TITANNature.NATURE_ID);
		if (index == -1) {
			newIds.add(TITANNature.NATURE_ID);
			newIds.add(TITANNature.LOG_NATURE_ID);
		}

		description.setNatureIds(newIds.toArray(new String[newIds.size()]));

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(final IProgressMonitor monitor) throws CoreException {
				createProject(description, newProjectHandle, monitor);

				String sourceFolder = optionsPage.getSourceFolder();
				if (!"".equals(sourceFolder)) {
					IFolder folder = newProjectHandle.getFolder(sourceFolder);
					if (!folder.exists()) {
						try {
							folder.create(true, true, null);
						} catch (CoreException e) {
							ErrorReporter.logExceptionStackTrace(e);
						}
					}
					final SampleProject sample = contentPage.getSampleProject();
					if (sample != null) {
						sample.setupProject(newProjectHandle.getProject(), folder);
						ProjectFileHandler pfHandler = new ProjectFileHandler(newProjectHandle.getProject());
						pfHandler.saveProjectSettings();
					}
					if (optionsPage.isExcludeFromBuildSelected()) {
						folder.setPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
								FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY), TRUE);
					}
				}

				newProjectHandle.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY), TRUE);
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
				ErrorReporter.parallelErrorDisplayInMessageDialog(CREATION_FAILED, t.getMessage());
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}

	/**
	 * Creating a new project.
	 * 
	 * @param description
	 *                - IProjectDescription that belongs to the newly
	 *                created project.
	 * @param projectHandle
	 *                - a project handle that is used to create the new
	 *                project.
	 * @param monitor
	 *                - reference to the monitor object
	 * @exception CoreException
	 *                    thrown if access to the resources throws a
	 *                    CoreException.
	 * @exception OperationCanceledException
	 *                    if the operation was canceled by the user.
	 */
	protected void createProject(final IProjectDescription description, final IProject projectHandle, final IProgressMonitor monitor)
			throws CoreException {
		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
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

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(NEWPROJECT_WINDOWTITLE);
	}

	@Override
	public boolean performFinish() {
		if (!isCreated) {
			createNewProject();
		}

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
			return false;
		}

		try {
			TITANNature.addTITANBuilderToProject(newProject);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		try {
			newProject.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY), optionsPage.getWorkingFolder());

			String executable = MakefileCreationData.getDefaultTargetExecutableName(newProject);

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
		BasicNewProjectResourceWizard.updatePerspective(config);
		selectAndReveal(newProject);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(Display.getDefault());
				PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, newProject,
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
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) {
		this.config = config;
	}
}
