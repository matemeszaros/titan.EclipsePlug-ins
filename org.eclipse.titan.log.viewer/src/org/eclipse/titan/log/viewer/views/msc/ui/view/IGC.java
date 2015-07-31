/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.view;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * IGC
 *
 */
public interface IGC {
	
	/**
	 * Set the current line style
	 * @param style the new line style
	 */
	void setLineStyle(int style);
	
	/**
	 * Returns current the line style used in the graphical context
	 * @return the current line style
	 */
	int getLineStyle();
	
	/**
	 * Returns the contents x coordinate that is at the upper left corner of the view
	 * @return the contents x coordinate
	 */
	int getContentsX();
	
	/**
	 * Returns the contents y coordinate that is at the upper left corner of the view
	 * @return the contents y coordinate
	 */
	int getContentsY();
	
	/**
	 * Returns the contents visible width
	 * @return the contents width
	 */
	int getVisibleWidth();
	
	/**
	 * Returns the contents visible height
	 * @return the contents height
	 */
	int getVisibleHeight();
	
	/**
	 * Draws a line, using the foreground color, between the points (x1, y1) and (x2, y2). 
	 * @param x1 the first point's x coordinate
	 * @param y1 the first point's y coordinate
	 * @param x2 the second point's x coordinate
	 * @param y2 the second point's y coordinate
	 */
	void drawLine(int x1, int y1, int x2, int y2);
	
	/**
	 * Draws the outline of the rectangle specified by the arguments, 
	 * using the receiver's foreground color. 
	 * The left and right edges of the rectangle are at x and x + width. 
	 * The top and bottom edges are at y and y + height. 
	 * @param x the x coordinate of the rectangle to be drawn
	 * @param y the y coordinate of the rectangle to be drawn
	 * @param width the width of the rectangle to be drawn
	 * @param height the height of the rectangle to be drawn 
	 */
	void drawRectangle(int x, int y, int width, int height);
	
	/**
	 * Draws a rectangle, based on the specified arguments, 
	 * which has the appearance of the platform's focus rectangle if the platform supports 
	 * such a notion, and otherwise draws a simple rectangle in the receiver's foreground color. 
	 * @param x the x coordinate of the rectangle
	 * @param y the y coordinate of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */ 
	void drawFocus(int x, int y, int width, int height);
	
	/**
	 * Fills the interior of the closed polygon which is defined by the specified array of integer coordinates, 
	 * using the receiver's background color. 
	 * The array contains alternating x and y values which are considered to represent points which are the vertices of the polygon. 
	 * Lines are drawn between each consecutive pair, and between the first pair and last pair in the array. 
	 * @param points an array of alternating x and y values which are the vertices of the polygon 
	 */
	void fillPolygon(int[] points);
	
	/**
	 * Draws the closed polygon which is defined by the specified array of integer coordinates, 
	 * using the receiver's foreground color. 
	 * The array contains alternating x and y values which are considered to represent points which are the vertices of the polygon. 
	 * Lines are drawn between each consecutive pair, and between the first pair and last pair in the array.
	 * @param points an array of alternating x and y values which are the vertices of the polygon 
	 */
	void drawPolygon(int[] points);
	
	/**
	 * Fills the interior of the rectangle specified by the arguments, using the receiver's background color. 
	 * @param x the x coordinate of the rectangle to be filled
	 * @param y the y coordinate of the rectangle to be filled
	 * @param width the width of the rectangle to be filled
	 * @param height the height of the rectangle to be filled
	 */
	void fillRectangle(int x, int y, int width, int height);
	
	/**
	 * Fills the interior of the specified rectangle with a gradient sweeping from left to right 
	 * or top to bottom progressing from the graphical context gradient color to its background color. 
	 * @param x the x coordinate of the rectangle to be filled
	 * @param y the y coordinate of the rectangle to be filled
	 * @param width the width of the rectangle to be filled, may be negative (inverts direction of gradient if horizontal)
	 * @param height the height of the rectangle to be filled, may be negative (inverts direction of gradient if horizontal)
	 * @param vertical if true sweeps from top to bottom, else sweeps from left to right
	 */
	void fillGradientRectangle(int x, int y, int width, int height, boolean vertical);
	
	/**
	 * Returns the given string width in pixels
	 * @param name the string
	 * @return the string width
	 */
	int textExtent(String name);
	
	/**
	 * Draws the given string, using the receiver's current font and foreground color. 
	 * Tab expansion and carriage return processing are performed. 
	 * If trans is true, then the background of the rectangular area where the text is being drawn will not be modified, 
	 * otherwise it will be filled with the receiver's background color. 
	 * @param string the string to be drawn
	 * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
	 * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
	 * @param trans if true the background will be transparent, otherwise it will be opaque 
	 */
	void drawText(String string, int x, int y, boolean trans);
	
	/**
	 * Draws the given string, using the receiver's current font and foreground color. 
	 * Tab expansion and carriage return processing are performed. 
	 * The background of the rectangular area where the text is being drawn will be filled with the receiver's background color. 
	 * @param string the string to be drawn
	 * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
	 * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
	 */
	void drawText(String string, int x, int y);
	
	/**
	 * Fills the interior of an oval, within the specified rectangular area, 
	 * with the receiver's background color. 
	 * @param x the x coordinate of the upper left corner of the oval to be filled
	 * @param y the y coordinate of the upper left corner of the oval to be filled
	 * @param width the width of the oval to be filled
	 * @param height the width of the oval to be filled
	 */
	void fillOval(int x, int y, int width, int height);
	
	/**
	 * Returns current the background color used in the graphical context
	 * @return the background color
	 */
	Color getBackground();
	
	/**
	 * Returns current the background color used in the graphical context
	 * @return the background color
	 */
	Color getForeground();
	
	/**
	 * Set the graphical context foreground color
	 * @param color the foreground color
	 */
	void setBackground(Color color);
	
	/**
	 * Set the graphical context background color
	 * @param color the background color
	 */
	void setForeground(Color color);
	
	/**
	 * Set the color to use when filling regions using gradient.
	 * The color will progress from the given color to the current background color
	 * @param color the gradient color to use
	 */
	void setGradientColor(Color color);
	
	/**
	 * Set the line width to use for drawing
	 * @param width the line width
	 */
	void setLineWidth(int width);
	
	/**
	 * Returns the current graphical context line width used for drawing
	 * @return the line width
	 */
	int getLineWidth();
	
	/**
	 * Returns the LineDotD style contant
	 * @return the constant value
	 */
	int getLineDotStyle();
	
	/**
	 * Returns the LineDash style constant
	 * @return the constant
	 */
	int getLineDashStyle();
	
	/**
	 * Returns the LineSolid style constant
	 * @return the constant
	 */
	int getLineSolidStyle();
	
	/**
	 * Draws the given string centered into the given rectangle.
	 * If the string cannot fit in the rectangle area, the string is truncated.
	 * If trans is true, then the background of the rectangular area where the text is being drawn will not be modified, 
	 * otherwise it will be filled with the receiver's background color. 
	 * @param name the string to draw
	 * @param _x the _x coordinate of the rectangle to draw the string 
	 * @param _y the _y coordinate of the rectangle to draw the string
	 * @param width the width of the rectangle to draw the string
	 * @param height the height of the rectangle to draw the string
	 * @param trans if true the background will be transparent, otherwise it will be opaque
	 */
	void drawTextTruncatedCentred(String name, int x, int y, int width, int height, boolean trans);
	void drawTextTruncatedLeft(String name, int x, int y, int width, int height, boolean trans);

	
	/**
	 * Draws the given string into the given rectangle (left justify)
	 * If the string cannot fit in the rectangle area, the string is truncated.
	 * If trans is true, then the background of the rectangular area where the text is being drawn will not be modified, 
	 * otherwise it will be filled with the receiver's background color. 
 	 * @param _x the _x coordinate of the rectangle to draw the string 
	 * @param _y the _y coordinate of the rectangle to draw the string
	 * @param width the width of the rectangle to draw the string
	 * @param height the height of the rectangle to draw the string
	 * @param trans if true the background will be transparent, otherwise it will be opaque
	 */
	void drawTextTruncated(String name, int x, int y, int width, int height, boolean trans);
	
	/**
	 * Set the current font used in the graphical context
	 * @param font the font to use
	 */
	void setFont(Font font);

	/**
	 * Returns the average character width for the given font
	 * @param font
	 * @return the average width
	 */
	int getFontWidth(Font font);

	/**
	 * Returns the zoom factor applied in both x and y directions when drawing
	 * @return the zoom factor
	 */
	float getZoom();
}
