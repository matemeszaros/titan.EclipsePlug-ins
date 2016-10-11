/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering.visualization;

import org.eclipse.titanium.graph.components.EdgeDescriptor;

/**
 * This class represents a set of edges that connect two clusters. It is the
 * subclass of {@link EdgeDescriptor}
 * 
 * @author Gobor Daniel
 */
public class ClusterEdge extends EdgeDescriptor {
	/**
	 * Constructor
	 * 
	 * @param name
	 *            : The name to set (in order to make difference among distinct
	 *            edges)
	 */
	public ClusterEdge(final String name) {
		super(name);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            : The name to set (in order to make difference among distinct
	 *            edges)
	 * @param edgenum
	 *            : The number of edges among the clusters
	 */
	public ClusterEdge(final String name, final Integer edgenum) {
		this(name);
		weight = edgenum;
	}

	@Override
	public Integer getWeight() {
		return (Integer) weight;
	}
}
