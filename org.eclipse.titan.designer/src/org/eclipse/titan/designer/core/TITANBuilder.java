/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.common.utils.Cygwin;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.makefile.ExternalMakefileGenerator;
import org.eclipse.titan.designer.core.makefile.InternalMakefileGenerator;
import org.eclipse.titan.designer.decorators.TITANDecorator;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.GlobalProjectStructureTracker;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.IPropertyChangeListener;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * The heart of the build system.
 * 
 * @author Kristof Szabados
 */
public final class TITANBuilder extends IncrementalProjectBuilder {
	private static final String TITAN_GIF = "titan.gif";
	public static final String BUILDER_ID = ProductConstants.PRODUCT_ID_DESIGNER + ".core.TITANBuilder";
	static final String EMPTY_STRING = "";
	static final String APOSTROPHE = "'";
	static final String TRUE = "true";
	static final String FALSE = "false";
	static final String ERROR = "error";
	static final String BUILD_PROCESS = "Build process";
	static final String MAKEFILEGENERATOR = "ttcn3_makefilegen";
	static final String BIN_DIRECTORY = "bin";
	static final String MAKE = "make";
	static final String DEP = "dep";
	static final String CHECK = "check";
	static final String CLEAN = "clean";
	static final String MAKE_CHECK = "make check";
	static final String MAKE_COMPILE = "make compile";
	static final String MAKE_DEP = "make dep";
	static final String MAKE_ALL = "make all";
	static final String MAKE_OBJECTS = "make objects";
	static final String MAKE_SHARED_OBJECTS = "make shared_objects";
	static final String MAKE_CLEAN = "make clean";
	static final String CREATE_DEPENDENCY = "Create dependency";
	static final String CREATE_MAKEFILE = "Create makefile";
	static final String MAKEFILE = "Makefile";
	static final String TEMPORARY_MAKEFILE = "Makefile.tmp";
	static final String REMOVE = "rm";
	static final String MOVE = "mv";
	static final String FORCE_EXECUTION = "-f";
	static final String RENAMING_MAKEFILE = "Renaming " + TEMPORARY_MAKEFILE + " to " + MAKEFILE;
	static final String RUNNING_UPDATER = "Running updater script";
	static final String REMOVING_MAKEFILE = "Removing " + MAKEFILE;
	static final String REMOVING_TEMPORARY_MAKEFILE = "Removing " + TEMPORARY_MAKEFILE;
	static final String REMOVING_EXECUTABLE = "Removing outdated executable";
	static final String REMOVE_COMPILE_FILE = "removing compile file";
	static final String BUILD_WITHOUT_MAKEFILE_ERROR = "The build process is not able to execute,"
			+ " as a Makefile was not found in the working directory and automatic Makefile generation is not enabled for project ";
	static final String CLEAN_WITHOUT_MAKEFILE_ERROR = "make clean for project {0} was not issued as the Makefile is missing.";
	static final String PROCESSINGUNITSTOUSENOTSET = "Note:"
			+ " The processing units to use property of this project is not set, but could enhance build speed.";
	static final String FEWPROCESSINGUNITTOUSESET = "Note:"
			+ " The build process is set to use only {0} parallel processing units, while there are {1} available.";

	private static final String MISSING_CYGWIN = "Build failed. No cygwin installation found.";


	private static IPropertyChangeListener listener = new IPropertyChangeListener() {

		@Override
		public void propertyChanged(final IResource resouce) {
			WorkspaceJob refreshJob = new WorkspaceJob("Refreshing built resources") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					boolean proceedingOK = SymbolicLinkHandler.createSymlinks(resouce);
					if (proceedingOK) {
						proceedingOK = TITANBuilder.regenerateMakefile(resouce.getProject());
					}
					if (proceedingOK) {
						proceedingOK = TITANBuilder.removeExecutable(resouce.getProject());
					}
					if (proceedingOK) {
						TITANBuilder.invokeBuild(resouce.getProject());
					}

					return Status.OK_STATUS;
				}
			};
			refreshJob.setPriority(Job.LONG);
			refreshJob.setUser(false);
			refreshJob.setSystem(true);
			refreshJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			refreshJob.schedule();
		}
	};
	static {
		PropertyNotificationManager.addListener(listener);
	}

	/**
	 * This method returns a resource delta visitor that has visited the
	 * project resource delta.
	 * 
	 * 
	 * @param delta
	 *                - the delta to be visited by the visitor
	 * @return the TITANBuilderResourceDeltaVisitor that has visited the
	 *         project resource delta
	 */
	private TITANBuilderResourceDeltaVisitor getDeltaVisitor(final IResourceDelta delta) {
		TITANBuilderResourceDeltaVisitor visitor = new TITANBuilderResourceDeltaVisitor(delta);
		if (delta != null) {
			try {
				delta.accept(visitor);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While visiting resource delta: " + delta, e);
			}
		}
		return visitor;
	}

	/**
	 * Removes the executable of the provided project.
	 * 
	 * @param project
	 *                the project whose executable must be removed
	 * @return true if the operation was successful, false otherwise.
	 */
	public static boolean removeExecutable(final IProject project) {
		return removeExecutable(project, new ArrayList<IProject>(), false);
	}

	/**
	 * Removes the executable of the provided project.
	 * 
	 * @param project
	 *                the project whose executable must be removed
	 * @param processedProjects
	 *                the list of projects already processed, to stop
	 *                cycles.
	 * @return true if the operation was successful, false otherwise.
	 */
	private static boolean removeExecutable(final IProject project, final List<IProject> processedProjects, final boolean sync) {
		try {
			String targetExecutable = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
			return removeExecutable(project, targetExecutable, processedProjects, sync);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Target executable setting could not be read", e);
		}

		return false;
	}

	/**
	 * Removes the out dated executable. This must be done when the name or
	 * the execution mode of the generated executable changes.
	 * 
	 * @param project
	 *                the project whose executable must be removed
	 * @param targetExecutable
	 *                the name of the executable to be deleted
	 * @param processedProjects
	 *                the list of projects already processed, to stop
	 *                cycles.
	 * @return true if the operation was successful, false otherwise.
	 */
	public static boolean removeExecutable(final IProject project, final String targetExecutable, final List<IProject> processedProjects) {
		return removeExecutable(project, targetExecutable, processedProjects, false);
	}

	/**
	 * Removes the out dated executable. This must be done when the name or
	 * the execution mode of the generated executable changes.
	 * 
	 * @param project
	 *                the project whose executable must be removed
	 * @param targetExecutable
	 *                the name of the executable to be deleted
	 * @param processedProjects
	 *                the list of projects already processed, to stop
	 *                cycles.
	 * @param sync
	 *                if true, the function will not return until the
	 *                makefile is removed
	 * @return true if the operation was successful, false otherwise.
	 */
	public static boolean removeExecutable(final IProject project, final String targetExecutable, final List<IProject> processedProjects,
			final boolean sync) {
		if (!isBuilderEnabled(project)) {
			return true;
		}

		if (processedProjects.contains(project)) {
			return true;
		}
		processedProjects.add(project);

		String realTargetExecutable = targetExecutable;

		List<String> command;
		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(false);
		if (workingDir == null || !workingDir.toFile().exists()) {
			return true;
		}

		if (EMPTY_STRING.equals(realTargetExecutable)) {
			realTargetExecutable = project.getName();
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				realTargetExecutable += ".exe";
			}
		}
		if (realTargetExecutable != null && realTargetExecutable.length() > 0) {
			// start preparing the command, which will remove the
			// executable
			command = new ArrayList<String>();
			command.add(REMOVE);
			command.add(FORCE_EXECUTION);

			boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

			if ((new File(realTargetExecutable)).exists()) {
				// if the executable to be removed is given with
				// full path
				command.add(APOSTROPHE
						+ PathConverter.convert(realTargetExecutable, reportDebugInformation, TITANDebugConsole.getConsole())
						+ APOSTROPHE);
			} else if ((new File(workingDir.toOSString() + File.separatorChar + realTargetExecutable)).exists()) {
				// if the executable to be removed is given only
				// with a file name
				command.add(APOSTROPHE
						+ PathConverter.convert(workingDir.toOSString() + File.separatorChar + realTargetExecutable,
								reportDebugInformation, TITANDebugConsole.getConsole()) + APOSTROPHE);
			} else {
				// the file does not seem to exist, so it does
				// not need to be removed ;)
				return true;
			}

			// create and schedule the job, that will execute the
			// command
			TITANJob job = new TITANJob(BUILD_PROCESS, new HashMap<String, IFile>(), workingDir.toFile(), project);
			job.setPriority(Job.DECORATE);
			job.setUser(true);
			job.addCommand(command, REMOVING_EXECUTABLE);
			if (sync) {
				job.runInWorkspace(new NullProgressMonitor());
			} else {
				job.setRule(project);
				job.schedule();
			}
		}

		IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencingProjects();
		for (IProject tempProject : referencedProjects) {
			removeExecutable(tempProject, processedProjects, sync);
		}

		return true;
	}

	/**
	 * Forces the refresh of the Makefile. This must be done when some
	 * project settings changed. (but only in automatic project management
	 * mode)
	 * 
	 * @param project
	 *                the project whose Makefile must be removed.
	 * 
	 * @return true if the operation was successful, false otherwise.
	 */
	public static boolean removeMakefile(final IProject project) {
		return removeMakefile(project, new ArrayList<IProject>());
	}

	/**
	 * Forces the refresh of the Makefile. This must be done when some
	 * project settings changed. (but only in automatic project management
	 * mode)
	 * 
	 * @param project
	 *                the project whose Makefile must be removed.
	 * @param processedProjects
	 *                the list of projects already processed, to stop
	 *                cycles.
	 * 
	 * @return true if the operation was successful, false otherwise.
	 */
	private static boolean removeMakefile(final IProject project, final List<IProject> processedProjects) {
		if (!isBuilderEnabled(project)) {
			return true;
		}

		if (processedProjects.contains(project)) {
			return true;
		}
		processedProjects.add(project);

		String needsMakefile = null;

		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(true);
		if (workingDir == null || !workingDir.toFile().exists()) {
			return true;
		}

		if ( ProjectBuildPropertyData.useAutomaticMakefilegeneration(project) ) {

			if (ProjectBuildPropertyData.useInternalMakefilegeneration(project)) {
				// in case of the internal Makefile generator it
				// is better to run it and refresh the Makefile
				// instead of just deleting it
				// this looks better on the user interface as
				// the file does not disappears from the user.
				InternalMakefileGenerator makefileGenerator = new InternalMakefileGenerator();
				makefileGenerator.generateMakefile(project);
			} else {
				File file = new File(workingDir.toOSString() + File.separatorChar + MAKEFILE);
				if (file.exists()) {
					file.delete();
				}
			}
		}

		IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencingProjects();
		for (IProject tempProject : referencedProjects) {
			removeMakefile(tempProject, processedProjects);
		}

		return true;
	}

	/**
	 * Forces the refresh of the Makefile. This must be done when some
	 * project settings changed. (but only in automatic project management
	 * mode)
	 * 
	 * @param project
	 *                the project whose Makefile must be removed.
	 * 
	 * @return true if the operation was successful, false otherwise.
	 */
	public static boolean regenerateMakefile(final IProject project) {
		return regenerateMakefile(project, new ArrayList<IProject>());
	}

	/**
	 * Forces the refresh of the Makefile. This must be done when some
	 * project settings changed. (but only in automatic project management
	 * mode)
	 * 
	 * @param project
	 *                the project whose Makefile must be removed.
	 * @param processedProjects
	 *                the list of projects already processed, to stop
	 *                cycles.
	 * 
	 * @return true if the operation was successful, false otherwise.
	 */
	private static boolean regenerateMakefile(final IProject project, final List<IProject> processedProjects) {
		if (!isBuilderEnabled(project)) {
			return true;
		}

		if (processedProjects.contains(project)) {
			return true;
		}
		processedProjects.add(project);

		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(true);
		if (workingDir == null || !workingDir.toFile().exists()) {
			return true;
		}

		if (ProjectBuildPropertyData.useAutomaticMakefilegeneration(project)) {

			if (ProjectBuildPropertyData.useInternalMakefilegeneration(project)) {
				// in case of the internal Makefile generator it
				// is better to run it and refresh the Makefile
				// instead of just deleting it
				// this looks better on the user interface as
				// the file does not disappears from the user.
				InternalMakefileGenerator makefileGenerator = new InternalMakefileGenerator();
				makefileGenerator.generateMakefile(project);
			} else {
				final TITANJob buildJob = new TITANJob(BUILD_PROCESS, new HashMap<String, IFile>(), workingDir.toFile(),
						project);
				buildJob.setPriority(Job.DECORATE);
				buildJob.setUser(true);
				buildJob.setRule(project);

				List<String> command = ExternalMakefileGenerator.createMakefilGeneratorCommand(project);
				buildJob.addCommand(command, CREATE_MAKEFILE);

				buildJob.schedule();
			}
		}

		IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencingProjects();
		for (IProject tempProject : referencedProjects) {
			removeMakefile(tempProject, processedProjects);
		}

		return true;
	}

	/**
	 * Cleans the provided project for a rebuild. Involves cleaning,
	 * removing the makefile and touching the project.
	 * 
	 * @param project
	 * @param sync
	 * */
	public static void cleanProjectForRebuild(final IProject project, final boolean sync) {
		if (project == null || project.getWorkspace() == null) {
			// mystic error
			return;
		}

		WorkspaceJob refreshJob = new WorkspaceJob("Build") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				try {
					project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While cleaning `" + project.getName() + "'", e);
				}
				TITANBuilder.removeMakefile(project, new ArrayList<IProject>());
				ProjectBasedBuilder.setForcedMakefileRebuild(project);
				ProjectBasedBuilder.setForcedBuild(project);
				try {
					project.touch(null);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While touching `" + project.getName() + "'", e);
				}

				return Status.OK_STATUS;
			}
		};
		refreshJob.setPriority(Job.LONG);
		refreshJob.setUser(false);
		refreshJob.setSystem(true);
		refreshJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor(TITAN_GIF));

		if (sync) {
			try {
				refreshJob.runInWorkspace(new NullProgressMonitor());
			} catch (final CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		} else {
			refreshJob.setRule(project.getWorkspace().getRoot());
			refreshJob.schedule();
		}
	}

	/**
	 * Marks the provided project to require a rebuild. Deletes the
	 * makefile, and enforces both build and makefile creation for the next
	 * build. But does not invoke the build.
	 * 
	 * @param project
	 *                the project to use.
	 * */
	public static void markProjectForRebuild(final IProject project) {
		if (project == null || project.getWorkspace() == null) {
			// mystic error
			return;
		}

		WorkspaceJob refreshJob = new WorkspaceJob("Build") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				ProjectBasedBuilder.setForcedMakefileRebuild(project);
				ProjectBasedBuilder.setForcedBuild(project);

				return Status.OK_STATUS;
			}
		};
		refreshJob.setPriority(Job.LONG);
		refreshJob.setUser(false);
		refreshJob.setSystem(true);
		refreshJob.setRule(project.getWorkspace().getRoot());
		refreshJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor(TITAN_GIF));
		refreshJob.schedule();
	}

	/**
	 * Tries to build the project if automatic building is turned on This
	 * must be done when some project settings changed.
	 * 
	 * @param project
	 *                the project to be built
	 */
	public static void invokeBuild(final IProject project) {
		if (project == null || project.getWorkspace() == null) {
			// mystic error
			return;
		}

		IWorkspaceDescription description = project.getWorkspace().getDescription();

		if (description != null && description.isAutoBuilding()) {
			invokeForcedBuild(project);
		}
	}

	/**
	 * Tries to build the project. This must be done when some project
	 * settings changed.
	 * 
	 * @param project
	 *                the project to be built
	 */
	public static void invokeForcedBuild(final IProject project) {
		final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().createRule(project);
		try {
			Job.getJobManager().beginRule(rule, null);

			if (project == null || project.getWorkspace() == null) {
				// mystic error
				return;
			}

			WorkspaceJob refreshJob = new WorkspaceJob("Build") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					try {
						project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace("While building `" + project.getName() + "'", e);
					}
					return Status.OK_STATUS;
				}
			};
			refreshJob.setPriority(Job.LONG);
			refreshJob.setUser(false);
			refreshJob.setSystem(true);
			refreshJob.setRule(project.getWorkspace().getRoot());
			refreshJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor(TITAN_GIF));
			refreshJob.schedule();
		} finally {
			Job.getJobManager().endRule(rule);
		}
	}

	/**
	 * Creates a makefile for the build system of TITAN.
	 * 
	 * @param buildJob
	 *                the build job to extend with the commands created
	 *                here.
	 * 
	 * @throws CoreException
	 *                 if this resource fails the reasons include:
	 *                 <ul>
	 *                 <li>properties can not be accessed.
	 *                 </ul>
	 * */
	private void createMakefile(final TITANJob buildJob) throws CoreException {
		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		if (ProjectBuildPropertyData.useInternalMakefilegeneration(getProject())) {
			InternalMakefileGenerator makefileGenerator = new InternalMakefileGenerator();
			makefileGenerator.generateMakefile(getProject());
		} else {
			List<String> command = ExternalMakefileGenerator.createMakefilGeneratorCommand(getProject());
			buildJob.addCommand(command, CREATE_MAKEFILE);
		}

		String makefileScript = getProject().getPersistentProperty(
				new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY));

		if (makefileScript != null && makefileScript.length() > 0) {
			URI uri = TITANPathUtilities.getURI(makefileScript, getProject().getLocation().toOSString());
			makefileScript = URIUtil.toPath(uri).toOSString();

			List<String> command = new ArrayList<String>();
			command.add(REMOVE);
			command.add(FORCE_EXECUTION);
			command.add(TEMPORARY_MAKEFILE);
			buildJob.addCommand(command, REMOVING_TEMPORARY_MAKEFILE);

			command = new ArrayList<String>();
			command.add(APOSTROPHE + PathConverter.convert(makefileScript, reportDebugInformation, TITANDebugConsole.getConsole())
					+ APOSTROPHE);
			command.add(MAKEFILE);
			command.add(TEMPORARY_MAKEFILE);
			buildJob.addCommand(command, RUNNING_UPDATER);

			command = new ArrayList<String>();
			command.add(MOVE);
			command.add(FORCE_EXECUTION);
			command.add(TEMPORARY_MAKEFILE);
			command.add(MAKEFILE);
			buildJob.addCommand(command, RENAMING_MAKEFILE);
		}
	}

	/**
	 * Please let the code below explain itself.
	 * <p>
	 * Process:
	 * <ul>
	 * <li>The resource delta is checked for changes and the project's files
	 * are collected.
	 * <li>Project settings are evaluated and Markers are deleted from the
	 * collected files.
	 * <li>Even in case of autobuild we force the compiler to check the
	 * files, by deleting the .compile file.
	 * <li>If the makefile must be regenerated then, the makefile generating
	 * command and the makefile updater script's command are created
	 * <li>The dependency updating command is created if automatic
	 * dependency update is not turned of by the user explicitly.
	 * <li>The make command is created.
	 * <li>A new TITANJob is scheduled to execute the created commands.
	 * </ul>
	 * 
	 * @see TITANJob
	 * 
	 * @param kind
	 *                the kind of build being requested. Valid values are
	 *                <ul>
	 *                <li>{@link #FULL_BUILD} - indicates a full build.</li>
	 *                <li>{@link #INCREMENTAL_BUILD}- indicates an
	 *                incremental build.</li> <li>{@link #AUTO_BUILD} -
	 *                indicates an automatically triggered incremental build
	 *                (autobuilding on).</li>
	 *                </ul>
	 * @param args
	 *                a table of builder-specific arguments keyed by
	 *                argument name
	 * @param monitor
	 *                the progress monitor to report errors to.
	 * @return the list of projects for which this builder would like deltas
	 *         the next time it is run or <code>null</code> if none
	 * @exception CoreException
	 *                    if this build fails.
	 */
	@Override
	protected IProject[] build(final int kind, @SuppressWarnings("rawtypes") final Map args, final IProgressMonitor monitor) throws CoreException {
		
		IProject project = getProject();
		if (!TITANInstallationValidator.check(true)) {
			return project.getReferencedProjects();
		}

		if (!LicenseValidator.check()) {
			return project.getReferencedProjects();
		}

		if( Cygwin.isMissingInOSWin32() ) {
			ErrorReporter.logError(MISSING_CYGWIN);
			return project.getReferencedProjects();
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean reportDebugInformation = store.getBoolean(PreferenceConstants.DISPLAYDEBUGINFORMATION);

		if (store.getBoolean(PreferenceConstants.TREATONTHEFLYERRORSFATALFORBUILD)) {
			// IF the option to treat on-the-fly errors as fatal for
			// build was set, and we find an error marker, quit the
			// build.
			IMarker[] markers = project.findMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, true, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
					return project.getReferencedProjects();
				}
			}

			markers = project.findMarkers(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, true, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
					return project.getReferencedProjects();
				}
			}
			
			markers = project.findMarkers(GeneralConstants.ONTHEFLY_MIXED_MARKER, true, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
					return project.getReferencedProjects();
				}
			}
		}

		if (!ProjectSourceParser.checkConfigurationRequirements(project, GeneralConstants.COMPILER_ERRORMARKER)) {
			return project.getReferencedProjects();
		}
		
		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		
		
		String consoleActionBeforeBuild = store.getString(PreferenceConstants.CONSOLE_ACTION_BEFORE_BUILD);	 
		
		if ( PreferenceConstantValues.BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS.equals(consoleActionBeforeBuild) ) {
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
			TITANConsole.println(
				"**************************************************************" + 
		        "\n" + timeStamp + ": starting to build " + project.getName() +
		      "\n**************************************************************");
		} else if ( PreferenceConstantValues.BEFORE_BUILD_CLEAR_CONSOLE.equals(consoleActionBeforeBuild) ) {
			TITANConsole.clearConsole();
		} 
		//else: nothing
		
		if (reportDebugInformation) {
			TITANDebugConsole.println("starting to build " + project.getName());
		}

		internalMonitor.beginTask("Build", 6);
		IProgressMonitor initialisationMonitor = new SubProgressMonitor(internalMonitor, 1);
		initialisationMonitor.beginTask("Checking prerequisites", 3);
		if (!isBuilderEnabled(project)) {
			initialisationMonitor.done();
			internalMonitor.done();
			if (reportDebugInformation) {
				TITANDebugConsole.println("Finished building " + project.getName());
			}
			return project.getReferencedProjects();
		}
		initialisationMonitor.worked(1);
		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(true);

		if (workingDir == null) {
			initialisationMonitor.done();
			internalMonitor.done();
			if (reportDebugInformation) {
				TITANDebugConsole.println("Finished building " + project.getName());
			}
			return project.getReferencedProjects();
		}

		File file = new File(workingDir.toOSString());
		if (!file.exists()) {
			file.mkdirs();
		}

		final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(true);
		for (IContainer workingDirectory : workingDirectories) {
			if (workingDirectory != null) {
				if (!workingDirectory.isSynchronized(IResource.DEPTH_ZERO)) {
					workingDirectory.refreshLocal(IResource.DEPTH_ZERO, null);
				}
				if (workingDirectory.isAccessible() && !workingDirectory.isDerived()) {
					try {
						workingDirectory.setDerived(true, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}

		}

		String targetExecutable = project.getPersistentProperty(
				new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
		if (targetExecutable != null && targetExecutable.length() != 0) {
			URI uri = TITANPathUtilities.getURI(targetExecutable, project.getLocation().toOSString());
			IPath path = URIUtil.toPath(uri);
			path = path.removeLastSegments(1);
			targetExecutable = path.toOSString();
			file = new File(targetExecutable);
			if (!file.exists()) {
				if (file.mkdirs()) {
					ErrorReporter.logError("The folder to contain the generated executable could not be created");
				}
			}
		}

		initialisationMonitor.worked(1);
		TITANBuilderResourceDeltaVisitor deltavisitor = getDeltaVisitor(getDelta(project));
		// Makefile needs to be rebuilt if a new resource has been added
		// to or an existing one
		// has been deleted from the project.
		boolean mandatoryMakefileRebuild = deltavisitor.needsMakefileRebuild() || ProjectBasedBuilder.getForcedMakefileRebuild(project);
		int nofChangedResources = deltavisitor.getChangedResources().size();
		if (ProjectBasedBuilder.getForcedBuild(project) || mandatoryMakefileRebuild) {
			nofChangedResources++;
		}
		if (mandatoryMakefileRebuild) {
			IProject[] referencingProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencingProjects();
			for (int i = 0; i < referencingProjects.length; i++) {
				ProjectBasedBuilder.setForcedMakefileRebuild(referencingProjects[i]);
			}
		}

		ProjectBasedBuilder.clearForcedBuild(project);
		ProjectBasedBuilder.clearForcedMakefileRebuild(project);

		// If auto build is on and no resources has been changed auto
		// build still
		// has to be executed for at least two reasons:
		// - After a specific idle time eclipse will delete markers. It
		// is possible that no
		// resource change has been occurred either. So auto build has
		// to continue execution
		// to check the source code and place back markers.
		if (nofChangedResources == 0 && kind != FULL_BUILD) {
			initialisationMonitor.done();
			internalMonitor.done();
			if (reportDebugInformation) {
				TITANDebugConsole.println("Finished building " + project.getName());
			}
			return project.getReferencedProjects();
		}
		initialisationMonitor.worked(1);
		TITANBuilderResourceVisitor visitor = ProjectBasedBuilder.getProjectBasedBuilder(project).getResourceVisitor();
		Map<String, IFile> files = visitor.getFiles();

		initialisationMonitor.done();

		final TITANJob buildJob = new TITANJob(BUILD_PROCESS, files, workingDir.toFile(), project);
		buildJob.setPriority(Job.DECORATE);
		buildJob.setUser(true);
		buildJob.setRule(project);
		buildJob.removeCompilerMarkers();
		buildJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				try {
					boolean buildSucceeded = buildJob.getResult().isOK();
					getProject().setSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE, buildSucceeded);

					TITANDecorator.refreshSelectively(getProject());
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While setting `" + getProject().getName() + "' as up-to-date", e);
				}
			}
		});

		if (PreferenceConstantValues.ONTHEFLYOPTIONREMOVE.equals(store.getString(PreferenceConstants.ONTHEFLYMARKERSAFTERCOMPILER))) {
			buildJob.removeOnTheFlyMarkers();
		}

		IProgressMonitor symboliclinkMonitor = new SubProgressMonitor(internalMonitor, 1);
		symboliclinkMonitor.beginTask("Refreshing symbolic links", 3);
		SymbolicLinkHandler
				.copyExternalFileToWorkingDirectory(files, workingDir.toOSString(), new SubProgressMonitor(symboliclinkMonitor, 1));

		boolean automaticMakefileGeneration = ProjectBuildPropertyData.useAutomaticMakefilegeneration(project);
		boolean useInternalMakefileGeneration = ProjectBuildPropertyData.useInternalMakefilegeneration(project);
		boolean useSymbolicLinks = ProjectBuildPropertyData.useSymbolicLinks(project);

		if (useSymbolicLinks){

			SymbolicLinkHandler.addSymlinkRemovingCommandForRemovedFiles(workingDir.toOSString(), buildJob,
					deltavisitor.getLastTimeRemovedFiles(), new SubProgressMonitor(symboliclinkMonitor, 1));

			SymbolicLinkHandler.addSymlinkCreationCommand(files, workingDir.toOSString(), buildJob, deltavisitor
					.getLastTimeRemovedFiles(), new SubProgressMonitor(symboliclinkMonitor, 1), automaticMakefileGeneration);
		}
		symboliclinkMonitor.done();

		Map<String, IFile> filesOfReferencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project)
				.getFilesofReferencedProjects();

		if (files.isEmpty() && visitor.getCentralStorageFiles().isEmpty() && filesOfReferencedProjects.isEmpty()) {
			buildJob.schedule();
			if (reportDebugInformation) {
				TITANDebugConsole.println("Finished building " + project.getName());
			}
			return project.getReferencedProjects();
		}

		IProgressMonitor filepreparerMonitor = new SubProgressMonitor(internalMonitor, 1);
		filepreparerMonitor.beginTask("removing markers from files", files.size());
		if (PreferenceConstantValues.ONTHEFLYOPTIONREMOVE.equals(store.getString(PreferenceConstants.ONTHEFLYMARKERSAFTERCOMPILER))) {

			List<IFile> outdatedFiles = new ArrayList<IFile>();
			List<IFile> semanticallyOutdatedFiles = new ArrayList<IFile>();
			for (IFile tempFile : files.values()) {
				// As all of the on-the-fly markers will be
				// deleted during the build,
				// we have to mark (outdate) those files with
				// missing markers to be parsed on the
				// subsequent invocation of the on-the-fly
				// parser,
				// which happens when the user is editing an
				// arbitrary file.
				try {
					if (tempFile.findMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, true, IResource.DEPTH_INFINITE).length != 0
							|| tempFile.findMarkers(GeneralConstants.ONTHEFLY_TASK_MARKER, true, IResource.DEPTH_INFINITE).length != 0) {
						outdatedFiles.add(tempFile);
					}
					if (tempFile.findMarkers(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, true, IResource.DEPTH_INFINITE).length != 0) {
						semanticallyOutdatedFiles.add(tempFile);
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While deleting markers from `" + tempFile.getName() + "'", e);
				}
				filepreparerMonitor.worked(1);
			}

			if (!outdatedFiles.isEmpty()) {
				GlobalParser.getProjectSourceParser(project).reportOutdating(outdatedFiles);
			}
			if (!semanticallyOutdatedFiles.isEmpty()) {
				GlobalParser.getProjectSourceParser(project).reportSemanticOutdating(semanticallyOutdatedFiles);
			}

		}
		filepreparerMonitor.done();

		IPath makefile = new Path(workingDir.toOSString() + File.separatorChar + MAKEFILE);
		boolean makefileExists = makefile.toFile().exists();
		// It is true if the makefile is going to exist before the build
		// is started
		boolean makefileWillExist = TRUE.equals(automaticMakefileGeneration) && (mandatoryMakefileRebuild || !makefileExists);

		if (makefileWillExist) {
			if (!"true".equals(useInternalMakefileGeneration)) {
				IProject[] projects = project.getReferencedProjects();
				for (IProject referencedProject : projects) {
					if ( ! ProjectBuildPropertyData.useSymbolicLinks(referencedProject) ) {
						ErrorReporter.logError("Can not generate a makefile to project `" + project.getName()
								+ "' with the external makefile generator as project `" + referencedProject.getName()
								+ "' is set to build without generating symbolic links");
						return projects;
					}
				}
			}
			createMakefile(buildJob);
		}

		String buildLevel = project.getPersistentProperty(
				new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakeAttributesData.BUILD_LEVEL_PROPERTY));
		if (buildLevel == null) {
			buildLevel = MakeAttributesData.BUILD_LEVEL_5;
			project.setPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakeAttributesData.BUILD_LEVEL_PROPERTY),
					MakeAttributesData.BUILD_LEVEL_5);
		}
		
		buildLevel = MakeAttributesData.getBuildLevel(buildLevel);
		
		if (makefileExists || makefileWillExist) {
			List<String> command = new ArrayList<String>();

			String makeFlags = project.getPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakeAttributesData.TEMPORAL_MAKEFILE_FLAGS_PROPERTY));
			String dynamicLinking = project.getPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakefileCreationData.DYNAMIC_LINKING_PROPERTY));
			// Setting proper command for the build level.
			String makeCommand = null;
			if (MakeAttributesData.BUILD_LEVEL_5.equals(buildLevel)) {
				command.add(MAKE_DEP);
				makeCommand = MAKE_DEP;
				buildJob.addCommand(command, makeCommand);
				command = new ArrayList<String>();
				command.add(MAKE_ALL);
				makeCommand = MAKE_ALL;
			} else if (MakeAttributesData.BUILD_LEVEL_4_5.equals(buildLevel)) {
				if (GlobalProjectStructureTracker.dependencyChanged(project) || deltavisitor.getUnAnalyzedFileChanged()) {
					command.add(MAKE_DEP);
					makeCommand = MAKE_DEP;
					buildJob.addCommand(command, makeCommand);
					command = new ArrayList<String>();
				}
				command.add(MAKE_ALL);
				makeCommand = MAKE_ALL;
			} else if (MakeAttributesData.BUILD_LEVEL_4.equals(buildLevel)) {
				command.add(MAKE);
				makeCommand = MAKE;
			} else if (MakeAttributesData.BUILD_LEVEL_3.equals(buildLevel)) {
				command.add(MAKE_DEP);
				makeCommand = MAKE_DEP;
				buildJob.addCommand(command, makeCommand);
				command = new ArrayList<String>();
				if (dynamicLinking != null && TRUE.equals(dynamicLinking)) {
					command.add(MAKE_SHARED_OBJECTS);
					makeCommand = MAKE_SHARED_OBJECTS;
				} else {
					command.add(MAKE_OBJECTS);
					makeCommand = MAKE_OBJECTS;
				}
			} else if (MakeAttributesData.BUILD_LEVEL_2_5.equals(buildLevel)) {
				if (GlobalProjectStructureTracker.dependencyChanged(project) || deltavisitor.getUnAnalyzedFileChanged()) {
					command.add(MAKE_DEP);
					makeCommand = MAKE_DEP;
					buildJob.addCommand(command, makeCommand);
					command = new ArrayList<String>();
				}
				if (dynamicLinking != null && TRUE.equals(dynamicLinking)) {
					command.add(MAKE_SHARED_OBJECTS);
					makeCommand = MAKE_SHARED_OBJECTS;
				} else {
					command.add(MAKE_OBJECTS);
					makeCommand = MAKE_OBJECTS;
				}
			} else if (MakeAttributesData.BUILD_LEVEL_2.equals(buildLevel)) {
				if (dynamicLinking != null && TRUE.equals(dynamicLinking)) {
					command.add(MAKE_SHARED_OBJECTS);
					makeCommand = MAKE_SHARED_OBJECTS;
				} else {
					command.add(MAKE_OBJECTS);
					makeCommand = MAKE_OBJECTS;
				}
			} else if (MakeAttributesData.BUILD_LEVEL_1.equals(buildLevel)) {
				command.add(MAKE_COMPILE);
				makeCommand = MAKE_COMPILE;
			} else if (MakeAttributesData.BUILD_LEVEL_0.equals(buildLevel)) {
				command.add(MAKE_CHECK);
				makeCommand = MAKE_CHECK;
			} else {
				command.add(MAKE_DEP);
				makeCommand = MAKE_DEP;
				buildJob.addCommand(command, makeCommand);
				command = new ArrayList<String>();
				command.add(MAKE_ALL);
				makeCommand = MAKE_ALL;
			}

			int availableProcessors = Runtime.getRuntime().availableProcessors();
			IPreferencesService prefs = Platform.getPreferencesService();
			int processingUnitsToUse = prefs.getInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.PROCESSINGUNITSTOUSE,
					availableProcessors, null);

			if (processingUnitsToUse > 1) {
				command.add(" -j " + processingUnitsToUse);
			}
			if (processingUnitsToUse < availableProcessors) {	
				TITANConsole.println(MessageFormat.format(FEWPROCESSINGUNITTOUSESET, processingUnitsToUse, availableProcessors));
			}

			if (makeFlags != null && !EMPTY_STRING.equals(makeFlags) && makeFlags.length() > 0) {
				command.add(makeFlags);
			}
			buildJob.addCommand(command, makeCommand);
		} else {
			TITANConsole.println(BUILD_WITHOUT_MAKEFILE_ERROR + project.getName());
			ErrorReporter.logError(BUILD_WITHOUT_MAKEFILE_ERROR + project.getName());
		}

		IStatus status = buildJob.runInWorkspace(new SubProgressMonitor(internalMonitor, 3));
		try {
			if (status.isOK()) {
				project.setSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE, true);
			} else {
				project.setSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE, false);
			}
			TITANDecorator.refreshSelectively(project);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting project `" + project.getName() + "' up-to-date", e);
		}
		if (reportDebugInformation) {
			TITANDebugConsole.println("Finished building " + project.getName());
		}

		internalMonitor.done();
		if (buildJob.foundErrors() || internalMonitor.isCanceled()) {
			if (project != null && (kind == FULL_BUILD || kind == INCREMENTAL_BUILD)) {
				final IProject project2 = project;
				WorkspaceJob op = new WorkspaceJob("Touching the project") {
					@Override
					public IStatus runInWorkspace(final IProgressMonitor monitor) {
						try {
							project2.touch(null);
						} catch (CoreException e) {
							ErrorReporter.logExceptionStackTrace("While touching project `" + project2.getName() + "'", e);
						}
						return Status.OK_STATUS;
					}
				};
				op.setPriority(Job.SHORT);
				op.setRule(project2.getWorkspace().getRoot());
				op.setSystem(true);
				op.setUser(false);
				op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor(TITAN_GIF));
				op.schedule();
			}
		}

		return project.getReferencedProjects();
	}

	/**
	 * This function creates and issues the make clean command.
	 * 
	 * @see TITANJob
	 * 
	 * @param monitor
	 *                the progress monitor to report errors to.
	 * @exception CoreException
	 *                    if this build fails.
	 */
	@Override
	protected void clean(final IProgressMonitor monitor) throws CoreException {
		if (!isBuilderEnabled(getProject())) {
			return;
		}

		monitor.beginTask("Cleaning", 2);

		getProject().touch(new SubProgressMonitor(monitor, 1));

		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(getProject()).getWorkingDirectoryPath(true);

		if (workingDir == null) {
			monitor.done();
			return;
		}

		IPath makefile = new Path(workingDir.toOSString() + File.separatorChar + MAKEFILE);
		boolean makefileExists = makefile.toFile().exists();

		if (makefileExists) {
			TITANBuilderResourceVisitor visitor = ProjectBasedBuilder.getProjectBasedBuilder(getProject()).getResourceVisitor();

			TITANJob titanJob = new TITANJob(MAKE_CLEAN, visitor.getFiles(), workingDir.toFile(), getProject());
			titanJob.setPriority(Job.DECORATE);
			titanJob.setUser(true);
			titanJob.setRule(getProject());

			List<String> command = new ArrayList<String>();
			command.add(MAKE_CLEAN);
			titanJob.addCommand(command, MAKE_CLEAN);

			titanJob.runInWorkspace(new SubProgressMonitor(monitor, 1));
		} else {
			TITANConsole.println(MessageFormat.format(CLEAN_WITHOUT_MAKEFILE_ERROR, getProject().getName()));
			ErrorReporter.logError(MessageFormat.format(CLEAN_WITHOUT_MAKEFILE_ERROR, getProject().getName()));
		}

		monitor.done();
	}

	/**
	 * This function checks the project for the TITANBuilder.
	 * 
	 * @param project
	 *                the project we would like to build
	 * @return whether the project has the TITANBuilder enabled on it, or
	 *         not.
	 */
	public static boolean isBuilderEnabled(final IProject project) {
		if (!project.isAccessible()) {
			return false;
		}

		IProjectDescription description;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			return false;
		}

		ICommand[] cmds = description.getBuildSpec();
		for (int i = 0; i < cmds.length; i++) {
			if (BUILDER_ID.equals(cmds[i].getBuilderName())) {
				return true;
			}
		}

		return false;
	}
}
