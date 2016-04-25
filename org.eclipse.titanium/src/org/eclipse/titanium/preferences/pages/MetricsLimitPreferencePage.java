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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricGroup;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.preferences.IRiskEditorListener;
import org.eclipse.titanium.metrics.preferences.IRiskEditorPropertyListener;
import org.eclipse.titanium.metrics.preferences.IRiskFieldEditor;
import org.eclipse.titanium.metrics.preferences.InstabilityRiskFieldEditor;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;
import org.eclipse.titanium.metrics.preferences.SimpleRiskFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MetricsLimitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Setting limits of metrics to warn";
	private List<IRiskFieldEditor> editors;

	public MetricsLimitPreferencePage() {
		super();
		editors = new ArrayList<IRiskFieldEditor>();
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
		setPreferenceStore(PreferenceManager.getStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite page = new Composite(parent, 0);
		GridLayout l = new GridLayout();
		l.numColumns = 2;
		page.setLayout(l);
		for (MetricGroup type : MetricGroup.values()) {
			Label header = new Label(page, 0);
			header.setText(type.getGroupName() + " metrics");
			GridData headerData = new GridData();
			headerData.horizontalSpan = 2;
			header.setLayoutData(headerData);
			Composite padding = new Composite(page, 0);
			padding.setLayoutData(new GridData(25, 0));
			final Composite inner = new Composite(page, 0);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			inner.setLayout(layout);

			IRiskEditorListener listener = new IRiskEditorListener() {
				@Override
				public void editorChanged() {
					inner.layout();
					page.layout();
				}
			};
			IRiskEditorPropertyListener propertyListener = new IRiskEditorPropertyListener() {
				
				@Override
				public void propertyChange(final boolean valid) {
					if (!valid) {
						setValid(false);
						updateApplyButton();
					} else if (!isValid()) {
						checkState();
					}
				}
			};

			for (IMetricEnum metric : type.getMetrics()) {
				Label name = new Label(inner, 0);
				name.setText(metric.getName());
				name.setToolTipText(metric.getHint());
				IRiskFieldEditor editor = getRiskEditor(inner, metric);
				editor.addRiskEditorListener(listener);
				editor.setPropListener(propertyListener);
				editor.load();
				editors.add(editor);
			}
		}
		checkState();
		return page;
	}

	@Override
	protected void performDefaults() {
		for (IRiskFieldEditor editor : editors) {
			editor.loadDefault();
		}
		checkState();
	}

	protected void checkState() {
		boolean valid = true;
		for (IRiskFieldEditor ed : editors) {
			valid = valid && ed.isValid();
			if (!valid) {
				break;
			}
		}
		setValid(valid);
		updateApplyButton();
	}

	@Override
	public boolean performOk() {
		for (IRiskFieldEditor ed : editors) {
			ed.store();
		}
		return true;
	}

	public void propertyChange(final boolean valid) {
		if (!valid) {
			setValid(false);
			updateApplyButton();
		} else if (!isValid()) {
			checkState();
		}
	}

	private static IRiskFieldEditor getRiskEditor(final Composite parent, final IMetricEnum metric) {
		if (metric.equals(ModuleMetric.INSTABILITY)) {
			return new InstabilityRiskFieldEditor(parent, metric);
		} else {
			return new SimpleRiskFieldEditor(parent, metric);
		}
	}
}