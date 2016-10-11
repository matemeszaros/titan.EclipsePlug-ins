/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This version of the module name cluster only shows the non empty clustres as nodes in the cluster graph.
 * 
 * @author Gobor Daniel
 *
 */
public class SparseModuleNameCluster extends FullModuleNameCluster {

	public SparseModuleNameCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph) {
		super(graph);
	}

	@Override
	protected void check(final String name) {
		if (!mapNameCluster.get(name).isEmpty()) {
			super.check(name);
		}
	}
}
