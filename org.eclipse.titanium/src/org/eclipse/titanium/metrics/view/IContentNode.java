/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node, which is not a root node of any tree, meaning it is part of a
 * metric-subtree (so it is sensible to query the risk corresponding this node).
 * 
 * @author poroszd
 * 
 */
interface IContentNode extends INode {
	RiskLevel getRiskLevel(MetricData data);

	double risk(MetricData data);
}
