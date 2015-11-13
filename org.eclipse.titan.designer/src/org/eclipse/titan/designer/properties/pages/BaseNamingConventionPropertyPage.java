/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * @author Kristof Szabados
 * */
abstract class BaseNamingConventionPropertyPage extends FieldEditorPropertyPage {

	private boolean changed = false;
	private Composite pagecomp;
	private boolean initilizing = true;

	private static class RegexpFieldEditor extends StringFieldEditor {
		private final String regexpName;

		public RegexpFieldEditor(final String name, final String labelText, final Composite parent, final String regexpName) {
			super(name, labelText, UNLIMITED, parent);
			this.regexpName = regexpName;
		}

		@Override
		public boolean isValid() {
			super.refreshValidState();
			return super.isValid();
		}

		@Override
		protected boolean checkState() {
			boolean result = false;
			if (isEmptyStringAllowed()) {
				result = true;
			}

			if (getTextControl() == null) {
				result = false;
			}

			String txt = getTextControl().getText();

			result = !txt.trim().isEmpty() || isEmptyStringAllowed();

			// call hook for subclasses
			result = result && doCheckState();

			if (!result && getPage().getErrorMessage() == null) {
				showErrorMessage(getErrorMessage());
			}

			return result;
		}

		@Override
		protected boolean doCheckState() {
			String newValue = getStringValue();
			try {
				Pattern.compile(newValue);
			} catch (PatternSyntaxException e) {
				setErrorMessage("The format of " + regexpName + " name regexp is wrong");
				return false;
			}

			return true;
		}

	}

	BaseNamingConventionPropertyPage(final int style) {
		super(style);
	}

	protected boolean getChanged() {
		return changed;
	}

	protected void setChanged(final boolean changed) {
		this.changed = changed;
	}

	@Override
	protected Control createContents(final Composite parent) {
		pagecomp = new Composite(parent, SWT.NONE);
		pagecomp.setLayout(new GridLayout(1, false));
		pagecomp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Control result = super.createContents(pagecomp);
		initilizing = false;
		return result;
	}

	@Override
	protected void checkState() {
		if (!initilizing) {
			setErrorMessage(null);
		}

		super.checkState();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		changed = true;
		super.propertyChange(event);
	}

	/**
	 * Create the body part of the naming convention preference page.
	 * 
	 * @param parent
	 *                the parent composite.
	 * */
	protected void createNamingConventionBody(final Composite parent) {
		createModuleSection(parent);
		createTTCN3GlobalSection(parent);
		createTTCN3LocalSection(parent);
		createTTCN3ComponentSection(parent);
	}

	/**
	 * Creates a field editor with the provided preference and name.
	 * 
	 * @param parent
	 *                the composite to add the field editor to.
	 * @param preference
	 *                the preference the field editor should handle.
	 * @param namepart
	 *                the name of the field editor to be displayed.
	 * 
	 * @return the new field editor.
	 * */
	private RegexpFieldEditor createFieldEditor(final Composite parent, final String preference, final String namepart) {
		RegexpFieldEditor stringEditor = new RegexpFieldEditor(preference, "Format of " + namepart + " names", parent, namepart);
		stringEditor.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
				setErrorMessage(null);
				String newValue = ((StringFieldEditor) event.getSource()).getStringValue();
				try {
					Pattern.compile(newValue);
				} catch (PatternSyntaxException e) {
					setErrorMessage("The format of " + namepart + " name regexp is wrong");
				}
			}
		});
		return stringEditor;
	}

	/**
	 * Creates an expandable composite on the user interface.
	 * 
	 * @param parent
	 *                the parent composite where this one can be added to.
	 * @param title
	 *                the title of the new composite.
	 * 
	 * @return the created composite.
	 * */
	private ExpandableComposite createExtendableComposite(final Composite parent, final String title) {
		final ExpandableComposite ex = new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE
				| ExpandableComposite.CLIENT_INDENT | ExpandableComposite.COMPACT);
		ex.setText(title);
		ex.setExpanded(false);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		ex.setLayoutData(data);
		ex.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				Composite temp = parent;
				while (temp != null && !(temp instanceof ScrolledComposite)) {
					temp = temp.getParent();
				}

				if (temp != null) {
					Point point = pagecomp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					((ScrolledComposite) temp).setMinSize(point);
					((ScrolledComposite) temp).layout(true, true);
				}
			}
		});

		return ex;
	}

	/**
	 * Creates the section of naming conventions governing TTCN-3 and ASN.1
	 * module names.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createModuleSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "module names");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_TTCN3MODULE, "TTCN-3 module name"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_ASN1MODULE, "ASN.1 module name"));
	}

	/**
	 * Creates the section of naming conventions governing TTCN-3 module
	 * level definitions.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createTTCN3GlobalSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "global TTCN-3 definitions");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(true);
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_ALTSTEP, "altstep"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_CONSTANT, "global constant"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALCONSTANT, "external constant"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_FUNCTION, "function"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALFUNCTION, "external function"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_MODULEPAR, "module parameter"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_PORT, "global port"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TEMPLATE, "global template"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_TESTCASE, "testcase"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TIMER, "global timer"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_TYPE, "type"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_GROUP, "group"));
	}

	/**
	 * Creates the section of naming conventions governing local TTCN-3
	 * definitions.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createTTCN3LocalSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "local TTCN-3 definitions");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);

		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_CONSTANT, "local constant"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARIABLE, "local variable"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TEMPLATE, "local template"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE, "local variable template"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TIMER, "local timer"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_FORMAL_PARAMETER, "formal paremeter"));
	}

	/**
	 * Creates the section of naming conventions governing component
	 * internal definitions.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createTTCN3ComponentSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "component internal TTCN-3 definitions");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);

		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_CONSTANT, "component constant"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_VARIABLE, "component variable"));
		addField(createFieldEditor(comp, PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_TIMER, "component timer"));
	}
}
