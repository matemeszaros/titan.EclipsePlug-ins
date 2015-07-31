/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.designer.editors.configeditor.ConfigSkeletons;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * @author Kristof Szabados
 * */
public final class NewConfigFileCreationWizardPage extends WizardNewFileCreationPage {

	private static final String TITLE = "New Configuration File";
	private static final String DESCRIPTION = "Create a new configuration file";
	private static final String ERROR_MESSAGE = "When provided the extension of the configuration file must be \"cfg\"";


	/** Error message for new cfg file name validation for case of empty name */
	private static final String EMPTYCFGFILENAMEERROR = "Configuration files must have a name.";
	
	/** Error message for new cfg file name validation for case of invalid name. Valid name must match CFGFILENAMEREGEXP */
	private static final String INVALIDCFGFILENAME = "Invalid configuration file name {0}";
	
	/** Pattern string for new cfg file validation */
	private static final String CFGFILENAMEREGEXP = "[a-zA-Z][a-zA-Z_0-9]*";
	
	/** Pattern for new cfg file validation */
	private static final Pattern CFGFILENAMEPATTERN = Pattern.compile(CFGFILENAMEREGEXP);
	
	private final NewConfigFileWizard wizard;

	public NewConfigFileCreationWizardPage(final IStructuredSelection selection, final NewConfigFileWizard wizard) {
		super(TITLE, selection);
		this.wizard = wizard;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		String extension = getContainerFullPath().append(getFileName()).getFileExtension();

		if (extension == null) {
			setErrorMessage(null);
			return validateName();
		}

		boolean valid = false;
		for (int i = 0; i < GlobalParser.SUPPORTED_CONFIG_FILE_EXTENSIONS.length; i++) {
			if (GlobalParser.SUPPORTED_CONFIG_FILE_EXTENSIONS[i].equals(extension)) {
				valid = true;
				break;
			}
		}
		if (!valid) {
			setErrorMessage(ERROR_MESSAGE);
			return false;
		}

		setErrorMessage(null);
		return validateName();
	}

	@Override
	protected InputStream getInitialContents() {
		if (wizard.getOptionsPage().isGenerateSkeletonSelected()) {
			return new BufferedInputStream(new ByteArrayInputStream(ConfigSkeletons.CONFIG_FILE_SKELETON.getBytes()));
		}

		return super.getInitialContents();
	}

	/**
	 * Validates page in case of new cfg file is created.
	 * If cfg file name is invalid,
	 * Finish button is disabled and error message is displayed. 
	 */
	public boolean validateName() {
		String originalmoduleName = getFileName();
		if (originalmoduleName == null) {
			return false;
		}

		int dotIndex = originalmoduleName.lastIndexOf('.');
		String longModuleName = dotIndex == -1 ? originalmoduleName : originalmoduleName.substring(0, dotIndex);

		if ("".equals(longModuleName)) {
			setErrorMessage(EMPTYCFGFILENAMEERROR);
			return false;
		}

		if (!CFGFILENAMEPATTERN.matcher(longModuleName).matches()) {
			setErrorMessage(MessageFormat.format(INVALIDCFGFILENAME, longModuleName));
			return false;
		}

		setErrorMessage(null);
		return true;
	}
}
