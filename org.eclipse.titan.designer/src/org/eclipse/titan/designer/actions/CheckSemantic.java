/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANInstallationValidator;
import org.eclipse.titan.designer.core.TITANJob;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;

/**
 * This action invokes the semantic checking of the selected files.
 * 
 * @author Kristof Szabados
 */
public final class CheckSemantic extends ExternalTitanAction {
	private static final String JOB_TITLE = "Semantic check";

	/**
	 * This method creates the needed {@link TITANJob} and schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>If there are no files to be checked than an error is reported to the user, as in this case TITAN would not any work.
	 * <li>removes markers from the files to be checked
	 * <li>creates the command that does the semantic checking
	 * <li>creates a TITANJob for invoking the command and redirecting the results
	 * <li>schedules the job.
	 * </ul>
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 *
	 * @param action the action proxy that handles the presentation portion of the action (not used here)
	 */
	@Override
	public void run(final IAction action) {
		doCheckSemantics();
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doCheckSemantics();

		return null;
	}

	/**
	 * do the actual work of checking the syntax of the selected resources.
	 * */
	private void doCheckSemantics() {
		if (!TITANInstallationValidator.check(true)) {
			return;
		}

		if (!LicenseValidator.check()) {
			return;
		}

		processSelection();

		if (files == null || files.isEmpty()) {
			ErrorReporter.parallelErrorDisplayInMessageDialog( JOB_TITLE + FAILURE_SUFFIX, NO_VALID_FILES);
			return;
		}

		reportOnTheFlyOutdating();

		TITANJob titanJob = new TITANJob(JOB_TITLE, files, workingDir, project);
		titanJob.setPriority(Job.DECORATE);
		titanJob.setUser(true);
		titanJob.setRule(project);

		boolean reportDebugInformation =
				Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		List<String> command = new ArrayList<String>();
		command.add(PathConverter.convert(getCompilerPath().toOSString(), reportDebugInformation, TITANDebugConsole.getConsole()));

		// If a single project is selected used than use it's attributes used to generate the Makefile, otherwise use the general preferences.
		if (singleSelectedProject == null) {
			TITANConsole.println("Using the general preferences to do the external semantic check");
			command.add('-' + SEMANTIC_CHECK_FLAG + getTITANActionFlags());
		} else {
			TITANConsole.println("Using the project properties to do the external semantic check");
			boolean useRuntime2 = false;
			try {
				if ("true".equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						MakefileCreationData.FUNCTIONTESTRUNTIME_PROPERTY)))) {
					useRuntime2 = true;
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Error while reading persistent property", e);
			}
			String flags = TITANFlagsOptionsData.getTITANFlags(singleSelectedProject, useRuntime2);
			command.add('-' + SEMANTIC_CHECK_FLAG + " " + flags);
		}
		for (String filePath : files.keySet()) {
			command.add('\'' + filePath + '\'');
		}
		titanJob.addCommand(command, JOB_TITLE);
		titanJob.removeCompilerMarkers();

		String markersAfterCompiler = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.ONTHEFLYMARKERSAFTERCOMPILER, PreferenceConstantValues.ONTHEFLYOPTIONREMOVE, null);

		if (PreferenceConstantValues.ONTHEFLYOPTIONREMOVE.equals(markersAfterCompiler)) {
			titanJob.removeOnTheFlyMarkers();
		}

		titanJob.schedule();
	}
}
