/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Identifier;

/**
 * Wizard page #1: edit the name of the new function.
 *
 * @author Viktor Varga
 */
public class ExtractToFunctionWizardFuncNamePage extends UserInputWizardPage {

	private static final String LABEL_NEWFUNCNAME = "New function name:";
	private static final String DEFAULT_FUNC_NAME = "newFunction";
	private Text newFuncName;

	public ExtractToFunctionWizardFuncNamePage(final String name) {
		super(name);
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite top = new Composite(parent, SWT.NONE);
		initializeDialogUnits(top);
		setControl(top);
		top.setLayout(new GridLayout(2, false));
		final Label label = new Label(top, SWT.NONE);
		label.setText(LABEL_NEWFUNCNAME);
		newFuncName = new Text(top, SWT.BORDER);
		newFuncName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newFuncName.setText(DEFAULT_FUNC_NAME);
		newFuncName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (checkNewNameValidity()) {
					final StringBuilder newFuncNameSB = ((ExtractToFunctionRefactoring)getRefactoring()).getNewFunctionName();
					newFuncNameSB.setLength(0);
					newFuncNameSB.append(newFuncName.getText());
				}
			}
		});
		newFuncName.setFocus();
		newFuncName.selectAll();
		checkNewNameValidity();

	}


	private boolean checkNewNameValidity() {
		final String newName = newFuncName.getText();
		if (newName.length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return false;
		}
		switch (((ExtractToFunctionRefactoring)getRefactoring()).getSelectedModule().getModuletype()) {
		case TTCN3_MODULE:
			if (!Identifier.isValidInTtcn(newName)) {
				setErrorMessage("Not a valid TTCN-3 identifier!");
				setPageComplete(false);
				return false;
			}
			break;
		case ASN_MODULE:
			if (!Identifier.isValidInAsn(newName)) {
				setErrorMessage("Not a valid ASN.1 identifier!");
				setPageComplete(false);
				return false;
			}
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
		}
		setErrorMessage(null);
		setPageComplete(true);
		return true;
	}


}
