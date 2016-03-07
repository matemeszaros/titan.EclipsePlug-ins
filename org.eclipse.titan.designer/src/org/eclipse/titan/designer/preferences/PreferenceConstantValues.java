/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import org.eclipse.swt.graphics.RGB;

/**
 * Possible values of internal preference settings.
 * 
 * @author Kristof Szabados
 * */
public final class PreferenceConstantValues {

	// syntax coloring
	public static final RGB GREY20 = new RGB(51, 51, 51);
	public static final RGB WHITE = new RGB(255, 255, 255);
	public static final RGB BLACK = new RGB(0, 0, 0);
	public static final RGB BROWN = new RGB(165, 42, 42);
	public static final RGB SADDLE_BROWN = new RGB(139, 69, 69);
	public static final RGB DARKGREEN = new RGB(0, 100, 0);
	public static final RGB SEAGREEN = new RGB(46, 139, 87);
	public static final RGB ROYALBLUE4 = new RGB(39, 64, 139);
	public static final RGB BLUE = new RGB(0, 0, 255);
	public static final RGB CHOCOLATE = new RGB(210, 105, 30);
	public static final RGB STEELBLUE = new RGB(70, 130, 180);
	public static final RGB RED = new RGB(255, 0, 0);
	public static final RGB STEELBLUE4 = new RGB(54, 100, 139);
	public static final RGB VIOLETRED4 = new RGB(139, 34, 82);
	public static final RGB GREY30 = new RGB(77, 77, 77);
	public static final RGB PLUM = new RGB(221, 160, 221);
	public static final RGB YELLOW = new RGB(225, 225, 127);

	// options for the compiler on how compiler and designer markers interact
	public static final String COMPILEROPTIONSTAY = "Stay unchanged";
	public static final String COMPILEROPTIONOUTDATE = "Become outdated";
	public static final String COMPILEROPTIONREMOVE = "Are removed";
	
	// options for selecting the broken parts to analyze
	public static final String MODULESELECTIONORIGINAL = "Original";
	public static final String BROKENPARTSVIAREFERENCES = "Broken parts via references";

	// options for the designer on how compiler and designer markers interact
	public static final String ONTHEFLYOPTIONSTAY = "Stay";
	public static final String ONTHEFLYOPTIONREMOVE = "Are removed";

	// The amount of processing cores in the hardware
	public static final int AVAILABLEPROCESSORS = Runtime.getRuntime().availableProcessors();

	// How should the content assist order the elements in its list.
	public static final String SORT_ALPHABETICALLY = "alphabetically";
	public static final String SORT_BY_RELEVANCE = "by relevance";

	// indentation policies on how to handles tabulators
	public static final String TAB_POLICY_1 = "Tab";
	public static final String TAB_POLICY_2 = "Spaces";

	//What to do on the console before build (consoleActionBeforeBuild):
	public static final String BEFORE_BUILD_NOTHING_TO_DO = "Nothing";
	public static final String BEFORE_BUILD_CLEAR_CONSOLE = "Clear";
	public static final String BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS = "Print delimiter";
	

	/** private constructor to disable instantiation */
	private PreferenceConstantValues() {
		//Do nothing
	}
}
