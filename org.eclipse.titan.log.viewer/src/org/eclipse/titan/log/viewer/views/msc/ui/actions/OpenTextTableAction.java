/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.text.table.TextTableViewHelper;

/**
 * Opens the Text Table view
 *
 */
public class OpenTextTableAction extends SelectionProviderAction {

	private static final String NAME = Messages.getString("OpenTextTableAction.0"); //$NON-NLS-1$
	private MSCView mscView;
	
	/**
	 * Constructor 
	 * @param view the MSC View
	 */
	public OpenTextTableAction(final MSCView view) {
		super(view.getMSCWidget(), NAME);
		this.mscView = view;
	}

	@Override
	public void run() {
		LogFileMetaData logFileMetadata = mscView.getLogFileMetaData();
		TextTableViewHelper.open(logFileMetadata.getProjectName(), logFileMetadata.getProjectRelativePath(), mscView.getSelectedRecordNumber());
	}
	
	@Override
	public void selectionChanged(final IStructuredSelection selection) {
		//do nothing
	}
}
