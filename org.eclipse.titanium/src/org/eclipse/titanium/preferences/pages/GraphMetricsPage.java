/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricGroup;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GraphMetricsPage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Selecting metrics to display in the module graph view";

	private final List<FieldEditor> editors;

	public GraphMetricsPage() {
		super();
		editors = new ArrayList<FieldEditor>();
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
		setPreferenceStore(PreferenceManager.getStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite page = new Composite(parent, 0);
		GridLayout l = new GridLayout();
		l.numColumns = 2;
		page.setLayout(l);
		for (MetricGroup type : new MetricGroup[] { MetricGroup.MODULE, MetricGroup.FUNCTION,
				MetricGroup.TESTCASE, MetricGroup.ALTSTEP }) {
			Label header = new Label(page, 0);
			header.setText(type.getGroupName() + " metrics");
			GridData headerData = new GridData();
			headerData.horizontalSpan = 2;
			header.setLayoutData(headerData);
			Composite padding = new Composite(page, 0);
			padding.setLayoutData(new GridData(25, 0));
			Composite inner = new Composite(page, 0);
			for (IMetricEnum metric : type.getMetrics()) {
				BooleanFieldEditor editor = new BooleanFieldEditor(PreferenceConstants.nameMetricGraph(metric.id()),
						metric.getName(), inner);
				editor.getDescriptionControl(inner).setToolTipText(metric.getHint());
				editor.setPage(this);
				editor.setPreferenceStore(this.getPreferenceStore());
				editor.load();
				editors.add(editor);
			}
		}

		return page;
	}

	@Override
	protected void performDefaults() {
		for (FieldEditor editor : editors) {
			editor.loadDefault();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		for (FieldEditor editor : editors) {
			editor.store();
		}

		return super.performOk();
	}
}