/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;

/**
 * Special class to support the parsers in putting out their markers on the resources.
 * ANTLR 4 specific
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class ParserMarkerSupport_V4 {
	
	// disabled constructor
	private ParserMarkerSupport_V4() {
		//Do nothing
	}

	public static void createOnTheFlySyntacticMarker(final IFile file, final SyntacticErrorStorage errorStorage, final int severity,
			final Location relativeLocation) {
		if (!file.isAccessible()) {
			return;
		}

		int lineNumber = errorStorage.lineNumber;
		int charStart = errorStorage.charStart;
		int charEnd = errorStorage.charEnd;
		String message = errorStorage.message;

		Location location = new Location(file, relativeLocation.getLine() + lineNumber - 1, relativeLocation.getOffset() + charStart,
				relativeLocation.getOffset() + charEnd);
		location.reportSingularExternalProblem(message, severity, GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
	}

	public static void createOnTheFlyMixedMarker(final IFile file, final SyntacticErrorStorage errorStorage, final int severity,
			final Location relativeLocation) {
		if (!file.isAccessible()) {
			return;
		}

		int lineNumber = errorStorage.lineNumber;
		int charStart = errorStorage.charStart;
		int charEnd = errorStorage.charEnd;
		String message = errorStorage.message;

		Location location = new Location(file, relativeLocation.getLine() + lineNumber - 1, relativeLocation.getOffset() + charStart,
				relativeLocation.getOffset() + charEnd);
		location.reportSingularExternalProblem(message, severity, GeneralConstants.ONTHEFLY_MIXED_MARKER);
	}

	
	public static void createOnTheFlySyntacticMarker(final IFile file, final SyntacticErrorStorage errorStorage, final int severity) {
		if (!file.isAccessible()) {
			return;
		}
		int lineNumber = errorStorage.lineNumber;
		int charStart = errorStorage.charStart;
		int charEnd = errorStorage.charEnd;
		String message = errorStorage.message;
		boolean justDoIt = errorStorage.exceptionType == SyntacticErrorStorage.ExceptionType.LexerNoViableAltException;

		try {
			if (justDoIt && lineNumber >= 0) {
				IDocument document = null;

				ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
				IPath fullPath = file.getFullPath();
				if (manager != null) {
					manager.connect(fullPath, LocationKind.IFILE, null);
					ITextFileBuffer buffer = manager.getTextFileBuffer(fullPath, LocationKind.IFILE );
					document = buffer.getDocument();
				}

				try {
					if (document != null && lineNumber > 0 && lineNumber <= document.getNumberOfLines()) {
						charStart = document.getLineOffset(lineNumber-1);
						charEnd = document.getLineOffset(lineNumber-1);
						charStart += errorStorage.charStart;
						charEnd += errorStorage.charEnd;
					}
				} catch (BadLocationException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		
			Location location = new Location(file, lineNumber, charStart, charEnd);
			location.reportSingularExternalProblem(message, severity, GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Places an on-the-fly error marker on the provided file. If the file
	 * is being edited in an editor the marker is reported and must be
	 * placed on the document edited, not on the file in the file system. In
	 * this form it is possible to provide a location as a region that was
	 * originally housing the erroneous location. This is used as syntactic marker
	 * but placed during semantic check
	 * 
	 * @param file
	 *                the file which seems to be erroneous
	 * @param errorStorage
	 *                the info extracted from the caught exception(s)
	 * @param severity
	 *                the severity level of the error
	 * 
	 * */
	public static void createOnTheFlyMixedMarker(final IFile file, final SyntacticErrorStorage errorStorage, final int severity) {
		if (!file.isAccessible()) {
			return;
		}
		int lineNumber = errorStorage.lineNumber;
		int charStart = errorStorage.charStart;
		int charEnd = errorStorage.charEnd;
		String message = errorStorage.message;
		boolean justDoIt = errorStorage.exceptionType == SyntacticErrorStorage.ExceptionType.LexerNoViableAltException;

		try {
			if (justDoIt && lineNumber >= 0) {
				IDocument document = null;

				ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
				IPath fullPath = file.getFullPath();
				if (manager != null) {
					manager.connect(fullPath, LocationKind.IFILE, null);
					ITextFileBuffer buffer = manager.getTextFileBuffer(fullPath, LocationKind.IFILE );
					document = buffer.getDocument();
				}

				try {
					if (document != null && lineNumber > 0 && lineNumber <= document.getNumberOfLines()) {
						charStart = document.getLineOffset(lineNumber-1);
						charEnd = document.getLineOffset(lineNumber-1);
						charStart += errorStorage.charStart;
						charEnd += errorStorage.charEnd;
					}
				} catch (BadLocationException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		
			Location location = new Location(file, lineNumber, charStart, charEnd);
			location.reportSingularExternalProblem(message, severity, GeneralConstants.ONTHEFLY_MIXED_MARKER);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
