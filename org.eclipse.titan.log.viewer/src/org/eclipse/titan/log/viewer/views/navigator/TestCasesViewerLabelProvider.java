/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.navigator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for the test cases in the project explorer
 * 
 */
public class TestCasesViewerLabelProvider extends LabelProvider {

	@Override
	public String getText(final Object element) {
		if (!(element instanceof TestCase)) {
			return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getText(element);
		}

		TestCase tc = (TestCase) element;
		return tc.getTestCaseName();
	}

	@Override
	public Image getImage(final Object element) {
		if (!(element instanceof TestCase)) {
			return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getImage(element);
		}

		TestCase tc = (TestCase) element;
		if (tc.getVerdict() == Constants.VERDICT_PASS) {
			return Activator.getDefault().getIcon(Constants.ICONS_PASS);
		} else if (tc.getVerdict() == Constants.VERDICT_FAIL) {
			return Activator.getDefault().getIcon(Constants.ICONS_FAIL);
		} else if (tc.getVerdict() == Constants.VERDICT_ERROR) {
			return Activator.getDefault().getIcon(Constants.ICONS_ERROR);
		} else if (tc.getVerdict() == Constants.VERDICT_INCONCLUSIVE) {
			return Activator.getDefault().getIcon(Constants.ICONS_INCONCLUSIVE);
		} else if (tc.getVerdict() == Constants.VERDICT_NONE) {
			return Activator.getDefault().getIcon(Constants.ICONS_NONE);
		} else if (tc.getVerdict() == Constants.VERDICT_CRASHED) {
			return Activator.getDefault().getIcon(Constants.ICONS_CRASHED);
		} else {
			// Could not find image return null
			return null;
		}
	}
}
