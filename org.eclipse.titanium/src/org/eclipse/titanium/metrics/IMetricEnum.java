/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.Set;


/**
 * Common interface for all metric enums.
 * <p>
 * They serve as a light weight, globally accessible handle for concrete metric
 * implementations.
 * 
 * @author poroszd
 * 
 */
public interface IMetricEnum {
	/**
	 * The unique ID of this metric.
	 * <p>
	 * This ID is used in the <code>PreferenceStore</code> to access persistent
	 * data of the represented metric (is it enabled in the view, etc.).
	 * 
	 * @return The unique ID.
	 * 
	 * @see org.eclipse.titan.designer.preferences.PreferenceConstants
	 */
	String id();

	/**
	 * @return The human readable name of the represented metric
	 */
	String getName();

	/**
	 * @return the human readable name of the entity related to this metric
	 */
	String groupName();

	/**
	 * @return A short tooltip for the represented metric
	 */
	String getHint();

	/**
	 * @return Which statistics should be used for this metric.
	 */
	Set<StatColumn> requestedStatistics();

	/**
	 * @return Whether this metric measures only integer values
	 */
	boolean isInteger();
}
