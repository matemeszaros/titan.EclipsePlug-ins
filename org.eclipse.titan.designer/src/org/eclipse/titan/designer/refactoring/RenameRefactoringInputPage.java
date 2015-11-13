/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.refactoring;

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
 * @author Adam Delic
 * */
public class RenameRefactoringInputPage extends UserInputWizardPage {

	private Text newNameText;

	public RenameRefactoringInputPage(final String name) {
		super(name);
	}

	@Override
	public void createControl(final Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		initializeDialogUnits(top);
		setControl(top);
		top.setLayout(new GridLayout(2, false));
		Label label = new Label(top, SWT.NONE);
		label.setText("New name:");
		newNameText = new Text(top, SWT.BORDER);
		newNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (checkNewNameValidity()) {
					((RenameRefactoring) getRefactoring()).setNewIdentifierName(newNameText.getText());
				}
			}
		});
		String oldName = "";
		switch (((RenameRefactoring) getRefactoring()).getModule().getModuletype()) {
		case TTCN3_MODULE:
			oldName = ((RenameRefactoring) getRefactoring()).getRefdIdentifier().getTtcnName();
			break;
		case ASN_MODULE:
			oldName = ((RenameRefactoring) getRefactoring()).getRefdIdentifier().getAsnName();
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
		}
		newNameText.setText(oldName);
		newNameText.setFocus();
		newNameText.selectAll();
		checkNewNameValidity();
	}

	/**
	 * Checks if the new name is a valid identifier
	 * 
	 * @return true if it is valid, false otherwise
	 */
	boolean checkNewNameValidity() {
		String newName = newNameText.getText();
		if (newName.length() == 0) {
			setErrorMessage(null);
			setPageComplete(false);
			return false;
		}
		switch (((RenameRefactoring) getRefactoring()).getModule().getModuletype()) {
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
