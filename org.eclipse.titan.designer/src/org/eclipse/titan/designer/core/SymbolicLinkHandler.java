/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * A library class to handle symbolic link related actions.
 * 
 * @author Kristof Szabados
 * */
public final class SymbolicLinkHandler {
	static final String LINK_EXTENSION = ".lnk";
	static final String EMPTY_STRING = "";
	static final String LINK_CREATION = "ln";
	static final String FORCE_LINK_CREATION = "-sf";
	static final String APOSTROPHE = "'";
	static final String GENERATING_SYMBOLIC_LINKS = "Generating symbolic links";
	static final String TRUE = "true";
	static final String SYMBOLIC_LINK_CREATION_PROCESS = "Symbolic link creation";
	static final String REMOVE = "rm";
	static final String CREATING_OUTDATED_LINK_REMOVAL = "Creating command for removing possibly outdated symbolic links";
	static final String REMOVING_OUTDATED_LINK = "Removing possibly outdated symbolic link";
	static final String FORCE_EXECUTION = "-f";

	/** private constructor to deny instantiation of this class. */
	private SymbolicLinkHandler() {
	}

	/**
	 * This function creates the command that in turn creates the required
	 * symbolic links. If there are two files with the same name in the
	 * resource tree then the symbolic link will be made only to the firstly
	 * visited one (which is actually closer to the root of the resource
	 * tree).
	 * <p>
	 * Symbolic links will be created in the following cases:
	 * <ul>
	 * <li>No symbolic link exists to a file with the same name in the
	 * working directory.
	 * <li>The existing symbolic link points to the wrong place.
	 * </ul>
	 * 
	 * @see #createSymlinks(IProject)
	 * @see #build(int, Map, IProgressMonitor)
	 * 
	 * @param files
	 *                the list of files to be checked
	 * @param workingDirectory
	 *                the base directory where the symbolic links must be
	 *                generated
	 * @param buildJob
	 *                the buildJob to be appended with symbolic link
	 *                creation commands
	 * @param lastTimeRemovedFiles
	 *                HashMap of files that have been removed
	 * @param monitor
	 *                the progress monitor to report errors to.
	 * @param automaticMakefileManagement
	 *                true if automatic makefile handling project property is set
	 */
	public static void addSymlinkCreationCommand(final Map<String, IFile> files, final String workingDirectory, final TITANJob buildJob,
			final Map<String, IFile> lastTimeRemovedFiles, final IProgressMonitor monitor, final boolean automaticMakefileManagement) {
		List<String> symlinkFiles = new ArrayList<String>();
		List<String> command;
		boolean win32 = Platform.OS_WIN32.equals(Platform.getOS());
		final String extension = win32 ? LINK_EXTENSION : EMPTY_STRING;

		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		internalMonitor.beginTask("Checking the symbolic links of files", files.size());
		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		for (IFile file : files.values()) {
			// We  want makefile to be symlinked
			// except if automatic makefile generation is ON
			if (automaticMakefileManagement) {
				if (file.getName().contentEquals("Makefile") || file.getName().contentEquals("makefile")) {
					continue;
				}
			}

			IPath path = file.getLocation();
			if (path == null) {
				try {
					// in case of files which are not local,
					// the symbolic link creation means copy
					file.copy(new Path(workingDirectory + file.getName()), IResource.FORCE, monitor);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While copying the file `" + file.getLocationURI()
							+ "' to the working directory", e);
				}

				internalMonitor.worked(1);
				continue;
			}
			String lastSegment = path.lastSegment();
			boolean tempFileRemoved = lastTimeRemovedFiles.containsKey(lastSegment);
			File tempFile = new File(workingDirectory + File.separatorChar + lastSegment + extension);

			if (tempFile.exists() && !tempFileRemoved) {
				if (win32) {
					symlinkFiles.add(lastSegment);
					internalMonitor.worked(1);
					continue;
				}

				try {
					String canonicalPath = tempFile.getCanonicalPath();
					String absolutePath = tempFile.getAbsolutePath();
					if (!absolutePath.equals(canonicalPath) && path.toString().equals(canonicalPath)) {
						symlinkFiles.add(lastSegment);
						internalMonitor.worked(1);
						continue;
					}
				} catch (IOException e) {
					ErrorReporter.logExceptionStackTrace("While getting the canonical path of `" + tempFile.getName() + "'", e);
				}
			}

			// bugfix: checks if the symbolic link would point to
			// itself
			if (tempFile.getAbsolutePath().equals(file.getLocation().toOSString())) {
				symlinkFiles.add(lastSegment);
				internalMonitor.worked(1);
				continue;
			}

			if (!symlinkFiles.contains(lastSegment)) {
				symlinkFiles.add(lastSegment);
				command = new ArrayList<String>();
				command.add(LINK_CREATION);
				command.add(FORCE_LINK_CREATION);
				command.add(APOSTROPHE
						+ PathConverter.convert(file.getLocation().toOSString(), reportDebugInformation,
								TITANDebugConsole.getConsole()) + APOSTROPHE);
				command.add(APOSTROPHE + lastSegment + APOSTROPHE);
				buildJob.addCommand(command, GENERATING_SYMBOLIC_LINKS);
			}

			internalMonitor.worked(1);
		}

		internalMonitor.done();
	}

	/**
	 * Copies all files coming from external locations (where getlocation()
	 * == null) into the working directory. If a file already exists at the
	 * destination, there is no copying.
	 * 
	 * @param files
	 *                the list of files to process.
	 * @param workingDirectory
	 *                the workingdirectory to put the copy into.
	 * @param monitor
	 *                the progress monitor to report errors to.
	 * */
	public static void copyExternalFileToWorkingDirectory(final Map<String, IFile> files, final String workingDirectory,
			final IProgressMonitor monitor) {

		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		internalMonitor.beginTask("Checking the symbolic links of external files", files.size());

		for (IFile file : files.values()) {
			IPath path = file.getLocation();
			if (path == null) {
				try {
					final URI uri = file.getLocationURI();
					// in case of files which are not local,
					// the symbolic link creation means copy
					final IFileStore source = EFS.getFileSystem(uri.getScheme()).getStore(uri);
					final Path destinationPath = new Path(workingDirectory + File.separator + file.getName());
					if (!destinationPath.toFile().exists()) {
						// FIXME we should somehow
						// detect if a copy is needed
						IFileStore destination = EFS.getLocalFileSystem().getStore(destinationPath);
						source.copy(destination, EFS.OVERWRITE, internalMonitor);
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While processing file `" + file.getName() + "'", e);
				}

				internalMonitor.worked(1);
				continue;
			}
		}

		internalMonitor.done();
	}

	/**
	 * Creates symbolic links for the provided resource and all resources
	 * below it. This must be done when a files exclusion of project status,
	 * or a directory's central storage status changes. The symlinks will
	 * only be created if the "SYMLINKLESS_BUILD_PROPERTY" is not enabled.
	 * 
	 * @param resource
	 *                the resource for which the symbolic links should be
	 *                refreshed
	 * @return true if the refresh succeeded, false otherwise.
	 */
	public static boolean createSymlinks(final IResource resource) {
		IProject rProject = resource.getProject();
		if (!TITANBuilder.isBuilderEnabled(rProject)) {
			return true;
		}

		if (!ProjectBuildPropertyData.useSymbolicLinks(rProject)) {
			return true;
		}


		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(rProject).getWorkingDirectoryPath(true);

		if (workingDir == null || !workingDir.toFile().exists()) {
			return true;
		}

		if (ResourceExclusionHelper.isExcluded(resource) || isInCentralStorage(resource)) {
			return true;
		}

		TITANBuilderResourceVisitor visitor = ProjectBasedBuilder.getProjectBasedBuilder(rProject).getResourceVisitor();

		if (visitor.getFiles().isEmpty()) {
			return true;
		}

		TITANJob buildJob = new TITANJob(SYMBOLIC_LINK_CREATION_PROCESS, visitor.getFiles(), workingDir.toFile(), rProject);
		buildJob.setPriority(Job.DECORATE);
		buildJob.setUser(true);
		buildJob.setRule(rProject);

		SymbolicLinkHandler.addSymlinkCreationCommand(visitor.getFiles(), workingDir.toOSString(), buildJob,
					new HashMap<String, IFile>(), null, ProjectBuildPropertyData.useAutomaticMakefilegeneration(rProject) );


		buildJob.schedule();

		return true;
	}

	/**
	 * Removes all symbolic links in the provided project's working
	 * directory, which point to excluded files.
	 * <p>
	 * Should be used where files/folder can be excluded from build.
	 * 
	 * @param project
	 *                the project for which the symbolic links should be
	 *                removed
	 * 
	 * @return true if the refresh succeeded, false otherwise.
	 */
	public static boolean removeSymlinksForExcluded(final IProject project) {
		if (!TITANBuilder.isBuilderEnabled(project)) {
			return true;
		}

		IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(true);

		if (workingDir == null) {
			return false;
		}

		TITANBuilderResourceVisitor visitor = ProjectBasedBuilder.getProjectBasedBuilder(project).getResourceVisitor();
		Map<String, IFile> excludedFiles = visitor.getExcludedFiles();

		TITANJob buildJob = new TITANJob("symbolic link removal", excludedFiles, workingDir.toFile(), project);
		buildJob.setPriority(Job.DECORATE);
		buildJob.setUser(true);
		buildJob.setRule(project);

		SymbolicLinkHandler.addSymlinkRemovingCommandForExcludedFiles(workingDir.toOSString(), buildJob, excludedFiles,
				new NullProgressMonitor());
		buildJob.schedule();

		return true;
	}

	/**
	 * Creates the command to remove the out-dated files, and adds it to the
	 * other commands that are already in buildJob.
	 * <p>
	 * Used when the symbolic links need to be removed because some files
	 * were removed. As such these symbolic links would be reported by Java
	 * as not existing.
	 * 
	 * @param workingDirectory
	 *                the base directory where the symbolic links must be
	 *                generated
	 * @param job
	 *                the buildJob to be appended with symbolic link
	 *                creation commands
	 * @param files
	 *                HashMap of files that have been removed
	 * @param monitor
	 *                the progress monitor to report errors to.
	 */
	public static void addSymlinkRemovingCommandForRemovedFiles(final String workingDirectory, final TITANJob job,
			final Map<String, IFile> files, final IProgressMonitor monitor) {
		if (files.isEmpty()) {
			return;
		}

		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		File tempFile;
		String extension = Platform.OS_WIN32.equals(Platform.getOS()) ? LINK_EXTENSION : EMPTY_STRING;
		monitor.beginTask(CREATING_OUTDATED_LINK_REMOVAL, files.size());
		for (String key : files.keySet()) {
			tempFile = new File(workingDirectory + File.separatorChar + key + extension);
			List<String> command = new ArrayList<String>();
			command.add(REMOVE);
			command.add(FORCE_EXECUTION);
			command.add(APOSTROPHE
					+ PathConverter.convert(tempFile.getAbsolutePath(), reportDebugInformation, TITANDebugConsole.getConsole())
					+ APOSTROPHE);
			job.addCommand(command, REMOVING_OUTDATED_LINK);
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Creates the command to remove the out-dated files, and adds it to the
	 * other commands that are already in buildJob.
	 * <p>
	 * Used when the symbolic links need to be removed because some files
	 * were excluded from build. This can be optimized as the symbolic links
	 * will be reported as existing files by Java.
	 * 
	 * @param workingDirectory
	 *                the base directory where the symbolic links must be
	 *                generated
	 * @param job
	 *                the buildJob to be appended with symbolic link
	 *                creation commands
	 * @param files
	 *                HashMap of files that have been removed
	 * @param monitor
	 *                the progress monitor to report errors to.
	 */
	public static void addSymlinkRemovingCommandForExcludedFiles(final String workingDirectory, final TITANJob job,
			final Map<String, IFile> files, final IProgressMonitor monitor) {
		if (files.isEmpty()) {
			return;
		}

		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		File tempFile;
		boolean isWindows = Platform.OS_WIN32.equals(Platform.getOS());
		String extension = isWindows ? LINK_EXTENSION : EMPTY_STRING;
		monitor.beginTask(CREATING_OUTDATED_LINK_REMOVAL, files.size());
		String originalLocation;
		for (Map.Entry<String, IFile> entry : files.entrySet()) {
			tempFile = new File(workingDirectory + File.separatorChar + entry.getKey() + extension);
			IPath location = entry.getValue().getLocation();
			if (location == null) {
				continue;
			}
			originalLocation = location.toOSString();
			try {
				if (tempFile.exists() && (isWindows || originalLocation.equals(tempFile.getCanonicalPath()))) {
					List<String> command = new ArrayList<String>();
					command.add(REMOVE);
					command.add(FORCE_EXECUTION);
					command.add(APOSTROPHE
							+ PathConverter.convert(tempFile.getAbsolutePath(), reportDebugInformation,
									TITANDebugConsole.getConsole()) + APOSTROPHE);
					job.addCommand(command, REMOVING_OUTDATED_LINK);
				}
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace("While removing symlink for `" + tempFile.getName() + "'", e);
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Decides if the provided resource is inside a central storage resource
	 * (folder).
	 * 
	 * @param resource
	 *                the resource to check
	 * @return true if none of the parents of the resource is a central
	 *         storage, false otherwise.
	 * */
	public static boolean isInCentralStorage(final IResource resource) {
		IResource temp = resource;
		while (temp.getType() != IResource.PROJECT) {
			if (temp.getType() == IResource.FOLDER) {
				try {
					if (TRUE.equals(temp.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
							FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY)))) {
						return true;
					}
				} catch (CoreException e) {
					return true;
				}
			}
			temp = temp.getParent();
		}

		return false;
	}
}
