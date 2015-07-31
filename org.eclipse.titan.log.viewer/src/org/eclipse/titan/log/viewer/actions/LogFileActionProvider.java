/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class LogFileActionProvider extends CommonActionProvider {
	private OpenTextTableProjectsViewMenuAction openLogFileAction;
	private ICommonViewerWorkbenchSite viewSite = null;

	@Override
	public void init(final ICommonActionExtensionSite site) {
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			viewSite = (ICommonViewerWorkbenchSite) site.getViewSite();
			openLogFileAction = new OpenTextTableProjectsViewMenuAction();
		}
	}

	@Override
	public void fillActionBars(final IActionBars actionBars) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof IFile) {
			openLogFileAction.selectionChanged(null, selection);
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openLogFileAction);
		}
	}

	@Override
	public void fillContextMenu(final IMenuManager menu) {
		if (getContext().getSelection().isEmpty()) {
			return;
		}
		super.fillContextMenu(menu);
		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		openLogFileAction.selectionChanged(null, selection);
		if (openLogFileAction.isEnabled()) {
			menu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openLogFileAction);
		}
		addOpenWithMenu(menu);
		
	}

	private void addOpenWithMenu(final IMenuManager aMenu) {
		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		if (selection == null || selection.size() != 1 
				|| !(selection.getFirstElement() instanceof IFile)) {
			return;
		}

		final IFile file = (IFile) selection.getFirstElement();

		IMenuManager submenu = new MenuManager("Open with", ICommonMenuConstants.GROUP_OPEN_WITH);
		submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
		submenu.add(new OpenWithMenu(viewSite.getPage(), file));
		submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));

		if (submenu.getItems().length > 2 && submenu.isEnabled()) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
		}
	}
}
