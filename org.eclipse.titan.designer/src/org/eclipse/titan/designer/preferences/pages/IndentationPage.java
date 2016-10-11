/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.actions.IndentationSupport;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public final class IndentationPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences for automatic indentation";
	private static final String GROUP_LABEL = "General settings";
	private static final String TAB_POLICY_LABEL = "Indentation policy:";

	private static final String[][] TAB_POLICY_OPTIONS = new String[][] {
			{ PreferenceConstantValues.TAB_POLICY_1, PreferenceConstantValues.TAB_POLICY_1 },
			{ PreferenceConstantValues.TAB_POLICY_2, PreferenceConstantValues.TAB_POLICY_2 } };

	private static final String INTENDATION_SIZE_LABEL = "Indentation size:";

	private Composite pageComposite;
	private Group generalGroup;

	private Composite indentationComposit;
	private ComboFieldEditor tabPolicy;
	private IntegerFieldEditor indentationSize;

	public IndentationPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		pageComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(gridData);

		generalGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		generalGroup.setText(GROUP_LABEL);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 2;
		generalGroup.setLayout(layout2);
		GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		generalGroup.setLayoutData(gridData2);

		indentationComposit = new Composite(generalGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		indentationComposit.setLayoutData(gridData);

		tabPolicy = new ComboFieldEditor(PreferenceConstants.INDENTATION_TAB_POLICY, TAB_POLICY_LABEL, TAB_POLICY_OPTIONS,
				indentationComposit);
		addField(tabPolicy);

		indentationSize = new IntegerFieldEditor(PreferenceConstants.INDENTATION_SIZE, INTENDATION_SIZE_LABEL, indentationComposit, 2);
		indentationSize.setValidRange(1, 32);
		addField(indentationSize);

		initialize();

		return pageComposite;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (PreferenceConstantValues.TAB_POLICY_2.equals(tabPolicy.getActualValue())) {
			indentationSize.setEnabled(true, indentationComposit);
		} else {
			indentationSize.setEnabled(false, indentationComposit);
		}
		super.propertyChange(event);
	}

	@Override
	protected void createFieldEditors() {
		//Do nothing
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public boolean performOk() {
		IndentationSupport.clearIndentString();

		boolean result = super.performOk();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ProductConstants.PRODUCT_ID_DESIGNER);
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
	public void dispose() {
		pageComposite.dispose();
		generalGroup.dispose();
		indentationComposit.dispose();
		tabPolicy.dispose();
		indentationSize.dispose();
		super.dispose();
	}
}
