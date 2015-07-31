/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.log.merge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.StandardCharsets;

public class LogMerger {

	private boolean isErroneous;
	private long dataSize;

	/**
	 * Returns false if error occurred
	 * @param files the list of files to be merged.
	 * @param outputFile the output file
	 * @param monitor the progress monitor
	 * @return {@code true} if the operation completed successfully, {@code false} otherwise
	 */
	public boolean merge(List<IFile> files, File outputFile, IProgressMonitor monitor) {
		isErroneous = false;
		FileUtils.deleteQuietly(outputFile);

		PrintWriter writer;
		try {
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF8)));
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while creating writer for " + outputFile.getName(), e);
			return false;
		}

		final IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;

		final List<MergeAble> mergeAbles = collectMergeAbles(files, outputFile);

		// The amount of bytes representing a unit of progress, by default 0.1%
		long tickSize;
		if (dataSize > 1000) {
			tickSize = dataSize / 1000;
			internalMonitor.beginTask("Merging log files", 1000);
		} else {
			tickSize = 1;
			internalMonitor.beginTask("Merging log files", (int) dataSize);
		}

		boolean dataProcessed = true;
		long nofProcessedBytes = 0;
		while (dataProcessed && !internalMonitor.isCanceled()) {
			MergeAble earliestMergeable = getEarliestMergeAble(mergeAbles);
			dataProcessed = false;

			if (earliestMergeable != null) {
				String text = appendRecord(writer, earliestMergeable);
				nofProcessedBytes += text.length();

				if (nofProcessedBytes > tickSize) {
					internalMonitor.worked((int) (nofProcessedBytes / tickSize));
					nofProcessedBytes %= tickSize;
				}
				earliestMergeable.next();

				dataProcessed = true;
			}
		}

		IOUtils.closeQuietly(mergeAbles.toArray(new Closeable[0]));

		writer.close();
		internalMonitor.done();

		for (int i = 0; i < mergeAbles.size() && !isErroneous; i++) {
			isErroneous = mergeAbles.get(i).isErroneous();
		}
		return !isErroneous;
	}

	private String appendRecord(PrintWriter writer, MergeAble earliestMergeable) {
		LogRecord record = earliestMergeable.getActualRecord();
		writer.print(record.getTimestamp());
		if (earliestMergeable.getComponentID() != null) {
			writer.print(' ');
			writer.print(earliestMergeable.getComponentID());
		}
		String text = record.getText();
		writer.print(text);
		if (text.charAt(text.length() - 1) != '\n') {
			// If it does not end on a new line, than extend it to do so.
			writer.print('\n');
		}
		return text;
	}

	private List<MergeAble> collectMergeAbles(List<IFile> files, File outputFile) {
		final List<MergeAble> mergeAbles = new ArrayList<MergeAble>(files.size());
		TimestampFormat commonFormat = null;
		// try to open each file, and check if their timestamp format is the same.
		final IPath outputPath = new Path(outputFile.getAbsolutePath());
		for (IFile file : files) {
			if (outputPath.equals(file.getLocation())) {
				continue;
			}
			BufferedReader reader;
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				reader = new BufferedReader(new InputStreamReader(file.getContents(), StandardCharsets.UTF8));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Error while refreshing and opening " + file.getLocationURI() +
						", skipping", e);
				isErroneous = true;
				continue;
			}
			MergeAble mergeAble = new MergeAble(file, reader, true);
			if (mergeAble.hasNext()) {
				if (commonFormat == null) {
					commonFormat = mergeAble.getTimestampFormat();
					mergeAbles.add(mergeAble);
					dataSize += file.getLocation().toFile().length();
				} else if (commonFormat == mergeAble.getTimestampFormat()) {
					mergeAbles.add(mergeAble);
					dataSize += file.getLocation().toFile().length();
				} else {
					ErrorReporter.logError("The format of the timestamp in the file '"
							+ mergeAble.getFile().getLocation().toOSString() + "' is "
							+ mergeAble.getTimestampFormat().getFormatName()
							+ " which does not match the format of the common timestamp "
							+ commonFormat.getFormatName());
					mergeAble.close();
					isErroneous = true;
				}
			} else {
				mergeAble.close();
			}
		}
		return mergeAbles;
	}

	/**
	 * Returns the mergeAble that occured earliest from the provided list of
	 * mergeAbles
	 * 
	 * @param mergeAbles
	 *                the list of mergeAbles to be processed
	 * 
	 * @return the mergeAble with the smallest timestamp in the list, or
	 *         null if no mergeAbles are found to process.
	 */
	private MergeAble getEarliestMergeAble(final List<MergeAble> mergeAbles) {
		String smallestTimestamp = null;
		MergeAble smallestMergeable = null;

		for (MergeAble mergeAble : mergeAbles) {
			if (mergeAble.hasNext()) {
				if (smallestTimestamp == null) {
					smallestTimestamp = mergeAble.getActualRecord().getTimestamp();
					smallestMergeable = mergeAble;
				} else if (smallestTimestamp.compareTo(mergeAble.getActualRecord().getTimestamp()) > 0) {
					smallestTimestamp = mergeAble.getActualRecord().getTimestamp();
					smallestMergeable = mergeAble;
				}
			}
		}
		
		return smallestMergeable;
	}
}
