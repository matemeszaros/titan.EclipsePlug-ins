/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;

/**
 * Special class to support the parsers in putting out their markers on the resources.
 * 
 * @author Kristof Szabados
 */
public class ParserMarkerSupport {
	
	// disabled constructor
	private ParserMarkerSupport() {
		//Do nothing
	}

	/**
	 * Creates and places a task marker on the provided location.
	 * 
	 * @param file
	 *                the file to put the task marker on
	 * @param taskMarker
	 *                the data of the marker needed for this operation
	 * */
	public static void createTaskMarker(final IFile file, final TITANMarker taskMarker) {
		if (!file.isAccessible()) {
			return;
		}

		Location location = new Location(file, taskMarker.getLine() - 1);
		location.reportExternalProblem(taskMarker.getMessage(), taskMarker.getSeverity(), taskMarker.getPriority(),
				GeneralConstants.ONTHEFLY_TASK_MARKER);
	}

	/**
	 * Creates and places a warning marker on the provided location.
	 * 
	 * @param file
	 *                the file to put the task marker on
	 * @param warningkMarker
	 *                the data of the marker needed for this operation
	 * */
	public static void createWarningMarker(final IFile file, final TITANMarker warningMarker) {
		if (!file.isAccessible()) {
			return;
		}

		Location location = new Location(file, warningMarker.getLine());
		location.reportSyntacticWarning(warningMarker.getMessage());
	}

	/**
	 * Deletes every supported marker from the provided resource.
	 * 
	 * @param resource
	 *                the resource whose markers should be removed
	 * @return the list of all compiler markers on the resource.
	 * */
	public static IMarker[] getAllCompilerMarkers(final IResource resource) {
		try {
			IMarker[] errors = resource.findMarkers(GeneralConstants.COMPILER_ERRORMARKER, false, IResource.DEPTH_INFINITE);
			IMarker[] warnings = resource.findMarkers(GeneralConstants.COMPILER_WARNINGMARKER, false, IResource.DEPTH_INFINITE);
			IMarker[] infos = resource.findMarkers(GeneralConstants.COMPILER_INFOMARKER, false, IResource.DEPTH_INFINITE);

			IMarker[] result = new IMarker[errors.length + warnings.length + infos.length];
			System.arraycopy(errors, 0, result, 0, errors.length);
			System.arraycopy(warnings, 0, result, errors.length, warnings.length);
			System.arraycopy(infos, 0, result, errors.length + warnings.length, infos.length);

			return result;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return new IMarker[] {};
	}

	public static void removeAllCompilerMarkers(final IResource resource) {
		try {
			resource.deleteMarkers(GeneralConstants.COMPILER_ERRORMARKER, false, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(GeneralConstants.COMPILER_WARNINGMARKER, false, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(GeneralConstants.COMPILER_INFOMARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	public static void removeAllOnTheFlyMarkers(final IResource resource) {
		try {
			resource.deleteMarkers(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, false, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, false, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(GeneralConstants.ONTHEFLY_TASK_MARKER, false, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(GeneralConstants.ONTHEFLY_MIXED_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
