/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.gui.layouts.algorithms.HierarcicalLayoutAlgorithm;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.preferences.PreferenceConstants;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;

/**
 * This is the base class of hierarchical layouts (reverse and normal)
 * 
 * @author Gabor Jenei
 * @param <V>
 *            The node type
 * @param <E>
 *            The edge type
 */
public abstract class BaseHierarchicalLayout<V, E> implements Layout<V, E> {
	private static final Double INNER_NODE_POSITION_RATIO_Y = 0.5;
	private static final Double INNER_NODE_POSITION_RATIO_X = 0.5;
	
	public static final String MAX_DISTANCE_ALGORITHM = "MAX";
	public static final String SUM_DISTANCE_ALGORITHM = "SUM";

	protected Graph<V, E> graph;
	protected HierarcicalLayoutAlgorithm<V> alg;
	protected Map<V, Point2D> places;
	protected Dimension size;
	protected final String distanceAlgorithm;
	protected IMetricEnum chosenMetric = null;
	protected final GUIErrorHandler errorHandler = new GUIErrorHandler();

	protected final Comparator<V> nodeComparator = new Comparator<V>() {
		@Override
		public int compare(V v1, V v2) {
			return Integer.signum(getNeighbours(v2).size()
					- getNeighbours(v1).size());
		}
	};

	/**
	 * Constructor
	 * 
	 * @param g
	 *            : The graph to lay out
	 * @param size
	 *            : the layout's size
	 */
	public BaseHierarchicalLayout(Graph<V, E> g, Dimension size) {
		graph = g;
		places = new HashMap<V, Point2D>();
		this.size = size;
		distanceAlgorithm = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.DAG_DISTANCE);
		initAlg();
		initialize();
	}
	
	public BaseHierarchicalLayout(Graph<V, E> g, Dimension size, IMetricEnum metric){
		chosenMetric = metric;
		graph = g;
		places = new HashMap<V, Point2D>();
		this.size = size;
		distanceAlgorithm = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.DAG_DISTANCE);
		initAlg();
		initialize();
	}
	
	/**
	 * This method implements the initialization of algorithm.
	 * We should initialize the <code>protected</code> {@link #alg}
	 * attribute here in subclasses
	 */
	protected abstract void initAlg();

	/**
	 * @param v
	 *            : The node to to tell position for
	 * @return Returns the position calculated for a given node
	 */
	@Override
	public Point2D transform(V v) {
		Point2D p = places.get(v);
		return p != null ? p : new Point2D.Double();
	}

	/**
	 * @return Returns the currently set graph, that is being laid out
	 */
	@Override
	public Graph<V, E> getGraph() {
		return graph;
	}

	/**
	 * @return Returns the currently set size for the drawing area (where the
	 *         whole graph is shown, it is usually more than the screen size)
	 */
	@Override
	public Dimension getSize() {
		return size;
	}

	/**
	 * This method runs the actual algorithm, here happen the time consuming
	 * calculations.
	 */
	@Override
	public final void initialize() {
		alg.run();
		organizeNodes();
	}

	/**
	 * As there is no locking option in this class this method returns constant
	 * <code>false</code>
	 * 
	 * @param v
	 *            : The node to check
	 */
	@Override
	public boolean isLocked(V v) {
		return false;
	}

	/**
	 * This method is not used, therefore it does not make any change.
	 * 
	 * @param v
	 *            : the node to set
	 * @param lock
	 *            : Should we lock, or unlock it?
	 */
	@Override
	public void lock(V v, boolean lock) {
		// intentionally empty
	}

	/**
	 * This method is not used, therefore it is empty (does not do anything)
	 */
	@Override
	public void reset() {
		// intentionally empty
	}

	/**
	 * This method changes the graph to lay out
	 * 
	 * @param g
	 *            : the graph to set
	 */
	@Override
	public void setGraph(Graph<V, E> g) {
		graph = g;
	}

	/**
	 * It is <b>discouraged</b> to use this method. It changes the transformer
	 * used in the layout. After this change we cannot provide tree like view.
	 * 
	 * @param trf
	 *            : The transformer to use
	 */
	@Override
	public void setInitializer(Transformer<V, Point2D> trf) {
		// intentionally empty
	}

	/**
	 * Set one node's place on the canvas. This position maybe overwritten while
	 * the algorithm is running (see {@link #initialize()})
	 * 
	 * @param v
	 *            : The node to set position for
	 * @param p
	 *            : the claimed position
	 */
	@Override
	public void setLocation(V v, Point2D p) {
		places.put(v, p);
	}

	/**
	 * Set the draw area's (canvas) size. This attribute will determine the
	 * width and height of the area used for drawing the whole graph.
	 * 
	 * @param size
	 *            : the size to use
	 */
	@Override
	public void setSize(Dimension size) {
		this.size = size;
	}

	/**
	 * Makes an X coordinate related ordering of the nodes. Tries to identify
	 * the optimal X position for all nodes according to their distance to the
	 * predecessor nodes.
	 */
	@SuppressWarnings("unchecked")
	protected void organizeNodes() {
		Map<V, Integer> nodeLevels = alg.getLevels();
		int[] nodesPerLevel = alg.getNumberOfNodesPerLevel();
		int noLevels = alg.getNumberOfLevels();
		Set<V> isolateNodes = alg.getIsolateNodes();
		double cellHeight = (double) size.height / noLevels;
		double[] cellWidths = new double[noLevels];
		Set<Integer>[] freePlaces = new HashSet[noLevels];
		int baseLevel = isolateNodes.isEmpty() ? 0 : 1;
		Queue<V>[] levels = new PriorityQueue[noLevels];

		for (int i = 0; i < noLevels; ++i) {
			levels[i] = new PriorityQueue<V>(nodesPerLevel[i], nodeComparator);
		}

		// build an array that contains the nodes ordered separated by the
		// levels
		for (Map.Entry<V, Integer> entry : nodeLevels.entrySet()) {
			levels[entry.getValue()].add(entry.getKey());
		}

		// set all cells free inside the rows
		for (int i = 0; i < noLevels; i++) {
			cellWidths[i] = (double) size.width / nodesPerLevel[i];
			freePlaces[i] = new HashSet<Integer>();
			for (int actCell = 0; actCell < nodesPerLevel[i]; ++actCell) {
				freePlaces[i].add(actCell);
			}
		}

		// place first isolate nodes (if there is any)
		int noPlacedElems = 0;
		for (V v : isolateNodes) {
			double actHeight = cellHeight * INNER_NODE_POSITION_RATIO_Y;
			double actXPos = cellWidths[0]
					* ((noPlacedElems++) + INNER_NODE_POSITION_RATIO_X);
			Point2D p = new Point2D.Double(actXPos, actHeight);
			places.put(v, p);
		}

		if (baseLevel >= noLevels && noPlacedElems != 0) {
			return;
		}
		
		// place the initial first row's nodes
		noPlacedElems = 0;
		for (V v : levels[baseLevel]) {
			double actHeight = cellHeight
					* (baseLevel + INNER_NODE_POSITION_RATIO_Y);
			double actXPos = cellWidths[baseLevel]
					* ((noPlacedElems++) + INNER_NODE_POSITION_RATIO_X);
			places.put(v, new Point2D.Double(actXPos, actHeight));
		}

		boolean badDistance = false;
		if (!distanceAlgorithm.equals(MAX_DISTANCE_ALGORITHM)
				&& !distanceAlgorithm.equals(SUM_DISTANCE_ALGORITHM)) {
			errorHandler
					.reportBadSetting("Distance algorithm error",
							"Not existing distance algorithm is set, for details see the preference page"
									+ "\n (DistanceAlgorithm="
									+ distanceAlgorithm + ")",
							"Open preference page",
							"org.eclipse.titanium.preferences.pages.GraphPreferencePage");
			badDistance = true;
		}

		// set optimal place for inner rows
		for (int actLevel = baseLevel + 1; actLevel < noLevels; ++actLevel) {
			double actHeight = cellHeight
					* (actLevel + INNER_NODE_POSITION_RATIO_Y);
			noPlacedElems = 0;
			for (V v : levels[actLevel]) {
				if (!badDistance) {
					places.put(
							v,
							new Point2D.Double(
									getBestXPosition(v, freePlaces[actLevel],
											cellWidths[actLevel]), actHeight));
				} else {
					places.put(
							v,
							new Point2D.Double(
									cellWidths[actLevel]
											* ((noPlacedElems++) + INNER_NODE_POSITION_RATIO_X),
									actHeight));
				}
			}
		}
	}

	/**
	 * Calculates the optimal X position for a given node
	 * 
	 * @param v
	 *            : The node to calculate the optimal position for
	 * @param freePlaces
	 *            : A <code>Set</code> containing the free places inside the
	 *            node's row
	 * @param cellWidth
	 *            : The calculated cellWidth inside the node's row
	 * @return The optimal X coordinate
	 * @throws Exception
	 */
	protected double getBestXPosition(V v, Set<Integer> freePlaces, double cellWidth){
		double position = 0;

		double actDistance = 0;
		double minDistance = Double.POSITIVE_INFINITY;
		Integer givenCell = Integer.MAX_VALUE;
		Collection<V> neighbours = getNeighbours(v);

		for (int actCell : freePlaces) {
			double actPos = cellWidth * (actCell + INNER_NODE_POSITION_RATIO_X);
			if (distanceAlgorithm.equals(SUM_DISTANCE_ALGORITHM)) {
				actDistance = getSumDistance(actPos, neighbours);
			} else if (distanceAlgorithm.equals(MAX_DISTANCE_ALGORITHM)) {
				actDistance = getMaxDistance(actPos, neighbours);
			}

			if (minDistance > actDistance) {
				minDistance = actDistance;
				givenCell = actCell;
				position = actPos;
			}
		}

		freePlaces.remove(givenCell);
		return position;
	}
	
	/**
	 * This method is used to return the nodes belonging to the next level
	 * of DAG layout, these can be the predecessors or oppositely the
	 * successors depending on the used algorithm
	 * 
	 * @param v : The source node
	 * @return The nodes that should be placed on the next level compared to v
	 */
	protected abstract Collection<V> getNeighbours(V v);

	/**
	 * Implementation of sum distance. This distance is defined as the sum of
	 * individual distances to the predecessor nodes
	 * 
	 * @param pos
	 *            : The suspected position of the node
	 * @param neighbours
	 *            : The list of used neighbour nodes (predecessors, or
	 *            successors)
	 * @return The distance
	 */
	protected double getSumDistance(double pos, Collection<V> neighbours) {
		double distance = 0;

		for (V node : neighbours) {
			Point2D pn = places.get(node);
			if (pn == null) {
				continue;
			}
			distance += Math.abs(pn.getX() - pos);
		}
		return distance;
	}

	/**
	 * Implementation of max distance This distance is defined as the maximum of
	 * the distances to the predecessor nodes
	 * 
	 * @param pos
	 *            : The suspected position of the node
	 * @param neighbours
	 *            : The list of used neighbour nodes (predecessors, or
	 *            successors)
	 * @return The distance
	 */
	protected double getMaxDistance(double pos, Collection<V> neighbours) {
		double distance = Double.NEGATIVE_INFINITY;

		for (V v : neighbours) {
			Point2D pv = places.get(v);
			if (pv == null) {
				continue;
			}
			double actDist = Math.abs(pv.getX() - pos);
			if (actDist > distance) {
				distance = actDist;
			}
		}
		return distance;
	}

}