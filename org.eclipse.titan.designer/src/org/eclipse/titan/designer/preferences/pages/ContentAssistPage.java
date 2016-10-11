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
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public final class ContentAssistPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences for the editor's content assist";
	private static final String[][] SORTING_OPTIONS = new String[][] {
			{ PreferenceConstantValues.SORT_BY_RELEVANCE, PreferenceConstantValues.SORT_BY_RELEVANCE },
			{ PreferenceConstantValues.SORT_ALPHABETICALLY, PreferenceConstantValues.SORT_ALPHABETICALLY } };

	private Composite pageComposite;

	private Group insertionGroup;
	private Composite insertionComposite;
	private BooleanFieldEditor insertSingleProposal;
	private BooleanFieldEditor insertCommonPrefixes;
	private Composite sortingComposite;
	private Group sortingGroup;
	private ComboFieldEditor sorting;
	private Group autoActivationGroup;
	private Composite autoActivationComposite;
	private BooleanFieldEditor enableAutoActivation;
	private IntegerFieldEditor autoActivationDelay;

	public ContentAssistPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
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

		insertionGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		insertionGroup.setText("Insertion");
		insertionGroup.setLayout(new GridLayout(2, false));
		insertionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		insertionComposite = new Composite(insertionGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		insertionComposite.setLayoutData(gridData);

		insertSingleProposal = new BooleanFieldEditor(PreferenceConstants.CONTENTASSISTANT_SINGLE_PROPOSAL_INSERTION,
				"Insert single proposals automatically", insertionComposite);
		addField(insertSingleProposal);

		insertCommonPrefixes = new BooleanFieldEditor(PreferenceConstants.CONTENTASSISTANT_COMMON_PREFIX_INSERTION,
				"Insert common prefixes automatically", insertionComposite);
		addField(insertCommonPrefixes);

		sortingGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		sortingGroup.setText("Sorting");
		sortingGroup.setLayout(new GridLayout(2, false));
		sortingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		sortingComposite = new Composite(sortingGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		sortingComposite.setLayoutData(gridData);

		sorting = new ComboFieldEditor(PreferenceConstants.CONTENTASSISTANT_PROPOSAL_SORTING, "Sort proposals", SORTING_OPTIONS,
				sortingComposite);
		Label text = sorting.getLabelControl(sortingComposite);
		text.setToolTipText("Sort proposals");
		addField(sorting);

		autoActivationGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		autoActivationGroup.setText("Auto-Activation");
		autoActivationGroup.setLayout(new GridLayout(2, false));
		autoActivationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		autoActivationComposite = new Composite(autoActivationGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		autoActivationComposite.setLayoutData(gridData);

		enableAutoActivation = new BooleanFieldEditor(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION, "Enable auto activation",
				autoActivationComposite);
		addField(enableAutoActivation);

		autoActivationDelay = new IntegerFieldEditor(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION_DELAY, "Auto activation delay:",
				autoActivationComposite);
		autoActivationDelay.setValidRange(10, 1000);
		autoActivationDelay.setTextLimit(10);
		autoActivationDelay.setEnabled(doGetPreferenceStore().getBoolean(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION),
				autoActivationComposite);
		addField(autoActivationDelay);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		pageComposite.dispose();
		enableAutoActivation.dispose();
		insertionGroup.dispose();
		insertionComposite.dispose();
		insertSingleProposal.dispose();
		insertCommonPrefixes.dispose();
		sortingComposite.dispose();
		sortingGroup.dispose();
		sorting.dispose();
		autoActivationGroup.dispose();
		autoActivationComposite.dispose();
		enableAutoActivation.dispose();
		autoActivationDelay.dispose();
		super.dispose();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		autoActivationDelay.setEnabled(enableAutoActivation.getBooleanValue(), autoActivationComposite);

		super.propertyChange(event);
	}

	@Override
	protected void performDefaults() {
		autoActivationDelay.setEnabled(true, autoActivationComposite);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
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
}
