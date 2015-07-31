/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.properties.PropertyStore;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.utils.ResourcePropertyHandler;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * Abstract class for Log Viewer preference/properties pages
 *
 */
public abstract class LogViewerPreferenceRootPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage, IWorkbenchPreferencePage {

	// Constants
	public static final String FALSE = "false"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String USEPROJECTSETTINGS = "useProjectSettings"; //$NON-NLS-1$

	private List<FieldEditor> editors = new ArrayList<FieldEditor>();
	private IAdaptable element;
	private IPreferenceStore overlayStore;
	private String pageId;
	private Button useProjectSettingsButton;
	private Button useWorkspaceSettingsButton;
	private Button okButton;
	private Button importButton;
	private Button exportButton;
	private Button selectAll;
	private Button deselectAll;
	private Map<String, String> oldPreferences;
	private boolean enableSelectDeselectButtons;
	
	/**
	 * Constructor
	 * @param style the layout style
	 * @param enableSelectDeselectButtons a flag indicating if Select All and Deselect All button should be used
	 */
	public LogViewerPreferenceRootPage(final int style, final boolean enableSelectDeselectButtons) {
		super(style);
		this.enableSelectDeselectButtons = enableSelectDeselectButtons;
	}

	@Override
	protected void addField(final FieldEditor editor) {
		this.editors.add(editor);
		super.addField(editor);
	}
	
	@Override
	protected void contributeButtons(final Composite parent) {
		// Add an import button
		this.importButton = new Button(parent, SWT.PUSH);
		this.importButton.setText(Messages.getString("LogViewerPreferenceRootPage.0")); //$NON-NLS-1$
		this.importButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				importPreferences();
				updatePage();
			}
		});

		// Add an export button
		this.exportButton = new Button(parent, SWT.PUSH);
		this.exportButton.setText(Messages.getString("LogViewerPreferenceRootPage.1")); //$NON-NLS-1$
		this.exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				exportPreferences();
			}
		});
		// Add two columns to the parent's layout
		((GridLayout) parent.getLayout()).numColumns += 2;
		
		if (this.enableSelectDeselectButtons) {
			// Add a select all button
		    this.selectAll = new Button(parent, SWT.PUSH);
		    this.selectAll.setText(Messages.getString("FilteredSilentEventPrefPage.0")); //$NON-NLS-1$
		    this.selectAll.addSelectionListener(new SelectionAdapter() {
		    	@Override
		    	public void widgetSelected(final SelectionEvent event) {     
		    		performSelectAll();
		    	}
		    });
	
		    // Add a deselect all button
		    this.deselectAll = new Button(parent, SWT.PUSH);
		    this.deselectAll.setText(Messages.getString("FilteredSilentEventPrefPage.1")); //$NON-NLS-1$
		    this.deselectAll.addSelectionListener(new SelectionAdapter() {
		    	@Override
		    	public void widgetSelected(final SelectionEvent event) {
		    		performDeselectAll();
		    	}
		    });
	
		    // Add two columns to the parent's layout
		    ((GridLayout) parent.getLayout()).numColumns += 2;
		}
	}

	@Override
	protected Control createContents(final Composite parent) {
		if (isPropertyPage()) {
			createSelectionGroup(parent);
		}
		return super.createContents(parent);
	}

	@Override
	public void createControl(final Composite parent) {
		// Check if this is a property page
		if (isPropertyPage()) {
			this.pageId = getPageId();
			// Create an overlay preference store and fill it with properties
			this.overlayStore = new PropertyStore((IResource) getElement(),
											 super.getPreferenceStore(),
											 this.pageId);
			// Set overlay store as current preference store
		}
		super.createControl(parent);
		// Update state of all subclass controls
		if (isPropertyPage()) {
			updateFieldEditors();
		}
		// Disable apply button
		getApplyButton().setEnabled(false);
		this.okButton = parent.getShell().getDefaultButton();
	}

	@Override
	protected abstract void createFieldEditors();
	
	@Override
	public void setElement(final IAdaptable element) {
		this.element = element;
	}
	
	@Override
	public IAdaptable getElement() {
		return this.element;
	}
	
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result && isPropertyPage()) {
			// Save state of radio buttons in project properties
			writeProperty();
		}
		return result;
	}

	@Override
	public boolean performCancel() {
		if (this.oldPreferences != null) {
			setProperties(this.oldPreferences);
			this.oldPreferences.clear();
		}
		return super.performCancel();
	}

	@Override
	protected void performApply() {
		//disable apply button
		getApplyButton().setEnabled(false);
		super.performApply();
		// settings saved -> set old (cache) to null
		this.oldPreferences = null;
		//if project scope
		if (isPropertyPage()) {
			try {
				//save changes
				((PropertyStore) this.overlayStore).save();
			} catch (IOException e) {
				// Do nothing
			}
		}
		this.okButton.setFocus();
	}

	@Override
	protected void performDefaults() {
		if (isPropertyPage()) {
			this.useWorkspaceSettingsButton.setSelection(true);
			this.useProjectSettingsButton.setSelection(false);
			updateFieldEditors();
		} else {
			super.performDefaults();
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getSource() instanceof FieldEditor) {
			getApplyButton().setEnabled(true);
		}
		super.propertyChange(event);
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		if (isPropertyPage()) {
			return this.overlayStore;
		}
		return super.getPreferenceStore();
	}
	
	/**
	 * Convenience method for creating a radio button
	 * @param parent the parent composite
	 * @param label the button label
	 * @return the new button
	 */
	private Button createRadioButton(final Composite parent, final String label) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateFieldEditors();
			}
		});
		return button;
	}

	/**
	 * Creates and initializes a selection group with two choice buttons and one push button.
	 * @param parent the parent composite
	 */
	private void createSelectionGroup(final Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite radioGroup = new Composite(comp, SWT.NONE);
		radioGroup.setLayout(new GridLayout());
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.useWorkspaceSettingsButton = createRadioButton(radioGroup, Messages.getString("FieldEditorOverlayPage.0")); //$NON-NLS-1$
		this.useProjectSettingsButton = createRadioButton(radioGroup, Messages.getString("FieldEditorOverlayPage.1")); //$NON-NLS-1$
		// Set workspace/project radio buttons
		try {
			String use =
				((IResource) getElement()).getPersistentProperty(
					new QualifiedName(this.pageId, USEPROJECTSETTINGS));
			if (TRUE.equals(use)) {
				this.useProjectSettingsButton.setSelection(true);
			} else {
				this.useWorkspaceSettingsButton.setSelection(true);
			}
		} catch (CoreException e) {
			this.useWorkspaceSettingsButton.setSelection(true);
		}
	}
	
	/**
	 * This method must be implemented by all pages
	 * It is called upon when the export button is pressed 
	 */
	protected abstract void exportPreferences();
	
	/**
	 * This method must be implemented by all pages
	 * It is called upon when the import button is pressed
	 */
	protected abstract void importPreferences();
	
	/**
	 * This method must be overridden by all pages that uses select all behavior
	 * It is called upon when the select all button is pressed
	 */
	protected void performSelectAll() {
		getApplyButton().setEnabled(true);
	}
	
	/**
	 * This method must be overridden by all pages that uses deselect all behavior
	 * It is called upon when the deselect all button is pressed
	 */
	protected void performDeselectAll() {
		getApplyButton().setEnabled(true);
	}

	/**
	 * Returns the id of the current preference page as defined in plugin.xml
	 * Subclasses must implement. 
	 * 
	 * @return the qualifier
	 */
	protected abstract String getPageId();
	
	/**
	 * Help method for getting a preference property
	 * @param key property key
	 * @return value of the key
	 */
	protected String getProperty(final String key) {
		if (isPropertyPage()) {
			// Property - use set preference store
			return getPreferenceStore().getString(key);
		}
		// Use workspace preference store
		return Activator.getDefault().getPreferenceStore().getString(key);
	}

	/**
	 * Help method for setting preference properties
	 * @param prop, key / values to be set
	 */
	protected void setProperties(final Map<String, String> prop) {
		Set<String> set = prop.keySet();
		IPreferenceStore preferenceStore;
		if (isPropertyPage()) {
			preferenceStore = getPreferenceStore();
			//project scope
			for (String key : set) {
				preferenceStore.setValue(key, prop.get(key));
			}
		} else {
			preferenceStore = Activator.getDefault().getPreferenceStore();
			//Workspace scope
			for (String key : set) {
				preferenceStore.setValue(key, prop.get(key));
			}
		}
	}

	/*
	 * Enables or disables the field editors and buttons of this page
	 */
	private void updateFieldEditors() {
		// We iterate through all field editors 
		boolean enabled = this.useProjectSettingsButton.getSelection();
		updateFieldEditors(enabled);
		getApplyButton().setEnabled(true);
		this.importButton.setEnabled(enabled);
		this.exportButton.setEnabled(enabled);
		if (this.enableSelectDeselectButtons) {
			this.selectAll.setEnabled(enabled);
			this.deselectAll.setEnabled(enabled);
		}
	}
	
	/**
	 * Enables or disables the field editors and buttons of this page
	 * Subclasses may override.
	 * @param enabled true if enabled
	 */
	protected void updateFieldEditors(final boolean enabled) {
		Composite parent = getFieldEditorParent();
		for (FieldEditor editor : this.editors) {
			editor.setEnabled(enabled, parent);
		}
	}
	
	/**
	 * This method is called after import preference 
	 * has been called and should update the page with the 
	 * new values set.
	 */
	protected abstract void updatePage();
	
	/**
	 * Returns true if this instance represents a property page
	 * @return true for property pages, false for preference pages
	 */
	private boolean isPropertyPage() {
		return getElement() != null;
	}

	private void writeProperty() {
		IResource resource = (IResource) getElement();
		String value = (this.useProjectSettingsButton.getSelection()) ? TRUE : FALSE;
		try {
			ResourcePropertyHandler.setProperty(resource, this.pageId, USEPROJECTSETTINGS, value);
		} catch (CoreException e) {
			// Do nothing
		}
	}

	/**
	 * Sets old preferences
	 * @param oldPreferences the old preferences
	 */
	public void setOldPreferences(final Map<String, String> oldPreferences) {
		this.oldPreferences = oldPreferences;
	}
	
	/**
	 * Enables/Disables if project setting is used by toggling the project/workspace radio buttons
	 * @param enabled flag which indicates if project setting is enabled
	 */
	public void setUseProjectSetting(final boolean enabled) {
		this.useProjectSettingsButton.setSelection(enabled);
		this.useWorkspaceSettingsButton.setSelection(!enabled);
		updateFieldEditors();
	}
}
