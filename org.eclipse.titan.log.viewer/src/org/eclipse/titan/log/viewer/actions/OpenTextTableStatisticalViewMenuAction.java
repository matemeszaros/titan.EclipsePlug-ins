/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.StatisticalView;
import org.eclipse.titan.log.viewer.views.text.table.TextTableViewHelper;

/**
 * Menu action for opening the text table view from the Test Cases tab in the Navigator view
 *
 */
public class OpenTextTableStatisticalViewMenuAction extends SelectionProviderAction {

	private static final String NAME = Messages.getString("OpenTextTableMenuAction.0"); //$NON-NLS-1$
	private IStructuredSelection selection;
	private StatisticalView statisticalView;
	
	/**
	 * Constructor
	 * @param navigatorView the navigator View
	 */
	public OpenTextTableStatisticalViewMenuAction(final StatisticalView statisticalView) {
		super(statisticalView, NAME);
		this.statisticalView = statisticalView;
	}

	@Override
	public void run() {
		Object element = this.selection.getFirstElement();
		if (!(element instanceof TestCase)) {
			return;
		}
		TestCase tc = (TestCase) element;
		LogFileMetaData logFileMetaData = this.statisticalView.getLogFileMetaData();
		TextTableViewHelper.open(logFileMetaData.getProjectName(), logFileMetaData.getProjectRelativePath(), tc.getStartRecordNumber());
	}
	
	@Override
	public void selectionChanged(final IStructuredSelection selection) {
		boolean enabled = true;
		this.selection = selection;
		if (this.selection.isEmpty() || !(this.selection.getFirstElement() instanceof TestCase)) {
			enabled = false;
		}
		setEnabled(enabled);
		super.selectionChanged(selection);
	}
}
