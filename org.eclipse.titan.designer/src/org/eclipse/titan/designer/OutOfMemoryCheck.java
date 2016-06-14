package org.eclipse.titan.designer;

public class OutOfMemoryCheck {
	private static boolean isOutOfMemory = false;

	public static boolean isOutOfMemory() {
		return isOutOfMemory;
	}

	public static void setOutOfMemory(boolean isOutOfMemory) {
		OutOfMemoryCheck.isOutOfMemory = isOutOfMemory;
	}

}
