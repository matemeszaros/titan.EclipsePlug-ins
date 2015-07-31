/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.MSGTPYE_PATTERN;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.RULE_PATTERN;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.deleteMsgType;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.deleteRuleset;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.exportToFile;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.getPreferenceKeyForMessageTypeList;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.getPreferenceKeyForRuleList;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.getPreferenceKeyForRulesets;
import static org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.importFromFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler.ImportFailedException;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.MutableComboFieldEditor;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.MutableComboFieldEditor.IItemListener;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.StringListEditor;
import org.eclipse.titan.log.viewer.utils.ImportExportUtils;
import org.eclipse.titan.log.viewer.utils.Messages;

public class DecipheringPreferencePage extends LogViewerPreferenceRootPage {

	private static final String XML_EXTENSION_MASK = "*.xml";
	private static final String XML_EXTENSION = ".xml";

	private MutableComboFieldEditor comboFieldEditor;

	private StringListEditor messageTypeEditor;

	private StringListEditor ruleEditor;

	private String selectedRuleSet;

	private final List<String> deletedRuleSets = new ArrayList<String>();

	/** Stores the already deleted message types.
	 *     key   - the ruleset it belongs to 
	 *     value - the name of the message type
	 */
	private final Map<String, String> deletedMsgTypes = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	public DecipheringPreferencePage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Message name deciphering options");
	}

	@Override
	public void createFieldEditors() {

		comboFieldEditor = new MutableComboFieldEditor(getPreferenceKeyForRulesets(), "rulesets", getFieldEditorParent());
		comboFieldEditor.setPreferenceStore(getPreferenceStore());
		addField(comboFieldEditor);

		comboFieldEditor.setInputValidator(new IInputValidator() {
			@Override
			public String isValid(final String newText) {
				if (newText == null || newText.length() == 0) {
					return "The name should contain at least one character.";
				}

				String[] items = DecipheringPreferencePage.this.comboFieldEditor.getItems();

				for (String str : items) {
					if (str.equals(newText)) {
						return "Ruleset with the given name already exists.";
					}
				}

				return null;
			}
		});

		comboFieldEditor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateMsgTypeEditor();
			}
		});

		comboFieldEditor.addItemListener(new IItemListener() {
			@Override
			public void itemRemoved(final String item) {
				deletedRuleSets.add(item);
				updateMsgTypeEditor();
			}

			@Override
			public void itemAdded(final String item) {
				comboFieldEditor.select(comboFieldEditor.getItemCount() - 1);
				updateMsgTypeEditor();
			}
		});

		messageTypeEditor = new StringListEditor("", "Message types", getFieldEditorParent(), false);
		messageTypeEditor.setPreferenceStore(getPreferenceStore());
		messageTypeEditor.setInputValidator(new IInputValidator() {
			@Override
			public String isValid(final String newText) {
				return MSGTPYE_PATTERN.matcher(newText).matches() ? null : "Invalid message type.";
			}
		});
		messageTypeEditor.setEnabled(false, getFieldEditorParent());
		messageTypeEditor.addSelectionChangedListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateRuleEditor();
			}
		});
		addField(messageTypeEditor);


		ruleEditor = new StringListEditor("", "Deciphering rules", getFieldEditorParent(), true);
		ruleEditor.setPreferenceStore(getPreferenceStore());
		ruleEditor.setInputValidator(new IInputValidator() {
			@Override
			public String isValid(final String newText) {
				return RULE_PATTERN.matcher(newText).matches() ? null : "Invalid rule type.";
			}
		});
		ruleEditor.setEnabled(false, getFieldEditorParent());
		addField(ruleEditor);
		updatePage();
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_MESSAGE_DECIPHERING_PAGE;
	}

	@Override
	protected void exportPreferences() {

		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		final String exportLastDir = PreferencesHandler.getInstance().getExportLastDir();
		boolean pathValid = new Path(exportLastDir).isValidPath(exportLastDir);
		if ((exportLastDir.compareTo("") != 0) && pathValid) {
			dialog.setFilterPath(exportLastDir);
		}
		dialog.setFilterExtensions(new String[] {XML_EXTENSION_MASK});
		dialog.setText(Messages.getString("ImportExportUtils.0"));
		String dialogResult = dialog.open();
		if (dialogResult == null) {
			return;
		}
		String resultFile = dialog.getFilterPath() + File.separator + dialog.getFileName();
		if (resultFile.compareTo(File.separator) != 0) {
			if (!resultFile.endsWith(XML_EXTENSION)) {
				resultFile = resultFile.concat(XML_EXTENSION);
			}
			File file = new File(resultFile);
			PreferencesHandler.getInstance().setExportLastDir(file.getParentFile().getPath());

			exportToFile(file);

		}
	}

	@Override
	protected void importPreferences() {
		String fileName = ImportExportUtils.getImportSourceFileWithDialog();
		if (fileName == null) {
			return;
		}

		File file = new File(fileName);
		if (!file.exists() || file.length() == 0) {
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(
							new Shell(Display.getDefault()),
							"Invalid file",
							"The file can not be found");
				}
			});
			return;
		}

		try {
			importFromFile(file);
		} catch (final ImportFailedException e) {
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(
							new Shell(Display.getDefault()),
							"Error while importing the rulesets",
							e.getMessage());
				}
			});
		}
		updatePage();
	}

	@Override
	protected void updatePage() {
		updateRulesetEditor();
	}

	private void updateRulesetEditor() {
		comboFieldEditor.load();
		updateMsgTypeEditor();
	}

	private void updateMsgTypeEditor() {
		if (comboFieldEditor.getItemCount() == 0 || comboFieldEditor.getSelectionIndex() == -1) {
			messageTypeEditor.setPreferenceName("");
			messageTypeEditor.load();
			messageTypeEditor.setEnabled(false, getFieldEditorParent());

			ruleEditor.setPreferenceName("");
			ruleEditor.load();
			ruleEditor.setEnabled(false, getFieldEditorParent());
			return;
		}

		selectedRuleSet = comboFieldEditor.getItem(comboFieldEditor.getSelectionIndex());
		messageTypeEditor.setPreferenceName(getPreferenceKeyForMessageTypeList(selectedRuleSet));
		messageTypeEditor.load();
		messageTypeEditor.setEnabled(true, getFieldEditorParent());

		updateRuleEditor();
	}

	private void updateRuleEditor() {
		final String[] msgTypes = messageTypeEditor.getElements();
		final String[] selection = messageTypeEditor.getSelection();
		if (msgTypes.length == 0 || selection.length == 0) {
			ruleEditor.setPreferenceName("");
			ruleEditor.load();
			ruleEditor.setEnabled(false, getFieldEditorParent());
			return;
		}

		ruleEditor.setPreferenceName(getPreferenceKeyForRuleList(selectedRuleSet, selection[0]));
		ruleEditor.load();
		ruleEditor.setEnabled(true, getFieldEditorParent());
	}

	@Override
	protected void performApply() {
		for (String ruleset : deletedRuleSets) {
			deleteRuleset(ruleset);
		}

		for (Entry<String, String> entry : deletedMsgTypes.entrySet()) {
			deleteMsgType(entry.getKey(), entry.getValue());
		}

		super.performApply();
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}
}
