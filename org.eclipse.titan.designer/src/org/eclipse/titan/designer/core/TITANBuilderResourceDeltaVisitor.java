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

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * This visitor simply checks the resource delta, to find out if new files have
 * been added to, or old files have been removed from the project. If either is
 * true a new makefile must be generated.
 * <p>
 * To get rid of the compile file (which is updated by the compiler) every
 * resource's path is stored in a list. If a file with name compile is found in
 * the delta, than every path that can be the prefix of this resource's path is
 * removed from the list. If only the compile file changed than the list will
 * become empty, if some other resources changed too, than some elements will
 * remain.
 * 
 * @author Kristof Szabados
 */
public final class TITANBuilderResourceDeltaVisitor implements IResourceDeltaVisitor {
	static final String[] FILES_NOT_WATCHED = new String[] { "compile", "Makefile", "Makefile.bak" };

	private boolean mandatoryMakefileRebuild = false;
	private final List<String> changedResources = new ArrayList<String>();
	private final Map<String, IFile> lastTimeRemovedFiles = new HashMap<String, IFile>();
	private boolean unAnalyzedFileChanged = false;
	private String makefileScript = null;

	private ProjectConfigurationParser configParser = null;

	public TITANBuilderResourceDeltaVisitor(final IResourceDelta delta) {
		if (delta == null) {
			return;
		}

		IProject project = delta.getResource().getProject();
		if (project == null) {
			return;
		}

		try {
			makefileScript = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY));
			if (makefileScript != null) {
				URI uri = TITANPathUtilities.resolvePathURI(makefileScript, project.getLocation().toOSString());
				makefileScript = URIUtil.toPath(uri).toOSString();
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		configParser = GlobalParser.getConfigSourceParser(project);
	}

	public boolean needsMakefileRebuild() {
		return mandatoryMakefileRebuild;
	}

	public List<String> getChangedResources() {
		return changedResources;
	}

	public Map<String, IFile> getLastTimeRemovedFiles() {
		return lastTimeRemovedFiles;
	}

	public boolean getUnAnalyzedFileChanged() {
		return unAnalyzedFileChanged;
	}

	/**
	 * Checks if the provided file could be dangerous for the build process,
	 * by causing an infinite build loop. And if so, than the file is
	 * removed from the list of changed resources.
	 *
	 * @param file
	 *                the file to check
	 * @return true if the file could have been dangerous
	 * */
	private boolean removeDangerousFiles(final IFile file) {
		boolean foundDangerousFile = false;
		for (int i = 0; i < FILES_NOT_WATCHED.length; i++) {
			if (FILES_NOT_WATCHED[i].equals(file.getFullPath().lastSegment())) {
				foundDangerousFile = true;
				break;
			}
		}
		if (!foundDangerousFile) {
			return false;
		}
		removeFromChangeList(file);
		return true;
	}

	/**
	 * Removes from the list of changes a specific file and all folders in
	 * the path of the file.
	 *
	 * @param file
	 *                the file to be removed.
	 * */
	private void removeFromChangeList(final IFile file) {
		for (int i = changedResources.size() - 1; i >= 0; i--) {
			if ((new Path(changedResources.get(i))).isPrefixOf(file.getFullPath())) {
				changedResources.remove(i);
			}
		}
	}

	@Override
	public boolean visit(final IResourceDelta delta) {
		IResource resource = delta.getResource();
		IPath resourceLocation = resource.getLocation();
		if (resourceLocation == null) {
			return false;
		}

		if (resource.getType() == IResource.FILE) {
			if (removeDangerousFiles((IFile) resource)) {
				return false;
			} else if (configParser != null && configParser.isFileKnown((IFile) resource)) {
				removeFromChangeList((IFile) resource);
				return false;
			}

			final String extension = resource.getFileExtension();
			if (!GlobalParser.SUPPORTED_TTCN3_EXTENSIONS[0].equals(extension)
					&& !GlobalParser.SUPPORTED_TTCN3_EXTENSIONS[1].equals(extension)
					&& !GlobalParser.SUPPORTED_ASN1_EXTENSIONS[0].equals(extension)
					&& !GlobalParser.SUPPORTED_ASN1_EXTENSIONS[1].equals(extension)
					&& !GlobalParser.SUPPORTED_CONFIG_FILE_EXTENSIONS[0].equals(extension)) {
				unAnalyzedFileChanged = true;
			}
		}
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			mandatoryMakefileRebuild = true;
			changedResources.add(delta.getFullPath().toString());
			break;
		case IResourceDelta.CHANGED:
			if (resource.getType() == IResource.FILE && makefileScript != null && makefileScript.equals(resourceLocation.toOSString())) {
				mandatoryMakefileRebuild = true;
			}
			changedResources.add(delta.getFullPath().toString());
			break;
		case IResourceDelta.REMOVED:
			mandatoryMakefileRebuild = true;
			changedResources.add(delta.getFullPath().toString());
			if (resource.getType() == IResource.FILE) {
				lastTimeRemovedFiles.put(resourceLocation.lastSegment(), (IFile) resource);
			}
			break;
		default:
			break;
		}

		return true;
	}

}
