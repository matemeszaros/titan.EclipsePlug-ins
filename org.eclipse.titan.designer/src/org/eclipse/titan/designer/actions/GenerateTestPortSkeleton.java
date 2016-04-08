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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANInstallationValidator;
import org.eclipse.titan.designer.core.TITANJob;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This action tries to generate a testport skeleton from the selected files.
 * 
 * @author Kristof Szabados
 */
public final class GenerateTestPortSkeleton extends ExternalTitanAction {
	private static final String JOB_TITLE = "Generating Test Port Skeleton";

	/**
	 * This method creates the needed {@link TITANJob} and schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>If there are no files to be checked than an error is reported to the user, as in this case TITAN would not any work.
	 * <li>removes markers from the files to be checked
	 * <li>creates the command that does the skeleton generation
	 * <li>creates a TITANJob for invoking the command and redirecting the results
	 * <li>schedules the job.
	 * </ul>
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 *
	 * @param action the action proxy that would handle the presentation portion of the action.
	 *   Not used.
	 */
	@Override
	public void run(final IAction action) {
		doGenerateSkeleton();
	}


	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		doGenerateSkeleton();

		return null;
	}

	/**
	 * Do generate the skeletons
	 * */
	private void doGenerateSkeleton() {
		if (!TITANInstallationValidator.check(true)) {
			return;
		}

		if (!LicenseValidator.check()) {
			return;
		}

		processSelection();

		if (files.isEmpty()) {
			ErrorReporter.parallelErrorDisplayInMessageDialog(JOB_TITLE + FAILURE_SUFFIX, NO_VALID_FILES);
			return;
		}

		reportOnTheFlyOutdating();

		TITANJob titanJob = new TITANJob(JOB_TITLE, files, workingDir, project);
		titanJob.setPriority(Job.DECORATE);
		titanJob.setUser(true);
		titanJob.setRule(project);

		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(
				ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		List<String> command = new ArrayList<String>();
		command.add(PathConverter.convert(getCompilerPath().toOSString(), reportDebugInformation, TITANDebugConsole.getConsole()));
		command.add('-' + GENERATE_TESTPORT_FLAG + getTITANActionFlags());
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
