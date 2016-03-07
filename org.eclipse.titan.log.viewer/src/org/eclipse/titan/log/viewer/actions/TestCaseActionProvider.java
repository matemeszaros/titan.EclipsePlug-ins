/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class TestCaseActionProvider extends CommonActionProvider {

	private OpenMSCViewAction openTestCaseAction;
	
	@Override
	public void init(final ICommonActionExtensionSite site) {
		openTestCaseAction = new OpenMSCViewAction();
	}

	@Override
	public void fillActionBars(final IActionBars actionBars) {

		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof TestCase) {
			openTestCaseAction.selectionChanged(null, selection);
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openTestCaseAction);
		}
	}
}
