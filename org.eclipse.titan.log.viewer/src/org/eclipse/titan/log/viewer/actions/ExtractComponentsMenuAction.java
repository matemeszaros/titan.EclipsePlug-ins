/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;
import org.eclipse.titan.log.viewer.preferences.pages.ComponentsVisualOrderPrefPage;
import org.eclipse.titan.log.viewer.preferences.pages.LogViewerPreferenceRootPage;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.utils.ResourcePropertyHandler;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This action extracts components from a TITAN log file, and adds
 * the components to the Components Visual Order list
 */
//TODO remove unnecessary functions after conversion
public class ExtractComponentsMenuAction extends AbstractHandler implements IActionDelegate {

	private ISelection selection;
	
	public ExtractComponentsMenuAction() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		run(selection);

		return null;
	}

	@Override
	public void run(final IAction action) {
		run(selection);
	}


	public void run(final ISelection selection) {
		List<String> components = null;
		if (selection == null) {
			return;
		}
		if (!SelectionUtils.isSelectionALogFile(selection)) {
			return;
		}
		IFile logFile = SelectionUtils.selectionToIFile(selection);
		if (logFile == null) {
			return;
		}
		
		try {
			ExtractComponentsAction extractCompAction = new ExtractComponentsAction(logFile);
			new ProgressMonitorDialog(null).run(false, false, extractCompAction);
			components = extractCompAction.getComponents();
		} catch (InvocationTargetException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(
							Messages.getString("ExtractComponentsMenuAction.0") + e.getTargetException().getMessage())); //$NON-NLS-1$
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
			// Should not happen, cancel button is de-activated
			TitanLogExceptionHandler.handleException(new TechnicalException(
							Messages.getString("ExtractComponentsMenuAction.1")  + e.getMessage())); //$NON-NLS-1$
		}

		String projectName = logFile.getProject().getName();
		PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(projectName);

		setNewProperties(components, logFile, preferences);
	}

	private void setNewProperties(List<String> components, IFile logFile, PreferencesHolder preferences) {
		if (components == null) {
			return;
		}

		// Check APPEND / REPLACE
		if (!preferences.getReplaceCompVisOrder()) {
			// Append
			List<String> compVisOrder = preferences.getVisualOrderComponents();
			for (String currComponent : components) {
				if (!compVisOrder.contains(currComponent)) {
					compVisOrder.add(currComponent);
				}
			}
			components = compVisOrder;
		} else {
			// Replace
			components.add(0, Constants.SUT);
			components.add(0, Constants.MTC);
		}

		// Check DIALOG / NO DIALOG
		if (preferences.getOpenPropAfterCompExt()) {
			// Open dialog
			PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(
					null, logFile.getProject(), PreferenceConstants.PAGE_ID_COMP_VIS_ORDER_PAGE, null, null);
			Object currentPage = dialog.getSelectedPage();
			if (currentPage instanceof ComponentsVisualOrderPrefPage) {
				ComponentsVisualOrderPrefPage componentsVisualOrderPrefPage = (ComponentsVisualOrderPrefPage) currentPage;
				componentsVisualOrderPrefPage.setUseProjectSetting(true);
				componentsVisualOrderPrefPage.clearList();
				for (String component : components) {
					componentsVisualOrderPrefPage.addComponent(component);
				}
				dialog.open();
			}
		} else {
			// No dialog - write directly to properties
			StringBuilder path = new StringBuilder(""); //$NON-NLS-1$
			for (String currComponent : components) {
				path.append(currComponent);
				path.append(File.pathSeparator);
			}
			try {
				// Set resource to use project settings
				ResourcePropertyHandler.setProperty(logFile.getProject(),
						PreferenceConstants.PAGE_ID_COMP_VIS_ORDER_PAGE,
						LogViewerPreferenceRootPage.USEPROJECTSETTINGS,
						LogViewerPreferenceRootPage.TRUE);
				// Set new component visual order
				ResourcePropertyHandler.setProperty(logFile.getProject(),
													PreferenceConstants.PAGE_ID_COMP_VIS_ORDER_PAGE,
													PreferenceConstants.PREF_COMPONENT_ORDER_ID,
													path.toString());
			} catch (CoreException e) {
				// Do nothing
			}
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			setEnabled(false);
			return;
		}
		
		this.selection = (IStructuredSelection) selection;
		setEnabled(SelectionUtils.isSelectionALogFile(this.selection));
	}
}
