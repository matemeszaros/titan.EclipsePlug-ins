/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.error;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * This class only has static methods that pop up an Eclipse error message with
 * its two parameters. They may be called from any thread (not
 * only from UI threads)
 * 
 * @author Gabor
 * 
 */
public final class ErrorMessage {
	
	public static final String LOG_ENTRY_NOTE = " (see error log for further information)";
	
	private ErrorMessage() {
		// disable constructor in utility class
	}

	/**
	 * Constructor
	 * 
	 * @param windowTitle
	 *            : the window's title
	 * @param message
	 *            : the message to show
	 * @param buttonTitle
	 *            : The text to show on the extra button
	 * @param pageName
	 *            : The string that identifies the preference page to open
	 * @param dialogType
	 * 			  : The type of Dialog to create (MessageDialog.WARNING,MessageDialog.ERROR,MessageDialog.Information)
	 */
	public static void show(final String windowTitle, final String message, final String buttonTitle, final String pageName, final int dialogType) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				final MessageDialog err = new MessageDialog(null, windowTitle, null, message, dialogType,
						new String[] { IDialogConstants.OK_LABEL, buttonTitle }, 0) {
					@Override
					protected void buttonPressed(final int buttonId) {
						super.buttonPressed(buttonId);

						if (buttonId == 1) {
							PreferencesUtil.createPreferenceDialogOn(null, pageName, null,
									null).open();
						}
					}
				};

				err.open();
			}
		});
	}

	/**
	 * @param title
	 *            : the window's title
	 * @param message
	 *            : the message to show
	 * @param dialogType
	 * 			  : The type of Dialog to create (MessageDialog.WARNING,MessageDialog.ERROR,MessageDialog.Information)
	 * @see ErrorMessage
	 */
	public static void show(final String title, final String message,final int dialogType) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				final MessageDialog err = new MessageDialog(null, title, null, message, dialogType,
						new String[] { IDialogConstants.OK_LABEL }, 0);
				err.open();
			}
		});
	}
}