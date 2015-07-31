/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GraphClusterFolderNamePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = "List of paths whose name will be ommitted from the cluster graph node names"
			+ " when using location or linked clustering.";

	public GraphClusterFolderNamePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		ListEditor editor = new ListEditor(PreferenceConstants.CLUSTER_TRUNCATE, "List of prefixes", getFieldEditorParent());
		addField(editor);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();

		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		if (node != null) {
			try {
				node.flush();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace("Error while saving preferences", e);
			}
		}

		return result;
	}

}