/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * @author Kristof Szabados
 * */
public class TITANProjectImportPage extends WizardNewProjectCreationPage {
	private String workingDirectory;

	public TITANProjectImportPage(final String pageName) {
		super(pageName);
	}

	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.WizardNewProjectCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		IProject handle = getProjectHandle();
		IPath path = handle.getLocation();
		if (path == null) {
			String name = handle.getName();
			IPath path2 = handle.getWorkspace().getRoot().getLocation();
			path2 = path2.append(name);
			if (path2.toFile().exists()) {
				setErrorMessage("A folder or file with that name already exists in the workspace.");
				return false;
			}
		}

		if (workingDirectory == null) {
			setErrorMessage(null);
			return true;
		}

		URI locUri = getLocationURI();
		locUri = locUri.normalize();
		URI uri2 = URIUtil.toURI(workingDirectory);
		uri2 = uri2.normalize();
		if (locUri != null && locUri.equals(uri2)) {
			setErrorMessage("The working directory of the project and its location can not be the same folder.");
			return false;
		}

		setErrorMessage(null);
		return true;
	}
}
