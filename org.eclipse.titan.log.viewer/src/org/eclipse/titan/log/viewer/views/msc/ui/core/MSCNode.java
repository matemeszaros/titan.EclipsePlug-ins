/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import java.awt.*;
import java.util.Comparator;

import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;

/**
 * The base class used for all MSC nodes displayed in the Sequence Diagram.
 * 
 */
public abstract class MSCNode {

	protected enum Type {
		LIFE_LINE (0),
		COMPONENT_CREATION(1),
		COMPONENT_TERMINATION(2),
		ENQUEUED(3),
		FUNCTION(4),
		FUNCTION_DONE(5),
		PORT_CONNECTION(6),
		PORT_DISCONNECTED(7),
		PORT_MAPPING(8),
		PORT_UNMAPPING(9),
		SETVERDICT_COMPONENT(10),
		SETVERDICT_UNKNOWN(11),
		SIGNAL(12),
		SILENT_EVENT(13),
		TESTCASE_END(14),
		TESTCASE_START(15),
		TIMESTAMP(16),
		LIFE_LINE_HEADER(17);

		private final Integer zOrder;

		Type(final int order) {
			zOrder = order;
		}

		public Integer getZOrder() {
			return zOrder;
		}

		public static final Comparator<Type> Z_ORDER_COMPARATOR = new Comparator<Type>() {

			@Override
			public int compare(final Type o1, final Type o2) {
				return o1.zOrder - o2.zOrder;
			}
		};
	}

	private String name = ""; //$NON-NLS-1$

	protected int eventOccurrence;

	protected MSCNode(int eventOccurrence) {
		this.eventOccurrence = eventOccurrence;
	}

	/**
	 * Set the graph node name.<br>
	 * It is the name display in the view to label the graph node.
	 * @param nodeName the name to set
	 */
	public void setName(final String nodeName) {
		this.name = nodeName;
	}

	/**
	 * Returns the graph node name.<br>
	 * It is the name display in the view to label the graph node.
	 * @return the graph node name
	 */
	public String getName()	{
		return this.name;
	}
	
	/**
	 * Returns true if the graph node contains the point given in parameter, return false otherwise.
	 * @param	x  the x coordinate of the point to test containment <br>
	 * 			y  the y coordinate of the point to test containment
	 * @return true if contained, false otherwise
	 */
	public boolean contains(int x, int y) {
		return new Rectangle(getX(), getY(), getWidth(), getHeight()).contains(x, y);
	}
	
	/**
	 * Returns the x coordinate of the graph node
	 * @return the x coordinate
	 */
	public abstract int getX();
	
	/**
	 * Returns the y coordinate of the graph node
	 * @return the y coordinate
	 */
	public abstract int getY();
	
	/**
	 * Returns the graph node height
	 * @return the graph node height
	 */
	public abstract int getHeight();
	
	/**
	 * Returns the graph node width
	 * @return the graph node width
	 */
	public abstract int getWidth();
	
	/**
	 * Draws the graph node in the given context
	 * @param context the graphical context to draw in
	 */
	protected abstract void draw(IGC context);
	
	/**
	 * Returns the GraphNode visibility for the given visible area.
	 * Wrong visibility calculation, may strongly impact drawing performance
	 * @param vx
	 * @param vy
	 * @param vwidth
	 * @param vheight
	 * @return true if visible false otherwise
	 */
	boolean isVisible(int x, int y, int width, int height) {
		return isVisibleRectange(this, x, y, width, height);
	}
	
	/**
	 * Return a comparator to sort the GraphNode of the same type
	 * This comparator is used to order the GraphNode array of the given node type.
	 * (see getArrayId).
	 * @return the comparator
	 */
	public Comparator<MSCNode> getComparator() {
		return null;
	}
	
	/**
	 * If needed, return a different comparator to backward scan the GraphNode array
	 * @return the backward comparator or null if not needed
	 */
	public Comparator<MSCNode> getBackComparator() {
		return null;
	}

	/**
	 * Returns the type of the node
	 * */
	public abstract Type getType();
	
	/**
	 * Return true if the distance from the GraphNode to the given point is positive
	 * @param x the point x coordinate
	 * @param y the point y coordinate
	 * @return true if positive false otherwise
	 */
	public boolean positiveDistanceToPoint(final int x, final int y) {
		return false;
	}

	/**
	 * Returns the start event occurrence attached to this graphNode.
	 * @return the start event occurrence attached to the graphNode
	 */
	public int getStartOccurrence() {
		return eventOccurrence;
	}
	
	/**
	 * Returns the end event occurrence attached to this graphNode
	 * @return the start event occurrence attached to the graphNode
	 */
	public int getEndOccurrence() {
		return eventOccurrence;
	}

	
	/**
	 * Determine if the given point (px,py) is contained in the rectangle
	 * (x,y,width,height)
	 * @param x the rectangle x coordinate
	 * @param y the rectangle y coordinate
	 * @param width the rectangle width
	 * @param height the rectangle height
	 * @param px the x coordinate of the point to test 
	 * @param py the y coordinate of the point to test 
	 * @return true if contained false otherwise
	 */
	public static boolean contains(final int x, final int y, final int width, final int height, final int px, final int py) {
		int locX = x;
		int locY = y;
		int locWidth = width;
		int locHeight = height;
		
		if (width < 0) {
			locX = locX + width;
			locWidth = -locWidth;
		}
		
		if (height < 0) {
			locY = locY + height;
			locHeight = -locHeight;
		}
		return (px >= locX) && (py >= locY) && ((px - locX) <= locWidth) && ((py - locY) <= locHeight);
	}

	protected static boolean isVisibleRectange(MSCNode node, int vx, int vy, int vwidth, int vheight) {
		int x = node.getX();
		int y = node.getY();
		int width = node.getWidth();
		int height = node.getHeight();
		if (((x + width) < vx) || // To the left
				(x > (vx + vwidth)) || // To the right
				((y + height) < vy) || // Above
				(y > (vy + vheight))) { // Below
			return false;
		}
		return true;
	}
}
