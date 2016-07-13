package org.eclipse.titan.designer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OutOfMemoryCheck {
	private static final long DecimalGigaByte = 1000 * 1000;
	private static boolean isOutOfMemory = false;

	public static final String OUTOFMEMORYERROR =
			"Free memory is running low. Syntactic and semantic check has been disabled. (Can be re-enabled in Window -> Preferences -> Titan preferences -> On-TheFly checker Preferences page)";

	private static class OutOfMemoryErrorDialog extends MessageDialog {
		private static volatile boolean isDialogOpen = false;

		public OutOfMemoryErrorDialog(final Shell parent, final String title, final String message) {
			super(parent, title, null, message, ERROR, new String[] {IDialogConstants.OK_LABEL}, 0);
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

	public static boolean isOutOfMemoryAlreadyReported() {
		return isOutOfMemory;
	}

	public static void outOfMemoryEvent() {
		isOutOfMemory = true;
		ErrorReporter.logError(OUTOFMEMORYERROR);

		if (PlatformUI.isWorkbenchRunning()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (OutOfMemoryErrorDialog.isDialogOpen) {
						return;
					}
					Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.USEONTHEFLYPARSING, false);
					OutOfMemoryErrorDialog.isDialogOpen = true;
					OutOfMemoryErrorDialog dialog = new OutOfMemoryErrorDialog(null, "Low memory", OUTOFMEMORYERROR);
					dialog.open();
					OutOfMemoryErrorDialog.isDialogOpen = false;
				}
			});
		}
		
		GlobalParser.clearAllInformation();
		System.gc();
	}

	public static void resetOutOfMemoryflag() {
		isOutOfMemory = false;
	}
	/**
	 * Check if remaining free memory is low
	 *
	 * @return true: if the remaining free memory is low
	 * */
	public static boolean isOutOfMemory() {
		if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.CHECKFORLOWMEMORY)) {
			Runtime Rt = Runtime.getRuntime();

			long free = Rt.freeMemory();
			long total = Rt.totalMemory();

			long limit = Math.min(200 * DecimalGigaByte, Math.round(total * (double)0.1));

			if (free < limit) {
				ErrorReporter.logError("limit: "+String.valueOf(limit)+", free: " + String.valueOf(free));
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
