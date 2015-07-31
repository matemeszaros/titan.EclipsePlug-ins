package org.eclipse.titan.designer;

public final class DebugUtils {

	// Returns the first "depth" number of elements of the call chain,
	// e.g.  (internalDoAnalyzeWithReferences -> markAllMarkersForRemoval -> markMarkersForRemoval)
	public static String getStackTrace(int depth) {
		if (depth < 1) {
			return "";
		}

		String st = "(";
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = depth+1 > stackTrace.length - 1 ? stackTrace.length - 1 : depth+1;i > 1; i--) {
			String[] classPath = stackTrace[i].getClassName().split("\\.");
			if (classPath.length > 0) {
				st += classPath[classPath.length -1] +":";
			}
			st += stackTrace[i].getMethodName();
			if (i>2) {
				st += " -> ";
			}
		}
		return st + ")";
	}

	//Returns a String representation of the current unix-style timestamp on the system
	public static String getTimestamp() {
		return String.valueOf(System.currentTimeMillis());
	}
}