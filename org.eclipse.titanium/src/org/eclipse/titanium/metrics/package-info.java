/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * Basic architecture of collecting metric information.
 * <p>
 * A metric is a procedure measuring various ttcn3 entities, like functions, altsteps,
 * test cases, modules or even a whole ttcn3 project. By measurement we mean that the
 * procedure maps a single value (integer or double) to the entity by analyzing the AST.
 * <p>
 * Metrics implement the common interface {@link IMarker}, and should be associated with
 * an {@link IMetricEnum}. <code>IMetricEnum</code>s serve as a handle, or named identifier
 * for a metric, for example while accessing the measured value of a metric.
 * <p>
 * Metrics are controlled via a {@link MetricData} instance, obtained through its
 * static factory method {@link MetricData#measure(org.eclipse.core.resources.IProject)}.
 * After creation, one can query any value measured on entities, statistics of them,
 * or the so-called risk of an entity (according to a given metric).
 * <p> 
 * Concrete implementations of the metrics are found in the packages, categorized by
 * the subject they measure.
 */
package org.eclipse.titanium.metrics;

