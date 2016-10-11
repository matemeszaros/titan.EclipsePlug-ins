/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.nativewin32actions;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.designer.core.TITANJob;
import org.eclipse.titan.designer.license.License;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class takes care of every job that involves calling parts of the TITAN
 * build environment on WIN32
 * <p>
 * Though this looks like {@link org.eclipse.titan.designer.core.TITANJob}, but
 * it is not the same.
 * <ul>
 * <li>This calls the native WIN32 version of TITANJob.
 * <li>This action might develop on a separate path, because of the different
 * usage.
 * </ul>
 * 
 * @author Kristof Szabados
 */
public final class NativeWIN32TITANJob extends TITANJob {
	public NativeWIN32TITANJob(final String name, final Map<String, IFile> files, final File workingDir, final IProject project) {
		super(name, files, workingDir, project);
	}

	/**
	 * Sets the required environmental variables for the ProcessBuilder
	 * passed in as argument. This must be separated from the general
	 * behavior, because different operating systems might require different
	 * environmental variables.
	 * <p>
	 * This version is for native win32 and assumes that no transformation
	 * is needed.
	 * 
	 * @param pb
	 *                the ProcessBuilder whose environmental variables need
	 *                to be set.
	 * 
	 * @see TITANJob#runInWorkspace(IProgressMonitor)
	 * @see TITANJob#setEnvironmentalVariables(ProcessBuilder)
	 * */
	@Override
	protected void setEnvironmentalVariables(final ProcessBuilder pb) {
		final IPreferencesService service = Platform.getPreferencesService();
		final String pathOfTITAN = service.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.TITAN_INSTALLATION_PATH, "",
				null);
		
		final Map<String, String> env = pb.environment();
		if(License.isLicenseNeeded()) {
			final String licenseFilePath = 
					service.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.LICENSE_FILE_PATH, "", null);
			env.put(TTCN3_LICENSE_FILE_KEY, licenseFilePath);
		}
		env.put(TTCN3_DIR_KEY, pathOfTITAN);
	}

	/**
	 * Creates the final command from the one actually selected for
	 * execution from the list of commands. This must be separated from the
	 * general behavior, because in different operating systems processes
	 * must be started differently
	 * <p>
	 * This version is for native win32 and assumes that no transformation
	 * is required.
	 * 
	 * @param actualCommand
	 *                the command selected for execution.
	 * @return the final command that can be passed to the operating system.
	 * 
	 * @see TITANJob#runInWorkspace(IProgressMonitor)
	 * */
	@Override
	protected List<String> getFinalCommand(final List<String> actualCommand) {
		return actualCommand;
	}
}
