/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.titan.common.logging.ErrorReporter;

public final class FileUtils {

	private FileUtils() {
		// Hide constructor
	}

	/**
	 * Deletes a file.
	 * @param file the file to delete
	 * @throws IOException if the file cannot be deleted
	 */
	public static void delete(final File file) throws IOException {
		if (deleteIfExists(file)) {
			throw new IOException("The file cannot be deleted: " + file.getAbsolutePath());
		}
	}

	/**
	 * Deletes a file. Logs the error if fails.
	 *
	 * @param file the file to delete
	 */
	public static boolean deleteQuietly(final File file) {
		final boolean failed = deleteIfExists(file);
		if (failed) {
			ErrorReporter.logError("Cannot delete file: " + file.getPath());
		}

		return !failed;
	}

	private static boolean deleteIfExists(final File file) {
		return file.exists() && !file.delete();
	}
}
