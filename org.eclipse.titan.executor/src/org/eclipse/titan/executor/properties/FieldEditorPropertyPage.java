/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.executor.Activator;
import org.eclipse.ui.IWorkbenchPropertyPage;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Szabolcs Beres
 * */
public abstract class FieldEditorPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	/**
	 * The element.
	 */
	private IAdaptable element;
	private IPreferenceStore overlayStore;
	private List<FieldEditor> editors = new ArrayList<FieldEditor>();

	private static final String USE_PROJECT_SETTINGS = "useProjectSettings";
	private Button useWorkspaceSettingsButton;
	private Button useProjectSettingsButton;
	private Button configureButton;
	private ImageDescriptor image;

	private String pageId;

	protected FieldEditorPropertyPage(final int style) {
		super(style);
	}

	public FieldEditorPropertyPage(final String title, final ImageDescriptor image, final int style) {
		super(title, image, style);
		this.image = image;
	}

	@Override
	public IAdaptable getElement() {
		return element;
	}

	@Override
	public void setElement(final IAdaptable element) {
		this.element = element;
	}

	protected void resetPreferenceStore() {
		overlayStore = new PropertyStore((IResource) getElement(), Activator.getDefault().getPreferenceStore(), getPageId());
	}

	protected abstract String getPageId();

	@Override
	protected Control createContents(final Composite parent) {
		if (isPropertyPage()) {
			createSelectionGroup(parent);
		}

		return super.createContents(parent);
	}

	@Override
	public void createControl(final Composite parent) {
		if (isPropertyPage()) {
			pageId = getPageId();
			overlayStore = new PropertyStore((IResource) getElement(), super.getPreferenceStore(), pageId);
		}
		super.createControl(parent);
		updateFieldEditors();
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		if (isPropertyPage()) {
			return overlayStore;
		}
		return super.getPreferenceStore();
	}

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
		useWorkspaceSettingsButton = createRadioButton(radioGroup, "Use workspace settings");
		useProjectSettingsButton = createRadioButton(radioGroup, "Use project settings");
		configureButton = new Button(comp, SWT.PUSH);
		configureButton.setText("Configure Workspace Settings ...");
		configureButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				configureWorkspaceSettings();
			}
		});
		try {
			String use = ((IResource) element).getPersistentProperty(new QualifiedName(pageId, USE_PROJECT_SETTINGS));
			if ("true".equals(use)) {
				useProjectSettingsButton.setSelection(true);
				configureButton.setEnabled(false);
			} else {
				useWorkspaceSettingsButton.setSelection(true);
			}
		} catch (CoreException e) {
			useWorkspaceSettingsButton.setSelection(true);
		}
	}

	private Button createRadioButton(final Composite parent, final String label) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				configureButton.setEnabled(button == useWorkspaceSettingsButton);
				updateFieldEditors();
			}
		});
		return button;
	}

	@Override
	protected void addField(final FieldEditor editor) {
		editors.add(editor);
		super.addField(editor);
	}

	@Override
	protected void checkState() {
		super.checkState();

		boolean valid = true;
		if (editors != null) {
			for (FieldEditor editor : editors) {
				valid = valid && editor.isValid();
				if (!valid) {
					break;
				}
			}
		}
		setValid(valid);
	}

	/**
	 * The field editor preference page implementation of a
	 * <code>PreferencePage</code> method loads all the field editors with their
	 * default values.
	 */
	@Override
	protected void performDefaults() {
		if (isPropertyPage()) {
			useWorkspaceSettingsButton.setSelection(true);
			useProjectSettingsButton.setSelection(false);
			configureButton.setEnabled(true);
			updateFieldEditors();
		}
		super.performDefaults();
	}

	/**
	 * The field editor preference page implementation of this
	 * <code>PreferencePage</code> method saves all field editors by calling
	 * <code>FieldEditor.store</code>. Note that this method does not save the
	 * preference store itself; it just stores the values back into the
	 * preference store.
	 * 
	 * @see FieldEditor#store()
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result && isPropertyPage()) {
			IResource resource = (IResource) element;
			try {
				String value = (useProjectSettingsButton.getSelection()) ? "true" : "false";
				resource.setPersistentProperty(new QualifiedName(pageId, USE_PROJECT_SETTINGS), value);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		return result;
	}

	/**
	 * The field editor preference page implementation of this
	 * <code>IPreferencePage</code> (and <code>IPropertyChangeListener</code>)
	 * method intercepts <code>IS_VALID</code> events but passes other events on
	 * to its superclass.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent event) {

		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			boolean newValue = (Boolean) event.getNewValue();
			if (newValue) {
				checkState();
			} else {
				setValid(newValue);
			}
		}
	}

	/**
	 * Enables or disables the field editors and buttons of this page
	*/
	protected void updateFieldEditors() {
		if (!isPropertyPage()) {
			return;
		}
		
		boolean projectSettings = useProjectSettingsButton.getSelection();
		Composite parent = getFieldEditorParent();
		for (FieldEditor editor : editors) {
			editor.setEnabled(projectSettings, parent);
		}
	}

	protected void configureWorkspaceSettings() {
		try {
			IPreferencePage page = this.getClass().newInstance();
			page.setTitle(getTitle());
			page.setImageDescriptor(image);
			showPreferencePage(pageId, page);
		} catch (InstantiationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} catch (IllegalAccessException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	protected void showPreferencePage(final String id, final IPreferencePage page) {
		final IPreferenceNode targetNode = new PreferenceNode(id, page);
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(getControl().getShell(), manager);
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			@Override
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				dialog.open();
			}
		});
	}
	
	protected List<FieldEditor> getFieldEditors() {
		return editors;
	}
	
	/**
	 * Returns true if the "Use Project Settings" radio button is selected
	 * @return true if the button is selected, false otherwise
	 */
	protected boolean isProjectSettingsSelected() {
		return useProjectSettingsButton != null && useProjectSettingsButton.getSelection();
	}

	public boolean isPropertyPage() {
		return element != null;
	}
	
	public static String getOverlayedPreferenceValue(final IPreferenceStore store, final IResource resource, final String pageId, final String name) {
		IProject project = resource.getProject();
		String value = null;
		if (useProjectSettings(project, pageId)) {
			value = ResourceUtils.getPersistentProperty(resource, pageId, name);
		}
		if (value != null) {
			return value;
		}
		return store.getString(name);
	}

	private static boolean useProjectSettings(final IResource resource, final String pageId) {
		String use = ResourceUtils.getPersistentProperty(resource, pageId, FieldEditorPropertyPage.USE_PROJECT_SETTINGS);
		return "true".equals(use);
	}
}
