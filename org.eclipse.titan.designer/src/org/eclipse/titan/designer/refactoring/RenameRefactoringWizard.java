/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.refactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Adam Delic
 * */
public class RenameRefactoringWizard extends RefactoringWizard {
	public RenameRefactoringWizard(final RenameRefactoring r) {
		super(r, DIALOG_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(getRefactoring().getName());
		RenameRefactoringInputPage page = new RenameRefactoringInputPage(getRefactoring().getName());
		addPage(page);
	}
}
