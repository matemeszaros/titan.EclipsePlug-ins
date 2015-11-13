/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters;

import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * Abstract base class of code smell spotters working on some part the AST.
 * <p>
 * They have a specific type of AST node, on which they can 'work', i.e. process
 * the node for a given code smell. If the spotter is registered in the
 * {@link Analyzer}, the <code>Analyzer</code> will call the
 * {@link #checkNode(IVisitableNode)} method every time it visits a node of this
 * type.
 * 
 * @author poroszd
 * 
 */
public abstract class BaseModuleCodeSmellSpotter extends BaseCodeSmellSpotter {
	public BaseModuleCodeSmellSpotter(CodeSmellType type) {
		super(type);
	}

	/**
	 * Processing the node.
	 * <p>
	 * The spotter checks the node, searching for the kind of problem that it
	 * can spot.
	 * 
	 * @param node
	 *            the node to process.
	 * @return the list of found problems
	 */
	public final List<Marker> checkNode(IVisitableNode node) {
		Problems problems = new Problems();
		process(node, problems);
		return problems.getMarkers();
	}

	/**
	 * Internal processing the node.
	 * <p>
	 * The actual work for matching the code smell is done here. If the spotter
	 * was registered for analysis, this method will be called when the AST
	 * traversal hits a node of type {@link #getStartNode()}. When the smell
	 * matches the node, <code>problems</code> should be notified about the new
	 * problem. In this case {@link Problems#report(Location, String)} should be
	 * called (usually with the location of the processed node), or in the
	 * absence of such location
	 * {@link Problems#report(org.eclipse.core.resources.IResource, String)}
	 * should be called with the analyzed module's associated resource.
	 * 
	 * @param node
	 *            the node to process
	 * @param problems
	 *            the handler class where problems should be reported
	 */
	protected abstract void process(IVisitableNode node, Problems problems);

	/**
	 * @return The type of node on which the spotter will work.
	 */
	public abstract List<Class<? extends IVisitableNode>> getStartNode();
}
