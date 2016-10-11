/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MetricsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "These preference pages gives control over the behavior " + "of the metrics-related views.\n\n"
			+ "On the Limits page one can change the default highlight " + "mechanism of the metrics, i. e. that which values of the "
			+ "are considered minor or major risk to the project.\n\n" + "On the Metrics View page one can set the metrics to "
			+ "display in the Metrics View\n\n" + "At last, on the Module Graph page one can choose which "
			+ "metrics can be selected while using the Module Graph View.";

	public MetricsPreferencePage() {
		super("Global settings regarding metrics");
	}

	@Override
	public void init(final IWorkbench sworkbench) {
		setDescription(DESCRIPTION);
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite comp = new Composite(parent, 0);
		comp.setLayout(new FillLayout());
		// Any global setting utilities of metrics should go here.
		return comp;
	}
}
