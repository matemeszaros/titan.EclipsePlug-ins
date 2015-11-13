/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titanium.metrics.MetricData;

/**
 * The common interface of the nodes in the tree, displayed in the
 * {@link MetricsView}'s TreeViewer.
 * 
 * @author poroszd
 * 
 */
interface INode {
	/**
	 * Get the text of a column.
	 * 
	 * @param data
	 *            object storing the metrics data
	 * @param i
	 *            which column's text to get
	 * @return text of that column.
	 */
	String getColumnText(MetricData data, int i);

	/**
	 * Get the children nodes.
	 * 
	 * @param data
	 *            object storing the metrics data
	 * @return the children
	 */
	Object[] getChildren(MetricData data);

	/**
	 * Is this node a leaf of the tree or not
	 * 
	 * @param data
	 *            object storing the metrics data
	 * @return <code>false</code> if leaf node.
	 */
	boolean hasChildren(MetricData data);

}
