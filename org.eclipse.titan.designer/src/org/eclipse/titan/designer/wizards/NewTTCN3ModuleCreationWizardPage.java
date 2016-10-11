/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * @author Kristof Szabados
 * */
public final class NewTTCN3ModuleCreationWizardPage extends WizardNewFileCreationPage {
	private static final String EMPTYNAMEERROR = "TTCN3 modules must have a name.";
	private static final String INVALIDMODULENAME = "Invalid TTCN3 module name {0}";
	private static final String MODULENAMEREGEXP = "[a-zA-Z][a-zA-Z_0-9]*";
	private static final Pattern MODULENAMEPATTERN = Pattern.compile(MODULENAMEREGEXP);

	private static final String TITLE = "New TTCN3 module";
	private static final String DESCRIPTION = "Create a new TTCN3 module";
	private static final String ERROR_MESSAGE = "When provided the extension of the TTCN3 Module must be \"ttcn\" or \"ttcn3\"";
	private static final String OCCUPIED = "This module name would create a file that already exists.";
	
	private boolean hasLicense;
	private final NewTTCN3ModuleWizard wizard;

	public NewTTCN3ModuleCreationWizardPage(final IStructuredSelection selection, final NewTTCN3ModuleWizard wizard) {
		super(TITLE, selection);
		hasLicense = LicenseValidator.check();
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
			// test what will happen if we add the extension
			IPath fullPath = getContainerFullPath().append(getFileName()).addFileExtension(GlobalParser.SUPPORTED_TTCN3_EXTENSIONS[1]);
			// path is invalid if any prefix is occupied by a file
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			while (fullPath.segmentCount() > 1) {
				if (root.getFile(fullPath).exists()) {
					setErrorMessage(OCCUPIED);
					return false;
				}
				fullPath = fullPath.removeLastSegments(1);
			}
		} else {
			// test the extension
			if (!GlobalParser.isSupportedTTCN3Extension(extension)) {
				setErrorMessage(ERROR_MESSAGE);
				return false;
			}
		}

		// check modulename
		IPath path = getContainerFullPath();
		if (hasLicense && path != null) {
			IFile file = createFileHandle(path.append(getFileName()));
			ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
			if (projectSourceParser.getLastTimeChecked() == null) {
				WorkspaceJob job = projectSourceParser.analyzeAll();

				if (job != null) {
					try {
						job.join();
					} catch (InterruptedException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}
			String moduleName = getFileName();
			int dotIndex = moduleName.indexOf('.');
			moduleName = dotIndex == -1 ? moduleName : moduleName.substring(0, dotIndex);
			Module module = projectSourceParser.getModuleByName(moduleName);
			if (module != null) {
				setErrorMessage("A module with the name " + moduleName + " already exists in the project "
						+ file.getProject().getName());
				return false;
			}
		}

		return validateName();

	}

	/**
	 * Validate the module name entered by the user.
	 * */
	private boolean validateName() {
		final String originalmoduleName = getFileName();
		if (originalmoduleName == null) {
			return false;
		}

		final int dotIndex = originalmoduleName.lastIndexOf('.');
		final String longModuleName = dotIndex == -1 ? originalmoduleName : originalmoduleName.substring(0, dotIndex);

		if ("".equals(longModuleName)) {
			setErrorMessage(EMPTYNAMEERROR);
			return false;
		}

		if (!MODULENAMEPATTERN.matcher(longModuleName).matches()) {
			setErrorMessage(MessageFormat.format(INVALIDMODULENAME, longModuleName));
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	@Override
	protected InputStream getInitialContents() {
		switch (wizard.getOptionsPage().getGeneratedModuleType()) {
		case EMPTY:
			return super.getInitialContents();
		case NAME_AND_EMPTY_BODY:
			String temporalModule = TTCN3CodeSkeletons.getTTCN3ModuleWithEmptyBody(getModuleName());
			return new BufferedInputStream(new ByteArrayInputStream(temporalModule.getBytes()));
		case SKELETON:
			String temporalModuleSkeleton = TTCN3CodeSkeletons.getTTCN3ModuleSkeleton(getModuleName());
			return new BufferedInputStream(new ByteArrayInputStream(temporalModuleSkeleton.getBytes()));
		default:
			return super.getInitialContents();
		}

	}

	private String getModuleName() {
		String moduleName = getFileName();
		int dotIndex = moduleName.indexOf('.');
		moduleName = dotIndex == -1 ? moduleName : moduleName.substring(0, dotIndex);
		return moduleName;
	}

}
