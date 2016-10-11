/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.mctr.cli;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.titan.executor.tabpages.hostcontrollers.HostControllersTab;
import org.eclipse.titan.executor.tabpages.maincontroller.MctrCliMainControllerTab;
import org.eclipse.titan.executor.tabpages.performance.MctrCliPerformanceSettingsTab;
import org.eclipse.titan.executor.tabpages.testset.TestSetTab;

/**
 * @author Kristof Szabados
 * */
public final class LaunchConfigurationTabGroup implements ILaunchConfigurationTabGroup {
	private ILaunchConfigurationTab[] tabs;

	@Override
	public void createTabs(final ILaunchConfigurationDialog arg0, final String arg1) {
		tabs = new ILaunchConfigurationTab[] {new MctrCliMainControllerTab(this), new HostControllersTab(this), new TestSetTab(),
				new MctrCliPerformanceSettingsTab(), new EnvironmentTab(), new CommonTab()};
	}

	@Override
	public void dispose() {
		if (null != tabs) {
			for (ILaunchConfigurationTab tab : tabs) {
				tab.dispose();
			}
		}
	}

	@Override
	public ILaunchConfigurationTab[] getTabs() {
		return tabs;
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration arg0) {
		for (ILaunchConfigurationTab tab : tabs) {
			tab.initializeFrom(arg0);
		}
	}

	@Override
	public void launched(final ILaunch arg0) {
	}

	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy arg0) {
		for (ILaunchConfigurationTab tab : tabs) {
			tab.performApply(arg0);
		}
	}

	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy arg0) {
		for (ILaunchConfigurationTab tab : tabs) {
			tab.setDefaults(arg0);
		}
	}

}
