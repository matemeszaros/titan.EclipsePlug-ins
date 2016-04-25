/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.license;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Peter Dimitrov
 * */
public final class LicenseValidator {
	private static String resolvedLicenseFilePath = "";

	// milliseconds in a day 1000 * 60 * 60 * 24
	public static final int MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24;

	// 2 weeks
	public static final int EXPIRATION_WARNING_TIMEOUT = 14;
	private static final String CORRECTIONSTRING = "Please check that the \"License file\" field"
			+ " on the Window / Preferences / TITAN preferences page is set correctly.";

	private static boolean wasChecked = false;
	private static boolean wasCorrect = false;

	private static class LicenseErrorDialog extends MessageDialog {
		private static volatile boolean isDialogOpen = false;

		public LicenseErrorDialog(final Shell parent, final String title, final String message) {
			super(parent, title, null, message, ERROR, new String[] { IDialogConstants.OK_LABEL, "Open License Preferences" }, 0);
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

	/** private constructor to disable instantiation. */
	private LicenseValidator() {
		// Do nothing
	}

	public static synchronized boolean check() {
		if ( !License.isLicenseNeeded() ) {
			return true;
		}
		
		if (wasChecked) {
			return wasCorrect;
		}

		final String licensePath = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.LICENSE_FILE_PATH, "", null);
		wasChecked = true;

		if (licensePath == null || licensePath.trim().length() == 0) {
			showErrorDialog("Checking for TITAN license failed", "The path of the license for TITAN is not set.\n\n" + CORRECTIONSTRING);
			wasCorrect = false;
			return false;
		}

		File realFile = new File(licensePath);
		if (!realFile.exists()) {
			showErrorDialog("Checking for TITAN license failed", "The file set as the TITAN license file does not seem to exist.\n\n"
					+ CORRECTIONSTRING);
			wasCorrect = false;
			return false;
		}

		License license = new License(licensePath);
		license.process();

		if (!license.isValid()) {
			showErrorDialog("Checking for TITAN license failed",
					"The file set as the TITAN license file is not TITAN license file or is corrupt.\n\n" + CORRECTIONSTRING);
			wasCorrect = false;
			return false;
		}

		long validUntil = license.getValidUntil().getTime();
		long now = System.currentTimeMillis();

		if (now > validUntil) {
			showErrorDialog("Checking for TITAN license failed", "The TITAN license has expired.\n\n" + CORRECTIONSTRING);
			wasCorrect = false;
			return false;
		}

		wasCorrect = true;
		final long difference = ((validUntil - now) / MILLISECONDS_IN_A_DAY) + 1;

		if (difference <= EXPIRATION_WARNING_TIMEOUT) {
			if (difference == 1) {
				showErrorDialog("Checking for TITAN license", "Please note that your TITAN license will expire within 1 day.");
			} else {
				showErrorDialog("Checking for TITAN license", "Please note that your TITAN license will expire within " + difference
						+ " days.");
			}
		}

		return true;
	}

	private static void showErrorDialog(final String title, final String message) {
		if (PlatformUI.isWorkbenchRunning()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (LicenseErrorDialog.isDialogOpen) {
						return;
					}

					LicenseErrorDialog.isDialogOpen = true;
					LicenseErrorDialog dialog = new LicenseErrorDialog(null, title, message);
					dialog.open();
					LicenseErrorDialog.isDialogOpen = false;
				}
			});
		} else {
			ErrorReporter.logError(message);
		}
	}

	public static void clear() {
		resolvedLicenseFilePath = "";
		wasChecked = false;
	}

	/**
	 * Calculates and returns the path of the license file resolved to be
	 * directly usable in the actual environment.
	 * 
	 * @param force
	 *                weather to update the value if it already exist or
	 *                not.
	 * @return the resolved license file path
	 * */
	public static String getResolvedLicenseFilePath(final boolean force) {
		if(!License.isLicenseNeeded()){
			return "";
		}
		final IPreferencesService preferenceService = Platform.getPreferencesService();
		boolean reportDebugInformation = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		if (force || resolvedLicenseFilePath.length() == 0) {
			String licensePath = preferenceService.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.LICENSE_FILE_PATH,
					"", null);
			resolvedLicenseFilePath = PathConverter.convert(licensePath, reportDebugInformation, TITANDebugConsole.getConsole());
		}

		return resolvedLicenseFilePath;
	}
}
