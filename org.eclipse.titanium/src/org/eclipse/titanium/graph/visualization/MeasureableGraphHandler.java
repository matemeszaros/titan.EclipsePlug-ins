/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.visualization;

import java.awt.Color;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.menus.MeasureableNodePopupMenu;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.utils.ModuleMetricsWrapper;
import org.eclipse.titanium.metrics.utils.RiskLevel;
import org.eclipse.titanium.metrics.utils.WrapperStore;

/**
 * This class implements additional features for graphs that can have measured
 * informations (for eg. metrics)
 * 
 * @author Gabor Jenei
 */
public class MeasureableGraphHandler extends GraphHandler {
	protected IMetricEnum chosenColouringMetric;

	public MeasureableGraphHandler(Shell parent) {
		super();
		popupMenu = new MeasureableNodePopupMenu(this, parent);
	}

	/**
	 * This method calculates the colour of a given node according to a given
	 * metric.
	 * 
	 * @param node
	 *            : The node that you want to colour
	 * @param metric
	 *            : The given metric type (currently it handles <font
	 *            color="red">ONLY</font> one metric)
	 * @return The calculated node colour
	 */
	public Color calculateColour(NodeDescriptor node, IMetricEnum metric) {
		chosenColouringMetric = metric;
		if (node.isMissing()) {
			node.setNodeColour(NodeColours.MISSING_COLOUR);
			return NodeColours.MISSING_COLOUR;
		}
		Color actColor;
		ModuleMetricsWrapper actProvider = WrapperStore.getWrapper(node.getProject());
		RiskLevel rColor = actProvider.getRisk(metric, node.getName());
		switch (rColor) {
		case NO:
			actColor = NodeColours.DARK_GREEN;
			break;
		case LOW:
			actColor = NodeColours.DARK_YELLOW;
			break;
		case HIGH:
			actColor = NodeColours.DARK_RED;
			break;
		default:
			actColor = NodeColours.NO_VALUE_COLOUR;
			break;
		}
		node.setNodeColour(actColor);
		return actColor;
	}

	/**
	 * Enables or disables the Info Window menu item according the provided
	 * value
	 * 
	 * @param value
	 */
	public void enableInfoWindow(boolean value) {
		((MeasureableNodePopupMenu) popupMenu).enableInfoWindow(value);
	}

	/**
	 * @return Returns the currently set metric
	 */
	public final IMetricEnum getChosenMetric() {
		return chosenColouringMetric;
	}

}
