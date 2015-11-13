/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public class MarkOccurrencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences for the editor's mark occurrences feature";
	private static final String ENABLE_MARK = "Mark occurrences of the selected element";
	private static final String DELAY_TEXT = "Delay (in milisecs)";
	private static final String KEEP_MARKS = "Keep the marks after changing the selection";
	private static final String MARK_ASN1_ASSIGNMENTS = "ASN.1 assignments";
	private static final String MARK_TTCN3_ASSIGNMENTS = "TTCN3 definitions";

	private Composite pageComposite;
	private BooleanFieldEditor enableMarkOccurrences;
	private IntegerFieldEditor delay;
	private BooleanFieldEditor keepMarks;

	private Composite generalGroup;
	private Group optionsGroup;

	private BooleanFieldEditor markASN1Assignments;
	private BooleanFieldEditor markTTCN3Assignments;

	public MarkOccurrencesPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
		super.initialize();
	}

	@Override
	protected void createFieldEditors() {
		pageComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		pageComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(gridData);

		createGeneralGroup(pageComposite);
		createOptionsGroup(pageComposite);
	}

	private void createGeneralGroup(final Composite parent) {
		generalGroup = new Composite(parent, SWT.NONE);

		enableMarkOccurrences = new BooleanFieldEditor(PreferenceConstants.MARK_OCCURRENCES_ENABLED, ENABLE_MARK, generalGroup);
		addField(enableMarkOccurrences);

		delay = new IntegerFieldEditor(PreferenceConstants.MARK_OCCURRENCES_DELAY, DELAY_TEXT, generalGroup);
		delay.setValidRange(0, Integer.MAX_VALUE);
		delay.setTextLimit(10);
		delay.setEnabled(doGetPreferenceStore().getBoolean(PreferenceConstants.MARK_OCCURRENCES_ENABLED), generalGroup);
		addField(delay);

		keepMarks = new BooleanFieldEditor(PreferenceConstants.MARK_OCCURRENCES_KEEP_MARKS, KEEP_MARKS, generalGroup);
		keepMarks.setEnabled(doGetPreferenceStore().getBoolean(PreferenceConstants.MARK_OCCURRENCES_ENABLED), generalGroup);
		addField(keepMarks);
	}

	private void createOptionsGroup(final Composite parent) {
		optionsGroup = new Group(parent, SWT.SHADOW_ETCHED_OUT);
		optionsGroup.setText("Mark occurrences of the following elements");
		optionsGroup.setLayout(new GridLayout(1, true));
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		boolean markOccurrencesEnabled = doGetPreferenceStore().getBoolean(PreferenceConstants.MARK_OCCURRENCES_ENABLED);

		markASN1Assignments = new BooleanFieldEditor(PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS, MARK_ASN1_ASSIGNMENTS,
				optionsGroup);
		markASN1Assignments.setEnabled(markOccurrencesEnabled, optionsGroup);
		addField(markASN1Assignments);

		markTTCN3Assignments = new BooleanFieldEditor(PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS, MARK_TTCN3_ASSIGNMENTS,
				optionsGroup);
		markTTCN3Assignments.setEnabled(markOccurrencesEnabled, optionsGroup);
		addField(markTTCN3Assignments);
	}

	protected void updateFieldEditors() {
		boolean markOccurrencesEnabled = enableMarkOccurrences.getBooleanValue();

		delay.setEnabled(markOccurrencesEnabled, generalGroup);
		keepMarks.setEnabled(markOccurrencesEnabled, generalGroup);

		markASN1Assignments.setEnabled(markOccurrencesEnabled, optionsGroup);
		markTTCN3Assignments.setEnabled(markOccurrencesEnabled, optionsGroup);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		super.propertyChange(event);

		if (event.getSource() == enableMarkOccurrences) {
			updateFieldEditors();
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		updateFieldEditors();
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();

		IEclipsePreferences node = (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE)
				.node(ProductConstants.PRODUCT_ID_DESIGNER);
		if (node != null) {
			try {
				node.flush();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		return result;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		markASN1Assignments.dispose();
		markTTCN3Assignments.dispose();
		optionsGroup.dispose();

		keepMarks.dispose();
		delay.dispose();
		enableMarkOccurrences.dispose();
		pageComposite.dispose();
		generalGroup.dispose();
		super.dispose();
	}
}
