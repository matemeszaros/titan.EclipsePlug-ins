/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;

/**
 * This class is a resource visitor collecting every file in the project which
 * is not:
 * 
 * <ul>
 * <li>under the working directory.
 * <li>excluded from build.
 * <li>uptodate.
 * </ul>
 * Should be started only on a project.
 * 
 * @see #analyzeAll(IFile, TTCN3Editor, IProgressMonitor)
 * 
 * @author Kristof Szabados
 */
public final class OutdatedFileCollector implements IResourceVisitor {
	public static final String TRUE = "true";
	private static final String DOT = ".";
	private String resourcename;

	private Map<IFile, String> uptodateFiles;
	private Set<IFile> highlySyntaxErroneousFiles;
	private List<IFile> asn1FilesToCheck;
	private List<IFile> cfgFilesToCheck;
	private List<IFile> ttcn3FilesToCheck;
	private List<IFile> ttcninFilesModified;
	private final IContainer[] workingDirectories;
	private ResourceExclusionHelper helper;

	public OutdatedFileCollector(final IContainer[] workingDirectories, final Map<IFile, String> uptodateFiles,
			final Set<IFile> highlySyntaxErroneousFiles) {
		this.uptodateFiles = uptodateFiles;
		this.highlySyntaxErroneousFiles = highlySyntaxErroneousFiles;
		this.asn1FilesToCheck = new ArrayList<IFile>();
		this.cfgFilesToCheck = new ArrayList<IFile>();
		this.ttcn3FilesToCheck = new ArrayList<IFile>();
		this.ttcninFilesModified = new ArrayList<IFile>();
		this.workingDirectories = workingDirectories;

		helper = new ResourceExclusionHelper();
	}

	public List<IFile> getASN1FilesToCheck() {
		return asn1FilesToCheck;
	}

	public List<IFile> getCFGFilesToCheck() {
		return cfgFilesToCheck;
	}

	public List<IFile> getTTCN3FilesToCheck() {
		return ttcn3FilesToCheck;
	}

	public List<IFile> getTtcninFilesModified() {
		return ttcninFilesModified;
	}

	@Override
	public boolean visit(final IResource resource) {
		if (resource == null || !resource.isAccessible()) {
			return false;
		}

		resourcename = resource.getName();
		if (resourcename == null || resourcename.startsWith(DOT)) {
			return false;
		}
		switch (resource.getType()) {
		case IResource.FILE:
			IFile file = (IFile) resource;
			String extension = file.getFileExtension();
			if (!ResourceExclusionHelper.isDirectlyExcluded((IFile) resource) && !uptodateFiles.containsKey(file) && !highlySyntaxErroneousFiles.contains(file)
					&& !helper.isExcludedByRegexp(resourcename)) {
				if (GlobalParser.isSupportedTTCN3Extension(extension)) {
					ttcn3FilesToCheck.add(file);
				} else if (GlobalParser.SUPPORTED_CONFIG_FILE_EXTENSIONS[0].equals(extension)) {
					cfgFilesToCheck.add(file);
				} else if (GlobalParser.SUPPORTED_ASN1_EXTENSIONS[0].equals(extension) || GlobalParser.SUPPORTED_ASN1_EXTENSIONS[1].equals(extension)) {
					asn1FilesToCheck.add(file);
				} else if (GlobalParser.TTCNIN_EXTENSION.equals(extension)) {
					ttcninFilesModified.add(file);
				}
			}
			break;
		case IResource.FOLDER:
			for (IContainer workingDirectory : workingDirectories) {
				if (workingDirectory.equals(resource)) {
					return false;
				}
			}

			if (ResourceExclusionHelper.isDirectlyExcluded((IFolder) resource) || helper.isExcludedByRegexp(resourcename)) {
				return false;
			}
			break;
		default:
		}
		return true;
	}
}
