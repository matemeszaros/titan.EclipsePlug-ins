/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.nativewin32actions;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.actions.ExternalTitanAction;
import org.eclipse.titan.designer.core.TITANInstallationValidator;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This action invokes the syntactic checking of the selected files
 * <p>
 * Though this looks like
 * {@link org.eclipse.titan.designer.actions.CheckSyntax}, but it is not the
 * same.
 * <ul>
 * <li>This calls the native WIN32 version of TITANJob.
 * <li>This action might develop on a separate path, because of the different
 * usage.
 * </ul>
 * 
 * @author Kristof Szabados
 */
public final class CheckSyntax extends ExternalTitanAction {
	private static final String JOB_TITLE = "Syntax check";

	@Override
	public void run(final IAction action) {
		doCheckSyntax();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		doCheckSyntax();

		return null;
	}

	/**
	 * This method creates the needed {@link NativeWIN32TITANJob} and
	 * schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>If there are no files to be checked than an error is reported to
	 * the user, as in this case TITAN would not any work.
	 * <li>removes markers from the files to be cheked
	 * <li>creates the command that does the syntactic checking
	 * <li>creates a NativeWIN32TITANJob for invoking the command and
	 * redirecting the results
	 * <li>schedules the job.
	 * </ul>
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * 
	 * @param action
	 *                the action proxy that would handle the presentation
	 *                portion of the action. Not used.
	 */
	private void doCheckSyntax() {
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
		command.add('-' + SYNTAX_CHECK_FLAG + getTITANActionFlags());
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
