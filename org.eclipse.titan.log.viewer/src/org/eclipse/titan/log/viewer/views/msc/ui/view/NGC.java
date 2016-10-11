/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.view;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * NGC
 * 
 */
public class NGC implements IGC {

	private GC context;
	private MSCWidget view;
	private Font tempFont = null;
	private Color gradientColor = null;
	private Color backGround = null;
	private Color foreGround = null;
	private int viewY;
	private int viewX;
	private int yx;
	private int xx;
	private boolean drawWithFocus = false;
	private static int vscreenBounds = 0;

	/**
	 * Constructor
	 * 
	 * @param scrollView the view
	 * @param gc the gc to draw on
	 */
	public NGC(final MSCWidget scrollView, final GC gc) {
		this.context = gc;
		this.view = scrollView;
	}

	@Override
	public void setLineStyle(final int style) {
		this.context.setLineStyle(style);
	}

	@Override
	public int getLineStyle() {
		return this.context.getLineStyle();
	}

	@Override
	public int getContentsX() {
		return Math.round(this.view.getContentsX() / this.view.getZoomFactor());
	}

	@Override
	public int getContentsY() {
		return Math.round(this.view.getContentsY() / this.view.getZoomFactor());
	}

	@Override
	public int getVisibleWidth() {
		return Math.round(this.view.getVisibleWidth() / this.view.getZoomFactor());
	}

	@Override
	public int getVisibleHeight() {
		return Math.round(this.view.getVisibleHeight() / this.view.getZoomFactor());
	}

	private byte code(final int x, final int y) {
		byte c = 0;
		this.viewY = vscreenBounds;
		this.viewX = vscreenBounds;
		this.yx = this.view.getVisibleHeight() + vscreenBounds;
		this.xx = this.view.getVisibleWidth() + vscreenBounds;
		if (y > this.yx) {
			c |= 0x01; // top
		} else if (y < this.viewY) {
			c |= 0x02; // bottom
		}
		if (x > this.xx) {
			c |= 0x04; // right
		} else if (x < this.viewX) {
			c |= 0x08; // left
		}
		return c;
	}

	@Override
	public void drawLine(final int x1, final int y1, final int x2, final int y2) {
		int tempX1 = Math.round(x1 * this.view.getZoomFactor());
		int tempY1 = Math.round(y1 * this.view.getZoomFactor());
		int tempX2 = Math.round(x2 * this.view.getZoomFactor());
		int tempY2 = Math.round(y2 * this.view.getZoomFactor());
		tempX1 = this.view.contentsToViewX(tempX1);
		tempY1 = this.view.contentsToViewY(tempY1);
		tempX2 = this.view.contentsToViewX(tempX2);
		tempY2 = this.view.contentsToViewY(tempY2);

		byte code1 = code(tempX1, tempY1);
		byte code2 = code(tempX2, tempY2);
		byte codex;
		boolean draw = false;
		boolean end = false;
		int x = 0, y = 0;

		do {
			if ((code1 == 0) && (code2 == 0)) {
				draw = true;
				end = true;
			} else if ((code1 & code2) != 0) {
				end = true;
			} else {
				codex = (code1 != 0) ? code1 : code2;
				if ((codex & 0x01) != 0) { // top
					x = tempX1 + ((tempX2 - tempX1) * (this.yx - tempY1)) / (tempY2 - tempY1);
					y = this.yx;
				} else if ((codex & 0x02) != 0) { // bottom
					x = tempX1 + ((tempX2 - tempX1) * (this.viewY - tempY1)) / (tempY2 - tempY1);
					y = this.viewY;
				} else if ((codex & 0x04) != 0) { // right
					y = tempY1 + ((tempY2 - tempY1) * (this.xx - tempX1)) / (tempX2 - tempX1);
					x = this.xx;
				} else if ((codex & 0x08) != 0) { // left
					y = tempY1 + ((tempY2 - tempY1) * (this.viewX - tempX1)) / (tempX2 - tempX1);
					x = this.viewX;
				}

				if (codex == code1) {
					tempX1 = x;
					tempY1 = y;
					code1 = code(tempX1, tempY1);
				} else {
					tempX2 = x;
					tempY2 = y;
					code2 = code(tempX2, tempY2);
				}
			}
		} while (!end);
		if (draw) {
			this.context.drawLine(tempX1, tempY1, tempX2, tempY2);
		}
	}

	@Override
	public void drawRectangle(final int x, final int y, final int width, final int height) {
		int tempX = Math.round(x * this.view.getZoomFactor());
		
		// Workaround to avoid problems for some special cases (not very nice)
		int tempY;
		if (y != getContentsY()) {
			tempY = Math.round(y * this.view.getZoomFactor());
			tempY = this.view.contentsToViewY(tempY);
		} else {
			tempY = 0;
		}
		
		int tempWidth = Math.round(width * this.view.getZoomFactor());
		int tempHeight = Math.round(height * this.view.getZoomFactor());
		tempX = this.view.contentsToViewX(tempX);

		Rectangle rectangle = new Rectangle(tempX, tempY, tempWidth, tempHeight);
		transformRectangle(rectangle);
		this.context.drawRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}

	private void transformRectangle(Rectangle rectangle) {
		if (rectangle.x < -vscreenBounds) {
			rectangle.width = rectangle.width + rectangle.x + vscreenBounds;
			rectangle.x = -vscreenBounds;
		}
		if (rectangle.y < -vscreenBounds) {
			rectangle.height = rectangle.height + rectangle.y + vscreenBounds;
			rectangle.y = -vscreenBounds;
		}
		if ((rectangle.width < -vscreenBounds) && (rectangle.x + rectangle.width < -vscreenBounds)) {
			rectangle.width = -vscreenBounds;
		} else if (rectangle.width + rectangle.x > this.view.getVisibleWidth() + vscreenBounds) {
			rectangle.width = this.view.getVisibleWidth() + vscreenBounds - rectangle.x;
		}
		if ((rectangle.height < -vscreenBounds) && (rectangle.y + rectangle.height < -vscreenBounds)) {
			rectangle.height = -vscreenBounds;
		} else if (rectangle.height + rectangle.y > this.view.getVisibleHeight() + vscreenBounds) {
			rectangle.height = this.view.getVisibleHeight() + vscreenBounds - rectangle.y;
		}
	}

	@Override
	public void drawFocus(final int x, final int y, final int width, final int height) {
		// Do nothing
	}

	@Override
	public void fillPolygon(final int[] points) {
		int len = (points.length / 2) * 2;
		int[] localPoint = new int[len];
		for (int i = 0; i < len; i++) {
			localPoint[i] = this.view.contentsToViewX(Math.round(points[i] * this.view.getZoomFactor()));
			i++;
			localPoint[i] = this.view.contentsToViewY(Math.round(points[i] * this.view.getZoomFactor()));
		}
		this.context.fillPolygon(localPoint);
	}

	@Override
	public void drawPolygon(final int[] points) {
		int len = (points.length / 2) * 2;
		int[] localPoint = new int[len];
		for (int i = 0; i < len; i++) {
			localPoint[i] = this.view.contentsToViewX(Math.round(points[i] * this.view.getZoomFactor()));
			i++;
			localPoint[i] = this.view.contentsToViewY(Math.round(points[i] * this.view.getZoomFactor()));
		}
		this.context.drawPolygon(localPoint);
	}

	@Override
	public void fillRectangle(final int x, final int y, final int width, final int height) {
		int tempX = Math.round(x * this.view.getZoomFactor());
		// Workaround to avoid problems for some special cases (not very nice)
		int tempY;
		if (y != getContentsY()) {
			tempY = Math.round(y * this.view.getZoomFactor());
			tempY = this.view.contentsToViewY(tempY) + 1;
		} else {
			tempY = 1;
		}
		int tempWidth = Math.round(width * this.view.getZoomFactor()) - 1;
		int tempHeight = Math.round(height * this.view.getZoomFactor()) - 1;
		tempX = this.view.contentsToViewX(tempX) + 1;

		Rectangle rectangle = new Rectangle(tempX, tempY, tempWidth, tempHeight);
		transformRectangle(rectangle);
		this.context.fillRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}

	@Override
	public void fillGradientRectangle(final int x, final int y, final int width, final int height, final boolean vertical) {
		int tempX = Math.round(x * this.view.getZoomFactor());
		int tempY = Math.round(y * this.view.getZoomFactor());
		int tempWidth = Math.round(width * this.view.getZoomFactor());
		int tempHeight = Math.round(height * this.view.getZoomFactor());
		Color tempColor = this.foreGround;
		setForeground(this.gradientColor);
		tempX = this.view.contentsToViewX(tempX);
		tempY = this.view.contentsToViewY(tempY);
		// Will see later this case (Lifeline name)
		if (vertical) {
			if (tempHeight > 0) {
				tempHeight++;
			} else {
				tempHeight--;
			}
			this.context.fillGradientRectangle(tempX, tempY, tempWidth, tempHeight, true);
			setForeground(tempColor);
		} else {
			Rectangle rectangle = new Rectangle(tempX, tempY, tempWidth, tempHeight);
			transformRectangle(rectangle);
			this.context.fillGradientRectangle(rectangle.x + rectangle.width, rectangle.y, -rectangle.width,
					rectangle.height + 1, false);
			setForeground(tempColor);
		}
	}

	@Override
	public int textExtent(final String name) {
		return this.context.textExtent(name).x;
	}

	@Override
	public void drawText(final String string, final int x, final int y, final boolean trans) {
		int tempX = Math.round(x * this.view.getZoomFactor());
		int tempY = Math.round(y * this.view.getZoomFactor());
		this.context.drawText(string, this.view.contentsToViewX(tempX), this.view.contentsToViewY(tempY), trans);
		if (this.drawWithFocus) {
			Point r = this.context.textExtent(string);
			this.context.drawFocus(tempX - 1, tempY - 1, r.x + 2, r.y + 2);
		}
	}

	@Override
	public void drawText(final String string, final int x, final int y) {
		int tempX = Math.round(x * this.view.getZoomFactor());
		int tempY = Math.round(y * this.view.getZoomFactor());
		this.context.drawText(string, this.view.contentsToViewX(tempX), this.view.contentsToViewY(tempY), true);
		if (this.drawWithFocus) {
			Point r = this.context.textExtent(string);
			this.context.drawFocus(tempX - 1, tempY - 1, r.x + 2, r.y + 2);
		}
	}

	@Override
	public void fillOval(final int x, final int y, final int width, final int height) {
		int tempX = Math.round(x * this.view.getZoomFactor());
		int tempY = Math.round(y * this.view.getZoomFactor());
		int tempWidth = Math.round(width * this.view.getZoomFactor());
		int tempHeight = Math.round(height * this.view.getZoomFactor());

		this.context.fillOval(this.view.contentsToViewX(tempX), this.view.contentsToViewY(tempY), tempWidth, tempHeight);
	}

	@Override
	public Color getBackground() {
		if ((this.backGround != null) && (!this.backGround.isDisposed())) {
			return this.foreGround;
		}
		return (Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR);
	}

	@Override
	public Color getForeground() {
		if ((this.foreGround != null) && (!this.foreGround.isDisposed())) {
			return this.foreGround;
		}
		return (Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_FOREGROUND_COLOR);
	}

	@Override
	public void setBackground(final Color color) {
		if (color == null) {
			return;
		}
		if (!color.isDisposed()) {
			this.context.setBackground(color);
			this.backGround = color;
		}
	}

	@Override
	public void setForeground(final Color color) {
		if (color == null) {
			return;
		}
		if (!color.isDisposed()) {
			this.context.setForeground(color);
			this.foreGround = color;
		}
	}

	@Override
	public void setGradientColor(final Color color) {
		if (color == null) {
			return;
		}
		this.gradientColor = color;
	}

	@Override
	public void setLineWidth(final int width) {
		this.context.setLineWidth(width);
	}

	@Override
	public int getLineWidth() {
		return this.context.getLineWidth();
	}

	// Linux GTK Workaround
	private void localDrawText(final String string, final int x, final int y, final boolean trans) {
		Point r = this.context.textExtent(string);
		if (!trans) {
			this.context.fillRectangle(x, y, r.x, r.y);
		}
		this.context.drawText(string, x, y, trans);
		if ((this.drawWithFocus) && (string.length() > 1)) {
			this.context.drawFocus(x - 1, y - 1, r.x + 2, r.y + 2);
		}
	}

	@Override
	public void drawTextTruncatedCentred(final String name, final int oldX, final int oldY, final int width, final int height, final boolean trans) {
		Point tx = this.context.textExtent(name);
		Rectangle rectangle = createIncorporatingRectangle(oldX, oldY, width, height);
		if (tx.x <= rectangle.width) {
			localDrawText(name, rectangle.x + 1 + (rectangle.width - tx.x) / 2, rectangle.y + 1 + (rectangle.height - tx.y) / 2, trans);
		} else {
			String nameToDisplay = getNameToDisplay(name, rectangle.width);
			localDrawText(nameToDisplay,
					rectangle.x + 1 + (rectangle.width - this.context.textExtent(nameToDisplay).x) / 2,
					rectangle.y + 1 + (rectangle.height - this.context.textExtent(nameToDisplay).y) / 2,
						trans);
		}
	}

	private Rectangle createIncorporatingRectangle(int oldX, int oldY, int width, int height) {
		Rectangle rectangle = new Rectangle(0, 0, 0, 0);
		int tempX = Math.round(oldX * this.view.getZoomFactor());
		// Workaround to avoid round problems for some special cases (not very
		// nice)
		if (oldY != getContentsY()) {
			int tempY = Math.round(oldY * this.view.getZoomFactor());
			rectangle.y = this.view.contentsToViewY(tempY);
		}
		rectangle.width = Math.round(width * this.view.getZoomFactor());
		rectangle.height = Math.round(height * this.view.getZoomFactor());
		rectangle.x = this.view.contentsToViewX(tempX);
		return rectangle;
	}

	@Override
	public void drawTextTruncatedLeft(final String name, final int old_x, final int old_y, final int width, final int height, final boolean trans) {
		Point tx = this.context.textExtent(name);
		Rectangle rectangle = createIncorporatingRectangle(old_x, old_y, width, height);
		if (tx.x <= rectangle.width) {
			localDrawText(name, rectangle.x + 1, rectangle.y + 1 + (rectangle.height - tx.y) / 2, trans);

		} else {
			String nameToDisplay = getNameToDisplay(name, rectangle.width);
			localDrawText(nameToDisplay,
					rectangle.x + 1,
					rectangle.y + 1 + (rectangle.height - tx.y) / 2,
					trans);
		}
	}

	private String getNameToDisplay(String name, int tempWidth) {
		String nameToDisplay = name;
		for (int i = name.length() - 1; (i >= 0)
				&& (this.context.textExtent(nameToDisplay).x >= tempWidth); i--) {
			nameToDisplay = name.substring(0, i);
		}
		int dotCount = 0;
		for (int i = 1; (i <= 3) && (nameToDisplay.length() - i > 0); i++) {
			dotCount++;
		}
		nameToDisplay = nameToDisplay.substring(0, nameToDisplay.length() - dotCount);
		nameToDisplay = addDots(nameToDisplay, dotCount);
		return nameToDisplay;
	}

	private String addDots(String nameToDisplay, int dotCount) {
		char[] chars = new char[dotCount];
		Arrays.fill(chars, '.');
		return nameToDisplay + new String(chars);
	}

	@Override
	public void drawTextTruncated(final String name, final int old_x, final int old_y, final int width, final int height, final boolean trans) {
		int tempX = Math.round(old_x * this.view.getZoomFactor());
		int tempY = Math.round(old_y * this.view.getZoomFactor());
		int tempWidth = Math.round(width * this.view.getZoomFactor());
		int tempHeight = Math.round(height * this.view.getZoomFactor());
		int x = this.view.contentsToViewX(tempX);
		int y = this.view.contentsToViewY(tempY);
		if (this.context.textExtent(name).x <= tempWidth) {
			localDrawText(name, x + 1, y + 1 + tempHeight, trans);
		} else {
			String nameToDisplay = getNameToDisplay(name, tempWidth);
			localDrawText(nameToDisplay,x + 1, y + 1 + tempHeight, trans);
		}
	}

	@Override
	public void setFont(final Font font) {
		if ((font != null) && (font.getFontData().length > 0)) {
			FontData fontData = font.getFontData()[0];
			int h = Math.round(fontData.getHeight() * this.view.getZoomFactor());
			if (h > 0) {
				fontData.setHeight(h);
			}
			if (this.tempFont != null) {
				this.tempFont.dispose();
			}
			this.tempFont = new Font(Display.getCurrent(), fontData);
			this.context.setFont(this.tempFont);
		}
	}

	/**
	 * Gets the height of the given font
	 * 
	 * @param font the font to get height for
	 * @return the height of the font
	 */
	public int getFontHeight(final Font font) {
		if (font != null) {
			Font toRestore = this.context.getFont();
			this.context.setFont(font);
			int height = this.context.textExtent("lp").y; //$NON-NLS-1$
			this.context.setFont(toRestore);
			return height;
		}
		return 0;
	}

	@Override
	public int getFontWidth(final Font font) {
		if (font != null) {
			Font toRestore = this.context.getFont();
			this.context.setFont(font);
			int width = this.context.getFontMetrics().getAverageCharWidth();
			this.context.setFont(toRestore);
			return width;
		}
		return 0;
	}

	/**
	 * Dispose
	 */
	public void dispose() {
		if (this.tempFont != null) {
			this.tempFont.dispose();
		}
		this.tempFont = null;
		if (this.context != null) {
			this.context.dispose();
		}
		this.context = null;
	}

	@Override
	public float getZoom() {
		if (this.view != null) {
			return this.view.getZoomFactor();
		}

		return 1;
	}

	@Override
	public int getLineDotStyle() {
		return SWT.LINE_DOT;
	}

	@Override
	public int getLineDashStyle() {
		return SWT.LINE_DASH;
	}

	@Override
	public int getLineSolidStyle() {
		return SWT.LINE_SOLID;
	}

}
