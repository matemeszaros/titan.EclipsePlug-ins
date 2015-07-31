/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeBranch;
import org.eclipse.titan.executor.executors.TreeLeaf;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.ViewPart;

import java.util.Iterator;

/**
 * @author Kristof Szabados
 * */
public final class ExecutorMonitorView extends ViewPart {
	public static final String EXECUTORMONITOR_VIEW_ID = Activator.PLUGIN_ID + ".views.executorMonitor.ExecutorMonitorView";
	public static final String SINGLE_MODE_LAUNCHCONFIGURATION_ID = Activator.PLUGIN_ID + ".executors.single.LaunchConfigurationDelegate";
	public static final String MCTR_CLI_MODE_LAUNCHCONFIGURATION_ID = Activator.PLUGIN_ID + ".executors.mctr.cli.LaunchConfigurationDelegate";
	public static final String JNI_MODE_LAUNCHCONFIGURATION_ID = Activator.PLUGIN_ID + ".executors.jni.LaunchConfigurationDelegate";
	public static final ILaunchConfigurationType MCTR_CLI_LAUNCHCONFIGURATION_TYPE = DebugPlugin.getDefault().getLaunchManager()
			.getLaunchConfigurationType(MCTR_CLI_MODE_LAUNCHCONFIGURATION_ID);
	public static final ILaunchConfigurationType SINGLE_LAUNCHCONFIGURATION_TYPE = DebugPlugin.getDefault().getLaunchManager()
			.getLaunchConfigurationType(SINGLE_MODE_LAUNCHCONFIGURATION_ID);
	public static final ILaunchConfigurationType JNI_LAUNCHCONFIGURATION_TYPE = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(
			JNI_MODE_LAUNCHCONFIGURATION_ID);

	private TreeViewer viewer;
	private MenuManager manager;
	private ExecutorMonitorContentProvider contentProvider;
	private ExecutorMonitorLabelProvider labelProvider;
	private ILaunchesListener2 launchListener;
	private ISelectionChangedListener selectionChangedListener;

	private TreeBranch root;

	private Action terminateSelectedAction;
	private Action terminateAllAction;
	private Action removeSelectedAction;
	private Action removeAllTerminatedAction;

	@Override
	public void createPartControl(final Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		contentProvider = new ExecutorMonitorContentProvider();
		viewer.setContentProvider(contentProvider);
		labelProvider = new ExecutorMonitorLabelProvider();
		viewer.setLabelProvider(labelProvider);

		createActions();
		manager = new MenuManager("menuManager");
		createEmptyContextMenu();
		createToolBar();

		Activator.setMainView(this);

		viewer.setInput(getInitialInput());
		viewer.expandToLevel(2);

		updateActions();

		getSite().setSelectionProvider(viewer);

		launchListener = new LaunchesListener(this);

		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(launchListener);

		selectionChangedListener = new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ITreeLeaf element = (ITreeLeaf) selection.getFirstElement();

				updateActions();

				if (null == element) {
					return;
				}

				if (element instanceof LaunchElement) {
					createLauncherContextMenu();
				} else if (element instanceof MainControllerElement) {
					createExecutorContextMenu((MainControllerElement) element);

					BaseExecutor executor = ((MainControllerElement) element).executor();
					IProcess process = executor.getProcess();
					if (null == process) {
						return;
					}

					IConsole console = DebugUITools.getConsole(process);
					if (null == console) {
						return;
					}

					IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
					boolean exists = false;
					for (IConsole console2 : consoles) {
						if (console2.equals(console)) {
							exists = true;
						}
					}
					if (!exists) {
						ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
					}
					ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
					ConsolePlugin.getDefault().getConsoleManager().refresh(console);
				} else {
					createEmptyContextMenu();
				}
			}
		};
		viewer.addSelectionChangedListener(selectionChangedListener);
	}

	@Override
	public void dispose() {
		if (null != launchListener) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(launchListener);
		}
		launchListener = null;
		viewer.removeSelectionChangedListener(selectionChangedListener);
		viewer = null;
		Activator.setMainView(null);
		contentProvider = null;
		labelProvider = null;
		removeSelectedAction = null;
		removeAllTerminatedAction = null;
		terminateSelectedAction = null;
		terminateAllAction = null;
		super.dispose();
	}

	@Override
	public void setFocus() {
		// Do nothing
	}

	private void createActions() {
		terminateSelectedAction = new Action("Terminate selected") {
			@Override
			public void run() {
				terminateSelected();
			}
		};
		terminateSelectedAction.setToolTipText("Terminate Selected Launch");
		terminateSelectedAction.setImageDescriptor(ImageCache.getImageDescriptor("terminate_enabled.gif"));
		terminateSelectedAction.setDisabledImageDescriptor(ImageCache.getImageDescriptor("terminate_disabled.gif"));
		terminateSelectedAction.setEnabled(false);

		terminateAllAction = new Action("Terminate all launch") {
			@Override
			public void run() {
				terminateAll();
			}
		};
		terminateAllAction.setToolTipText("Terminate All Lanches");
		terminateAllAction.setImageDescriptor(ImageCache.getImageDescriptor("terminate_all_enabled.gif"));
		terminateAllAction.setDisabledImageDescriptor(ImageCache.getImageDescriptor("terminate_all_disabled.gif"));
		terminateAllAction.setEnabled(false);

		removeSelectedAction = new Action("Remove selected terminated launch") {
			@Override
			public void run() {
				removeSelected();
			}
		};
		removeSelectedAction.setToolTipText("Remove Selected Terminated Launch");
		removeSelectedAction.setImageDescriptor(ImageCache.getImageDescriptor("remove_enabled.gif"));
		removeSelectedAction.setDisabledImageDescriptor(ImageCache.getImageDescriptor("remove_disabled.gif"));
		removeSelectedAction.setEnabled(false);

		removeAllTerminatedAction = new Action("Remove All Terminated Launches") {
			@Override
			public void run() {
				removeAllTerminated();
			}
		};
		removeAllTerminatedAction.setToolTipText("Remove all terminated launches");
		removeAllTerminatedAction.setImageDescriptor(ImageCache.getImageDescriptor("remove_all_enabled.gif"));
		removeAllTerminatedAction.setDisabledImageDescriptor(ImageCache.getImageDescriptor("remove_all_disabled.gif"));
		removeAllTerminatedAction.setEnabled(false);
	}

	private void createEmptyContextMenu() {
		manager.removeAll();
		Menu menu = manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	private void createLauncherContextMenu() {
		manager.removeAll();
		manager.add(removeSelectedAction);
		manager.add(removeAllTerminatedAction);
		manager.add(terminateSelectedAction);
		manager.add(terminateAllAction);
		Menu menu = manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	private void createExecutorContextMenu(final MainControllerElement element) {
		manager.removeAll();
		manager = element.executor().createMenu(manager);
		if (!manager.isEmpty()) {
			manager.add(new Separator());
		}
		manager.add(removeSelectedAction);
		manager.add(terminateSelectedAction);
		Menu menu = manager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	private void createToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(removeSelectedAction);
		toolbarManager.add(removeAllTerminatedAction);
		toolbarManager.add(terminateSelectedAction);
		toolbarManager.add(terminateAllAction);
	}

	/** @return the root of the launched hierarchy */
	public TreeBranch getRoot() {
		return root;
	}

	/** @return the viewer of the launch hierarchy */
	public TreeViewer getTreeViewer() {
		return viewer;
	}

	private TreeBranch getInitialInput() {
		root = new TreeBranch("root");
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		ILaunchConfiguration launchConfiguration;
		LaunchElement launchElement;
		ILaunch launch;
		for (ILaunch launche : launches) {
			launch = launche;
			launchConfiguration = launch.getLaunchConfiguration();

			if (isSupportedConfiguration(launchConfiguration)) {

				if (LaunchStorage.getLaunchElementMap().containsKey(launch)) {
					launchElement = LaunchStorage.getLaunchElementMap().get(launch);
				} else {
					launchElement = new LaunchElement(launchConfiguration.getName(), launch);
					LaunchStorage.registerLaunchElement(launchElement);
					ExecutorStorage.registerExecutorStorage(launchElement);
				}

				if (launch.isTerminated() && ExecutorStorage.getExecutorMap().containsKey(launch)) {
					launchElement.setTerminated();
					ExecutorStorage.getExecutorMap().get(launch).terminate(true);
				}

				root.addChildToEnd(launchElement);
			}
		}
		return root;
	}

	private void terminateSelected() {
		if (null == viewer || viewer.getSelection().isEmpty()) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			ITreeLeaf element = (ITreeLeaf) iterator.next();
			while (!(element instanceof LaunchElement)) {
				element = element.parent();
			}
			ILaunch launched = ((LaunchElement) element).launch();
			ILaunchConfiguration launchConfiguration = launched.getLaunchConfiguration();

			MainControllerElement mainController;
			if (1 == ((LaunchElement) element).children().size()) {
				mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
			} else {
				mainController = null;
			}
			if (isSupportedConfiguration(launchConfiguration) && null != mainController) {
				BaseExecutor executor = mainController.executor();
				if (!executor.isTerminated()) {
					executor.terminate(false);
				}
			}
		}
		updateActions();
	}

	private void terminateAll() {
		if (null == viewer) {
			return;
		}
		for (int i = root.children().size() - 1; i >= 0; i--) {
			LaunchElement element = (LaunchElement) root.children().get(i);
			ILaunch launched = element.launch();
			ILaunchConfiguration launchConfiguration = launched.getLaunchConfiguration();
			MainControllerElement mainController;

			if (1 == (element).children().size()) {
				mainController = (MainControllerElement) (element).children().get(0);
			} else {
				mainController = null;
			}
			if (isSupportedConfiguration(launchConfiguration) && null != mainController) {
				BaseExecutor executor = mainController.executor();
				if (!executor.isTerminated()) {
					executor.terminate(false);
				}
			}
		}
		updateActions();
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(root);
			}
		});
	}

	private void removeSelected() {
		if (null == viewer || viewer.getSelection().isEmpty()) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		viewer.getTree().setRedraw(false);
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			ITreeLeaf element = (ITreeLeaf) iterator.next();
			while (!(element instanceof LaunchElement)) {
				element = element.parent();
			}
			ILaunch launched = ((LaunchElement) element).launch();
			ILaunchConfiguration launchConfiguration = launched.getLaunchConfiguration();

			MainControllerElement mainController;
			if (1 == ((LaunchElement) element).children().size()) {
				mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
			} else {
				mainController = null;
			}

			if (isSupportedConfiguration(launchConfiguration) && null != mainController) {
				BaseExecutor executor = mainController.executor();
				if (executor.isTerminated()) {
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launched);
				}
			}
		}
		viewer.setSelection(null);
		updateActions();
		viewer.getTree().setRedraw(true);
	}

	private void removeAllTerminated() {
		if (null == viewer) {
			return;
		}
		for (int i = root.children().size() - 1; i >= 0; i--) {
			LaunchElement element = (LaunchElement) root.children().get(i);
			ILaunch launched = element.launch();
			if (null == launched) {
//				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launched);
				continue;
			}
			ILaunchConfiguration launchConfiguration = launched.getLaunchConfiguration();

			MainControllerElement mainController;
			if (1 == element.children().size()) {
				mainController = (MainControllerElement) element.children().get(0);
			} else {
				mainController = null;
			}

			if (isSupportedConfiguration(launchConfiguration) && null != mainController) {
				BaseExecutor executor = mainController.executor();
				if (executor.isTerminated()) {
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launched);
				}
			}
		}
		viewer.setSelection(null);
		updateActions();
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(root);
			}
		});
	}

	public void updateActions() {
		if (viewer == null) {
			return;
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				boolean foundTerminatable = false;
				boolean foundRemovable = false;
				ILaunch launched;
				MainControllerElement mainController;
				for (ITreeLeaf element : root.children()) {
					launched = ((LaunchElement) element).launch();
					if (((LaunchElement) element).children().size() == 1) {
						mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
					} else {
						mainController = null;
					}
					ILaunchConfiguration launchConfiguration = launched.getLaunchConfiguration();

					if (isSupportedConfiguration(launchConfiguration) && mainController != null) {
						BaseExecutor executor = mainController.executor();
						foundTerminatable |= !executor.isTerminated();
						foundRemovable |= executor.isTerminated();
					}
				}
				terminateAllAction.setEnabled(foundTerminatable);
				removeAllTerminatedAction.setEnabled(foundRemovable);

				ITreeLeaf element = (ITreeLeaf) selection.getFirstElement();
				if (element != null) {
					while (!(element instanceof LaunchElement)) {
						element = element.parent();
					}
					launched = ((LaunchElement) element).launch();
					if (((LaunchElement) element).children().size() == 1) {
						mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
					} else {
						mainController = null;
					}
					ILaunchConfiguration launchConfiguration = launched.getLaunchConfiguration();

					if (isSupportedConfiguration(launchConfiguration) && mainController != null) {
						BaseExecutor executor = mainController.executor();
						terminateSelectedAction.setEnabled(!executor.isTerminated());
						removeSelectedAction.setEnabled(executor.isTerminated());
					}
				} else {
					removeSelectedAction.setEnabled(false);
					terminateSelectedAction.setEnabled(false);
				}
			}
		});
	}

	/**
	 * Refreshes all elements.
	 * If only one main controller root is found, it will be selected
	 * */
	public void refreshAll() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();

				if (root == null || root.children().size() != 1) {
					return;
				}

				ITreeLeaf element = root.children().get(0);
				if (((LaunchElement) element).children().size() == 1) {
					MainControllerElement mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
					viewer.setSelection(new StructuredSelection(mainController), true);
				}
			}
		});
	}

	/**
	 * Refreshes the element if it is selected.
	 *
	 * @param element the node whose selection is to be refreshed if already selected.
	 * */
	public void refreshIfSelected(final TreeLeaf element) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || element == null) {
					return;
				}

				final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}

				ITreeLeaf tempElement = element;
				while (tempElement != null && !(tempElement instanceof MainControllerElement)) {
					tempElement = tempElement.parent();
				}

				ITreeLeaf selectionElement = (ITreeLeaf) selection.getFirstElement();
				while (selectionElement != null && !(selectionElement instanceof MainControllerElement)) {
					selectionElement = selectionElement.parent();
				}

				if (tempElement == selectionElement) {
					viewer.setSelection(selection, false);
				}
			}
		});
	}

	/**
	 * Expands all ancestors of the given element or tree path so that the given element becomes visible in this viewer's tree control, and then
	 * expands the subtree rooted at the given element to the given level.
	 *
	 * @param elementOrTreePath the element
	 * @param level non-negative level, or <code>ALL_LEVELS</code> to expand all levels of the tree
	 */
	public void expandToLevel(final Object elementOrTreePath, final int level) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer != null) {
					viewer.expandToLevel(elementOrTreePath, level);
				}
			}
		});
	}

	/**
	 * Checks whether the provided launch configuration is one of the supported kinds or not.
	 *
	 * @param launchConfiguration the launch configuration to check
	 * @return true if this configuration is supported in general, false if not.
	 * */
	public static boolean isSupportedConfiguration(final ILaunchConfiguration launchConfiguration) {
		if (launchConfiguration == null) {
			return false;
		}
		try {
			return JNI_LAUNCHCONFIGURATION_TYPE.equals(launchConfiguration.getType())
					|| SINGLE_LAUNCHCONFIGURATION_TYPE.equals(launchConfiguration.getType())
					|| MCTR_CLI_LAUNCHCONFIGURATION_TYPE.equals(launchConfiguration.getType());
		} catch (CoreException e) {
			return false;
		}
	}
}
