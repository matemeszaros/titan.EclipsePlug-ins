/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Wizard page #1: edit the name of the new project.
 *
 * @author Viktor Varga
 */
public class ExtractDefinitionWizardMainPage extends WizardNewProjectCreationPage {

	public ExtractDefinitionWizardMainPage(final String pageName) {
		super(pageName);
	}

	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		final String projectName = getProjectName();
		if (!projectName.matches("[a-zA-Z0-9[_-]]*")) {
			setErrorMessage("Invalid project name");
			return false;
		}

		setErrorMessage(null);
		return true;
	}

}
