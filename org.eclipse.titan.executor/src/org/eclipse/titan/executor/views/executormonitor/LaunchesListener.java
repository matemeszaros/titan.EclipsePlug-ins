/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeBranch;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kristof Szabados
 * */
public final class LaunchesListener implements ILaunchesListener2 {
	private ExecutorMonitorView executorMonitorView;

	public LaunchesListener(final ExecutorMonitorView executorMonitorView) {
		this.executorMonitorView = executorMonitorView;
	}

	@Override
	public void launchesAdded(final ILaunch[] launches) {
		for (ILaunch launch : launches) {
			ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();

			try {
				if (ExecutorMonitorView.isSupportedConfiguration(launchConfiguration)) {
					LaunchElement launchElement;

					if (LaunchStorage.getLaunchElementMap().containsKey(launch)) {
						launchElement = LaunchStorage.getLaunchElementMap().get(launch);
					} else {
						final String name = launchConfiguration.getName() + " [ " + launchConfiguration.getType().getName() + " ]";
						launchElement = new LaunchElement(name, launch);
						LaunchStorage.registerLaunchElement(launchElement);
						ExecutorStorage.registerExecutorStorage(launchElement);
					}

					executorMonitorView.getRoot().addChildToEnd(launchElement);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(" While processing launch configuration " + launchConfiguration.getName(),e);
			}
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				executorMonitorView.getTreeViewer().refresh(executorMonitorView.getRoot());
			}
		});
		executorMonitorView.updateActions();
	}

	@Override
	public void launchesChanged(final ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			ILaunch launched = launches[i];
			List<ITreeLeaf> children = executorMonitorView.getRoot().children();
			for (int j = 0, size = children.size(); i < size; i++) {
				final LaunchElement launchElement = (LaunchElement) children.get(j);
				if (launched.equals(launchElement.launch())) {
					launchElement.changed();
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							executorMonitorView.getTreeViewer().expandToLevel(launchElement, 3);
							executorMonitorView.getTreeViewer().refresh(launchElement);
						}
					});
				}
			}
		}
		executorMonitorView.updateActions();
	}

	@Override
	public void launchesRemoved(final ILaunch[] launches) {
		final TreeBranch root = executorMonitorView.getRoot();
		for (ILaunch launched : launches) {
			for (int i = root.children().size() - 1; i >= 0; i--) {
				final ILaunch temporal = ((LaunchElement) root.children().get(i)).launch();
				if (launched.equals(temporal)) {
					root.children().get(i).dispose();
					root.children().remove(i);
				}
			}
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				executorMonitorView.getTreeViewer().refresh(executorMonitorView.getRoot());
			}
		});
		executorMonitorView.updateActions();
	}

	@Override
	public void launchesTerminated(final ILaunch[] launches) {
		for (ILaunch launched : launches) {
			for (ITreeLeaf element : executorMonitorView.getRoot().children()) {
				final LaunchElement launchElement = (LaunchElement) element;
				if (launched.equals(launchElement.launch())) {
					launchElement.changed();
					MainControllerElement mainController;
					if (1 == ((LaunchElement) element).children().size()) {
						mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
					} else {
						mainController = null;
					}
					if (null != mainController) {
						mainController.executor().terminate(true);
					}
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							executorMonitorView.getTreeViewer().refresh(launchElement);
						}
					});
				}
			}
		}
		executorMonitorView.updateActions();
	}
}
