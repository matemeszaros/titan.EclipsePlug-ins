/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This preference page hold the controls and functionality to set the TITAN
 * related options.
 * 
 */
public final class TitaniumPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "These are the preference pages of Titanium.";

	public TitaniumPreferencePage() {
		super("Titanium preference page");
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		return comp;
	}

}
