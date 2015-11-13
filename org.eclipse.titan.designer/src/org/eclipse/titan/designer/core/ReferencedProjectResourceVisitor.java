/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class ReferencedProjectResourceVisitor implements IResourceVisitor {
	private static final String DOT = ".";
	private static final String TRUE = "true";
	private final IContainer[] workingDirectories;
	private final IPath workingDirectoryPath;
	private String resourcename;
	private final List<IPath> excludedFolders = new ArrayList<IPath>();
	private final List<URI> centralStorages = new ArrayList<URI>();

	private final Map<String, IFile> centralStorageFiles = new HashMap<String, IFile>();
	private final Map<String, IFile> excludedFiles = new HashMap<String, IFile>();
	private final Map<String, IFile> files = new HashMap<String, IFile>();

	private ResourceExclusionHelper helper;

	public ReferencedProjectResourceVisitor(final IContainer[] workingDirectories, final IPath workingDirectoryPath) {
		this.workingDirectories = workingDirectories;
		this.workingDirectoryPath = workingDirectoryPath;
		helper = new ResourceExclusionHelper();
	}

	public Map<String, IFile> getFiles() {
		return files;
	}

	@Override
	public boolean visit(final IResource resource) {
		if (resource == null || !resource.isAccessible()) {
			return false;
		}

		final URI resourceLocation = resource.getLocationURI();
		resourcename = resource.getName();
		if (resourcename == null || resourcename.startsWith(DOT)) {
			return false;
		}

		try {
			URI resolved = resource.getWorkspace().getPathVariableManager().resolveURI(resourceLocation);
			IFileStore store = EFS.getStore(resolved);
			IFileInfo fileInfo = store.fetchInfo();
			if (!fileInfo.exists()) {
				ErrorReporter.logError("The resource `" + resource.getFullPath() + "' points to a non-existing location.");
				return false;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		switch (resource.getType()) {
		case IResource.FILE:
			if (!ResourceExclusionHelper.isDirectlyExcluded((IFile) resource) && !helper.isExcludedByRegexp(resourcename)) {
				boolean inExcluded = false;
				IPath resourceFullPath = resource.getFullPath();
				for (int i = 0; i < excludedFolders.size(); i++) {
					IPath excludedFolder = excludedFolders.get(i);
					if (excludedFolder.isPrefixOf(resourceFullPath)) {
						inExcluded = true;
						break;
					}
				}

				IFile file = (IFile) resource;
				if (inExcluded) {
					excludedFiles.put(resource.getName(), file);
					return false;
				}

				boolean inCentralStorage = false;
				for (int i = 0; i < centralStorages.size(); i++) {
					URI centralFolder = centralStorages.get(i);
					if (centralFolder.getHost() == resourceLocation.getHost() && resourceLocation.getPath().startsWith(centralFolder.getPath())) {
						inCentralStorage = true;
						break;
					}
				}
				file = (IFile) resource;
				if (inCentralStorage) {
					if (file.getLocation() == null) {
						centralStorageFiles.put(file.getName(), file);
					} else {
						centralStorageFiles.put(file.getLocation().toOSString(), file);
					}
				} else {
					files.put(resourcename, file);
				}
			}
			return false;
		case IResource.FOLDER:
			for (IContainer workingDirectory : workingDirectories) {
				if (workingDirectory.equals(resource)) {
					return false;
				}
			}

			try {
				if (ResourceExclusionHelper.isDirectlyExcluded((IFolder) resource) || helper.isExcludedByRegexp(resourcename)) {
					excludedFolders.add(resource.getFullPath());
				}

				if (TRUE.equals(resource.getPersistentProperty(new QualifiedName(FolderBuildPropertyData.QUALIFIER,
						FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY)))) {
					centralStorages.add(resourceLocation);
				}
			} catch (CoreException e) {
				return false;
			}
			break;
		default:
		}
		return true;
	}

}
