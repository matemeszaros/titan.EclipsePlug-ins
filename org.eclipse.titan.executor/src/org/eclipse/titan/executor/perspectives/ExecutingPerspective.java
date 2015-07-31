/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.perspectives;

import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.designerconnection.DesignerHelper;
import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;
import org.eclipse.titan.executor.views.notification.NotificationView;
import org.eclipse.titan.executor.views.testexecution.TestExecutionView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * @author Kristof Szabados
 * */
// INTERNAL CONSTANTS HANDLE WITH CARE
public final class ExecutingPerspective  implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		final IFolderLayout topFolders = layout.createFolder(Activator.PLUGIN_ID + ".mainFolder", IPageLayout.TOP, 0.35f, layout.getEditorArea());
		topFolders.addView(ExecutorMonitorView.EXECUTORMONITOR_VIEW_ID);

		final IFolderLayout topRightFolders =
				layout.createFolder(Activator.PLUGIN_ID + "topRightFolder", IPageLayout.RIGHT, 0.5f, Activator.PLUGIN_ID + ".mainFolder");
		topRightFolders.addView(TestExecutionView.TESTEXECUTIONVIEW);
		topRightFolders.addView(NotificationView.NOTIFICATIONVIEW);

		final IFolderLayout bottomFolders = layout.createFolder(Activator.PLUGIN_ID + ".bottomFolder", IPageLayout.BOTTOM, 0.65f, layout.getEditorArea());
		bottomFolders.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottomFolders.addView(IPageLayout.ID_TASK_LIST);
		bottomFolders.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottomFolders.addView("org.eclipse.pde.runtime.LogView");

		final IFolderLayout leftFolders = layout.createFolder(Activator.PLUGIN_ID + ".leftFolder", IPageLayout.LEFT, 0.15f, layout.getEditorArea());
		leftFolders.addView("org.eclipse.ui.navigator.ProjectExplorer");

		layout.addPerspectiveShortcut(DesignerHelper.DESIGNER_PLUGIN_ID + ".perspectives.EditingPerspective");

		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
		layout.addShowViewShortcut(ExecutorMonitorView.EXECUTORMONITOR_VIEW_ID);
		layout.addShowViewShortcut(TestExecutionView.TESTEXECUTIONVIEW);
		layout.addShowViewShortcut(NotificationView.NOTIFICATIONVIEW);
	}
}
