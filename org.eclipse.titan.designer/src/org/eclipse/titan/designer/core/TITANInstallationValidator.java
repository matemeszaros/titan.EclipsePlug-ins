/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Kristof Szabados
 * */
public final class TITANInstallationValidator {
	private static final String CORRECTION_STRING = "Please check that the \"TITAN installation path\" field"
			+ " on the Window / Preferences / TITAN preferences page is set correctly.";

	private static boolean wasChecked = false;
	private static boolean wasCorrect = false;

	private static class InstallationErrorDialog extends MessageDialog {
		public InstallationErrorDialog(final Shell parent, final String title, final String message) {
			super(parent, title, null, message, ERROR, new String[] { IDialogConstants.OK_LABEL, "Open TITAN Preferences" }, 0);
		}

		@Override
		protected void buttonPressed(final int buttonId) {
			super.buttonPressed(buttonId);

			if (buttonId == Window.CANCEL) {
				PreferencesUtil.createPreferenceDialogOn(null,
						"org.eclipse.titan.designer.preferences.pages.TITANPreferencePage", null, null).open();
			}
		}
	}

	/** private constructor to disable instantiation */
	private TITANInstallationValidator() {
	}

	public static boolean check(final boolean forceDialog) {
		if (!forceDialog && wasChecked) {
			return wasCorrect;
		}

		final String installationPath = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.TITAN_INSTALLATION_PATH, null, null);

		wasChecked = true;
		wasCorrect = true;

		// check if the field is empty
		if (installationPath == null || "".equals(installationPath)) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					InstallationErrorDialog dialog = new InstallationErrorDialog(null,
							"Checking for installed TITAN failed", "The installation path of TITAN is not set.\n\n"
									+ CORRECTION_STRING);
					dialog.open();
				}
			});
			wasCorrect = false;
			return false;
		}

		String compilerPath = new StringBuilder(installationPath).append(File.separatorChar).append("bin").append(File.separatorChar)
				.append("compiler").toString();

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			compilerPath = compilerPath + ".exe";
		}

		File tempFile = new File(compilerPath);
		if (!tempFile.exists()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					InstallationErrorDialog dialog = new InstallationErrorDialog(null,
							"Checking for installed TITAN failed", "No TITAN was found at " + installationPath + "\n\n"
									+ CORRECTION_STRING);
					dialog.open();
				}
			});
			wasCorrect = false;
		}

		return wasCorrect;
	}

	public static void clear() {
		wasChecked = false;
	}
}
