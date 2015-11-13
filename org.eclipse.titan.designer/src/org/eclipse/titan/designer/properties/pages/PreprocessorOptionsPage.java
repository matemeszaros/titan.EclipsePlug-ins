/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Kristof Szabados
 * */
public final class PreprocessorOptionsPage implements IOptionsPage {
	private Composite mainComposite;

	private Label label;

	@Override
	public void dispose() {
		if (mainComposite != null) {
			mainComposite.dispose();
			mainComposite = null;

			label.dispose();
		}
	}

	@Override
	public Composite createContents(final Composite parent) {
		if (mainComposite != null) {
			return mainComposite;
		}

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		label = new Label(mainComposite, SWT.NONE);
		label.setText("The TTCN-3 preprocessor is used to preprocess C/C++ sources.");

		return mainComposite;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (mainComposite == null) {
			return;
		}

		label.setEnabled(enabled);
	}

	@Override
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		// does not have its own value
	}

	@Override
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		// does not have its own value
		return false;
	}

	@Override
	public void performDefaults() {
		if (mainComposite == null) {
			return;
		}
		
		label.setEnabled(true);
	}

	@Override
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		return true;
	}

	@Override
	public void loadProperties(final IProject project) {
		// does not have its own value
	}

	@Override
	public boolean saveProperties(final IProject project) {
		// does not have its own value
		return true;
	}
}
