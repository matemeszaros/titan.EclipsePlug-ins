/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.types;

/**
 * Helper class that can convert problem type names into preference names,
 * to retrieve the cost of correction values associated with the problem type.
 * 
 * @author ekrisza
 * 
 */
public class ProblemNameToPreferenceMapper {
	private static final String IMPACT = ".IMPACT";
	private static final String BASELINE = ".BASELINE";
	private static final String AVG = " .AVG";
	private static final String MIN = ".MIN";
	private static final String MAX = ".MAX";
	private static final String TIME= " .TIME";
	
	public static String nameSmellAvgTime(String smellName) {
		return smellName + AVG + TIME;
	}
	
	public static String nameSmellMinTime(String smellName) {
		return smellName + MIN +TIME;
	}
	
	public static String nameSmellMaxTime(String smellName) {
		return smellName + MAX +TIME;
	}
	
	// Some aid method to make accessing code smell data easier:
	public static String nameSmellImpact(String smellName) {
		return smellName + IMPACT;
	}

	public static String nameSmellBaseLine(String smellName) {
		return smellName + BASELINE;
	}
}
