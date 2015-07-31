/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * Abstract base class of code smell spotters working on whole projects.
 * <p>
 * A code smell should be derived from this class when it is necessary to see
 * the whole project to spot a kind of code smell.
 * 
 * @author poroszd
 * 
 */
public abstract class BaseProjectCodeSmellSpotter extends BaseCodeSmellSpotter {
	public BaseProjectCodeSmellSpotter(CodeSmellType type) {
		super(type);
	}

	/**
	 * Processing the node.
	 * <p>
	 * This method is called by the {@link Analyzer} during analysis.
	 * 
	 * @param project
	 *            the project to check with your code smell
	 */
	public final List<Marker> checkProject(IProject project) {
		Problems problems = new Problems();
		process(project, problems);
		return problems.getMarkers();
	}

	/**
	 * Internal processing the node.
	 * <p>
	 * The actual work for matching the code smell is done here. If the spotter
	 * was registered for analysis, this method will be called both during the
	 * analysis of modules and whole projects.
	 * <p>
	 * When the smell matches the node, <code>problems</code> should be notified
	 * about the new problem. In this case
	 * {@link Problems#report(Location, String)} should be called (usually with
	 * the location of the processed node), or in the absence of such location
	 * {@link Problems#report(org.eclipse.core.resources.IResource, String)}
	 * should be called with the analyzed module's associated resource.
	 * 
	 * @param project
	 *            the project to process
	 * @param problems
	 *            the handler class where problems should be reported
	 */
	protected abstract void process(IProject project, Problems problems);
}
