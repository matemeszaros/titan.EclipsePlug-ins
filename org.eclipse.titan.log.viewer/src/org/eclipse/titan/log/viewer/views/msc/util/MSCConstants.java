/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.log.viewer.utils.Constants;


/**
 * Class for MSC constants
 *
 */
public final class MSCConstants {
	
	/**
	* RESOURCE KEYS 
	**/

	// Color keys
	private static final String COLOR_KEY_BLACK = "COLOR_BLACK"; //$NON-NLS-1$
	private static final String COLOR_KEY_WHITE = "COLOR_WHITE"; //$NON-NLS-1$
	
	private static final String COLOR_KEY_LIGHTER_GREY = "COLOR_LIGHTER_GREY"; //$NON-NLS-1$
	private static final String COLOR_KEY_LIGHT_GREY = "COLOR_LIGHT_GREY"; //$NON-NLS-1$
	private static final String COLOR_KEY_GREY = "COLOR_GREY"; //$NON-NLS-1$
	private static final String COLOR_KEY_DARK_GREY = "COLOR_DARK_GREY"; //$NON-NLS-1$
	private static final String COLOR_KEY_SOFT_BLUE = "COLOR_SOFT_BLUE"; //$NON-NLS-1$
	
	// Silent event color keys
	private static final String COLOR_KEY_EVENT_RED = "COLOR_EVENT_RED"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_BLUE = "COLOR_EVENT_BLUE"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_YELLOW = "COLOR_EVENT_YELLOW"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_GREEN = "COLOR_EVENT_GREEN"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_ORANGE = "COLOR_EVENT_ORANGE"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_LIME = "COLOR_EVENT_LIME"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_DARK_BLUE = "COLOR_EVENT_DARK_BLUE"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_LIGHT_YELLOW = "COLOR_EVENT_LIGHT_YELLOW"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_LIGHT_ORANGE = "COLOR_EVENT_LIGHT_ORANGE"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_CORAL = "COLOR_EVENT_CORAL"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_FUCHSIA = "COLOR_EVENT_FUCHSIA"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_LIGHT_GREEN = "COLOR_EVENT_LIGHT_GREEN"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_PINK = "COLOR_EVENT_PINK"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_LIGHT_BLUE = "COLOR_EVENT_LIGHT_BLUE"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_DARK_PURPLE = "COLOR_EVENT_DARK_PURPLE"; //$NON-NLS-1$
	private static final String COLOR_KEY_EVENT_PALE_PURPLE = "COLOR_EVENT_PALE_PURPLE"; //$NON-NLS-1$
	
	// Verdict color keys
	private static final String COLOR_KEY_VERDICT_FAIL_ERROR = "COLOR_VERDICT_FAIL_ERROR"; //$NON-NLS-1$
	private static final String COLOR_KEY_VERDICT_INCONC = "COLOR_VERDICT_INCONC"; //$NON-NLS-1$
	private static final String COLOR_KEY_VERDICT_PASS = "COLOR_VERDICT_PASS"; //$NON-NLS-1$
	private static final String COLOR_KEY_VERDICT_NONE = "COLOR_VERDICT_NONE"; //$NON-NLS-1$
	
	// Font keys
	public static final String FONT_DEFAULT_KEY = "FONT_DEFAULT"; //$NON-NLS-1$
	public static final String FONT_BOLD_KEY = "FONT_BOLD"; //$NON-NLS-1$
	
	
	// Enumerator for resources
	private static enum Resources {
		// Colors
		COLOR_BLACK, COLOR_WHITE,
		COLOR_LIGHTER_GREY, COLOR_LIGHT_GREY, COLOR_GREY, COLOR_DARK_GREY, COLOR_SOFT_BLUE, 
		COLOR_EVENT_RED, COLOR_EVENT_BLUE, COLOR_EVENT_YELLOW, COLOR_EVENT_GREEN,
		// Silent event colors
		COLOR_EVENT_ORANGE, COLOR_EVENT_LIME, COLOR_EVENT_DARK_BLUE, COLOR_EVENT_LIGHT_YELLOW,
		COLOR_EVENT_LIGHT_ORANGE, COLOR_EVENT_CORAL, COLOR_EVENT_FUCHSIA, COLOR_EVENT_LIGHT_GREEN,
		COLOR_EVENT_PINK, COLOR_EVENT_LIGHT_BLUE, COLOR_EVENT_DARK_PURPLE, COLOR_EVENT_PALE_PURPLE,
		// Verdict colors
		COLOR_VERDICT_FAIL_ERROR, COLOR_VERDICT_INCONC, COLOR_VERDICT_PASS, COLOR_VERDICT_NONE,
		// Fonts
		FONT_DEFAULT, FONT_BOLD;

		public static Resources getResourceValue(final String str) {
			try {
				return valueOf(str);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	// TestFileReader Constants
	public static final String READ_ONLY = "r"; //$NON-NLS-1$

	public static final String READ_WRITE = "rw"; //$NON-NLS-1$
	public static final byte LF = '\n';
	public static final byte CR = '\r';
	public static final int K = 1024;
	public static final int INITIAL_BUFFER_SIZE = 16 * K;

	/**
	* IDs
	*/
	public static final String ID_ZOOM_IN = "org.eclipse.titan.log.viewer.views.msc.ui.actions.ZoomInCoolBar"; //$NON-NLS-1$
	public static final String ID_ZOOM_OUT = "org.eclipse.titan.log.viewer.views.msc.ui.actions.ZoomOutCoolBar"; //$NON-NLS-1$
	public static final String ID_NO_ZOOM = "org.eclipse.titan.log.viewer.views.msc.ui.actions.NoZoom"; //$NON-NLS-1$
	public static final String ID_RESET_ZOOM = "org.eclipse.titan.log.viewer.views.msc.ui.actions.ResetZoom"; //$NON-NLS-1$
	public static final String ID_ZOOM_GROUP = "ZOOM_GROUP"; //$NON-NLS-1$
	
	/**
	* MODEL *
	**/
	public static final String SUT_NAME = "System"; //$NON-NLS-1$
	public static final String MTC_NAME = "MTC"; //$NON-NLS-1$
	
	/**
	* ICONS *
	**/
	public static final String ICON_ZOOM_MASK = "icons/zoom_mask.bmp"; //$NON-NLS-1$
	public static final String ICON_ZOOM_IN_SOURCE = "icons/zoomin_source.bmp"; //$NON-NLS-1$
	public static final String ICON_ZOOM_OUT_SOURCE = "icons/zoomout_source.bmp"; //$NON-NLS-1$
	public static final String ICON_RESET_ZOOM = "icons/home_nav.gif"; //$NON-NLS-1$
	public static final String ICON_MOVE = "icons/move.gif"; //$NON-NLS-1$
	public static final String ICON_ZOOM_IN = "icons/zoomin_nav.gif"; //$NON-NLS-1$
	public static final String ICON_ZOOM_OUT = "icons/zoomout_nav.gif"; //$NON-NLS-1$
	
	/**********
	* GENERAL *
	**********/
	public static final boolean DRAW_BORDER = true;  
	public static final boolean DRAW_GRADIENT = true;
	public static final boolean DRAW_SHADOW = true;
	
	/*********
	* COLORS *
	*********/

	// Verdict colors
	public static final String VERDICT_FAIL_ERROR = COLOR_KEY_VERDICT_FAIL_ERROR;
	public static final String VERDICT_INCONC = COLOR_KEY_VERDICT_INCONC;
	public static final String VERDICT_PASS = COLOR_KEY_VERDICT_PASS;
	public static final String VERDICT_NONE = COLOR_KEY_VERDICT_NONE;
	
	public static final Map<String, String> SILENT_EVENT_COLORS = new HashMap<String, String>();
	static {
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_ERROR,      COLOR_KEY_EVENT_RED);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_FUNCTION,   COLOR_KEY_EVENT_BLUE);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_ACTION,     COLOR_KEY_EVENT_YELLOW);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_USER,       COLOR_KEY_EVENT_GREEN);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_WARNING,    COLOR_KEY_EVENT_ORANGE);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_UNKNOWN,    COLOR_KEY_EVENT_LIME);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_EXECUTOR,   COLOR_KEY_EVENT_DARK_BLUE);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_TIMEROP,    COLOR_KEY_EVENT_LIGHT_YELLOW);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_VERDICTOP,  COLOR_KEY_EVENT_LIGHT_ORANGE);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_DEFAULTOP,  COLOR_KEY_EVENT_CORAL);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_PORTEVENT,  COLOR_KEY_EVENT_FUCHSIA);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_TESTCASE,   COLOR_KEY_EVENT_LIGHT_GREEN);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_STATISTICS, COLOR_KEY_EVENT_PINK);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_PARALLEL,   COLOR_KEY_EVENT_LIGHT_BLUE);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_MATCHING,   COLOR_KEY_EVENT_DARK_PURPLE);
		SILENT_EVENT_COLORS.put(Constants.EVENTTYPE_DEBUG,      COLOR_KEY_EVENT_PALE_PURPLE);
	}
	
	public static final String COMPONENT_BG_COLOR = COLOR_KEY_LIGHT_GREY;
	public static final String COMPONENT_SHADOW_COLOR = COLOR_KEY_GREY;
	public static final String COMPONENT_LINE_COLOR = COLOR_KEY_BLACK;
	public static final String COMPONENT_FONT_COLOR = COLOR_KEY_BLACK;
	public static final String DEFAULT_FOREGROUND_COLOR = COLOR_KEY_BLACK;
	public static final String DEFAULT_BACKGROUND_COLOR = COLOR_KEY_WHITE;
	public static final String LIFELIFE_HEADER_BG_COLOR = COLOR_KEY_SOFT_BLUE;
	public static final String LIFELIFE_HEADER_SHADOW_COLOR = COLOR_KEY_GREY;
	public static final String LIFELIFE_HEADER_LINE_COLOR = COLOR_KEY_BLACK;
	public static final String LIFELIFE_HEADER_FONT_COLOR = COLOR_KEY_BLACK;
	public static final String LIFELIFE_LINE_COLOR = COLOR_KEY_DARK_GREY;
	public static final String MESSAGE_LINE_COLOR = COLOR_KEY_SOFT_BLUE;
	public static final String MESSAGE_FONT_COLOR = COLOR_KEY_BLACK;
	public static final String SETVERDICT_FONT_COLOR = COLOR_KEY_BLACK;
	public static final String SETVERDICT_LINE_COLOR = COLOR_KEY_BLACK;
	public static final String SETVERDICT_SHADOW_COLOR = COLOR_KEY_GREY;
	public static final String SILENT_EVENT_BG_COLOR = COLOR_KEY_LIGHT_GREY;
	public static final String SILENT_EVENT_LINE_COLOR = COLOR_KEY_BLACK;
	public static final String SILENT_EVENT_SHADOW_COLOR = COLOR_KEY_GREY;
	public static final String TESTCASESTART_FONT_COLOR = COLOR_KEY_BLACK;
	public static final String TESTCASEEND_FONT_COLOR = COLOR_KEY_BLACK;
	public static final String TESTCASEEND_LINE_COLOR = COLOR_KEY_BLACK;
	public static final String TESTCASEEND_SHADOW_COLOR = COLOR_KEY_GREY;
	public static final String TIMESTAMP_FONT_COLOR = COLOR_KEY_DARK_GREY;
	
	// Selection
	public static final String SELECTION_COLOR = COLOR_KEY_LIGHTER_GREY;

	/**
	* FONTS 
	**/
	public static final String MSC_DEFAULT_FONT = FONT_DEFAULT_KEY;
	public static final String MSC_BOLD_FONT = FONT_BOLD_KEY;

	/**
	* METRICS 
	**/

	// Font size
	private static int fontHeight = 10; // Default value
	private static int defaultFontWidth = 5; // Default value
	private static int boldFontWidth = 5; // Default value

	// Row/Column size
	public static final int ROW_SPACING = 10;
	public static final int COLUMN_SPACING = 12;
	public static final int ROW_HEIGHT = fontHeight * 2 + ROW_SPACING;
	public static final int COLUMN_WIDTH = defaultFontWidth * 23 + COLUMN_SPACING;
	
	// Margins
	public static final int LEFT_MARGIN = 10;
	public static final int RIGHT_MARGIN = 50;
	public static final int TOP_MARGIN = 10;
	public static final int BOTTOM_MARGIN = 50;
	
	// Header
	public static final int HEADER_TEXT_H_SPACING = 10;
	public static final int HEADER_TEXT_V_SPACING = 10;
	public static final int HEADER_SHADOW_SIZE = 1;
	
	// Component
	public static final int COMPONENT_TEXT_H_SPACING = 10;
	public static final int COMPONENT_SHADOW_SIZE = 1;
	
	// Message
	public static final int MESSAGE_SYMBOL_SIZE = 8;
	public static final int MESSAGES_TEXT_VERTICAL_SPACING = 10;
	public static final int INTERNAL_MESSAGE_WIDTH = 20;
	public static final int INTERNAL_MESSAGE_H_MARGIN = 5;
	public static final int SYNC_INTERNAL_MESSAGE_HEIGHT = 10;

	// Line width
	public static final int NORMAL_LINE_WIDTH = 1;
	
	// Silent Event
	public static final int SILENT_EVENT_SHADOW_SIZE = 1;
	
	public static final int SETVERDICT_SIZE = 4;
	public static final int SETVERDICT_SHADOW_SIZE = 1;
	
	// Test Case End
	public static final int TESTCASEEND_SHADOW_SIZE = 1;
	
	/**
	 * Used to sample the diagram.
	 * When the lifeline spacing is smaller than this constant when zooming out then less lifelines are displayed
	 * to avoid lifelines overlapping and mainly saving some execution time
	 */
	public static final int LIFELINE_SIGNIFICANT_HSPACING = 10;
	
	/**
	 * Used to sample the diagram.
	 * When the message spacing is smaller than this constant when zooming out then less message are displayed
	 * to avoid message overlapping and mainly saving some execution time
	 */
	public static final int MESSAGE_SIGNIFICANT_VSPACING = 1;
	
	public static final String ARROW_LEFT = " <- "; //$NON-NLS-1$
	public static final String ARROW_RIGHT = " -> "; //$NON-NLS-1$

	private MSCConstants() {
		// Hide constructor
	}

	/**
	 * Gets the resource for the given resource key
	 *
	 * @param resourceKey the resource key
	 * @return the resource of null if key not found
	 */
	public static Resource getResource(final String resourceKey) {
		Resources resourceValue = MSCConstants.Resources.getResourceValue(resourceKey);
		if (resourceValue == null) {
			return null;
		}

		Resource resource = null;
		switch (resourceValue) {
			// Colors
			case COLOR_BLACK: 			   return new Color(Display.getDefault(),   0,   0,   0);
			case COLOR_WHITE: 			   return new Color(Display.getDefault(), 255, 255, 255);

			case COLOR_LIGHTER_GREY:       return new Color(Display.getDefault(), 220, 220, 220);
			case COLOR_LIGHT_GREY:   	   return new Color(Display.getDefault(), 200, 200, 200);
			case COLOR_GREY :        	   return new Color(Display.getDefault(), 150, 150, 150);
			case COLOR_DARK_GREY :   	   return new Color(Display.getDefault(), 100, 100, 100);
			case COLOR_SOFT_BLUE :   	   return new Color(Display.getDefault(), 135, 155, 165);

			case COLOR_EVENT_RED:          return new Color(Display.getDefault(), 255,   0,   0);
			case COLOR_EVENT_BLUE:         return new Color(Display.getDefault(),   0,   0, 255);
			case COLOR_EVENT_YELLOW:       return new Color(Display.getDefault(), 255, 255,   0);
			case COLOR_EVENT_GREEN:        return new Color(Display.getDefault(),   0, 255,   0);
			case COLOR_EVENT_ORANGE:       return new Color(Display.getDefault(), 255, 153,   0);
			case COLOR_EVENT_LIME:         return new Color(Display.getDefault(), 204, 255,   0);
			case COLOR_EVENT_DARK_BLUE:    return new Color(Display.getDefault(),  51,   0, 102);
			case COLOR_EVENT_LIGHT_YELLOW: return new Color(Display.getDefault(), 255, 255, 204);
			case COLOR_EVENT_LIGHT_ORANGE: return new Color(Display.getDefault(), 255, 204, 102);
			case COLOR_EVENT_CORAL:        return new Color(Display.getDefault(), 255, 127,  80);
			case COLOR_EVENT_FUCHSIA:      return new Color(Display.getDefault(), 255,   0, 255);
			case COLOR_EVENT_LIGHT_GREEN:  return new Color(Display.getDefault(), 102, 255, 102);
			case COLOR_EVENT_PINK:         return new Color(Display.getDefault(), 255, 102, 204);
			case COLOR_EVENT_LIGHT_BLUE:   return new Color(Display.getDefault(),   0, 204, 255);
			case COLOR_EVENT_DARK_PURPLE:  return new Color(Display.getDefault(), 102,   0, 153);
			case COLOR_EVENT_PALE_PURPLE:  return new Color(Display.getDefault(), 255, 204, 255);

			case COLOR_VERDICT_FAIL_ERROR: return new Color(Display.getDefault(), 255, 102, 102);
			case COLOR_VERDICT_INCONC:     return new Color(Display.getDefault(), 232, 242, 254);
			case COLOR_VERDICT_PASS:       return new Color(Display.getDefault(), 102, 204, 102);
			case COLOR_VERDICT_NONE:       return new Color(Display.getDefault(), 200, 200, 200);

			// Fonts
			case FONT_DEFAULT:            return new Font(Display.getDefault(),
					Display.getDefault().getSystemFont().getFontData()[0].getName(),
					Display.getDefault().getSystemFont().getFontData()[0].getHeight(),
					SWT.NORMAL);
			case FONT_BOLD:               return new Font(Display.getDefault(),
					Display.getDefault().getSystemFont().getFontData()[0].getName(),
					Display.getDefault().getSystemFont().getFontData()[0].getHeight(),
					SWT.BOLD);
			default: break;
		}
		return resource;
	}

	public static String getVerdictColor(final String verdict) {
		String color = VERDICT_NONE; // none
		if (verdict.equals(Constants.TEST_CASE_VERDICT_PASS)) {
			color = VERDICT_PASS;
		} else if (verdict.equals(Constants.TEST_CASE_VERDICT_FAIL)
				|| verdict.equals(Constants.TEST_CASE_VERDICT_ERROR)) {
			color = VERDICT_FAIL_ERROR;
		} else if (verdict.equals(Constants.TEST_CASE_VERDICT_INCONCLUSIVE)) {
			color = VERDICT_INCONC;
		}
		return color;
	}

	/**
	 * Sets the font height of the default font
	 * @param height the height of the font
	 */
	public static void setFontHeight(final int height) {
		fontHeight = height;
	}

	/**
	 * Gets the font height of the default font
	 * @return the font height
	 */
	public static int getFontHeight() {
		return fontHeight;
	}

	/**
	 * Sets the font width of the default font
	 * @param width the font width
	 */
	public static void setDefaultFontWidth(final int width) {
		defaultFontWidth = width;
	}

	/**
	 * Gets the font width of the default font
	 */
	public static int getDefaultFontWidth() {
		return defaultFontWidth;
	}

	/**
	 * Sets the font width of the bold font
	 * @param width the font width
	 */
	public static void setBoldFontWidth(final int width) {
		boldFontWidth = width;
	}

	/**
	 * Gets the font width of the bold font
	 */
	public static int getBoldFontWidth() {
		return boldFontWidth;
	}
}
