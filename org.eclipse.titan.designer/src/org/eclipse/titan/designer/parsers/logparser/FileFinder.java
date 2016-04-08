/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.logparser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * This class merely servers as the locator of file with a given name.
 * 
 * @author Kristof Szabados
 * */
public class FileFinder implements IResourceVisitor {
	IFile targetFile = null;
	String fileName;

	public FileFinder(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the found file or null if none
	 * */
	public IFile getTargetFile() {
		return targetFile;
	}

	/**
	 * Visits the provided resource and all of it's children to find the specified file.
	 * 
	 * @param resource the resource to start searching at
	 * */
	@Override
	public boolean visit(final IResource resource) {
		if (resource.getType() == IResource.FILE) {
			final IFile file = (IFile) resource;
			if (file.getName().equals(fileName)) {
				targetFile = file;
			}
		} else {
			return true;
		}
		return false;
	}

}
