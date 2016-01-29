/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

/**
* @author Kristof Szabados, Jeno Balasko
* */
public final class NewASN1ModuleOptionsWizardPage extends NewModuleOptionsWizardPage {
	private static final String TITLE = "ASN1 Module creation options";
	private static final String DESCRIPTION = "Create the new ASN1 module according to these options";

	NewASN1ModuleOptionsWizardPage() {
		super(TITLE);
	}
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
}