/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.FolderNamingConventionPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.wizards.projectFormat.TITANAutomaticProjectExporter;

/**
 * @author Kristof Szabados
 * */
public class FolderNamingConventionPropertyPage extends BaseNamingConventionPropertyPage {
	private static final String DESCRIPTION = "Folder specific naming convention related preferences of the on-the-fly checker.\n"
			+ "All options use Java regular expressions.";

	private static final String ENABLEFOLDERSPECIFIC = "Enable folder specific settings";

	private ConfigurationManagerControl configurationManager;
	private String firstConfiguration;

	public FolderNamingConventionPropertyPage() {
		super(GRID);
		setDescription(DESCRIPTION);
	}

	@Override
	protected String getPageId() {
		return FolderNamingConventionPropertyData.QUALIFIER;
	}

	/**
	 * Handles the change of the active configuration. Sets the new
	 * configuration to be the active one, and loads its settings.
	 * 
	 * @param configuration
	 *                the name of the new configuration.
	 * */
	public void changeConfiguration(final String configuration) {
		configurationManager.changeActualConfiguration();

		resetPreferenceStore();
		initialize();

		PropertyNotificationManager.firePropertyChange((IFolder) getElement());
	}

	@Override
	protected void createFieldEditors() {
		final Composite tempParent = getFieldEditorParent();
		IFolder folder = (IFolder) getElement();

		configurationManager = new ConfigurationManagerControl(tempParent, folder.getProject());
		configurationManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (configurationManager.hasConfigurationChanged()) {
					changeConfiguration(configurationManager.getActualSelection());
				}
			}
		});
		firstConfiguration = configurationManager.getActualSelection();

		BooleanFieldEditor booleanedit = new BooleanFieldEditor(PreferenceConstants.ENABLEFOLDERSPECIFICNAMINGCONVENTIONS,
				ENABLEFOLDERSPECIFIC, tempParent);
		booleanedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				setChanged(true);
			}
		});
		addField(booleanedit);

		createNamingConventionBody(tempParent);
	}

	@Override
	public void setVisible(final boolean visible) {
		if (!visible) {
			return;
		}

		if (configurationManager != null) {
			configurationManager.refresh();
		}

		super.setVisible(visible);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();

		configurationManager.saveActualConfiguration();
	}

	@Override
	public boolean performCancel() {
		configurationManager.clearActualConfiguration();
		resetPreferenceStore();
		initialize();

		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		IFolder folder = (IFolder) getElement();
		boolean result = super.performOk();
		IPreferenceStore store = getPreferenceStore();
		if (store instanceof PropertyStore) {
			try {
				((PropertyStore) store).save();
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		IProject project = folder.getProject();
		configurationManager.saveActualConfiguration();
		ProjectDocumentHandlingUtility.saveDocument(project);
		TITANAutomaticProjectExporter.saveAllAutomatically(project);


		final boolean configurationChanged = !firstConfiguration.equals(configurationManager.getActualSelection());
		if (configurationChanged || (getChanged() && getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING))) {
			setChanged(false);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(null, "Naming convention settings changed",
							"Naming convention settings have changed, the known projects have to be re-analyzed completly.\nThis might take some time.");
				}
			});

			NamingConventionHelper.clearCaches();

			PropertyNotificationManager.firePropertyChange(project);
		}

		return result;
	}
}
