/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titan.designer.Activator;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * @author Kristof Szabados
 * */
public abstract class FieldEditorPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	/**
	 * The element.
	 */
	private IAdaptable element;
	// Overlay preference store for property pages
	private IPreferenceStore overlayStore;
	// Stores all created field editors
	private List<FieldEditor> editors = new ArrayList<FieldEditor>();

	protected FieldEditorPropertyPage(final int style) {
		super(style);
	}

	@Override
	public IAdaptable getElement() {
		return element;
	}

	@Override
	public void setElement(final IAdaptable element) {
		this.element = element;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return overlayStore;
	}

	protected void resetPreferenceStore() {
		overlayStore = new PropertyStore((IResource) getElement(), Activator.getDefault().getPreferenceStore(), getPageId());
	}

	protected abstract String getPageId();

	@Override
	protected Control createContents(final Composite parent) {
		overlayStore = new PropertyStore((IResource) getElement(), Activator.getDefault().getPreferenceStore(), getPageId());

		Control result = super.createContents(parent);

		return result;
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
			int size = editors.size();
			for (int i = 0; i < size; i++) {
				FieldEditor editor = editors.get(i);
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
	 * <code>PreferencePage</code> method loads all the field editors with
	 * their default values.
	 */
	@Override
	protected void performDefaults() {
		if (editors != null) {
			Iterator<FieldEditor> e = editors.iterator();
			while (e.hasNext()) {
				FieldEditor pe = e.next();
				pe.loadDefault();
			}
		}

		checkState();
		super.performDefaults();
	}

	/**
	 * The field editor preference page implementation of this
	 * <code>PreferencePage</code> method saves all field editors by calling
	 * <code>FieldEditor.store</code>. Note that this method does not save
	 * the preference store itself; it just stores the values back into the
	 * preference store.
	 * 
	 * @see FieldEditor#store()
	 */
	@Override
	public boolean performOk() {
		if (editors != null) {
			Iterator<FieldEditor> e = editors.iterator();
			while (e.hasNext()) {
				FieldEditor pe = e.next();
				pe.store();
				pe.getPreferenceStore().setToDefault(pe.getPreferenceName());
			}
		}

		super.performOk();

		return true;
	}

	/**
	 * The field editor preference page implementation of this
	 * <code>IPreferencePage</code> (and
	 * <code>IPropertyChangeListener</code>) method intercepts
	 * <code>IS_VALID</code> events but passes other events on to its
	 * superclass.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent event) {

		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
			if (newValue) {
				checkState();
			} else {
				setValid(newValue);
			}
		}
	}

	protected void updateFieldEditors(final boolean enabled) {
		Composite parent = getFieldEditorParent();
		Iterator<FieldEditor> it = editors.iterator();
		while (it.hasNext()) {
			FieldEditor editor = it.next();
			editor.setEnabled(enabled, parent);
		}
	}
}
