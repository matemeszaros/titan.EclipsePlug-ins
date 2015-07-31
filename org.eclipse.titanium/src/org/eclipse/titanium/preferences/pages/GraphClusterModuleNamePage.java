/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GraphClusterModuleNamePage extends GraphClusterPage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = "Settings for the module name clustering tool";

	@Override
	public void init(IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite page = new Composite(parent, 0);
		page.setLayout(new GridLayout(1, false));
		Composite inner;

		inner = setupLabel(page, "Split preferences", "More than one can be selected.");

		setupBooleanEditor(inner, PreferenceConstants.CLUSTER_SPACE, "Split module names around word boundaries",
				"Split module names around underscore (_) and hyphen (-) characters.");

		setupBooleanEditor(inner, PreferenceConstants.CLUSTER_SMALL_LARGE, "Split module names around alternating case",
				"Split module names around alternating case letter, for examle 'catDog' wil be split into 'cat' and 'Dog'.");

		inner = setupLabel(page, "Size preferences", "Settings that influence the number of clusters.");

		setupIntegerEditor(inner, PreferenceConstants.CLUSTER_DEPTH, "Number of times to split the names",
				"The names will only be split the first given number of times.");

		initialize();
		return page;
	}
}
