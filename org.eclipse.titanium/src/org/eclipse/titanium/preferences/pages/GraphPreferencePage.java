/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.gui.layouts.TitaniumDAGLayout;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.ComboFieldEditor;

public class GraphPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	/**
	 * Create the preference page.
	 */
	public GraphPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription("Preference page for the graphs");
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected void createFieldEditors() {
		final IntegerFieldEditor temp = new IntegerFieldEditor(PreferenceConstants.NO_ITERATIONS, "Maximal number of iterations: ",
				getFieldEditorParent());
		addField(temp);
		addField(new ComboFieldEditor(PreferenceConstants.DAG_DISTANCE, "Directed layout's distance:", new String[][] {
				{ "Sum of distances", TitaniumDAGLayout.SUM_DISTANCE_ALGORITHM },
				{ "Maximal distance", TitaniumDAGLayout.MAX_DISTANCE_ALGORITHM } }, getFieldEditorParent()));
	}
}
