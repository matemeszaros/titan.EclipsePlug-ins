/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public final class OnTheFlyCheckerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences of the on-the-fly checker";

	private static final String ENABLE_PARSING = "Enable parsing of TTCN-3, ASN.1 and Runtime Configuration files";
	private static final String ENABLE_INCREMENTAL_PARSING = "Enable the incremental parsing of TTCN-3 files";
	private static final String MINIMISE_MEMORY_USAGE = "Minimise memory usage";
	private static final String DELAY_SEMANTIC_CHECKING = "Delay the on-the-fly semantic check till the file is saved";
	private static final String RECONCILER_TIMEOUT = "Timeout in seconds before on-the-fly check starts";

	private	Composite composite;
	private BooleanFieldEditor useOnTheFlyParsing;
	private BooleanFieldEditor useIncrementalParsing;
//	private BooleanFieldEditor minimiseMemoryUsage;
	private BooleanFieldEditor delaySemanticCheckTillSave;
	private IntegerFieldEditor reconcilerTimeout;

//	private boolean minimiseMemoryChanged = false;

	public OnTheFlyCheckerPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		Composite tempParent = getFieldEditorParent();

		useOnTheFlyParsing = new BooleanFieldEditor(PreferenceConstants.USEONTHEFLYPARSING, ENABLE_PARSING, tempParent);
		addField(useOnTheFlyParsing);

		useIncrementalParsing = new BooleanFieldEditor(PreferenceConstants.USEINCREMENTALPARSING, ENABLE_INCREMENTAL_PARSING, tempParent);
		addField(useIncrementalParsing);
		
		composite = new Composite(tempParent, SWT.NONE);

//		minimiseMemoryUsage = new BooleanFieldEditor(PreferenceConstants.MINIMISEMEMORYUSAGE, MINIMISE_MEMORY_USAGE, tempParent);
//		addField(minimiseMemoryUsage);	

		reconcilerTimeout = new IntegerFieldEditor(PreferenceConstants.RECONCILERTIMEOUT, RECONCILER_TIMEOUT, composite);
		reconcilerTimeout.setValidRange(0, 10);
		reconcilerTimeout.setTextLimit(2);
		addField(reconcilerTimeout);

		delaySemanticCheckTillSave = new BooleanFieldEditor(PreferenceConstants.DELAYSEMANTICCHECKINGTILLSAVE, DELAY_SEMANTIC_CHECKING, tempParent);
		addField(delaySemanticCheckTillSave);
	}

/*	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getSource().equals(minimiseMemoryUsage)) {
			boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
			if (newValue) {
				minimiseMemoryChanged = true;
				ErrorReporter.parallelWarningDisplayInMessageDialog(
						"On-the-fly analyzer",
						"Minimise memory usage is on, this could indicate that in some cases rename refactoring function will not operate properly!");
			}
		}
		
		super.propertyChange(event);
	}*/
	
	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		useOnTheFlyParsing.dispose();
		useIncrementalParsing.dispose();
//		minimiseMemoryUsage.dispose();
		delaySemanticCheckTillSave.dispose();
		reconcilerTimeout.dispose();
		composite.dispose();
		super.dispose();
	}

	private boolean isImportantChanged() {
		return false;//getPreferenceStore().getBoolean(PreferenceConstants.MINIMISEMEMORYUSAGE) != minimiseMemoryUsage.getBooleanValue();
	}

	@Override
	public void performApply() {
		if (isImportantChanged() && getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			ErrorReporter.parallelWarningDisplayInMessageDialog(
				"On-the-fly analyzer",
				"Settings of the on-the-fly analyzer have changed, the known projects have to be re-analyzed completly.\n" 
				+ "This might take some time.");

//			if (minimiseMemoryChanged
//					&& Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
//							PreferenceConstants.MINIMISEMEMORYUSAGE, false, null)) {
//				GlobalParser.clearAllInformation();
//			} else {
				GlobalParser.clearSemanticInformation();
//			}

//			minimiseMemoryChanged = false;

			GlobalParser.reAnalyzeSemantically();
		}

		super.performApply();

	}	
}
