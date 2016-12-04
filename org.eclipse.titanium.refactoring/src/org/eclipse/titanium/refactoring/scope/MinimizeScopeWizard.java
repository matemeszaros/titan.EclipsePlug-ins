/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Wizard for the 'Minimize scope of local variables' refactoring operation.
 * 
 * @author Viktor Varga
 */
public class MinimizeScopeWizard extends RefactoringWizard implements
		IExecutableExtension {
	
	private static final String WIZ_WINDOWTITLE = "Minimize scope of local variables";
	
	private final MinimizeScopeRefactoring refactoring;

	MinimizeScopeWizard(final MinimizeScopeRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);
		this.refactoring = refactoring;
	}
	
	
	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(WIZ_WINDOWTITLE);
		MinimizeScopeWizardOptionsPage optionsPage = 
				new MinimizeScopeWizardOptionsPage(WIZ_WINDOWTITLE, refactoring.getSettings());
		addPage(optionsPage);
	}
}
