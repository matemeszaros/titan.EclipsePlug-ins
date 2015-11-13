/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
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