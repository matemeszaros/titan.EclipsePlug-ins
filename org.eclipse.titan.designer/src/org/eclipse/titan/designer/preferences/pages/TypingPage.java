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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This preference page hold the controls and functionality to set the typing
 * related options.
 * 
 * @author Kristof Szabados
 */
public final class TypingPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	static final String DESCRIPTION = "Preferences for typing in the editors";

	static final String CLOSE_GROUP = "Automatically insert closing bracket";
	static final String CLOSE_APOSTROPHE = "\"Apostrophe\"";
	static final String CLOSE_PARANTHESES = "(Parantheses)";
	static final String CLOSE_SQUARE = "[Square brackets]";
	static final String CLOSE_BRACES = "{Curly brackets}";

	static final String INSERT_GROUP = "Automatically move on a new line";
	static final String INSERT_BRACES = "Closing curly brackets";

	private Composite pageComposite;
	private Group autoCloseGroup;
	private Composite autoCloseComposite;
	private BooleanFieldEditor closeApostrophe;
	private BooleanFieldEditor closeParantheses;
	private BooleanFieldEditor closeSquare;
	private BooleanFieldEditor closeBraces;

	private Group autoCorrectGroup;
	private Composite autoCorrectComposite;
	private BooleanFieldEditor autoInsertBraces;

	public TypingPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
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

		autoCloseGroup = new Group(pageComposite, SWT.NONE);
		autoCloseGroup.setText(CLOSE_GROUP);
		layout = new GridLayout();
		layout.numColumns = 2;
		autoCloseGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		autoCloseGroup.setLayoutData(gridData);

		autoCloseComposite = new Composite(autoCloseGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		autoCloseComposite.setLayoutData(gridData);

		closeApostrophe = new BooleanFieldEditor(PreferenceConstants.CLOSE_APOSTROPHE, CLOSE_APOSTROPHE, autoCloseComposite);
		addField(closeApostrophe);
		closeParantheses = new BooleanFieldEditor(PreferenceConstants.CLOSE_PARANTHESES, CLOSE_PARANTHESES, autoCloseComposite);
		addField(closeParantheses);
		closeSquare = new BooleanFieldEditor(PreferenceConstants.CLOSE_SQUARE, CLOSE_SQUARE, autoCloseComposite);
		addField(closeSquare);
		closeBraces = new BooleanFieldEditor(PreferenceConstants.CLOSE_BRACES, CLOSE_BRACES, autoCloseComposite);
		addField(closeBraces);

		autoCorrectGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		autoCorrectGroup.setText(INSERT_GROUP);
		layout = new GridLayout();
		layout.numColumns = 2;
		autoCorrectGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		autoCorrectGroup.setLayoutData(gridData);

		autoCorrectComposite = new Composite(autoCorrectGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		autoCorrectComposite.setLayoutData(gridData);

		autoInsertBraces = new BooleanFieldEditor(PreferenceConstants.AUTOMATICALLY_MOVE_BRACES, INSERT_BRACES, autoCorrectComposite);
		addField(autoInsertBraces);

		initialize();

		return pageComposite;
	}

	@Override
	protected void createFieldEditors() {
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		pageComposite.dispose();
		autoCloseComposite.dispose();
		autoCloseGroup.dispose();
		closeApostrophe.dispose();
		closeParantheses.dispose();
		closeSquare.dispose();
		closeBraces.dispose();

		autoCorrectComposite.dispose();
		autoCorrectGroup.dispose();
		autoInsertBraces.dispose();
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
