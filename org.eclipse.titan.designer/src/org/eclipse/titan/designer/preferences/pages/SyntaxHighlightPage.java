/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.PreferenceInitializer;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This preference page hold the controls and functionality related to syntax
 * coloring.
 * 
 * @author Kristof Szabados
 */
public final class SyntaxHighlightPage extends PreferencePage implements IWorkbenchPreferencePage {
	private Composite pageComposite;
	private Composite upperHalfComposite;
	private Composite colorEditorsComposite;
	private ColorFieldEditor foregroundColorEditor;
	private ColorFieldEditor backgroundColorEditor;
	private BooleanFieldEditor useBackgroundColor;
	private BooleanFieldEditor isBold;
	private TreeViewer treeViewer;
	private Text coloredWords;
	private SyntaxhighlightLabelProvider labelProvider;

	private PreferenceStore tempstore;
	private Map<String, String> possiblyChangedPreferences = new HashMap<String, String>();

	private final ISelectionChangedListener treeListener = new ISelectionChangedListener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#
		 * selectionChanged
		 * (org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			if (event.getSelection().isEmpty()) {
				return;
			}
			if (event.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					if (selection.getFirstElement() instanceof SyntaxHighlightColoringElement) {
						SyntaxHighlightColoringElement element = (SyntaxHighlightColoringElement) selection.getFirstElement();

						foregroundColorEditor.setEnabled(true, colorEditorsComposite);
						loadIntoTemp(element.getBasePreferenceKey() + PreferenceConstants.FOREGROUND);
						foregroundColorEditor.setPreferenceName(element.getBasePreferenceKey()
								+ PreferenceConstants.FOREGROUND);
						foregroundColorEditor.load();

						backgroundColorEditor.setEnabled(true, colorEditorsComposite);
						loadIntoTemp(element.getBasePreferenceKey() + PreferenceConstants.BACKGROUND);
						backgroundColorEditor.setPreferenceName(element.getBasePreferenceKey()
								+ PreferenceConstants.BACKGROUND);
						backgroundColorEditor.load();

						useBackgroundColor.setEnabled(true, colorEditorsComposite);
						loadIntoTemp(element.getBasePreferenceKey() + PreferenceConstants.USEBACKGROUNDCOLOR);
						useBackgroundColor.setPreferenceName(element.getBasePreferenceKey()
								+ PreferenceConstants.USEBACKGROUNDCOLOR);
						useBackgroundColor.load();

						isBold.setEnabled(true, colorEditorsComposite);
						loadIntoTemp(element.getBasePreferenceKey() + PreferenceConstants.BOLD);
						isBold.setPreferenceName(element.getBasePreferenceKey() + PreferenceConstants.BOLD);
						isBold.load();

						if (element.getWords() != null) {
							coloredWords.setText(element.getWords());
						} else {
							coloredWords.setText("");
						}
						return;
					}
				}
			}

			foregroundColorEditor.setEnabled(false, colorEditorsComposite);
			backgroundColorEditor.setEnabled(false, colorEditorsComposite);
			useBackgroundColor.setEnabled(false, colorEditorsComposite);
			isBold.setEnabled(false, colorEditorsComposite);
			coloredWords.setText("");
		}
	};

	final class TempPreferenceInitializer extends PreferenceInitializer {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.titan.designer.preferences.PreferenceInitializer
		 * #getPreference()
		 */
		@Override
		public IPreferenceStore getPreference() {
			return tempstore;
		}
	}

	protected void loadIntoTemp(final String preferenceName) {
		if (!possiblyChangedPreferences.containsKey(preferenceName)) {
			tempstore.setValue(preferenceName, getPreferenceStore().getString(preferenceName));
		}
	}

	private void storeIntoFinal(final String preferenceName) {
		getPreferenceStore().setValue(preferenceName, tempstore.getString(preferenceName));
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		tempstore = new PreferenceStore();
		TempPreferenceInitializer inittializer = new TempPreferenceInitializer();
		inittializer.initializeDefaultPreferences();

	}

	@Override
	public boolean performOk() {
		for (String changedKey : possiblyChangedPreferences.keySet()) {
			storeIntoFinal(changedKey);
		}
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		String[] preferenceNames = tempstore.preferenceNames();
		for (String preferenceName : preferenceNames) {
			tempstore.setValue(preferenceName, tempstore.getDefaultString(preferenceName));
			possiblyChangedPreferences.put(preferenceName, null);
		}
		if (foregroundColorEditor != null) {
			foregroundColorEditor.loadDefault();
		}
		if (backgroundColorEditor != null) {
			backgroundColorEditor.loadDefault();
		}
		if (useBackgroundColor != null) {
			useBackgroundColor.loadDefault();
		}
		if (isBold != null) {
			isBold.loadDefault();
		}
		super.performDefaults();
	}

	@Override
	public void dispose() {
		foregroundColorEditor.dispose();
		backgroundColorEditor.dispose();
		useBackgroundColor.dispose();
		isBold.dispose();
		coloredWords.dispose();
		labelProvider.dispose();
		colorEditorsComposite.dispose();
		upperHalfComposite.dispose();
		pageComposite.dispose();
		super.dispose();
	}

	protected Control createTreeViewer(final Composite parent) {
		GridLayout treeLayout = new GridLayout();
		treeLayout.numColumns = 2;
		GridData treeData = new GridData();
		treeData.horizontalAlignment = GridData.FILL;
		treeData.verticalAlignment = SWT.FILL;
		treeData.grabExcessHorizontalSpace = true;
		treeData.grabExcessVerticalSpace = true;

		treeViewer = new TreeViewer(parent);
		treeViewer.getControl().setLayoutData(treeData);
		treeViewer.setContentProvider(new SyntaxHighlightContentProvider());
		labelProvider = new SyntaxhighlightLabelProvider();
		treeViewer.setLabelProvider(labelProvider);

		treeViewer.setInput(initialInput());
		treeViewer.addSelectionChangedListener(treeListener);

		return null;
	}

	protected Control createColorEditors(final Composite parent) {
		colorEditorsComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		colorEditorsComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		colorEditorsComposite.setLayoutData(gridData);

		loadIntoTemp(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.FOREGROUND);
		foregroundColorEditor = new ColorFieldEditor(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.FOREGROUND,
				"foreground color:", colorEditorsComposite);
		foregroundColorEditor.setEnabled(false, colorEditorsComposite);
		foregroundColorEditor.setPreferenceStore(tempstore);
		foregroundColorEditor.setPage(this);
		foregroundColorEditor.load();
		foregroundColorEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				possiblyChangedPreferences.put(foregroundColorEditor.getPreferenceName(), null);
				foregroundColorEditor.store();
			}
		});

		loadIntoTemp(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BACKGROUND);
		backgroundColorEditor = new ColorFieldEditor(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BACKGROUND,
				"background color:", colorEditorsComposite);
		backgroundColorEditor.setEnabled(false, colorEditorsComposite);
		backgroundColorEditor.setPreferenceStore(tempstore);
		backgroundColorEditor.setPage(this);
		backgroundColorEditor.load();
		backgroundColorEditor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				possiblyChangedPreferences.put(backgroundColorEditor.getPreferenceName(), null);
				backgroundColorEditor.store();
			}
		});

		loadIntoTemp(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR);
		useBackgroundColor = new BooleanFieldEditor(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR,
				"use background color", SWT.CHECK, colorEditorsComposite);
		useBackgroundColor.setEnabled(false, colorEditorsComposite);
		useBackgroundColor.setPreferenceStore(tempstore);
		useBackgroundColor.setPage(this);
		useBackgroundColor.load();
		useBackgroundColor.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				possiblyChangedPreferences.put(useBackgroundColor.getPreferenceName(), null);
				useBackgroundColor.store();
			}
		});

		loadIntoTemp(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BOLD);
		isBold = new BooleanFieldEditor(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BOLD, "bold", SWT.CHECK,
				colorEditorsComposite);
		isBold.setEnabled(false, colorEditorsComposite);
		isBold.setPreferenceStore(tempstore);
		isBold.setPage(this);
		isBold.load();
		isBold.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				possiblyChangedPreferences.put(isBold.getPreferenceName(), null);
				isBold.store();
			}
		});

		GridData wordsGridData = new GridData();
		wordsGridData.horizontalAlignment = GridData.FILL;
		wordsGridData.verticalAlignment = GridData.FILL;
		wordsGridData.horizontalSpan = 2;
		wordsGridData.grabExcessHorizontalSpace = true;
		wordsGridData.grabExcessVerticalSpace = true;

		coloredWords = new Text(colorEditorsComposite, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		coloredWords.setEditable(false);
		coloredWords.setLayoutData(wordsGridData);
		return null;
	}

	protected Control createUpperHalf(final Composite parent) {
		upperHalfComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		upperHalfComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		upperHalfComposite.setLayoutData(gridData);

		createTreeViewer(upperHalfComposite);
		createColorEditors(upperHalfComposite);

		return null;
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

		createUpperHalf(pageComposite);

		return pageComposite;
	}

	private SyntaxHighlightColoringGroup initialInput() {
		SyntaxHighlightColoringGroup root = new SyntaxHighlightColoringGroup("root");

		SyntaxHighlightColoringGroup generalGroup = new SyntaxHighlightColoringGroup("General");
		generalGroup.add(new SyntaxHighlightColoringElement("Plain text", PreferenceConstants.COLOR_NORMAL_TEXT, "Example text"));
		generalGroup.add(new SyntaxHighlightColoringElement("Comments", PreferenceConstants.COLOR_COMMENTS, "/* Example comment */"));
		generalGroup.add(new SyntaxHighlightColoringElement("Strings", PreferenceConstants.COLOR_STRINGS, "\" Example string \""));
		root.add(generalGroup);

		SyntaxHighlightColoringGroup asn1Group = new SyntaxHighlightColoringGroup("ASN.1 specific");
		asn1Group.add(new SyntaxHighlightColoringElement("Keywords", PreferenceConstants.COLOR_ASN1_KEYWORDS,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.KEYWORDS));
		asn1Group.add(new SyntaxHighlightColoringElement("CMIP verbs", PreferenceConstants.COLOR_CMIP_VERB,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.VERBS));
		asn1Group.add(new SyntaxHighlightColoringElement("compare types", PreferenceConstants.COLOR_COMPARE_TYPE,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.COMPARE_TYPES));
		asn1Group.add(new SyntaxHighlightColoringElement("Status", PreferenceConstants.COLOR_STATUS,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.STATUS_TYPE));
		asn1Group.add(new SyntaxHighlightColoringElement("Tags", PreferenceConstants.COLOR_TAG,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.TAGS));
		asn1Group.add(new SyntaxHighlightColoringElement("Storage", PreferenceConstants.COLOR_STORAGE,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.STORAGE));
		asn1Group.add(new SyntaxHighlightColoringElement("Modifier", PreferenceConstants.COLOR_MODIFIER,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.MODIFIER));
		asn1Group.add(new SyntaxHighlightColoringElement("Access types", PreferenceConstants.COLOR_ACCESS_TYPE,
				org.eclipse.titan.designer.editors.asn1editor.CodeScanner.ACCESS_TYPE));
		root.add(asn1Group);

		SyntaxHighlightColoringGroup configGroup = new SyntaxHighlightColoringGroup("Configuration specific");
		configGroup.add(new SyntaxHighlightColoringElement("Keywords", PreferenceConstants.COLOR_CONFIG_KEYWORDS,
				org.eclipse.titan.designer.editors.configeditor.CodeScanner.KEYWORDS));
		configGroup.add(new SyntaxHighlightColoringElement("Section title", PreferenceConstants.COLOR_SECTION_TITLE,
				org.eclipse.titan.designer.editors.configeditor.CodeScanner.SECTION_TITLES));
		configGroup.add(new SyntaxHighlightColoringElement("File and control mask options",
				PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS,
				org.eclipse.titan.designer.editors.configeditor.CodeScanner.MASK_OPTIONS));
		configGroup.add(new SyntaxHighlightColoringElement("External command types", PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES,
				org.eclipse.titan.designer.editors.configeditor.CodeScanner.EXTERNAL_COMMAND_TYPES));
		root.add(configGroup);

		SyntaxHighlightColoringGroup ttcn3Group = new SyntaxHighlightColoringGroup("TTCN-3 specific");
		ttcn3Group.add(new SyntaxHighlightColoringElement("Keywords", PreferenceConstants.COLOR_TTCN3_KEYWORDS,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.KEYWORDS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Preprocessor", PreferenceConstants.COLOR_PREPROCESSOR, "Example #include"));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Visibility modifiers", PreferenceConstants.COLOR_VISIBILITY_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.VISIBILITY_MODIFIERS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Template match", PreferenceConstants.COLOR_TEMPLATE_MATCH,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.TEMPLATE_MATCH));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Type", PreferenceConstants.COLOR_TYPE,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.TYPES));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Timer operators", PreferenceConstants.COLOR_TIMER_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.TIMER_OPERATIONS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Port operators", PreferenceConstants.COLOR_PORT_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.PORT_OPERATIONS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Config operators", PreferenceConstants.COLOR_CONFIG_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.CONFIGURATION_OPERATIONS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Verdict operators", PreferenceConstants.COLOR_VERDICT_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.VERDICT_OPERATIONS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("System under test related operators", PreferenceConstants.COLOR_SUT_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.SUT_OPERATION));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Function operators", PreferenceConstants.COLOR_FUNCTION_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.FUNCTION_OPERATIONS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Predefined operators", PreferenceConstants.COLOR_PREDEFINED_OP,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.PREDEFINED_OPERATIONS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Boolean consts", PreferenceConstants.COLOR_BOOLEAN_CONST,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.BOOLEAN_CONSTANTS));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Verdict consts", PreferenceConstants.COLOR_TTCN3_VERDICT_CONST,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.VERDICT_CONSTANT));
		ttcn3Group.add(new SyntaxHighlightColoringElement("Other consts", PreferenceConstants.COLOR_OTHER_CONST,
				org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.OTHER_CONSTANT));
		root.add(ttcn3Group);
		return root;
	}
}
