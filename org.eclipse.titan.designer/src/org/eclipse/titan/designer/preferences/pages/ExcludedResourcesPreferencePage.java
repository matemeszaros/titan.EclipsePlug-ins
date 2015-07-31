/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.decorators.TITANDecorator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public class ExcludedResourcesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "The list of java regular expressions used to exclude resources from build and on-the-fly analysis";

	private boolean changed = false;

	public ExcludedResourcesPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void createFieldEditors() {
		ExcludeRegexpEditor editor = new ExcludeRegexpEditor(PreferenceConstants.EXCLUDED_RESOURCES, "regular expressions",
				getFieldEditorParent());
		editor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});

		addField(editor);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		changed = true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
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

		if (changed && getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			changed = false;

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(new Shell(Display.getDefault()), "Resource exclusion settings changed",
							"Resource exclusion settings have changed, the known projects have to be re-analyzed completly.\nThis might take some time.");
				}
			});

			GlobalParser.clearSemanticInformation();
			TITANDecorator.resetExclusion();

			final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				PropertyNotificationManager.firePropertyChange(project);
			}
		}

		return result;
	}
}
