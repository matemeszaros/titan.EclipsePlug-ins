/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.components;

import java.awt.Color;

/**
 * This class describes an edge of the module graph, it stores colour, name and
 * weight
 * 
 * @author Gabor Jenei
 */
public class EdgeDescriptor implements Comparable<EdgeDescriptor>{
	protected Color edgeColour = Color.black;
	protected String edgeName;
	protected Number weight;

	/**
	 * Creates an edge that can be added to the module graph
	 * 
	 * @param name
	 *            : The edge name
	 * @param colour
	 *            : The edge colour
	 * @param weight
	 *            : The edge weight (can be a floating point or integer number)
	 */
	public EdgeDescriptor(final String name, final Color colour, final Number weight) {
		edgeName = name;
		edgeColour = colour;
		this.weight = weight;
	}

	/**
	 * Creates an edge that can be added to the module graph, the weight is
	 * default 1
	 * 
	 * @param name
	 *            : The edge name
	 * @param colour
	 *            : The edge colour
	 */
	public EdgeDescriptor(final String name, final Color colour) {
		this(name, colour, 1);
	}

	/**
	 * Creates an edge that can be added to the module graph, the weight is
	 * default 1, and the colour is black.
	 * 
	 * @param name
	 *            :
	 */
	public EdgeDescriptor(final String name) {
		this(name, Color.black, 1);
	}

	/**
	 * Changes the edge's colour
	 * 
	 * @param col
	 *            : The colour to set
	 */
	public void setColour(final Color col) {
		edgeColour = col;
	}

	/**
	 * @return returns the edge's name
	 */
	public String getName() {
		return edgeName;
	}

	/**
	 * @return returns the currently set edge colour
	 */
	public Color getColor() {
		return edgeColour;
	}

	/**
	 * Sets the edge's weight
	 * 
	 * @param weight
	 *            : The weight to set
	 */
	public void setWeight(final Number weight) {
		this.weight = weight;
	}

	/**
	 * @return returns the edge's weight
	 */
	public Number getWeight() {
		return weight;
	}

	/**
	 * This method is same as {@link EdgeDescriptor#getName()}, it overrides
	 * {@link Object#toString()} method in order to have a better name upon
	 * debug.
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Overrides the {@link Object#equals(Object)} method, it implements a
	 * comparison of two instances.
	 * 
	 * @param obj
	 *            : The other instance to compare with
	 * @return A boolean that indicates whether the two objects represent the
	 *         same module
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EdgeDescriptor)) {
			return false;
		}

		return ((EdgeDescriptor) obj).getName().equals(this.edgeName);
	}

	@Override
	public int hashCode() {
		return edgeName.hashCode();
	}

	@Override
	public int compareTo(final EdgeDescriptor other) {
		return edgeName.compareTo(other.edgeName);
	}

}