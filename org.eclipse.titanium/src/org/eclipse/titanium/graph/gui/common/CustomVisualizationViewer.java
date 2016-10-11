/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.common;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.menus.NodePopupMenu;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ViewTranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * @author Gabor Jenei This class is the subclass of {@link VisualizationViewer}
 *         class, it implements additional features.
 */
public class CustomVisualizationViewer extends VisualizationViewer<NodeDescriptor, EdgeDescriptor> {
	private static final long serialVersionUID = 1736343406579424405L;

	/**
	 * Constructor
	 * 
	 * @param layout
	 *            : the graph layout to use
	 * @param popupMenu
	 *            : A reference to the popup menu to show on click to a graph
	 *            node (null if nothing)
	 */
	public CustomVisualizationViewer(final Layout<NodeDescriptor, EdgeDescriptor> layout, final NodePopupMenu popupMenu) {
		super(layout);

		final GraphMouseListener<NodeDescriptor> nodeMouseEvents = new GraphMouseListener<NodeDescriptor>() {
			
			// nothing to do on this action
			@Override
			public void graphReleased(final NodeDescriptor arg0, final MouseEvent arg1) {
				//do nothing because this function is not used
			}

			@Override
			public void graphPressed(final NodeDescriptor node, final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 && popupMenu != null) {
					popupMenu.show(node, e.getX(), e.getY());
				}
			}

			//nothing to do on this action
			@Override
			public void graphClicked(final NodeDescriptor arg0, final MouseEvent arg1) {
				//do nothing because this function is not used
			}

		};

		final VisualizationViewer<NodeDescriptor, EdgeDescriptor> thisViewer = this;
		final PluggableGraphMouse mouse = new PluggableGraphMouse() {
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				float scale;
				if (e.getWheelRotation() > 0) {
					scale = 0.9f;
				} else {
					scale = 1.1f;
				}
				CrossoverScalingControl control = new CrossoverScalingControl();
				control.scale(thisViewer, scale, getCenter());
			}
		};

		mouse.add(new ViewTranslatingGraphMousePlugin(InputEvent.BUTTON3_MASK));
		final PickingGraphMousePlugin<NodeDescriptor, EdgeDescriptor> pickingMouse = new PickingGraphMousePlugin<NodeDescriptor, EdgeDescriptor>() {
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (down == null) {
					return;
				}
				super.mouseDragged(e);
			}
		};
		mouse.add(pickingMouse);

		final KeyListener keyboardEvents = new KeyListener() {
			
			//nothing to do on this action
			@Override
			public void keyTyped(final KeyEvent e) {
				//do nothing because this function is not used
			}

			//nothing to do on this action
			@Override
			public void keyReleased(final KeyEvent e) {
				//do nothing because this function is not used
			}

			@Override
			public void keyPressed(final KeyEvent e) {
				char pressed = e.getKeyChar();
				if (pressed != '-' && pressed != '+') {
					return;
				}

				float scale = 1;
				if (pressed == '-') {
					scale = 0.9f;
				} else {
					scale = 1.1f;
				}
				CrossoverScalingControl control = new CrossoverScalingControl();
				control.scale(thisViewer, scale, getCenter());
			}
		};

		setGraphMouse(mouse);
		addGraphMouseListener(nodeMouseEvents);
		addKeyListener(keyboardEvents);
	}

	/**
	 * Makes the given place to be in the center of the screen (jumps to a
	 * selected place on the canvas)
	 * 
	 * @param place
	 *            : The destination place
	 */
	public void jumpToPlace(final Point2D place) {
		final MutableTransformer modelTransformerMaster = getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
		final Point2D orig = getRenderContext().getMultiLayerTransformer().inverseTransform(getCenter());
		modelTransformerMaster.translate(orig.getX() - place.getX(), orig.getY() - place.getY());
	}

}
