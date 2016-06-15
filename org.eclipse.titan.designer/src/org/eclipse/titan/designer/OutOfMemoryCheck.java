package org.eclipse.titan.designer;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

public class OutOfMemoryCheck {
	private static boolean isOutOfMemory = false;

	public static final String OUTOFMEMORYERROR =
			"Free memory is running low. Syntactic and semantic check has been disabled. (Can be re-enabled in Window -> Preferences -> Titan preferences -> On-TheFly checker Preferences page)";

	public static boolean isOutOfMemoryAlreadyReported() {
		return isOutOfMemory;
	}

	public static void outOfMemoryEvent() {
		isOutOfMemory = true;
		ErrorReporter.parallelErrorDisplayInMessageDialog("Low memory", OUTOFMEMORYERROR);
		ErrorReporter.logError(OUTOFMEMORYERROR);
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.USEONTHEFLYPARSING, false);
		GlobalParser.clearAllInformation();
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
		Runtime Rt = Runtime.getRuntime();

		long free = Rt.freeMemory();
		long total = Rt.totalMemory();

		long limit = Math.round(total * (double)0.15);

		if (free < limit) {
			ErrorReporter.logError("limit: "+String.valueOf(limit)+", free: " + String.valueOf(free));
			return true;
		} else {
			return false;
		}
	}

}
