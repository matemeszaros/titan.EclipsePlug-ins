/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import java.util.List;

import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Represents the base sequence diagram graph nodes container.<br>
 */
public abstract class BasicFrame {

	/**
	 * The greater event occurrence created 
	 * on graph nodes drawn in this Frame
	 * This directly impact the Frame height
	 */
	private int verticalIndex = 0;

	/**
	 * The index along the x axis where the next lifeline will is drawn
	 * This directly impact the Frame width
	 * 0 reserved for time stamp
	 */
	private int horizontalIndex = 1;

	/**
	 * The current Frame visible area
	 */
	private int visibleAreaX;
	private int visibleAreaY;
	private int visibleAreaWidth;
	private int visibleAreaHeight;

	/**
	 * 
	 * Returns the greater event occurrence 
	 * known by the Frame
	 * @return the greater event occurrence
	 */	
	protected int getMaxEventOccurrence() {
		return this.verticalIndex;
	}
	
	/**
	 * Set the greater event occurrence created
	 * in GraphNodes included in the frame
	 * @param eventOccurrence the new greater event occurrence
	 */
	protected void setMaxEventOccurrence(final int eventOccurrence) {
		this.verticalIndex = eventOccurrence;
	}
	
	/**
	 * This method increase the lifeline place holder
	 * The return value is usually assign to a lifeline. This can be used to set the lifelines drawing order.
	 * Also, calling this method two times and assigning only the last given index to a lifeline will increase
	 * this lifeline draw spacing (2 times the default spacing) from the last added lifeline.
	 * @return a new lifeline index
	 */
	protected int getNewHorizontalIndex() {
		return this.horizontalIndex++;
	}
	
	/**
	 * Returns the current horizontal index
	 * @return the current horizontal index
	 * @see Frame#getNewHorizontalIndex() for horizontal index description
	 */
	protected int getHorizontalIndex() {
		return this.horizontalIndex;
	}

	/**
	 * Returns the x coordinate of the frame
	 * @return the x coordinate
	 */
	public int getX() {
		return MSCConstants.TOP_MARGIN;
	}
	
	/**
	 * Returns the y coordinate of the frame
	 * @return the y coordinate
	 */
	public int getY() {
		return MSCConstants.LEFT_MARGIN;
	}

	/**
	 * Returns the frame's width
	 * @return the frame's width
	 */
	public int getWidth() {
		return this.horizontalIndex * MSCConstants.COLUMN_WIDTH + MSCConstants.LEFT_MARGIN + MSCConstants.RIGHT_MARGIN;
	}
	
	/**
	 * Returns the frame's height
	 * @return the frame's height
	 */
	public int getHeight() {
		return  this.verticalIndex * MSCConstants.ROW_HEIGHT + MSCConstants.TOP_MARGIN + MSCConstants.BOTTOM_MARGIN;
	}

	/**
	 * Returns the graph node which contains the point given in parameter
	 * for the given graph node list and starting the iteration at the given index<br>
	 * WARNING: Only graph nodes with smaller coordinates
	 * than the current visible area can be returned.<br>
	 * 
	 * @param x the x coordinate of the point to test
	 * @param y the y coordinate of the point to test
	 * @param list the list to search in
	 * @param fromIndex list browsing starting point
	 * @return the graph node containing the point given in parameter, null otherwise
	 */ 
	protected MSCNode getNodeFromListAt(final int x, final int y, final List<MSCNode> list, final int fromIndex) {
		if (list == null) {
			return null;
		}
		for (int i = fromIndex; i < list.size(); i++) {
			MSCNode node = list.get(i);
			// Only lifeline list is x ordered
			// Stop browsing the list if the node is outside the visible area
			// All others nodes will not be visible			
			if ((node instanceof Lifeline)
					&& (node.getX() > this.visibleAreaX + this.visibleAreaWidth)
					|| node.getHeight() < 0
					&& node.getY() + node.getHeight() > this.visibleAreaY + this.visibleAreaHeight) {
				break;
			}
			if (node.contains(x, y)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Returns the header graph node which contains the point given in parameter
	 * for the given header graph node list and starting the iteration at the given index<br>
	 * WARNING: Only header graph nodes with smaller coordinates
	 * than the current visible area can be returned.<br>
	 * 
	 * @param x the x coordinate of the point to test
	 * @param y the y coordinate of the point to test
	 * @param list the list to search in
	 * @param fromIndex list browsing starting point
	 * @return the graph node containing the point given in parameter, null otherwise
	 */ 
	protected MSCNode getHeaderNodeFromListAt(final int x, final int y, final List<LifelineHeader> list, final int fromIndex) {
		if (list == null) {
			return null;
		}
		for (int i = fromIndex; i < list.size(); i++) {
			LifelineHeader node = list.get(i);
			if (node.getHeight() < 0
					&& node.getY() + node.getHeight() > this.visibleAreaY + this.visibleAreaHeight) {
				break;
			}
			if (node.contains(x, y)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Return the X coordinate of the frame visible area
	 * @return the X coordinate of the frame visible area
	 */
	public int getVisibleAreaX() {
		return this.visibleAreaX;
	}
	
	/**
	 * Return the frame visible area width
	 * @return the frame visible area width
	 */
	public int getVisibleAreaWidth() {
		return this.visibleAreaWidth;
	}
	
	/**
	 * Return the frame visible area height
	 * @return the frame visible area height
	 */
	public int getVisibleAreaHeight() {
		return this.visibleAreaHeight;
	}
	
	/**
	 * Return the Y coordinate of the frame visible area
	 * @return the Y coordinate of the frame visible area
	 */
	public int getVisibleAreaY() {
		return this.visibleAreaY;
	}

	/**
	 * Sets the metrics of the visible area.
	 *
	 * @param x the X coordinate of the visible area.
	 * @param y the Y coordinate of the visible area.
	 * @param width the width of the visible area.
	 * @param height the height of the visible area.
	 * */
	public void setVisibleArea(final int x, final int y, final int width, final int height) {
		visibleAreaX = x;
		visibleAreaY = y;
		visibleAreaWidth = width;
		visibleAreaHeight = height;
	}
}
