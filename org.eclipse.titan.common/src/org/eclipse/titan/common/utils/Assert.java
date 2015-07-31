package org.eclipse.titan.common.utils;

public final class Assert {

	private Assert() {
		// Hide constructor
	}

	/**
	 * Assert that an object is not null.
	 * @param object the object to check
	 * @param message the exception message
	 * @throws java.lang.IllegalArgumentException if the object is {@code null}
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
}
