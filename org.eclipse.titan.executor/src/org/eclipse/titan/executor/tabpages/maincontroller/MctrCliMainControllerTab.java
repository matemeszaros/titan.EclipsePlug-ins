/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.maincontroller;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

/**
 * @author Kristof Szabados
 * */
public final class MctrCliMainControllerTab extends BaseMainControllerTab {

	public MctrCliMainControllerTab(final ILaunchConfigurationTabGroup tabGroup) {
		super(tabGroup);

		workingDirectoryRequired = true;
	}

	@Override
	public boolean canSave() {
		if (!EMPTY.equals(executableFileText.getStringValue()) && !executableFileIsValid) {
			return false;
		}

		return super.canSave();
	}

	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		boolean result = super.isValid(launchConfig);
		if (!result) {
			return false;
		}

		if (!EMPTY.equals(executableFileText.getStringValue()) && !executableFileIsValid) {
			setErrorMessage("The executable file is not valid.");
			return false;
		}

		if (executableIsForSingleMode) {
			setErrorMessage("The executable was built for single mode execution, it can not be launched in a parallel mode launcher.");
			return false;
		}
		if (!executableIsExecutable) {
			setErrorMessage("The executable is not actually executable. Please set an executable generated for parallel mode execution as the executable.");
			return false;
		}
		return true;
	}

	/**
	 * Initializes the provided launch configuration for mctr_cli mode execution.
	 *
	 * @param configuration the configuration to initialize.
	 * @param project the project to gain data from.
	 * @param configFilePath the path of the configuration file.
	 * */
	public static boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration, final IProject project, final String configFilePath) {
		return BaseMainControllerTab.initLaunchConfiguration(configuration, project, configFilePath, false);
	}
}
