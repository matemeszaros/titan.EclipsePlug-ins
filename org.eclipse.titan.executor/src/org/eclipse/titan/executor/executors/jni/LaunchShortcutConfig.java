/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.jni;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.titan.executor.tabpages.maincontroller.JNIMainControllerTab;

/**
 * @author Kristof Szabados
 * */
public final class LaunchShortcutConfig extends org.eclipse.titan.executor.executors.LaunchShortcutConfig {
	@Override
	protected String getConfigurationId() {
		return "org.eclipse.titan.executor.executors.jni.LaunchConfigurationDelegate";
	}

	@Override
	protected String getDialogTitle() {
		return "Select jni mode execution";
	}

	@Override
	public boolean initLaunchConfiguration(ILaunchConfigurationWorkingCopy configuration, IProject project, String configFilePath) {
		return JNIMainControllerTab.initLaunchConfiguration(configuration, project, configFilePath);
	}
}
