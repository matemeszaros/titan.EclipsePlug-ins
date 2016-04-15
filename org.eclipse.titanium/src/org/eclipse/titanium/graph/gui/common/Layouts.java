/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.common;

import org.eclipse.titanium.graph.gui.layouts.algorithms.DAGLayoutAlgorithm;
import org.eclipse.titanium.graph.gui.layouts.algorithms.DAGLayoutReverseAlgorithm;
import org.eclipse.titanium.graph.gui.utils.LayoutEntry;

/**
 * This class cannot be instantiated, it only contains a few {@link LayoutEntry}
 * constants <b><font color="red">They all only can be cloned!</font></b>*
 * 
 * @author Gabor Jenei
 */
public final class Layouts {

	/**
	 * layout button for ISOM layout <b><font color="red">It must be used
	 * through {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_ISOM = new LayoutEntry("ISOM", "Self organizing (ISOM)");
	/**
	 * layout button for KK layout <b><font color="red">It must be used through
	 * {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_KK = new LayoutEntry("KK", "Kamada-Kawai algorithm");
	/**
	 * layout button for FR layout <b><font color="red">It must be used through
	 * {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_FR = new LayoutEntry("FR", "Fruchterman-Reingold algorithm");
	/**
	 * layout button for Spring layout <b><font color="red">It must be used
	 * through {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_SPRING = new LayoutEntry("Spring", "Spring force directed");
	/**
	 * layout button for Circle layout <b><font color="red">It must be used
	 * through {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_CIRCLE = new LayoutEntry("Circle", "Logical ring");
	/**
	 * layout button for Titanium DAG layout <b><font color="red">It must be
	 * used through {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_TDAG = 
			new LayoutEntry(DAGLayoutAlgorithm.ALG_ID, "General Directed Graph (shows hierarchy)");
	/**
	 * layout button for Titanium Reverse DAG layout <b><font color="red">It
	 * must be used through {@link LayoutEntry#clone()} method!</font></b>
	 */
	public static final LayoutEntry LAYOUT_RTDAG = 
			new LayoutEntry(DAGLayoutReverseAlgorithm.ALG_ID, "Reverse Directed Graph (shows hierarchy)");
	
	public static final LayoutEntry LAYOUT_STATIC = new LayoutEntry("STATIC", "Static Layout");

	/** Layout string for cluster knots layout */
	public static final String S_LAYOUT_CLUSTER = "Cluster";
	
	/**
	 * Layout code for all metric layouts (not depending which metric was chosen)
	 */
	public static final String METRIC_LAYOUT_CODE = "METRIC";
	

	private Layouts() {
	}
}