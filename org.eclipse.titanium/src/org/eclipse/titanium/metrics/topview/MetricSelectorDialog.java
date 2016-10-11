/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.topview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;

/**
 * A popup dialog, which is displayed to the user when she starts the analysis
 * on a TTCN3 project on the {@link TopView}.
 * 
 * @author poroszd
 * 
 */
class MetricSelectorDialog implements Runnable {
	private final Set<IMetricEnum> used;
	private final Map<IMetricEnum, Button> bs;

	public MetricSelectorDialog() {
		used = new HashSet<IMetricEnum>();
		bs = new HashMap<IMetricEnum, Button>();
	}

	@Override
	public void run() {
		final Display disp = Display.getDefault();
		final Shell shell = new Shell(disp, SWT.SHELL_TRIM);
		shell.setText("Choose metrics");
		shell.setLayout(new GridLayout());

		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Composite content = new Composite(sc, 0);
		sc.setContent(content);

		final GridLayout l = new GridLayout();
		l.numColumns = 2;
		content.setLayout(l);
		content.setSize(200, 300);
		createSection(content, ModuleMetric.GROUP_NAME, ModuleMetric.values());
		createSection(content, FunctionMetric.GROUP_NAME, FunctionMetric.values());
		createSection(content, AltstepMetric.GROUP_NAME, AltstepMetric.values());
		createSection(content, TestcaseMetric.GROUP_NAME, TestcaseMetric.values());

		final Button button = new Button(shell, SWT.PUSH);
		button.setText("Ok");
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				okPressed();
				shell.dispose();
			}
		});

		shell.pack();
		content.setSize(content.computeSize(-1, -1));
		shell.open();

		while (!shell.isDisposed()) {
			if (!disp.readAndDispatch()) {
				disp.sleep();
			}
		}
	}

	public Set<IMetricEnum> getUsed() {
		return used;
	}

	protected void okPressed() {
		for (final IMetricEnum m : bs.keySet()) {
			if (bs.get(m).getSelection()) {
				used.add(m);
			}
		}
	}

	private void createSection(final Composite content, final String title, final IMetricEnum[] metrics) {
		final Label header = new Label(content, 0);
		header.setText(title);
		final GridData headerData = new GridData();
		headerData.horizontalSpan = 2;
		header.setLayoutData(headerData);
		final Composite padding = new Composite(content, 0);
		padding.setLayoutData(new GridData(25, 0));
		final Composite inner = new Composite(content, 0);
		inner.setLayout(new GridLayout());
		for (final IMetricEnum metric : metrics) {
			final Button b = new Button(inner, SWT.CHECK);
			b.setText(metric.getName());
			b.setToolTipText(metric.getHint());
			bs.put(metric, b);
		}

	}
}
