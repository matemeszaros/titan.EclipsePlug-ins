/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This is a utility class providing IO handling routines.
 */
public final class IOUtils {

	private IOUtils() {
		// Do nothing
	}

	/**
	 * Flush the contents of single string into a file, in a single step.
	 *
	 * @param file the file to flush the string to.
	 * @param data the string data to write into the file.
	 *
	 * @throws IOException when there was an error writing to the file.
	 * */
	public static void writeStringToFile(final File file, final String data) throws IOException {
		BufferedWriter outStream = null;
		try {
			outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF8_KEY));
			outStream.write(data);
		} finally {
			IOUtils.closeQuietly(outStream);
		}
	}

	/**
	 * Equivalent to Closeable.close(), except any exceptions will be ignored. This is typically used in finally blocks.
	 *
	 * see org.apache.commons.io.IOUtils.closeQuietly()
	 *
	 * @param closeable the objects to close, may be null or already closed
	 */
	public static void closeQuietly(final Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (final IOException e) {
				ErrorReporter.logExceptionStackTrace("Error while closing a resource", e);
			}
		}
	}

	/**
	 * Equivalent to Closeable.close(), except any exceptions will be ignored. This is typically used in finally blocks.
	 *
	 * see org.apache.commons.io.IOUtils.closeQuietly()
	 *
	 * @param closeableArr the objects to close, may contain null or already closed objects
	 */
	public static void closeQuietly(final Closeable ... closeableArr) {
		for (final Closeable closeable : closeableArr) {
			closeQuietly(closeable);
		}
	}

	/**
	 * Converts the provided input stream into a single string.
	 *
	 * @param input the stream to read and convert.
	 *
	 * @return the string read from the provided input stream.
	 * */
	public static String inputStreamToString(final InputStream input) throws IOException {
		final Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF8_KEY));
		final StringBuilder content = new StringBuilder();
		final char[] buffer = new char[1024];
		int n;

		while ((n = reader.read(buffer)) != -1) {
			content.append(buffer, 0, n);
		}

		return content.toString();
	}

}
