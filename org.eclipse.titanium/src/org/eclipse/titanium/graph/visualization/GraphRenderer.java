/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.visualization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import org.apache.commons.collections15.Transformer;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.common.CustomVisualizationViewer;

import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * This class sets round rectangle as node's shape
 * 
 * @author Gabor Jenei
 * @param <V>
 *            the node's type
 */
class NodeShape<V> implements Transformer<V, Shape> {
	private static final int BOX_HEIGHT = 30;
	private static final int ESTIMATED_CHARACTER_WIDTH = 8; // TODO works, but
															// real calculations
															// would be more
															// robust.

	private Transformer<V, String> labeller;

	/**
	 * Constructor
	 * 
	 * @param labeller
	 *            : the node name -> node's shown name converter
	 */
	public NodeShape(final Transformer<V, String> labeller) {
		if (labeller != null) {
			this.labeller = labeller;
		} else {
			this.labeller = new ToStringLabeller<V>();
		}
	}

	/**
	 * @return returning constant round rectangle
	 */
	@Override
	public Shape transform(final V v) {
		final int textLength = labeller.transform(v).length();
		final int textDrawnWidth = textLength * ESTIMATED_CHARACTER_WIDTH;
		return new RoundRectangle2D.Double(-0.5 * textDrawnWidth, -0.5 * BOX_HEIGHT, textDrawnWidth, BOX_HEIGHT, 10, 10);
	}
}

/**
 * This class returns the node coloour set in the node, or
 * {@link NodeColours#LIGHT_GREEN} if there is no colour attribute of the node
 * describing class
 * 
 * @author Gabor Jenei
 * @param <V>
 *            the node's type
 */
class NodeColour<V> implements Transformer<V, Paint> {
	private final PickedState<V> picked;

	/**
	 * Constructor, we need to store a reference of the class which tells us the
	 * currently chosen nodes
	 * 
	 * @param p
	 *            : the describing class's instance
	 */
	public NodeColour(final PickedState<V> p) {
		picked = p;
	}

	/**
	 * The function returns a colour as described in the class's documentation
	 */
	@Override
	public Paint transform(final V v) {
		if (picked.isPicked(v)) {
			return NodeColours.PICKED_COLOUR;
		} else {
			if (v instanceof NodeDescriptor) {
				return ((NodeDescriptor) v).getColor();
			}
			return NodeColours.LIGHT_GREEN;
		}
	}

}

/**
 * A class that sets the edge's colour
 * 
 * @author Gabor Jenei
 * @param <E>
 *            edge's type
 */
class EdgeColour<E> implements Transformer<E, Paint> {
	private final PickedState<E> picked;

	/**
	 * We need to store a reference to the currently chosen edges
	 * 
	 * @param p
	 *            : the class describing currently chosen edges
	 */
	public EdgeColour(final PickedState<E> p) {
		picked = p;
	}

	/**
	 * This method returns black if there is no node colour attribute of the
	 * edge class, the set edge colour otherwise. Or gray if the edge is not
	 * chosen, but there are edges chosen (they will have red colour).
	 */
	@Override
	public Paint transform(final E e) {
		if (picked.getPicked().isEmpty()) {
			if (e instanceof EdgeDescriptor) {
				return ((EdgeDescriptor) e).getColor();
			}
			return Color.black;
		}
		if (picked.isPicked(e)) {
			return Color.red;
		} else {
			return Color.lightGray;
		}
	}

}

/**
 * A class describing the font type to use on the graph nodes (actually it
 * controls all the rendering of label texts)
 * 
 * @author Gabor Jenei
 * @param <V>
 */
class NodeFont<V> extends DefaultVertexLabelRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param pickedVertexLabelColor
	 *            : The colour of selected nodes
	 */
	public NodeFont(final Color pickedVertexLabelColor) {
		super(pickedVertexLabelColor);
	}

	/**
	 * <b>Important: </b> This method is normally never called from our code,
	 * Jung itself calls it inside while rendering graph.
	 * 
	 * @param vv
	 *            : The current context of visualizing (see
	 *            {@link CustomVisualizationViewer})
	 * @param value
	 *            : The value to assign to the label of the vertex
	 * @param font
	 *            : A font object describing which font to use
	 * @param isSelected
	 *            : Indicates whether the node is selected now
	 * @param vertex
	 *            : A reference to the node to render
	 * @return Returns an object describing the current rendering of node text
	 */
	@Override
	public <W extends Object> Component getVertexLabelRendererComponent(final JComponent vv, final Object value, final Font font, final boolean isSelected, final W vertex) {
		final Component comp = super.getVertexLabelRendererComponent(vv, value, font, isSelected, vertex);

		if (vertex instanceof NodeDescriptor) {
			final NodeDescriptor v = (NodeDescriptor) vertex;
			comp.setFont(v.getFontType());
			if (!isSelected) {
				comp.setForeground(v.getFontColour());
			}
		}

		return comp;
	}

}

/**
 * This class stores a custom display for graph, it provides a specific node
 * shape, node colour and edge colour the customization works for both selected
 * and unselected nodes/edges differently.
 * 
 * @author Gabor Jenei
 * @param <V>
 *            The vertex type
 * @param <E>
 *            The node type
 * @see GraphHandler#setNodeRenderer(GraphRenderer,VisualizationViewer)
 */
public class GraphRenderer<V, E> {
	private final NodeShape<V> shape;
	private final NodeColour<V> vertexColour;
	private final EdgeColour<E> edgeColour;
	private final NodeFont<V> font;

	/**
	 * The constructor
	 * 
	 * @param labeller
	 *            : A graph node ID -> graph node name converter
	 * @param vertexPicked
	 *            : A class that stores, which nodes are selected currently on
	 *            the graph
	 * @param edgePicked
	 *            : A class that stores, which edges are selected currently on
	 *            the graph
	 */
	public GraphRenderer(final Transformer<V, String> labeller, final PickedState<V> vertexPicked, final PickedState<E> edgePicked) {
		shape = new NodeShape<V>(labeller);
		vertexColour = new NodeColour<V>(vertexPicked);
		edgeColour = new EdgeColour<E>(edgePicked);
		font = new NodeFont<V>(Color.white);
	}

	/**
	 * @return returns the currently set shape returning class of the vertices
	 */
	public NodeShape<V> getShape() {
		return shape;
	}

	/**
	 * @return returns the currently set coloring class of the vertices
	 */
	public NodeColour<V> getVertexColour() {
		return vertexColour;
	}

	/**
	 * @return returns the currently set edge coloring class
	 */
	public EdgeColour<E> getEdgeColour() {
		return edgeColour;
	}

	/**
	 * @return returns a class that describes the font style (colour,type) used
	 *         for rendering
	 */
	public NodeFont<V> getFont() {
		return font;
	}

}