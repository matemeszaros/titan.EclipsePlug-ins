/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

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
import org.eclipse.titanium.metrics.utils.RiskLevel;

class ExportSetDialog {
	private final Display parent;
	private RiskLevel r;

	public ExportSetDialog(final Display display) {
		parent = display;
		r = null;
	}

	public RiskLevel open() {
		final Shell shell = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Choose metrics");
		shell.setLayout(new GridLayout());

		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Composite content = new Composite(sc, 0);
		sc.setContent(content);

		final GridLayout l = new GridLayout();
		content.setLayout(l);
		content.setSize(370, 100);
		final Label hint = new Label(content, SWT.WRAP);
		hint.setText("Choose which metrics do you want to include in the exported xls file");

		final Button bAll = new Button(content, SWT.RADIO);
		bAll.setText("All metrics");
		final Button bWarning = new Button(content, SWT.RADIO);
		bWarning.setText("Those reporting at least warning");
		bWarning.setSelection(true);
		final Button bError = new Button(content, SWT.RADIO);
		bError.setText("Those reporting error");

		final Button bOk = new Button(shell, SWT.PUSH);
		bOk.setText("Ok");
		bOk.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		bOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (bAll.getSelection()) {
					r = RiskLevel.NO;
				} else if (bWarning.getSelection()) {
					r = RiskLevel.LOW;
				} else {
					r = RiskLevel.HIGH;
				}
				shell.dispose();
			}
		});
		bOk.setFocus();

		shell.pack();
		content.setSize(content.computeSize(-1, -1));
		shell.open();

		while (!shell.isDisposed()) {
			if (!parent.readAndDispatch()) {
				parent.sleep();
			}
		}

		return r;
	}
}
