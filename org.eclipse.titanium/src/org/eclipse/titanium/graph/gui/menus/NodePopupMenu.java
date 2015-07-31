/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Deque;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.utils.CheckParallelPaths;
import org.eclipse.titanium.graph.visualization.GraphHandler;
import org.eclipse.titanium.utils.LocationHighlighter;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * This class implements the popup menu shown upon graph node click by right
 * mouse button
 * 
 * @author Gabor Jenei
 */
public class NodePopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	protected NodeDescriptor node;
	protected final GraphHandler handler;
	protected final JMenuItem goToDefinition = new JMenuItem("Go to definition");

	/**
	 * Constructor
	 * 
	 * @param handler
	 *            : The handler of the analyzed graph
	 */
	public NodePopupMenu(GraphHandler handler) {
		node = null;
		JMenuItem selectNode = new JMenuItem("Select node");
		JMenuItem getParalellPaths = new JMenuItem("Search paralell paths");
		this.handler = handler;

		final NodePopupMenu thisPopUpMenu = this;
		selectNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualizationViewer<NodeDescriptor, EdgeDescriptor> actVisualisator = thisPopUpMenu.handler.getVisualizator();
				if (actVisualisator == null) {
					return;
				}
				Layout<NodeDescriptor, EdgeDescriptor> tmpLayout = actVisualisator.getGraphLayout();
				if (tmpLayout == null) {
					return;
				}
				Graph<NodeDescriptor, EdgeDescriptor> g = actVisualisator.getGraphLayout().getGraph();
				if (g == null) {
					return;
				}
				if (node == null) {
					ErrorReporter.logError("null node attribute for NodePopupMenu");
					return;
				}

				actVisualisator.getPickedVertexState().clear();
				actVisualisator.getPickedVertexState().pick(node, true);
				actVisualisator.getPickedEdgeState().clear();
				for (EdgeDescriptor edge : g.getIncidentEdges(node)) {
					actVisualisator.getPickedEdgeState().pick(edge, true);
				}
			}
		});

		getParalellPaths.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final VisualizationViewer<NodeDescriptor, EdgeDescriptor> actVisualisator = thisPopUpMenu.handler.getVisualizator();
				if (actVisualisator == null) {
					return;
				}
				Layout<NodeDescriptor, EdgeDescriptor> tmpLayout = actVisualisator.getGraphLayout();
				if (tmpLayout == null) {
					return;
				}
				final Graph<NodeDescriptor, EdgeDescriptor> g = actVisualisator.getGraphLayout().getGraph();
				if (node == null) {
					ErrorReporter.logError("null node attribute for NodePopupMenu");
					return;
				}
				Job searchJob = new Job("Searching for parallel paths...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						CheckParallelPaths<NodeDescriptor, EdgeDescriptor> checker = new CheckParallelPaths<NodeDescriptor, EdgeDescriptor>(g, node);
						if (checker.hasParallelPaths()) {
							for (Deque<EdgeDescriptor> list : checker.getPaths()) {
								for (EdgeDescriptor edge : list) {
									edge.setColour(NodeColours.DARK_RED);
								}
							}
						}
						actVisualisator.repaint();
						return Status.OK_STATUS;
					}

				};
				searchJob.schedule();
			}
		});
		add(selectNode);
		add(getParalellPaths);

		goToDefinition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (node != null) {
							Location loc = node.getLocation();
							if (loc == null || loc instanceof NULL_Location) {
								return;
							}
							LocationHighlighter.jumpToLocation(loc);
						}
					}
				});
			}
		});

		add(goToDefinition);
	}

	protected NodePopupMenu(String label) {
		super(label);
		handler = null;
	}

	/**
	 * Show the popup menu
	 * 
	 * @param node
	 *            : The node where we show the menu
	 * @param x
	 *            : The X coordinate of the node
	 * @param y
	 *            : The Y coordinate of the node
	 */
	public void show(NodeDescriptor node, int x, int y) {
		this.node = node;
		super.show(handler.getVisualizator(), x, y);
	}

	/**
	 * Adds a new menu entry to the popup menu
	 * 
	 * @param title
	 *            : The entry's title
	 * @param listener
	 *            : The action listener for the entry
	 */
	public void addEntry(String title, ActionListener listener) {
		JMenuItem newItem = new JMenuItem(title);
		newItem.addActionListener(listener);
		add(newItem);
	}

	/**
	 * Enables/disables the go to definition menu entry
	 * 
	 * @param value
	 *            : True if entry should be enabled
	 */
	public void enableGoToDefinition(boolean value) {
		goToDefinition.setEnabled(value);
	}

}
