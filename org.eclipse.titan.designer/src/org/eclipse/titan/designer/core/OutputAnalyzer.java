/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;

/**
 * This class reads in lines from the output, and if one can be identified as a
 * TITAN warning or error report, then a marker is created, and placed on the
 * right place.
 * 
 * @author Kristof Szabados
 */
public final class OutputAnalyzer {
	private Map<String, IFile> files;
	private HashMap<IResource, IDocument> documentMap;
	private List<IPath> filesOpened;
	private IProject project;

	private boolean processedErrorMessages;

	static final Pattern BASE_TITAN_ERROR_PATTERN = Pattern.compile("[ ]*(.+):(.+):[ ]+(error|warning|note):[ ]+(.+)");
	private final Matcher baseTITANErrorMatcher = BASE_TITAN_ERROR_PATTERN.matcher("");

	static final Pattern LOCATION_FORMAT_PATTERN_1 = Pattern.compile("(\\d+)");
	private final Matcher locationFormatMatcher1 = LOCATION_FORMAT_PATTERN_1.matcher("");
	static final Pattern LOCATION_FORMAT_PATTERN_2 = Pattern.compile("(\\d+)\\.(\\d+)\\-(\\d+)\\.(\\d+)");
	private final Matcher locationFormatMatcher2 = LOCATION_FORMAT_PATTERN_2.matcher("");
	static final Pattern LOCATION_FORMAT_PATTERN_3 = Pattern.compile("(\\d+)\\-(\\d+)");
	private final Matcher locationFormatMatcher3 = LOCATION_FORMAT_PATTERN_3.matcher("");
	static final Pattern LOCATION_FORMAT_PATTERN_4 = Pattern.compile("(\\d+)\\.(\\d+)\\-(\\d+)");
	private final Matcher locationFormatMatcher4 = LOCATION_FORMAT_PATTERN_4.matcher("");
	static final Pattern LOCATION_FORMAT_PATTERN_5 = Pattern.compile("(\\d+)\\.(\\d+)");
	private final Matcher locationFormatMatcher5 = LOCATION_FORMAT_PATTERN_5.matcher("");
	static final Pattern LOCATION_FORMAT_PATTERN_6 = Pattern.compile("<unknown>");
	private final Matcher locationFormatMatcher6 = LOCATION_FORMAT_PATTERN_6.matcher("");

	static final Pattern BASE_GCC_ERROR_PATTERN_1 = Pattern.compile("[ ]*(.+):(\\d+):(\\d+):[ ]+(error|warning):[ ]+(.+)");
	private final Matcher baseGCCErrorMatcher1 = BASE_GCC_ERROR_PATTERN_1.matcher("");
	static final Pattern BASE_GCC_ERROR_PATTERN_2 = Pattern.compile("[ ]*(.+):(\\d+):(\\d+):[ ]+(.+)");
	private final Matcher baseGCCErrorMatcher2 = BASE_GCC_ERROR_PATTERN_2.matcher("");

	private static final String ERROR = "error";
	private static final String WARNING = "warning";

	/**
	 * Constructor.
	 * 
	 * @param files
	 *                the map of files, according to which the file names in
	 *                the output will be mapped to resource names
	 * @param project
	 *                if the error can not be mapped to a file, then it will
	 *                be mapped on the project
	 */
	public OutputAnalyzer(final Map<String, IFile> files, final IProject project) {
		this.files = files;
		this.project = project;
		documentMap = new HashMap<IResource, IDocument>();
		filesOpened = new ArrayList<IPath>();
		processedErrorMessages = false;
	}

	/**
	 * @return whether this output analyzer was processing error or warning
	 *         messages or not.
	 * */
	public boolean hasProcessedErrorMessages() {
		return processedErrorMessages;
	}

	/**
	 * Disposes the objects that were buffered to speed up marker creation.
	 * <p>
	 * Please note that the textFileBufferManagers are disconnected from the
	 * file system at this point.
	 * 
	 * @see #addTITANMarker(String, int, int, int, int, String, String,
	 *      String)
	 * */
	public void dispose() {
		documentMap.clear();
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		for (IPath path : filesOpened) {
			try {
				manager.disconnect(path, null);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		filesOpened.clear();
		files = null;
		project = null;
	}

	/**
	 * Tries to identify the input line as a TITAN reported warning or
	 * error.
	 * 
	 * @see TITANJob
	 * 
	 * @param line
	 *                the line to be parsed
	 * 
	 * @return weather the line really was a TITAN reported error or not
	 */
	public boolean parseTitanErrors(final String line) {
		if (baseTITANErrorMatcher.reset(line).matches()) {
			String filename = baseTITANErrorMatcher.group(1);
			String location = baseTITANErrorMatcher.group(2);
			String type = baseTITANErrorMatcher.group(3);
			String message = baseTITANErrorMatcher.group(4);

			if (locationFormatMatcher1.reset(location).matches()) {
				return addTITANMarker(filename, Integer.parseInt(locationFormatMatcher1.group(1)), -1,
						Integer.parseInt(locationFormatMatcher1.group(1)), -1, type, message);
			}

			if (locationFormatMatcher2.reset(location).matches()) {
				return addTITANMarker(filename, Integer.parseInt(locationFormatMatcher2.group(1)),
						Integer.parseInt(locationFormatMatcher2.group(2)),
						Integer.parseInt(locationFormatMatcher2.group(3)),
						Integer.parseInt(locationFormatMatcher2.group(4)), type, message);
			}

			if (locationFormatMatcher3.reset(location).matches()) {
				return addTITANMarker(filename, Integer.parseInt(locationFormatMatcher3.group(1)), 0,
						Integer.parseInt(locationFormatMatcher3.group(2)) + 1, 0, type, message);
			}

			if (locationFormatMatcher4.reset(location).matches()) {
				return addTITANMarker(filename, Integer.parseInt(locationFormatMatcher4.group(1)),
						Integer.parseInt(locationFormatMatcher4.group(2)),
						Integer.parseInt(locationFormatMatcher4.group(1)),
						Integer.parseInt(locationFormatMatcher4.group(3)), type, message);
			}

			if (locationFormatMatcher5.reset(location).matches()) {
				return addTITANMarker(filename, Integer.parseInt(locationFormatMatcher5.group(1)),
						Integer.parseInt(locationFormatMatcher5.group(2)),
						Integer.parseInt(locationFormatMatcher5.group(1)),
						Integer.parseInt(locationFormatMatcher5.group(2)) + 1, type, message);
			}

			if (locationFormatMatcher6.reset(location).matches()) {
				return addTITANMarker(filename, 0, -1, 0, -1, type, message);
			}
		} else if (baseGCCErrorMatcher1.reset(line).matches()) {
			return addTITANMarker(baseGCCErrorMatcher1.group(1), Integer.parseInt(baseGCCErrorMatcher1.group(2)),
					Integer.parseInt(baseGCCErrorMatcher1.group(3)), Integer.parseInt(baseGCCErrorMatcher1.group(2)),
					Integer.parseInt(baseGCCErrorMatcher1.group(3)) + 1, baseGCCErrorMatcher1.group(4),
					baseGCCErrorMatcher1.group(5));
		} else if (baseGCCErrorMatcher2.reset(line).matches()) {
			return addTITANMarker(baseGCCErrorMatcher2.group(1), Integer.parseInt(baseGCCErrorMatcher2.group(2)),
					Integer.parseInt(baseGCCErrorMatcher2.group(3)), Integer.parseInt(baseGCCErrorMatcher2.group(2)),
					Integer.parseInt(baseGCCErrorMatcher2.group(3)) + 1, WARNING, baseGCCErrorMatcher2.group(4));
		}
		return false;
	}

	/**
	 * Places a Marker on the right place.
	 * <p>
	 * how it works:
	 * <ul>
	 * <li>first it searches in the files map to find the right resource to
	 * a file name.
	 * <li>if no such resource can be found, but the project is available,
	 * then the marker will be placed on the project.
	 * <li>tries to put the marker on the right place. With errors coming
	 * from pre-processed files, it can happen that their column number
	 * point to full wrong, or not even existing positions.
	 * 
	 * </ul>
	 * 
	 * Please note that the textFileBufferManagers are created only once for
	 * every file resource, their disconnection is done in the dispose()
	 * method.
	 * 
	 * @see #dispose()
	 * 
	 * @param fileName
	 *                the file name
	 * @param startLineNumber
	 *                the line number of the beginning of the error or
	 *                <code>0</code>
	 * @param startOffset
	 *                the offset of the beginning of the error in line given
	 *                by startLineNumber or <code>-1</code>
	 * @param endLineNumber
	 *                the line number of of the ending of the error or
	 *                equals startLineNumber
	 * @param endOffset
	 *                the offset of the end of the error in line given by
	 *                endLineNumber or <code>-1</code>
	 * @param type
	 *                the type of the message: can be error / warning / note
	 * @param message
	 *                the text of the error.
	 * 
	 * @return whether it was successful or not
	 */
	private boolean addTITANMarker(final String fileName, final int startLineNumber, final int startOffset, final int endLineNumber,
			final int endOffset, final String type, final String message) {
		IResource resource = null;
		if (files.containsKey(fileName)) {
			resource = files.get(fileName);
		} else {
			boolean found = false;
			IPath filePath = new Path(fileName);
			String lastSegment = filePath.lastSegment();
			if (files.containsKey(lastSegment)) {
				resource = files.get(lastSegment);
				found = true;
			}
			if (!found) {
				for (IFile file2 : files.values()) {
					IPath path2 = file2.getProject().getLocation().append(fileName);
					String temp = path2.toOSString();
					if (files.containsKey(temp)) {
						resource = files.get(temp);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				for (IFile file2 : files.values()) {
					IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(file2.getProject()).getWorkingDirectoryPath(
							true);
					if (workingDir != null) {
						IPath path2 = workingDir.append(fileName);
						String temp = path2.toOSString();
						if (files.containsKey(temp)) {
							resource = files.get(temp);
							found = true;
							break;
						}
					}
				}
			}
			if (!found) {
				URI workingDirUri = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryURI(true);
				if (workingDirUri != null) {
					URI uri2 = URIUtil.append(workingDirUri, fileName);
					IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
					IFile[] results = wroot.findFilesForLocationURI(uri2);
					if (results != null && results.length > 0) {
						resource = results[0];
						found = true;
					}
				}
			}
		}

		Location location = null;
		if (resource == null || !resource.isAccessible()) {
			if (project != null && project.isAccessible()) {
				location = new Location(project);
				if (ERROR.equals(type)) {
					location.reportExternalProblem(message.trim(), IMarker.SEVERITY_ERROR, GeneralConstants.COMPILER_ERRORMARKER);
				} else {
					location.reportExternalProblem(message.trim(), IMarker.SEVERITY_WARNING,
							GeneralConstants.COMPILER_WARNINGMARKER);
				}

				processedErrorMessages = true;
				return true;
			}

			return false;
		}

		try {
			if (startLineNumber > 0 && startOffset != -1 && endOffset != -1) {
				IDocument document = null;
				if (documentMap.containsKey(resource)) {
					document = documentMap.get(resource);
				} else {
					ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
					IPath fullPath = resource.getFullPath();
					if (manager != null) {
						manager.connect(fullPath, null);
						ITextFileBuffer buffer = manager.getTextFileBuffer(fullPath);
						document = buffer.getDocument();
						documentMap.put(resource, document);
						filesOpened.add(fullPath);
					}
				}
				try {
					if (document != null && endLineNumber < document.getNumberOfLines()
							&& document.getLineLength(startLineNumber - 1) > startOffset
							&& document.getLineLength(endLineNumber - 1) >= endOffset) {
						location = new Location(resource, startLineNumber, document.getLineOffset(startLineNumber - 1)
								+ startOffset - 1, document.getLineOffset(endLineNumber - 1) + endOffset);
					}
				} catch (BadLocationException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}

			if (location == null) {
				location = new Location(resource, startLineNumber);
			}

			if (ERROR.equals(type)) {
				location.reportExternalProblem(message, IMarker.SEVERITY_ERROR, GeneralConstants.COMPILER_ERRORMARKER);
			} else {
				location.reportExternalProblem(message, IMarker.SEVERITY_WARNING, GeneralConstants.COMPILER_WARNINGMARKER);
			}

			processedErrorMessages = true;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return true;
	}
}
