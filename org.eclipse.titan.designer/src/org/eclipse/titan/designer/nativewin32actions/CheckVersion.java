/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.nativewin32actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.designer.actions.ExternalTitanAction;
import org.eclipse.titan.designer.core.TITANInstallationValidator;
import org.eclipse.titan.designer.license.LicenseValidator;

/**
 * This action invokes the compiler to get it's version information
 * <p>
 * Though this looks like
 * {@link org.eclipse.titan.designer.actions.CheckVersion}, but it is not the
 * same.
 * <ul>
 * <li>This calls the native WIN32 version of TITANJob.
 * <li>This action might develop on a separate path, because of the different
 * usage.
 * </ul>
 * 
 * @author Kristof Szabados
 */
public final class CheckVersion extends ExternalTitanAction {
	private static final String JOB_TITLE = "Version check";
	private static final String DOT = ".";

	@Override
	public void run(final IAction action) {
		doCheckVersion();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doCheckVersion();

		return null;
	}

	/**
	 * This method creates the needed {@link NativeWIN32TITANJob} and
	 * schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>creates the command that invokes the compiler with the -v flag
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
	private void doCheckVersion() {
		if (!TITANInstallationValidator.check(true)) {
			return;
		}

		if (!LicenseValidator.check()) {
			return;
		}

		processSelection();

		NativeWIN32TITANJob titanJob = new NativeWIN32TITANJob(JOB_TITLE, new HashMap<String, IFile>(), new File(DOT), project);
		titanJob.setPriority(Job.DECORATE);
		titanJob.setUser(true);
		titanJob.setRule(project);

		ArrayList<String> command = new ArrayList<String>();
		command.add(getCompilerPath().toOSString());
		command.add('-' + VERSION_CHECK_FLAG);
		titanJob.addCommand(command, JOB_TITLE);

		titanJob.schedule();
	}
}
