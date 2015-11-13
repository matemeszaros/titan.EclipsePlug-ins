/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.decorators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;

/**
 * The visitor used by the TITANDecorator.
 * <p>
 * Used to find out if the project has central storages and or preprocessable
 * files.
 * 
 * @see TITANDecorator
 * 
 * @author Kristof Szabados
 */
public final class DecoratorVisitor implements IResourceVisitor {
	static final String TRUE_STRING = "true";
	static final String PREPROCESSABLE_EXTENSION = "ttcnpp";
	static final String INCLUDEABLE_EXTENSION = "ttcnin";

	private boolean hasCentralStorage = false;
	private boolean hasPreprocessable = false;

	private QualifiedName centralStorageQualifier = new QualifiedName(FolderBuildPropertyData.QUALIFIER,
			FolderBuildPropertyData.CENTRAL_STORAGE_PROPERTY);

	@Override
	public boolean visit(final IResource resource) {
		if (!resource.isAccessible()) {
			return false;
		}
		switch (resource.getType()) {
		case IResource.FILE:
			if (!ResourceExclusionHelper.isDirectlyExcluded((IFile) resource)) {
				if (PREPROCESSABLE_EXTENSION.equals(resource.getFileExtension()) || INCLUDEABLE_EXTENSION.equals(resource.getFileExtension())) {
					hasPreprocessable = true;
				}
			}
			break;
		case IResource.FOLDER:
			try {
				if (ResourceExclusionHelper.isDirectlyExcluded((IFolder) resource)) {
					return false;
				}
				if (TRUE_STRING.equals(resource.getPersistentProperty(centralStorageQualifier))) {
					hasCentralStorage = true;
				}
			} catch (CoreException e) {
				return false;
			}
			break;
		default:
		}
		return true;
	}

	public boolean getHasCentralStorage() {
		return hasCentralStorage;
	}

	public boolean getHasPreprecessable() {
		return hasPreprocessable;
	}
}
