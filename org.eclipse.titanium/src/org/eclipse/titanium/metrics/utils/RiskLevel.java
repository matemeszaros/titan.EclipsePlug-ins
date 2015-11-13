/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.utils;

/**
 * Levels of risk.
 * <p>
 * A <code>RiskColor</code> value is usually associated with a metric and an
 * entity measured by that metric. In this case, this value gives a hint, that
 * how risky this entity is considered by the metric.
 * 
 * @author poroszd
 * 
 */
public enum RiskLevel {
	/** No information is available about the metric values */
	UNKNOWN,
	/** No risk */
	NO,
	/** Dubious */
	LOW,
	/** High chance that something is wrong */
	HIGH
}