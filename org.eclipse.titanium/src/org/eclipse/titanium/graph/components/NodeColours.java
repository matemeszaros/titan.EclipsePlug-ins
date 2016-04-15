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
 * This class stores the possible module graph node colours, they are all
 * <code>public static</code> attributes. This class cannot have any instance
 * 
 * @author Gabor Jenei
 */
public final class NodeColours {
	public static final Color LIGHT_GREEN = new Color(137, 186, 23);
	public static final Color DARK_GREEN = new Color(0, 123, 120);
	public static final Color LIGHT_YELLOW = new Color(250, 187, 0);
	public static final Color DARK_YELLOW = new Color(240, 138, 0);
	public static final Color LIGHT_RED = new Color(227, 33, 25);
	public static final Color DARK_RED = new Color(227, 33, 25);

	public static final Color MISSING_COLOUR = new Color(143, 63, 123);
	public static final Color PICKED_COLOUR = new Color(0, 40, 95);
	public static final Color NO_VALUE_COLOUR = new Color(177, 179, 180);
	public static final Color RESULT_COLOUR = new Color(0, 169, 212);
	public static final Color NOT_RESULT_COLOUR = new Color(177, 179, 180);

	private NodeColours() {
	}
}