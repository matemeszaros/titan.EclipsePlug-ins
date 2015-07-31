/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.types;

/**
 * This is a base interface of all problems that are marked
 * @author Gabor Jenei
 */
public interface ProblemType {
	/**
	 * @return The baseline value of the number of code smells
	 */
	public int getBaseLine();
	
	/**
	 * @return The impact of the code smell
	 */
	public int getImpact();
	
	/**
	 * @return The currently set average repair time of the code smell
	 */
	public double getAvgRepairTime();
	
	/**
	 * @return The currently set minimal repair time of the code smell
	 */
	public double getMinRepairTime();
	
	/**
	 * @return The currently set maximal repair time of the code smell
	 */
	public double getMaxRepairTime();
	 /**
	 * @return The default average repair time of the code smell
	 */
	 public double getAvgDefaultTime();
	
	 /**
	  * @return The default minimal repair time of the code smell
	  */
	 public double getMinDefaultTime();
	
	 /**
	  * @return The default maximal repair time of the code smell
	  */
	 public double getMaxDefaultTime();
	
	/**
	 * @return The easier (human readable name of the code smell)
	 */
	public String getHumanReadableName();
}
