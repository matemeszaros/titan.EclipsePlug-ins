/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This preference page hold the controls and functionality to set the folding
 * related options.
 * 
 * @author Kristof Szabados
 */
public final class FoldingPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences for the editor's folding";
	private static final String ENABLE_FOLDING = "Enable folding";
	private static final String FOLD_ELEMENTS = "Fold these elements:";
	private static final String COMMENTS = "Comments";
	private static final String STATEMENTSBLOCK = "Statementblocks";
	private static final String PARENTHESIS = "Between parentheses";
	private static final String DISTANCE = "When the distance in lines is at least";
	private Label headLabel;

	private BooleanFieldEditor enableFolding;
	private BooleanFieldEditor foldComments;
	private BooleanFieldEditor foldStatementBlocks;
	private BooleanFieldEditor foldParenthesis;
	private Composite foldingComposite;
	private IntegerFieldEditor foldingDistance;

	public FoldingPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		enableFolding = new BooleanFieldEditor(PreferenceConstants.FOLDING_ENABLED, ENABLE_FOLDING, getFieldEditorParent());
		addField(enableFolding);

		headLabel = new Label(getFieldEditorParent(), SWT.NONE);
		headLabel.setText(FOLD_ELEMENTS);

		foldComments = new BooleanFieldEditor(PreferenceConstants.FOLD_COMMENTS, COMMENTS, getFieldEditorParent());
		addField(foldComments);
		foldStatementBlocks = new BooleanFieldEditor(PreferenceConstants.FOLD_STATEMENT_BLOCKS, STATEMENTSBLOCK, getFieldEditorParent());
		addField(foldStatementBlocks);
		foldParenthesis = new BooleanFieldEditor(PreferenceConstants.FOLD_PARENTHESIS, PARENTHESIS, getFieldEditorParent());
		addField(foldParenthesis);

		foldingComposite = new Composite(getFieldEditorParent(), SWT.NONE);

		foldingDistance = new IntegerFieldEditor(PreferenceConstants.FOLD_DISTANCE, DISTANCE, foldingComposite);
		foldingDistance.setValidRange(0, Integer.MAX_VALUE);
		foldingDistance.setTextLimit(10);
		addField(foldingDistance);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		headLabel.dispose();
		enableFolding.dispose();
		foldComments.dispose();
		foldStatementBlocks.dispose();
		foldParenthesis.dispose();
		foldingComposite.dispose();
		foldingDistance.dispose();
		super.dispose();
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
