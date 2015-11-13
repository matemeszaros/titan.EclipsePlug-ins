/*******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.text.table;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.DetailsView;

class CloseAllAction extends Action {

	public CloseAllAction() {
		super("", ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_MSC_DELETE)));
		setId("closeTextTable");
		setToolTipText(Messages.getString("TextTableView.6"));
	}

	@Override
	public void run() {

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference[] viewReferences = activePage.getViewReferences();

		for (IViewReference reference : viewReferences) {
			IViewPart view = reference.getView(false);

			// memento restored views that never have had focus are null!!!
			if (view == null) {
				activePage.hideView(reference);
			} else if (view instanceof TextTableView) {
				activePage.hideView(reference);
			}
		}

		// Clear Details View if needed
		DetailsView detailsView = (DetailsView) activePage.findView(Constants.DETAILS_VIEW_ID);
		if (detailsView != null
				&& "".equals(detailsView.getTestCaseName())) {
			detailsView.setData(null, false);
		}
	}
}
