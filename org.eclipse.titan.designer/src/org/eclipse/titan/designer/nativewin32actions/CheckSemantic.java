/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.nativewin32actions;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.actions.ExternalTitanAction;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.core.TITANInstallationValidator;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;

/**
 * This action invokes the semantic checking of the selected files on native
 * WIN32
 * <p>
 * Though this looks like
 * {@link org.eclipse.titan.designer.actions.CheckSemantic}, but it is not the
 * same.
 * <ul>
 * <li>This calls the native WIN32 version of TITANJob.
 * <li>This action might developed on a separate path, because of the different
 * usage.
 * </ul>
 * 
 * @author Kristof Szabados
 */
public final class CheckSemantic extends ExternalTitanAction {
	private static final String JOB_TITLE = "Semantic check";

	/**
	 * This method creates the needed {@link NativeWIN32TITANJob} and
	 * schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>If there are no files to be checked than an error is reported to
	 * the user, as in this case TITAN would not any work.
	 * <li>removes markers from the files to be checked
	 * <li>creates the command that does the semantic checking
	 * <li>creates a NativeWIN32TITANJob for invoking the command and
	 * redirecting the results
	 * <li>schedules the job.
	 * </ul>
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * 
	 * @param action
	 *                the action proxy that would handle the presentation
	 *                portion of the action
	 */
	@Override
	public void run(final IAction action) {
		doCheck();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		doCheck();

		return null;
	}

	protected void doCheck() {
		if (!TITANInstallationValidator.check(true)) {
			return;
		}

		if (!LicenseValidator.check()) {
			return;
		}

		processSelection();

		if (files == null || files.isEmpty()) {
			ErrorReporter.parallelErrorDisplayInMessageDialog(JOB_TITLE + FAILURE_SUFFIX, NO_VALID_FILES);
			return;
		}

		reportOnTheFlyOutdating();

		NativeWIN32TITANJob titanJob = new NativeWIN32TITANJob(JOB_TITLE, files, workingDir, project);
		titanJob.setPriority(Job.DECORATE);
		titanJob.setUser(true);
		titanJob.setRule(project);

		ArrayList<String> command = new ArrayList<String>();
		command.add(getCompilerPath().toOSString());

		String titanFlags = '-' + SEMANTIC_CHECK_FLAG;
		if (GeneralConstants.ETSI_BUILD) {
			titanFlags += '0';
		}

		// If a single project is selected used than use it's attributes
		// used to generate the Makefile, otherwise use the general
		// preferences.
		if (singleSelectedProject == null) {
			TITANConsole.println("Using the general preferences to do the external semantic check");
			titanFlags += getTITANActionFlags();
			command.add(titanFlags);
		} else {
			TITANConsole.println("Using the project properties to do the external semantic check");
			boolean useRuntime2 = false;
			try {
				if ("true".equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						MakefileCreationData.FUNCTIONTESTRUNTIME_PROPERTY)))) {
					useRuntime2 = true;
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While collecting properties of project`" + project.getName() + "'", e);
			}

			command.add(titanFlags);

			String flags = TITANFlagsOptionsData.getTITANFlags(singleSelectedProject, useRuntime2);
			if (flags != null && flags.length() > 0) {
				command.add(flags);
			}
		}

		command.addAll(files.keySet());
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
