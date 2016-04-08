/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.makefile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * This visitor while analyzing a project also fills the data structures
 * needed by the Makefile generator.
 * <p/>
 * If the project being processed is not the one for which we generate
 * the Makefile, the working directory of the project is treated as a
 * central storage.
 * 
 * @author Szabolcs Beres
 */
public final class MakefileGeneratorVisitor implements IResourceVisitor {

	private final InternalMakefileGenerator makefileGenerator;
	/**
	 * The project being processed.
	 */
	private final IProject projectVisited;
	private final IContainer[] workingDirectories;

	private final String actualWorkingDirectory;
	private final List<URI> centralStorages = new ArrayList<URI>();

	private ResourceExclusionHelper helper;

	public MakefileGeneratorVisitor(final InternalMakefileGenerator makefileGenerator, final IProject project) {
		this.makefileGenerator = makefileGenerator;
		this.projectVisited = project;
		helper = new ResourceExclusionHelper();
		workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);
		IPath path = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryPath(false);
		if (path != null) {
			actualWorkingDirectory = path.toOSString();
		} else {
			actualWorkingDirectory = null;
		}

		if (workingDirectories.length > 0) {
			this.makefileGenerator.addBaseDirectory(workingDirectories[0].getLocation());
		} else if (path != null) {
			this.makefileGenerator.addBaseDirectory(path.toOSString());
		}

		this.makefileGenerator.setAllProjectsUseSymbolicLinks(this.makefileGenerator.isAllProjectsUseSymbolicLinks()
				&& (!ResourceUtils.getBooleanPersistentProperty(
				project, ProjectBuildPropertyData.QUALIFIER, ProjectBuildPropertyData.GENERATE_INTERNAL_MAKEFILE_PROPERTY)
				|| !ResourceUtils.getBooleanPersistentProperty(
				project, ProjectBuildPropertyData.QUALIFIER, ProjectBuildPropertyData.SYMLINKLESS_BUILD_PROPERTY)));
	}

	@Override
	public boolean visit(final IResource resource) throws CoreException {
		if (!resource.isAccessible()) {
			return false;
		}

		URI resourceURI = resource.getLocationURI();

		// Not having a location in the local file system is an
		// error, but should only be reported if the resource is
		// not excluded from build.
		String resourceName = new Path(resourceURI.getPath()).lastSegment();
		if (resourceName.startsWith(".")) {
			return false;
		}

		try {
			URI resolved = resource.getWorkspace().getPathVariableManager().resolveURI(resource.getLocationURI());
			IFileStore store = EFS.getStore(resolved);
			IFileInfo fileInfo = store.fetchInfo();
			if (!fileInfo.exists()) {
				return false;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		switch (resource.getType()) {
			case IResource.FILE:
				if (ResourceExclusionHelper.isDirectlyExcluded((IFile) resource) || helper.isExcludedByRegexp(resourceName)) {
					return false;
				}

				String folder = projectVisited == makefileGenerator.getProject() ? null : actualWorkingDirectory;
				for (URI centralStorage : getCentralStorages()) {
					if (resourceURI.getHost() == centralStorage.getHost() && resourceURI.getPath().startsWith(centralStorage.getPath())) {
						folder = centralStorage.getPath();
						for (BaseDirectoryStruct dir : makefileGenerator.getBaseDirectories()) {
							if (dir.getDirectory() != null && dir.getDirectory().isPrefixOf(resource.getFullPath())) {
								dir.setHasModules(true);
								break;
							}
						}
						break;
					}
				}
				if (resource.getLocation() == null && folder == null) {
					folder = actualWorkingDirectory;
				}

				IFile file = (IFile) resource;
				String extension = file.getFileExtension();
				if ("ttcn3".equals(extension) || "ttcn".equals(extension)) {
					makefileGenerator.addTTCN3Module(file, folder);
				} else if ("asn".equals(extension) || "asn1".equals(extension)) {
					makefileGenerator.addASN1Module(file, folder);
				} else if ("ttcnpp".equals(extension)) {
					makefileGenerator.addPreprocessingModule(file, folder);
				} else if ("ttcnin".equals(extension)) {
					makefileGenerator.addIncludeModule(file, folder);
				} else if ("c".equals(extension) || "cc".equals(extension)) {
					makefileGenerator.addUserSourceFile(file, folder);
				} else if ("h".equals(extension) || "hh".equals(extension)) {
					makefileGenerator.addUserHeaderFile(file, folder);
				} else {
					makefileGenerator.addOtherFiles(file, folder);
				}
				return false;
			case IResource.FOLDER:
				for (IContainer workingDirectory : workingDirectories) {
					if (workingDirectory.equals(resource)) {
						if (projectVisited != makefileGenerator.getProject()) {
							makefileGenerator.addBaseDirectory(resource.getLocation());
						}
						return false;
					}
				}

				if (ResourceExclusionHelper.isDirectlyExcluded((IFolder) resource)
						|| helper.isExcludedByRegexp(resourceName)) {
					return false;
				}

				if (ResourceUtils.getBooleanPersistentProperty(
						resource, FolderBuildPropertyData.QUALIFIER, FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY)) {
					makefileGenerator.addBaseDirectory(resource.getLocation());
					getCentralStorages().add(resourceURI);
				}
				break;
			default:
		}
		return true;
	}

	/**
	 * The list of central storages already encountered Needed to
	 * identify files, which are in central storages.
	 */
	public List<URI> getCentralStorages() {
		return centralStorages;
	}
}
