/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titanium.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GraphClusterPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = "Settings for the module graph clustering tools\n\n"
			+ "The preferences of the clustering algorithms can be set on the sub-pages.\n\n" + "Clustering by folder name \n"
			+ "Modules in the same folder will belong to the same cluster.\n\n" + "Clustering using regular expressions \n"
			+ "Modules, whose name match a given regular expression will belong to the same cluster.\n\n"
			+ "Clustering by module names \n" + "Module names are split, and a nested sequence of clusters is created.\n"
			+ "A module will belong to the narrowest cluster forming a hierarchy.\n\n" + "Clustering automatically \n"
			+ "Triest to improve the clustering of the existing tools.\n"
			+ "A clustering is consdered better if there are more edges inside clusters, and less between.";

	public GraphClusterPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription(DESCRIPTION);
		noDefaultAndApplyButton();
	}

	protected Composite setupLabel(final Composite page, final String text, final String hint) {
		Group header = new Group(page, SWT.NONE);
		header.setText(text);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		header.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		header.setLayoutData(gridData);
		header.setToolTipText(hint);

		Composite composite = new Composite(header, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		composite.setLayoutData(gridData);

		return composite;
	}

	protected void setupBooleanEditor(final Composite comp, final String pref, final String text, final String hint) {
		BooleanFieldEditor editor = new BooleanFieldEditor(pref, text, comp);
		editor.getDescriptionControl(comp).setToolTipText(hint);
		editor.fillIntoGrid(comp, 2);
		addField(editor);
	}

	protected void setupIntegerEditor(final Composite comp, final String pref, final String text, final String hint) {
		IntegerFieldEditor editor = new IntegerFieldEditor(pref, text, comp);
		editor.getLabelControl(comp).setToolTipText(hint);
		addField(editor);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected void createFieldEditors() {
		//Do nothing
	}

}
