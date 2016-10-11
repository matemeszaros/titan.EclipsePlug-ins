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
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Kristof Szabados
 * */
public final class SingleMainControllerTab extends BaseMainControllerTab {

	public SingleMainControllerTab(final ILaunchConfigurationTabGroup tabGroup) {
		super(tabGroup);
		workingDirectoryRequired = true;
		executableRequired = true;
	}

	@Override
	public boolean canSave() {
		if (EMPTY.equals(executableFileText.getStringValue()) || !executableFileIsValid) {
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

		if (EMPTY.equals(executableFileText.getStringValue())) {
			setErrorMessage("The executable file must be set.");
			return false;
		} else if (!executableFileIsValid) {
			setErrorMessage("The executable file is not valid.");
			return false;
		}

		if (!executableIsForSingleMode) {
			setErrorMessage("The executable was built for parallel mode execution, it can not be launched in a single mode launcher.");
			return false;
		}
		if (!executableIsExecutable) {
			setErrorMessage("The executable is not actually executable. Please set an executable generated for single mode execution as the executable.");
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	/**
	 * Initializes the provided launch configuration for single mode execution.
	 *
	 * @param configuration the configuration to initialize.
	 * @param project the project to gain data from.
	 * @param configFilePath the path of the configuration file.
	 * */
	public static boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration, final IProject project, final String configFilePath) {
		boolean result = BaseMainControllerTab.initLaunchConfiguration(configuration, project, configFilePath, true);
		if (!result) {
			return false;
		}

		final String executable = getExecutableForProject(project);

		if ("".equals(executable)) {
			ErrorReporter.parallelErrorDisplayInMessageDialog(
				"An error was found while creating the default launch configuration for project " + project.getName(),
				"The executable file must be set.");
			return false;
		}
		return true;
	}
}
