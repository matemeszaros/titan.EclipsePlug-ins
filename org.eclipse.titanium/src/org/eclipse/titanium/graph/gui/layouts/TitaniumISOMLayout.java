/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.common.logging.ErrorReporter;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.RadiusGraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;

/**
 * The ISOM layout of Jung optimized for performance.
 * 
 * @author Kristof Szabados
 * */
public class TitaniumISOMLayout<V, E> extends AbstractLayout<V, E> implements IterativeContext {

	private static class ISOMVertexData {
		private int distance;
		private boolean visited;

		protected ISOMVertexData() {
			distance = 0;
			visited = false;
		}
	}


	private Map<V, ISOMVertexData> isomVertexData = new HashMap<V, TitaniumISOMLayout.ISOMVertexData>();

	private int maxEpoch = 2000;
	private AtomicInteger epoch = new AtomicInteger(1);

	private int radiusConstantTime;
	private int radius;
	private int minRadius;

	private double adaption;
	private double initialAdaption;
	private double minAdaption;

	protected GraphElementAccessor<V, E> elementAccessor = new RadiusGraphElementAccessor<V, E>();

	private double coolingFactor;

	private List<V> queue = new ArrayList<V>();

	/**
	 * Creates an <code>ISOMLayout</code> instance for the specified graph
	 * <code>g</code>.
	 * 
	 * @param g
	 */
	public TitaniumISOMLayout(final Graph<V, E> g) {
		super(g);
		queue = new ArrayList<V>(g.getVertexCount());
	}

	/**
	 * Sets the maximum number of iterations.
	 */
	public void setMaxIterations(final int maxIterations) {
		maxEpoch = maxIterations;
	}

	@Override
	public void initialize() {

		setInitializer(new RandomLocationTransformer<V>(getSize()));
		epoch.set(1);

		radiusConstantTime = 100;
		radius = 5;
		minRadius = 1;

		initialAdaption = 90.0D / 100.0D;
		adaption = initialAdaption;
		minAdaption = 0;

		coolingFactor = 2;
	}

	/**
	 * Advances the current positions of the graph elements.
	 */
	@Override
	public synchronized void step() {
		if (epoch.get() < maxEpoch) {
			adjust();
			updateParameters();
		}
	}

	private synchronized void adjust() {
		// Generate random position in graph space
		final Point2D tempXYD = new Point2D.Double();

		// creates a new XY data location
		tempXYD.setLocation(10 + Math.random() * getSize().getWidth(), 10 + Math.random() * getSize().getHeight());

		// Get closest vertex to random position
		final V winner = elementAccessor.getVertex(this, tempXYD.getX(), tempXYD.getY());

		try {
			for (final V v : getGraph().getVertices()) {
				final ISOMVertexData ivd = getISOMVertexData(v);
				ivd.distance = 0;
				ivd.visited = false;
			}
		} catch (ConcurrentModificationException cme) {
			ErrorReporter.logExceptionStackTrace("Error while adjusting vertex data", cme);
		}
		adjustVertex(winner, tempXYD);
	}

	private synchronized void updateParameters() {
		epoch.incrementAndGet();
		final double factor = Math.exp(-1 * coolingFactor * (1.0 * epoch.get() / maxEpoch));
		adaption = Math.max(minAdaption, factor * initialAdaption);
		if ((radius > minRadius) && (epoch.get() % radiusConstantTime == 0)) {
			radius--;
		}
	}

	private synchronized void adjustVertex(final V v, final Point2D tempXYD) {
		queue.clear();
		final ISOMVertexData ivd = getISOMVertexData(v);
		ivd.distance = 0;
		ivd.visited = true;
		queue.add(v);
		V current;

		while (!queue.isEmpty()) {
			current = queue.remove(queue.size() - 1);
			final ISOMVertexData currData = getISOMVertexData(current);
			final Point2D currXYData = transform(current);

			final double dx = tempXYD.getX() - currXYData.getX();
			final double dy = tempXYD.getY() - currXYData.getY();
			final double factor = adaption / Math.pow(2, currData.distance);

			currXYData.setLocation(currXYData.getX() + (factor * dx), currXYData.getY() + (factor * dy));

			if (currData.distance < radius) {
				final Collection<V> s = getGraph().getNeighbors(current);
				try {
					for (final V child : s) {
						final ISOMVertexData childData = getISOMVertexData(child);
						if (childData != null && !childData.visited) {
							childData.visited = true;
							childData.distance = currData.distance + 1;
							queue.add(child);
						}
					}
				} catch (ConcurrentModificationException cme) {
					ErrorReporter.logExceptionStackTrace("Error while adjusting vertex data", cme);
				}
			}
		}
	}

	protected ISOMVertexData getISOMVertexData(final V v) {
		ISOMVertexData temp = isomVertexData.get(v);
		if (temp == null) {
			temp = new ISOMVertexData();
			isomVertexData.put(v, temp);
		}

		return temp;
	}

	/**
	 * This one is an incremental visualization.
	 * 
	 * @return <code>true</code> is the layout algorithm is incremental,
	 *         <code>false</code> otherwise
	 */
	public boolean isIncremental() {
		return true;
	}

	/**
	 * Returns <code>true</code> if the vertex positions are no longer being
	 * updated. Currently <code>ISOMLayout</code> stops updating vertex
	 * positions after a certain number of iterations have taken place.
	 * 
	 * @return <code>true</code> if the vertex position updates have stopped,
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean done() {
		return epoch.get() >= maxEpoch;
	}

	/**
	 * Resets the layout iteration count to 0, which allows the layout algorithm
	 * to continue updating vertex positions.
	 */
	@Override
	public void reset() {
		epoch.set(0);
	}
}
