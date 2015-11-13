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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class is a resource visitor collecting every configuration file in the
 * project.
 * 
 * Should be started only on a project.
 * 
 * @see #internalDoAnalyzeSyntactically(IProgressMonitor)
 * 
 * @author Kristof Szabados
 */
public class OutdatedConfigFileCollector implements IResourceVisitor {
	public static final String TRUE = "true";
	private static final String DOT = ".";
	private String resourcename;

	private Map<IFile, String> uptodateFiles;
	private Set<IFile> highlySyntaxErroneousFiles;
	private List<IFile> cfgFilesToCheck;
	private final IContainer[] workingDirectories;

	public OutdatedConfigFileCollector(final IContainer[] workingDirectories, final Map<IFile, String> uptodateFiles,
			final Set<IFile> highlySyntaxErroneousFiles) {
		this.uptodateFiles = uptodateFiles;
		this.highlySyntaxErroneousFiles = highlySyntaxErroneousFiles;
		this.cfgFilesToCheck = new ArrayList<IFile>();
		this.workingDirectories = workingDirectories;
	}

	public List<IFile> getCFGFilesToCheck() {
		return cfgFilesToCheck;
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
		case IResource.FILE: {
			IFile file = (IFile) resource;
			String extension = file.getFileExtension();
			if (!uptodateFiles.containsKey(file) && !highlySyntaxErroneousFiles.contains(file)) {
				if (GlobalParser.SUPPORTED_CONFIG_FILE_EXTENSIONS[0].equals(extension)) {
					cfgFilesToCheck.add(file);
				}
			}
		}
			break;
		case IResource.FOLDER:
			for (IContainer workingDirectory : workingDirectories) {
				if (workingDirectory.equals(resource)) {
					return false;
				}
			}

			break;
		default:
		}
		return true;
	}
}
