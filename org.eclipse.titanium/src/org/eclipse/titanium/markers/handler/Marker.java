/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.handler;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * An abstraction over code smell markers
 * <p>
 * <code>Marker</code> is an immutable value class, encapsulating any
 * information that is required to create and show an {@link IMarker} with a
 * code smell problem.
 * 
 * @author poroszd
 * 
 */
public class Marker {
	/** The location where the marker indicates the problem */
	private final Location loc;
	/** The message to show */
	private final String message;
	/** The severity of marked problem */
	private final int severity;
	/** The code smell type */
	private final CodeSmellType problemType;

	/**
	 * Creates a marker on a given exact location.
	 * 
	 * @param loc
	 *            might not be null, but can be {@link NULL_Location}
	 * @param message
	 *            the warning to show in Eclipse
	 * @param severity
	 *            one of {@link IMarker#SEVERITY_INFO},
	 *            {@link IMarker#SEVERITY_WARNING} or
	 *            {@link IMarker#SEVERITY_ERROR}
	 * @param problemType
	 *            the type of the code smell
	 */
	public Marker(final Location loc, final String message, final int severity, final CodeSmellType problemType) {
		this.loc = loc;
		this.message = message;
		this.severity = severity;
		this.problemType = problemType;
	}

	/**
	 * Creates a marker without exact location.
	 * 
	 * @param res
	 *            the associated {@link IResource}
	 * @param message
	 *            the warning to show in Eclipse
	 * @param severity
	 *            one of {@link IMarker#SEVERITY_INFO},
	 *            {@link IMarker#SEVERITY_WARNING} or
	 *            {@link IMarker#SEVERITY_ERROR}
	 * @param problemType
	 *            the type of the code smell
	 */
	public Marker(final IResource res, final String message, final int severity, final CodeSmellType problemType) {
		this.loc = new Location(res);
		this.message = message;
		this.severity = severity;
		this.problemType = problemType;
	}

	public IResource getResource() {
		return loc.getFile();
	}

	public int getLine() {
		return loc.getLine();
	}

	public int getCharBegin() {
		return loc.getOffset();
	}

	public int getCharEnd() {
		return loc.getEndOffset();
	}

	public String getMessage() {
		return message;
	}

	public CodeSmellType getProblemType() {
		return problemType;
	}

	public int getSeverity() {
		return severity;
	}

	/**
	 * Show the actual marker in eclipse.
	 * <p>
	 * Note that creating an {@link IMarker} on an {@link IResource} requires
	 * obtaining the markerRule lock on that resource. Acquiring that is the
	 * responsibility of the caller.
	 */
	public void show() {
		loc.reportExternalProblem(message, severity, IMarker.PRIORITY_LOW, CodeSmellType.MARKER_ID);
		// TODO: interface for setting more attribute of the marker, like in
		// marker.setAttribute(CodeSmellType.PROBLEM, problemType);
	}
}
