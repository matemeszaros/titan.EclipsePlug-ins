/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * @author Kristof Szabados
 * */
public final class NewTITANProjectCreationPage extends WizardNewProjectCreationPage {

	public NewTITANProjectCreationPage(final String pageName) {
		super(pageName);
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

		String projectName = getProjectName();
		for (int i = 0; i < projectName.length(); i++) {
			char c = projectName.charAt(i);
			final boolean isLetter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
			final boolean isDigit = c >= '0' && c <= '9';
			final boolean isSeparator = c == '_' || c == '-' || c == '.';
			if (!(isDigit || isLetter || isSeparator)) {
				setErrorMessage("The build system of TITAN currently does not support projects whose name contains the character '"
						+ c + "'");
				return false;
			}
		}

		URI uri = getLocationURI();
		IPath path = URIUtil.toPath(uri);
		if (path == null) {
			setErrorMessage("The build system of TITAN currently does not support completely remote projects");
			return false;
		}

		setErrorMessage(null);
		return true;
	}
}
