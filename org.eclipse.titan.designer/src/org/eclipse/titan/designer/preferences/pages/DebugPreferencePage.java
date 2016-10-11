/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * @author Szabolcs Beres
 * */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Debug options for the Titan plugins";

	private Composite pagecomp;

	public DebugPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	/**
	 * Creates an expandable composite on the user interface.
	 * 
	 * @param parent
	 *                the parent composite where this one can be added to.
	 * @param title
	 *                the title of the new composite.
	 * 
	 * @return the created composite.
	 * */
	private ExpandableComposite createExtendableComposite(final Composite parent, final String title) {
		final ExpandableComposite ex = new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE
				| ExpandableComposite.CLIENT_INDENT | ExpandableComposite.COMPACT);
		ex.setText(title);
		ex.setExpanded(false);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		ex.setLayoutData(data);
		ex.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				Composite temp = parent;
				while (temp != null && !(temp instanceof ScrolledComposite)) {
					temp = temp.getParent();
				}

				if (temp != null) {
					Point point = pagecomp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					((ScrolledComposite) temp).setMinSize(point);
					((ScrolledComposite) temp).layout(true, true);
				}
			}
		});

		return ex;
	}

	@Override
	protected Control createContents(final Composite parent) {
		pagecomp = new Composite(parent, SWT.NONE);
		pagecomp.setLayout(new GridLayout(1, false));
		pagecomp.setLayoutData(new GridData(GridData.FILL_BOTH));

		return super.createContents(pagecomp);
	}

	@Override
	protected void createFieldEditors() {
		final Composite tempParent = getFieldEditorParent();
		createConsoleSection(tempParent);
		createLoadBalancingSection(tempParent);
	}

	private void createConsoleSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "Debug console");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);
		addField(new BooleanFieldEditor(PreferenceConstants.DISPLAYDEBUGINFORMATION, "Enable debug console", comp));
		addField(new BooleanFieldEditor(PreferenceConstants.DEBUG_CONSOLE_TIMESTAMP, "Console timestamp", comp));
		addField(new BooleanFieldEditor(PreferenceConstants.DEBUG_CONSOLE_AST_ELEM, "Print AST element for the cursor position", comp));
	}

	private void createLoadBalancingSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "Load balancing");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);
		IntegerFieldEditor tokens = new IntegerFieldEditor(PreferenceConstants.DEBUG_LOAD_TOKENS_TO_PROCESS_IN_A_ROW,
				"Tokens to process between thread switches", comp);
		tokens.setValidRange(0, Integer.MAX_VALUE);
		addField(tokens);

		ScaleFieldEditor threadPriority = new ScaleFieldEditor(PreferenceConstants.DEBUG_LOAD_THREAD_PRIORITY, "Thread priority", comp);
		threadPriority.setMinimum(Thread.MIN_PRIORITY);
		threadPriority.setMaximum(Thread.MAX_PRIORITY);
		threadPriority.setIncrement(1);
		threadPriority.getScaleControl().setToolTipText("Sets the priority of the threads created by the syntax analyzer.");
		addField(threadPriority);

		IntegerFieldEditor sleepBetweenFiles = new IntegerFieldEditor(PreferenceConstants.DEBUG_LOAD_SLEEP_BETWEEN_FILES,
				"Sleep the syntax analyzer thread after processing a single file(-1 to do not sleep at all)", comp);
		sleepBetweenFiles.setValidRange(-1, Integer.MAX_VALUE);
		addField(sleepBetweenFiles);

		BooleanFieldEditor yieldBetweenChecks = new BooleanFieldEditor(PreferenceConstants.DEBUG_LOAD_YIELD_BETWEEN_CHECKS,
				"Switch thread after semantically checking modules or definitions.", comp);
		addField(yieldBetweenChecks);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
