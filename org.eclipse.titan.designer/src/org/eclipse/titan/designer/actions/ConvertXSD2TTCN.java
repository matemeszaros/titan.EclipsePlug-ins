/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
 * This action invokes the xsd2ttcn converter on the selected files.
 * 
 * @author Kristof Szabados
 */
public final class ConvertXSD2TTCN extends ExternalTitanAction {
	private static final String JOB_TITLE = "Converting XSD files to TTCN-3";

	private File outputFolder;

	private void setOutputFolder(final File outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * This method creates the needed {@link TITANJob} and schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>If there are no files to be checked than an error is reported to the user, as in this case TITAN would not any work.
	 * <li>removes markers from the files to be checked
	 * <li>creates the command that invokes the converter
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
		doConversion();
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doConversion();

		return null;
	}

	/**
	 * Do the actual work of converting the selected files to TTCN-3
	 * */
	private void doConversion() {
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

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				DirectoryDialog dialog = new DirectoryDialog(new Shell(Display.getDefault()), SWT.SAVE);
				dialog.setFilterPath(project.getLocation().toOSString());
				String outFolder = dialog.open();
				if (outFolder != null) {
					outFolder = outFolder.trim();
					if (outFolder.length() > 0) {
						setOutputFolder(new File(outFolder));
					}
				}
			}
		});

		if (outputFolder == null) {
			return;
		}

		TITANJob titanJob = new TITANJob(JOB_TITLE, files, outputFolder, project);
		titanJob.setPriority(Job.DECORATE);
		titanJob.setUser(true);
		titanJob.setRule(project);

		boolean reportDebugInformation =
				Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		List<String> command = new ArrayList<String>();
		IPreferencesService prefs = Platform.getPreferencesService();
		String pathOfTITAN = prefs.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.TITAN_INSTALLATION_PATH, "", null);
		command.add(PathConverter.convert(
				new Path(pathOfTITAN + File.separatorChar + "bin" + File.separatorChar + "xsd2ttcn").toOSString(),
				reportDebugInformation, TITANDebugConsole.getConsole()));
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
