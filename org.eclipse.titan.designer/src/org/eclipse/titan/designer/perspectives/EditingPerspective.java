/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.perspectives;

import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.wizards.NewASN1ModuleWizard;
import org.eclipse.titan.designer.wizards.NewConfigFileWizard;
import org.eclipse.titan.designer.wizards.NewTITANProjectWizard;
import org.eclipse.titan.designer.wizards.NewTTCN3ModuleWizard;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * This class initializes the TITAN Editing perspective.
 * 
 * @author Kristof Szabados
 */
public final class EditingPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		final IFolderLayout folderLayout1 = layout.createFolder(ProductConstants.PRODUCT_ID_DESIGNER + ".mainFolder", IPageLayout.LEFT, 0.2f,
				layout.getEditorArea());
		folderLayout1.addView("org.eclipse.ui.navigator.ProjectExplorer");

		final IFolderLayout folderLayout = layout.createFolder(ProductConstants.PRODUCT_ID_DESIGNER + ".bottomFolder", IPageLayout.BOTTOM, 0.86f,
				layout.getEditorArea());
		folderLayout.addView(IPageLayout.ID_PROBLEM_VIEW);
		folderLayout.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folderLayout.addView(IPageLayout.ID_PROGRESS_VIEW);
		folderLayout.addView(IPageLayout.ID_TASK_LIST);
		folderLayout.addView("org.eclipse.pde.runtime.LogView");

		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.8f, layout.getEditorArea());

		/*
		 * layout.addActionSet(Activator.PLUGIN_ID + ".TITANEditing.actionSet");
		 * layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		 * layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		 */

		layout.addNewWizardShortcut(NewTITANProjectWizard.NEWTITANPROJECTWIZARD);
		layout.addNewWizardShortcut(NewASN1ModuleWizard.NEWASN1MODULEWIZARD);
		layout.addNewWizardShortcut(NewTTCN3ModuleWizard.NEWTTCN3MODULEWIZARD);
		layout.addNewWizardShortcut(NewConfigFileWizard.NEWCONFIGFILEWIZARD);
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");

		layout.addPerspectiveShortcut("org.eclipse.titan.executor.perspectives.ExecutingPerspective");

		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
	}
}
